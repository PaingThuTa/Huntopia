package com.example.huntopia

import com.google.firebase.firestore.DocumentSnapshot
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
            val existing = getCollectedAchievementByCode(normalizedUid, normalizedCode)
            if (existing != null) {
                return CollectResult.AlreadyCollected(catalogItem, existing.collectedAt)
            }

            val primaryDocRef = primaryCollectedDocument(normalizedUid, normalizedCode)
            val collectedTimestamp = FieldValue.serverTimestamp()
            primaryDocRef.set(
                mapOf(
                    FIELD_CODE to normalizedCode,
                    FIELD_COLLECTED_AT to collectedTimestamp,
                    FIELD_COLLECTED_DATE to collectedTimestamp,
                    FIELD_IMAGE_NAME to catalogItem.imageName,
                    FIELD_FOUND_TITLE to catalogItem.foundTitle,
                    FIELD_FOUND_DESCRIPTION to catalogItem.foundDescription
                )
            ).await()

            val savedSnapshot = primaryDocRef.get().await()
            CollectResult.SuccessNew(
                catalogItem,
                savedSnapshot.getTimestamp(FIELD_COLLECTED_AT)
                    ?: savedSnapshot.getTimestamp(FIELD_COLLECTED_DATE)
            )
        } catch (error: Exception) {
            CollectResult.Error(error.message ?: "Unknown error")
        }
    }

    suspend fun getCollectedAchievements(uid: String): List<UserAchievement> {
        val normalizedUid = uid.trim()
        if (normalizedUid.isBlank()) {
            return emptyList()
        }

        val primaryResult = runCatching {
            firestore.collection(PRIMARY_USERS_COLLECTION)
                .document(normalizedUid)
                .collection(PRIMARY_COLLECTED_COLLECTION)
                .get()
                .await()
        }

        val legacyResult = runCatching {
            firestore.collection(LEGACY_USERS_COLLECTION)
                .document(normalizedUid)
                .collection(LEGACY_COLLECTED_COLLECTION)
                .get()
                .await()
        }

        if (primaryResult.isFailure && legacyResult.isFailure) {
            throw primaryResult.exceptionOrNull()
                ?: legacyResult.exceptionOrNull()
                ?: IllegalStateException("Unable to load collected achievements")
        }

        val primaryDocs = primaryResult.getOrNull()?.documents.orEmpty()
        val legacyDocs = legacyResult.getOrNull()?.documents.orEmpty()

        val mergedByCode = LinkedHashMap<String, UserAchievement>()

        (primaryDocs + legacyDocs).forEach { doc ->
            val item = doc.toUserAchievement(doc.id)
            val key = item.code.ifBlank { doc.id }
            val current = mergedByCode[key]
            mergedByCode[key] = choosePreferredAgainstCandidate(current, item)
        }

        val withCatalogFallback = mergedByCode.values
            .map { fillMissingCollectedFields(it) }

        return withCatalogFallback.sortedByDescending { it.collectedAt?.toDate()?.time ?: Long.MIN_VALUE }
    }

    suspend fun getCollectedAchievementByCode(uid: String, code: String): UserAchievement? {
        val normalizedUid = uid.trim()
        val normalizedCode = code.trim()

        if (normalizedUid.isBlank() || !isValidCode(normalizedCode)) {
            return null
        }

        val primaryResult = runCatching {
            primaryCollectedDocument(normalizedUid, normalizedCode).get().await()
        }
        val legacyResult = runCatching {
            legacyCollectedDocument(normalizedUid, normalizedCode).get().await()
        }

        if (primaryResult.isFailure && legacyResult.isFailure) {
            throw primaryResult.exceptionOrNull()
                ?: legacyResult.exceptionOrNull()
                ?: IllegalStateException("Unable to load collected achievement")
        }

        val primaryDoc = primaryResult.getOrNull()
        val legacyDoc = legacyResult.getOrNull()

        val primaryItem = if (primaryDoc?.exists() == true) {
            primaryDoc.toUserAchievement(normalizedCode)
        } else {
            null
        }

        val legacyItem = if (legacyDoc?.exists() == true) {
            legacyDoc.toUserAchievement(normalizedCode)
        } else {
            null
        }

        val merged = choosePreferred(primaryItem, legacyItem)
        return merged?.let { fillMissingCollectedFields(it) }
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

    private suspend fun fillMissingCollectedFields(item: UserAchievement): UserAchievement {
        if (
            item.imageName.isNotBlank() &&
            item.foundTitle.isNotBlank() &&
            item.foundDescription.isNotBlank()
        ) {
            return item
        }

        val catalogItem = getCatalogByCode(item.code) ?: return item

        return item.copy(
            imageName = item.imageName.ifBlank { catalogItem.imageName },
            foundTitle = item.foundTitle.ifBlank { catalogItem.foundTitle },
            foundDescription = item.foundDescription.ifBlank { catalogItem.foundDescription }
        )
    }

    private fun choosePreferredAgainstCandidate(
        current: UserAchievement?,
        candidate: UserAchievement
    ): UserAchievement {
        if (current == null) {
            return candidate
        }

        val currentTime = current.collectedAt?.toDate()?.time ?: Long.MIN_VALUE
        val candidateTime = candidate.collectedAt?.toDate()?.time ?: Long.MIN_VALUE

        return when {
            candidateTime > currentTime -> candidate
            candidateTime < currentTime -> current
            fieldScore(candidate) > fieldScore(current) -> candidate
            else -> current
        }
    }

    private fun choosePreferred(primary: UserAchievement?, legacy: UserAchievement?): UserAchievement? {
        return when {
            primary == null && legacy == null -> null
            primary == null -> legacy
            legacy == null -> primary
            else -> choosePreferredAgainstCandidate(primary, legacy)
        }
    }

    private fun fieldScore(item: UserAchievement): Int {
        var score = 0
        if (item.imageName.isNotBlank()) score++
        if (item.foundTitle.isNotBlank()) score++
        if (item.foundDescription.isNotBlank()) score++
        return score
    }

    private fun DocumentSnapshot.toCatalogItem(defaultCode: String): AchievementCatalogItem {
        return AchievementCatalogItem(
            code = getString(FIELD_CODE).orEmpty().ifBlank { defaultCode },
            imageName = getString(FIELD_IMAGE_NAME).orEmpty(),
            unfoundTitle = getString(FIELD_UNFOUND_TITLE).orEmpty(),
            unfoundDescription = getString(FIELD_UNFOUND_DESCRIPTION).orEmpty(),
            foundTitle = getString(FIELD_FOUND_TITLE).orEmpty(),
            foundDescription = getString(FIELD_FOUND_DESCRIPTION).orEmpty()
        )
    }

    private fun DocumentSnapshot.toUserAchievement(defaultCode: String): UserAchievement {
        return UserAchievement(
            code = getString(FIELD_CODE).orEmpty().ifBlank { defaultCode },
            imageName = getString(FIELD_IMAGE_NAME).orEmpty(),
            foundTitle = getString(FIELD_FOUND_TITLE).orEmpty(),
            foundDescription = getString(FIELD_FOUND_DESCRIPTION).orEmpty(),
            collectedAt = getTimestamp(FIELD_COLLECTED_AT) ?: getTimestamp(FIELD_COLLECTED_DATE)
        )
    }

    private fun primaryCollectedDocument(uid: String, code: String) = firestore
        .collection(PRIMARY_USERS_COLLECTION)
        .document(uid)
        .collection(PRIMARY_COLLECTED_COLLECTION)
        .document(code)

    private fun legacyCollectedDocument(uid: String, code: String) = firestore
        .collection(LEGACY_USERS_COLLECTION)
        .document(uid)
        .collection(LEGACY_COLLECTED_COLLECTION)
        .document(code)

    companion object {
        private const val ACHIEVEMENTS_COLLECTION = "achievements"

        private const val PRIMARY_USERS_COLLECTION = "user"
        private const val PRIMARY_COLLECTED_COLLECTION = "collected"

        private const val LEGACY_USERS_COLLECTION = "users"
        private const val LEGACY_COLLECTED_COLLECTION = "collectedAchievements"

        private const val FIELD_CODE = "code"
        private const val FIELD_COLLECTED_AT = "collectedAt"
        private const val FIELD_COLLECTED_DATE = "collectedDate"
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
