package com.example.service

import com.example.data.model.InAppNotification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

object InAppNotificationManager {

    private val _notifications = MutableStateFlow<List<InAppNotification>>(emptyList())
    val notifications: StateFlow<List<InAppNotification>> = _notifications.asStateFlow()

    private val _currentBanner = MutableStateFlow<InAppNotification?>(null)
    val currentBanner: StateFlow<InAppNotification?> = _currentBanner.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    fun dismissBanner() {
        _currentBanner.value = null
    }

    fun markAsRead(id: String) {
        _notifications.value = _notifications.value.map {
            if (it.id == id) it.copy(isRead = true) else it
        }
        updateUnreadCount()
        if (_currentBanner.value?.id == id) {
            _currentBanner.value = null
        }
    }

    fun markAllAsRead() {
        _notifications.value = _notifications.value.map { it.copy(isRead = true) }
        updateUnreadCount()
    }

    fun clearAll() {
        _notifications.value = emptyList()
        _currentBanner.value = null
        _unreadCount.value = 0
    }

    fun triggerSimulatedNotification(
        title: String,
        message: String,
        songId: Long? = null,
        type: String = "general"
    ) {
        val notif = InAppNotification(
            id = UUID.randomUUID().toString(),
            title = title,
            message = message,
            isRead = false,
            songId = songId,
            type = type
        )
        _notifications.value = listOf(notif) + _notifications.value
        _currentBanner.value = notif
        updateUnreadCount()
    }

    private fun updateUnreadCount() {
        _unreadCount.value = _notifications.value.count { !it.isRead }
    }
}
