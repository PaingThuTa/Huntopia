package com.example.huntopia

import com.google.firebase.Timestamp

data class UserAchievement(
    val code: String,
    val imageName: String,
    val foundTitle: String,
    val foundDescription: String,
    val collectedAt: Timestamp?
)
