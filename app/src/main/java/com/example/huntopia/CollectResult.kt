package com.example.huntopia

import com.google.firebase.Timestamp

sealed interface CollectResult {
    data class SuccessNew(
        val item: AchievementCatalogItem,
        val collectedAt: Timestamp?
    ) : CollectResult

    data class AlreadyCollected(
        val item: AchievementCatalogItem,
        val collectedAt: Timestamp?
    ) : CollectResult

    data object InvalidCode : CollectResult
    data object CatalogMissing : CollectResult
    data object NotLoggedIn : CollectResult
    data class Error(val message: String) : CollectResult
}
