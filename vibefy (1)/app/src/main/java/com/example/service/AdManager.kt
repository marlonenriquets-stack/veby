package com.example.service

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class AdManager(private val context: Context) {

    private var interstitialAd: InterstitialAd? = null
    private var songsPlayedCount = 0
    var intervalN: Int = 4 // Default 4 songs per ad

    // Test AdMob Interstitial Unit ID
    private val testAdUnitId = "ca-app-pub-3940256099942544/1033173712"

    init {
        loadInterstitialAd()
    }

    private fun loadInterstitialAd() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            testAdUnitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    Log.d("AdManager", "Interstitial ad loaded successfully")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    Log.e("AdManager", "Failed to load interstitial ad: ${error.message}")
                }
            }
        )
    }

    fun onSongPlayed(activity: Activity?, quitaAnuncios: Boolean) {
        if (quitaAnuncios) {
            Log.d("AdManager", "User has quita_anuncios = true. Skipping ad.")
            return
        }

        songsPlayedCount++
        Log.d("AdManager", "Song played count: $songsPlayedCount / $intervalN")

        if (songsPlayedCount >= intervalN) {
            songsPlayedCount = 0
            showAd(activity)
        }
    }

    private fun showAd(activity: Activity?) {
        val currentAd = interstitialAd
        if (currentAd != null && activity != null) {
            currentAd.show(activity)
            loadInterstitialAd() // Reload next ad
        } else {
            Log.d("AdManager", "Ad not ready or activity null, reloading...")
            loadInterstitialAd()
        }
    }
}
