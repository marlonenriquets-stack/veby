package com.example.data.model

data class InAppNotification(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val message: String,
    val timestampMs: Long = System.currentTimeMillis(),
    val imageUrl: String? = null,
    val songId: Long? = null,
    val type: String = "general", // "general", "music", "promo", "system"
    val isRead: Boolean = false
)
