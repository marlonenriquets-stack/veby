package com.example.service

import android.content.Context
import android.util.Log
import com.example.data.model.InAppNotification
import com.onesignal.OneSignal
import com.onesignal.inappmessages.IInAppMessageClickListener
import com.onesignal.inappmessages.IInAppMessageClickEvent
import com.onesignal.notifications.INotificationClickListener
import com.onesignal.notifications.INotificationClickEvent
import com.onesignal.notifications.INotificationLifecycleListener
import com.onesignal.notifications.INotificationWillDisplayEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PushNotificationManager {

    companion object {
        private const val TAG = "PushNotificationManager"
        const val DEFAULT_APP_ID = "b2f7d983-kinemax-onesignal-app-id"

        private val _notifications = MutableStateFlow<List<InAppNotification>>(
            listOf(
                InAppNotification(
                    title = "🎧 ¡Bienvenido a Kinemax Music!",
                    message = "Disfruta de sonido en alta fidelidad y la Inteligencia Artificial Mix IA.",
                    type = "system"
                )
            )
        )
        val notifications: StateFlow<List<InAppNotification>> = _notifications.asStateFlow()

        private val _currentBanner = MutableStateFlow<InAppNotification?>(null)
        val currentBanner: StateFlow<InAppNotification?> = _currentBanner.asStateFlow()

        private val _unreadCount = MutableStateFlow(1)
        val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

        private var isInitialized = false

        fun init(context: Context, appId: String = DEFAULT_APP_ID) {
            if (isInitialized) return
            try {
                OneSignal.initWithContext(context, appId)
                isInitialized = true

                // Foreground Notification Listener (Displays in-app notifications in real-time)
                OneSignal.Notifications.addForegroundLifecycleListener(object : INotificationLifecycleListener {
                    override fun onWillDisplay(event: INotificationWillDisplayEvent) {
                        val notif = event.notification
                        val title = notif.title ?: "Kinemax Music"
                        val body = notif.body ?: "Nueva notificación recibida"
                        val customData = notif.additionalData
                        val songId = customData?.optLong("song_id", -1L)?.takeIf { it > 0 }
                        val imageUrl = notif.bigPicture ?: notif.largeIcon

                        val inAppNotif = InAppNotification(
                            title = title,
                            message = body,
                            imageUrl = imageUrl,
                            songId = songId,
                            type = customData?.optString("type") ?: "general"
                        )

                        addNotification(inAppNotif)
                        showBanner(inAppNotif)
                    }
                })

                // Notification Click Listener
                OneSignal.Notifications.addClickListener(object : INotificationClickListener {
                    override fun onClick(event: INotificationClickEvent) {
                        val notif = event.notification
                        val title = notif.title ?: "Kinemax Music"
                        val body = notif.body ?: ""
                        Log.d(TAG, "Notification clicked: $title - $body")
                    }
                })

                // In-App Messages Listener
                OneSignal.InAppMessages.addClickListener(object : IInAppMessageClickListener {
                    override fun onClick(event: IInAppMessageClickEvent) {
                        Log.d(TAG, "In-App Message Clicked: ${event.result.actionId}")
                    }
                })

                // Request push permissions asynchronously
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        OneSignal.Notifications.requestPermission(true)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error requesting notification permission", e)
                    }
                }

                Log.d(TAG, "OneSignal initialized successfully with ID: $appId")
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing OneSignal", e)
            }
        }

        fun updateOneSignalAppId(context: Context, appId: String) {
            if (appId.isNotBlank() && appId != "YOUR-ONESIGNAL-APP-ID") {
                init(context, appId)
            }
        }

        fun loginUser(userId: String, email: String?) {
            try {
                OneSignal.login(userId)
                if (!email.isNullOrBlank()) {
                    OneSignal.User.addTag("email", email)
                }
                OneSignal.User.addTag("app_version", "1.0.0")
            } catch (e: Exception) {
                Log.e(TAG, "Error logging in user to OneSignal", e)
            }
        }

        fun logoutUser() {
            try {
                OneSignal.logout()
            } catch (e: Exception) {
                Log.e(TAG, "Error logging out user from OneSignal", e)
            }
        }

        fun setSubscriptionTag(isPremium: Boolean) {
            try {
                OneSignal.User.addTag("subscription_tier", if (isPremium) "premium" else "free")
            } catch (e: Exception) {
                Log.e(TAG, "Error setting subscription tag", e)
            }
        }

        fun addNotification(notification: InAppNotification) {
            val updated = listOf(notification) + _notifications.value
            _notifications.value = updated
            updateUnreadCount()
        }

        fun showBanner(notification: InAppNotification) {
            _currentBanner.value = notification
        }

        fun dismissBanner() {
            _currentBanner.value = null
        }

        fun markAsRead(notificationId: String) {
            val updated = _notifications.value.map {
                if (it.id == notificationId) it.copy(isRead = true) else it
            }
            _notifications.value = updated
            updateUnreadCount()
        }

        fun markAllAsRead() {
            val updated = _notifications.value.map { it.copy(isRead = true) }
            _notifications.value = updated
            updateUnreadCount()
        }

        fun clearAll() {
            _notifications.value = emptyList()
            updateUnreadCount()
        }

        private fun updateUnreadCount() {
            _unreadCount.value = _notifications.value.count { !it.isRead }
        }

        fun triggerSimulatedNotification(
            title: String = "🎵 Nueva Canción Recomendada",
            message: String = "Kinemax Mix IA ha seleccionado un nuevo tema para ti.",
            songId: Long? = null,
            type: String = "music"
        ) {
            val notif = InAppNotification(
                title = title,
                message = message,
                songId = songId,
                type = type
            )
            addNotification(notif)
            showBanner(notif)
        }
    }
}

