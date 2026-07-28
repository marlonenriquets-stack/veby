package com.example.service

import android.content.Context
import android.util.Log
import com.onesignal.OneSignal

class PushNotificationManager {

    companion object {
        fun updateOneSignalAppId(context: Context, appId: String) {
            if (appId.isNotEmpty() && appId != "YOUR-ONESIGNAL-APP-ID") {
                try {
                    OneSignal.initWithContext(context, appId)
                    Log.d("PushNotificationManager", "OneSignal initialized with ID: $appId")
                } catch (e: Exception) {
                    Log.e("PushNotificationManager", "Error initializing OneSignal", e)
                }
            }
        }

        fun loginUser(userId: String, email: String?) {
            try {
                OneSignal.login(userId)
                if (!email.isNull_or_empty()) {
                    OneSignal.User.addTag("email", email!!)
                }
            } catch (e: Exception) {
                Log.e("PushNotificationManager", "Error logging in user to OneSignal", e)
            }
        }

        fun logoutUser() {
            try {
                OneSignal.logout()
            } catch (e: Exception) {
                Log.e("PushNotificationManager", "Error logging out user from OneSignal", e)
            }
        }

        private fun String?.isNull_or_empty(): Boolean = this == null || this.trim().isEmpty()
    }
}
