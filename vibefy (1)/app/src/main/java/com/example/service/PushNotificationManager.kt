package com.example.service

import android.content.Context
import android.util.Log
import com.onesignal.OneSignal
import com.onesignal.debug.LogLevel
import com.onesignal.notifications.INotificationClickEvent
import com.onesignal.notifications.INotificationClickListener

object PushNotificationManager {

    private const val TAG = "PushNotificationManager"
    private const val ONESIGNAL_APP_ID = "YOUR_ONESIGNAL_APP_ID"

    fun init(context: Context) {
        try {
            OneSignal.Debug.logLevel = LogLevel.VERBOSE
            OneSignal.initWithContext(context, ONESIGNAL_APP_ID)

            OneSignal.Notifications.addClickListener(object : INotificationClickListener {
                override fun onClick(event: INotificationClickEvent) {
                    val notification = event.notification
                    Log.d(TAG, "Notificación presionada: ${notification.title}")
                }
            })

            Log.i(TAG, "OneSignal inicializado correctamente.")
        } catch (e: Exception) {
            Log.e(TAG, "Error al inicializar OneSignal", e)
        }
    }

    fun loginUser(userId: String, email: String? = null) {
        try {
            OneSignal.login(userId)
            if (!email.isNullOrEmpty()) {
                OneSignal.User.addEmail(email)
            }
            Log.d(TAG, "Usuario registrado en OneSignal: $userId")
        } catch (e: Exception) {
            Log.e(TAG, "Error al registrar usuario en OneSignal", e)
        }
    }

    fun logoutUser() {
        try {
            OneSignal.logout()
            Log.d(TAG, "Sesión de OneSignal cerrada")
        } catch (e: Exception) {
            Log.e(TAG, "Error al cerrar sesión de OneSignal", e)
        }
    }
}
