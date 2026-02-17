package com.example.huntopia

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AchievementRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    suspend fun getCatalogByCode(code: String): AchievementCatalogItem? {
        val normalized = code.trim()
        if (!isValidCode(normalized)) {
            return null
        }

        return try {
            val snapshot = firestore.collection(ACHIEVEMENTS_COLLECTION)
                .document(normalized)
                .get()
                .await()

            if (!snapshot.exists()) {
                null
            } else {
                snapshot.toCatalogItem(normalized)
            }
        } catch (_: Exception) {
            null
        }
    }

    suspend fun collectIfNew(uid: String, code: String): CollectResult {
        val normalizedUid = uid.trim()
        if (normalizedUid.isBlank()) {
            return CollectResult.NotLoggedIn
        }

        val normalizedCode = code.trim()
        if (!isValidCode(normalizedCode)) {
            return CollectResult.InvalidCode
        }

        return try {
            val catalogDoc = firestore.collection(ACHIEVEMENTS_COLLECTION)
                .document(normalizedCode)
                .get()
                .await()

            if (!catalogDoc.exists()) {
                return CollectResult.CatalogMissing
            }

            val catalogItem = catalogDoc.toCatalogItem(normalizedCode)
            val userDocRef = firestore.collection(USERS_COLLECTION)
                .document(normalizedUid)
                .collection(COLLECTED_COLLECTION)
                .document(normalizedCode)

            val existing = userDocRef.get().await()
            if (existing.exists()) {
                val existingCollectedAt = existing.getTimestamp(FIELD_COLLECTED_AT)
                return CollectResult.AlreadyCollected(catalogItem, existingCollectedAt)
            }

            userDocRef.set(
                mapOf(
                    FIELD_CODE to normalizedCode,
                    FIELD_COLLECTED_AT to FieldValue.serverTimestamp(),
                    FIELD_IMAGE_NAME to catalogItem.imageName,
                    FIELD_FOUND_TITLE to catalogItem.foundTitle,
                    FIELD_FOUND_DESCRIPTION to catalogItem.foundDescription
                )
            ).await()

            val savedSnapshot = userDocRef.get().await()
            CollectResult.SuccessNew(catalogItem, savedSnapshot.getTimestamp(FIELD_COLLECTED_AT))
        } catch (error: Exception) {
            CollectResult.Error(error.message ?: "Unknown error")
        }
    }

    suspend fun getCollectedAchievements(uid: String): List<UserAchievement> {
        val normalizedUid = uid.trim()
        if (normalizedUid.isBlank()) {
            return emptyList()
        }

        val query = firestore.collection(USERS_COLLECTION)
            .document(normalizedUid)
            .collection(COLLECTED_COLLECTION)
            .orderBy(FIELD_COLLECTED_AT)
            .get()
            .await()

        return query.documents
            .map { doc ->
                UserAchievement(
                    code = doc.getString(FIELD_CODE).orEmpty().ifBlank { doc.id },
                    imageName = doc.getString(FIELD_IMAGE_NAME).orEmpty(),
                    foundTitle = doc.getString(FIELD_FOUND_TITLE).orEmpty(),
                    foundDescription = doc.getString(FIELD_FOUND_DESCRIPTION).orEmpty(),
                    collectedAt = doc.getTimestamp(FIELD_COLLECTED_AT)
                )
            }
            .sortedByDescending { it.collectedAt?.toDate()?.time ?: 0L }
    }

    suspend fun getCollectedCount(uid: String): Int {
        return getCollectedAchievements(uid).size
    }

    suspend fun getAllCatalogItems(): List<AchievementCatalogItem> {
        val query = firestore.collection(ACHIEVEMENTS_COLLECTION)
            .get()
            .await()

        return query.documents
            .mapNotNull { doc ->
                if (!doc.exists()) {
                    null
                } else {
                    doc.toCatalogItem(doc.id)
                }
            }
            .sortedBy { it.code }
    }

    suspend fun getCatalogCount(): Int {
        return getAllCatalogItems().size
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toCatalogItem(
        defaultCode: String
    ): AchievementCatalogItem {
        return AchievementCatalogItem(
            code = getString(FIELD_CODE).orEmpty().ifBlank { defaultCode },
            imageName = getString(FIELD_IMAGE_NAME).orEmpty(),
            unfoundTitle = getString(FIELD_UNFOUND_TITLE).orEmpty(),
            unfoundDescription = getString(FIELD_UNFOUND_DESCRIPTION).orEmpty(),
            foundTitle = getString(FIELD_FOUND_TITLE).orEmpty(),
            foundDescription = getString(FIELD_FOUND_DESCRIPTION).orEmpty()
        )
    }

    companion object {
        private const val ACHIEVEMENTS_COLLECTION = "achievements"
        private const val USERS_COLLECTION = "users"
        private const val COLLECTED_COLLECTION = "collectedAchievements"

        private const val FIELD_CODE = "code"
        private const val FIELD_COLLECTED_AT = "collectedAt"
        private const val FIELD_IMAGE_NAME = "imageName"
        private const val FIELD_UNFOUND_TITLE = "unfoundTitle"
        private const val FIELD_UNFOUND_DESCRIPTION = "unfoundDescription"
        private const val FIELD_FOUND_TITLE = "foundTitle"
        private const val FIELD_FOUND_DESCRIPTION = "foundDescription"

        private val CODE_REGEX = Regex("^\\d{4}$")

        fun isValidCode(code: String): Boolean {
            return CODE_REGEX.matches(code.trim())
        }
    }
}
