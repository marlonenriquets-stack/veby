package com.example

import android.app.Application
import android.util.Log
import com.google.android.gms.ads.MobileAds
import com.google.firebase.FirebaseApp
import com.onesignal.OneSignal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class KinemaxApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize FirebaseApp if not already initialized
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
            }
            com.example.service.RemoteConfigManager.init()
        } catch (e: Exception) {
            Log.e("KinemaxApplication", "Failed to initialize FirebaseApp", e)
        }

        // Initialize AdMob SDK
        try {
            MobileAds.initialize(this) { status ->
                Log.d("KinemaxApplication", "AdMob initialized: ${status.adapterStatusMap}")
            }
        } catch (e: Exception) {
            Log.e("KinemaxApplication", "Failed to initialize AdMob", e)
        }

        // Initialize OneSignal Push Notifications & In-App Notifications
        try {
            com.example.service.PushNotificationManager.init(this)
        } catch (e: Exception) {
            Log.e("KinemaxApplication", "Failed to initialize OneSignal", e)
        }
    }
}
