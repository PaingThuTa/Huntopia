package com.example.huntopia

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class UserRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    suspend fun createOrUpdateProfile(uid: String, username: String, email: String) {
        val normalizedUid = uid.trim()
        if (normalizedUid.isBlank()) {
            return
        }

        val sanitizedEmail = email.trim()
        val resolvedUsername = sanitizeUsername(username, sanitizedEmail)
        val docRef = firestore.collection(PRIMARY_USERS_COLLECTION).document(normalizedUid)

        val existing = docRef.get().await()
        val updates = mutableMapOf<String, Any>(
            FIELD_USERNAME to resolvedUsername
        )

        if (sanitizedEmail.isNotBlank()) {
            updates[FIELD_EMAIL] = sanitizedEmail
        }

        if (!existing.exists() || existing.getTimestamp(FIELD_CREATED_AT) == null) {
            updates[FIELD_CREATED_AT] = FieldValue.serverTimestamp()
        }

        docRef.set(updates, SetOptions.merge()).await()
    }

    suspend fun getOrProvisionProfile(uid: String, email: String): UserProfile {
        val normalizedUid = uid.trim()
        val authEmail = email.trim()
        if (normalizedUid.isBlank()) {
            return UserProfile("", fallbackUsername(authEmail), authEmail)
        }

        val primaryDocRef = firestore.collection(PRIMARY_USERS_COLLECTION).document(normalizedUid)
        val primaryDoc = primaryDocRef.get().await()

        var resolvedUsername = primaryDoc.readCandidateUsername()
        var resolvedEmail = primaryDoc.getString(FIELD_EMAIL).orEmpty().trim()

        if (resolvedUsername.isBlank() || resolvedEmail.isBlank()) {
            val legacyDoc = firestore.collection(LEGACY_USERS_COLLECTION)
                .document(normalizedUid)
                .get()
                .await()

            if (resolvedUsername.isBlank()) {
                resolvedUsername = legacyDoc.readCandidateUsername()
            }
            if (resolvedEmail.isBlank()) {
                resolvedEmail = legacyDoc.getString(FIELD_EMAIL).orEmpty().trim()
            }
        }

        if (resolvedEmail.isBlank()) {
            resolvedEmail = authEmail
        }
        if (resolvedUsername.isBlank()) {
            resolvedUsername = fallbackUsername(resolvedEmail.ifBlank { authEmail })
        }

        val shouldPersistUsername = primaryDoc.getString(FIELD_USERNAME).orEmpty().trim() != resolvedUsername
        val shouldPersistEmail = resolvedEmail.isNotBlank() &&
            primaryDoc.getString(FIELD_EMAIL).orEmpty().trim() != resolvedEmail
        val needsCreatedAt = !primaryDoc.exists() || primaryDoc.getTimestamp(FIELD_CREATED_AT) == null

        if (shouldPersistUsername || shouldPersistEmail || needsCreatedAt) {
            val updates = mutableMapOf<String, Any>(
                FIELD_USERNAME to resolvedUsername
            )
            if (resolvedEmail.isNotBlank()) {
                updates[FIELD_EMAIL] = resolvedEmail
            }
            if (needsCreatedAt) {
                updates[FIELD_CREATED_AT] = FieldValue.serverTimestamp()
            }
            primaryDocRef.set(updates, SetOptions.merge()).await()
        }

        return UserProfile(
            uid = normalizedUid,
            username = resolvedUsername,
            email = resolvedEmail
        )
    }

    private fun sanitizeUsername(username: String, email: String): String {
        val trimmed = username.trim()
        if (trimmed.isNotBlank()) {
            return trimmed
        }
        return fallbackUsername(email)
    }

    private fun DocumentSnapshot.readCandidateUsername(): String {
        return getString(FIELD_USERNAME).orEmpty().trim()
            .ifBlank { getString(FIELD_NAME).orEmpty().trim() }
            .ifBlank { getString(FIELD_DISPLAY_NAME).orEmpty().trim() }
    }

    private fun fallbackUsername(email: String): String {
        val prefix = email.substringBefore("@").trim()
        return if (prefix.isNotBlank()) {
            prefix
        } else {
            DEFAULT_USERNAME
        }
    }

    companion object {
        private const val PRIMARY_USERS_COLLECTION = "user"
        private const val LEGACY_USERS_COLLECTION = "users"

        private const val FIELD_USERNAME = "username"
        private const val FIELD_NAME = "name"
        private const val FIELD_DISPLAY_NAME = "displayName"
        private const val FIELD_EMAIL = "email"
        private const val FIELD_CREATED_AT = "createdAt"

        private const val DEFAULT_USERNAME = "Explorer"
    }
}
