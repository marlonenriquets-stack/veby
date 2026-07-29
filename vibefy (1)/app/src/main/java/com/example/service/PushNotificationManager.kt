package com.example.service

import android.content.Context
import android.util.Log
import com.onesignal.OneSignal
import com.onesignal.debug.LogLevel
import com.onesignal.inAppMessages.IInAppMessageWillDisplayEvent
import com.onesignal.inAppMessages.IInAppMessageWillDismissEvent
import com.onesignal.inAppMessages.IInAppMessageClickEvent
import com.onesignal.inAppMessages.IInAppMessageClickListener
import com.onesignal.notifications.INotificationClickEvent
import com.onesignal.notifications.INotificationClickListener

class PushNotificationManager(private val context: Context) {

    companion object {
        private const val TAG = "PushNotificationManager"
        // Coloca tu APP ID real de OneSignal si tienes uno
        private const val ONESIGNAL_APP_ID = "YOUR_ONESIGNAL_APP_ID"
    }

    fun init() {
        try {
            // Habilitar logs detallados en debug
            OneSignal.Debug.logLevel = LogLevel.VERBOSE

            // Inicializar OneSignal
            OneSignal.initWithContext(context, ONESIGNAL_APP_ID)

            // Listener para cuando el usuario hace clic en una notificación
            OneSignal.Notifications.addClickListener(object : INotificationClickListener {
                override fun onClick(event: INotificationClickEvent) {
                    val notification = event.notification
                    val launchUrl = notification.launchURL
                    val additionalData = notification.additionalData

                    Log.d(TAG, "Notificación presionada: ${notification.title}")
                    Log.d(TAG, "Launch URL: $launchUrl")
                    Log.d(TAG, "Additional Data: $additionalData")
                }
            })

            // Listener para clics en mensajes In-App
            OneSignal.InAppMessages.addClickListener(object : IInAppMessageClickListener {
                override fun onClick(event: IInAppMessageClickEvent) {
                    Log.d(TAG, "In-App Message clickeado: ${event.result.actionId}")
                }
            })

            Log.i(TAG, "OneSignal inicializado correctamente.")
        } catch (e: Exception) {
            Log.e(TAG, "Error al inicializar OneSignal", e)
        }
    }

    fun setExternalUserId(userId: String) {
        try {
            OneSignal.login(userId)
            Log.d(TAG, "Usuario registrado en OneSignal: $userId")
        } catch (e: Exception) {
            Log.e(TAG, "Error al registrar usuario en OneSignal", e)
        }
    }

    fun logout() {
        try {
            OneSignal.logout()
            Log.d(TAG, "Sesión de OneSignal cerrada")
        } catch (e: Exception) {
            Log.e(TAG, "Error al cerrar sesión de OneSignal", e)
        }
    }
}
