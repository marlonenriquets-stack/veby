package com.example.data.remote

import android.util.Log
import com.example.data.local.UserSessionManager
import com.example.service.RemoteConfigManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response

object NetworkErrorTracker {
    var lastError: String? = null
    var hasBeenShown: Boolean = false

    fun setError(error: String) {
        lastError = error
        hasBeenShown = false
    }

    fun clear() {
        lastError = null
        hasBeenShown = true
    }
}

class AuthInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Get Firebase Auth ID token if available
        val firebaseUser = try {
            FirebaseAuth.getInstance().currentUser
        } catch (e: Exception) {
            null
        }

        val idToken: String? = if (firebaseUser != null) {
            try {
                runBlocking {
                    val tokenResult = firebaseUser.getIdToken(false).await()
                    tokenResult.token
                }
            } catch (e: Exception) {
                UserSessionManager.cachedToken
            }
        } else {
            UserSessionManager.cachedToken
        }

        val requestBuilder = originalRequest.newBuilder()

        // 1. Mandatory Header X-Api-Key from Remote Config
        val currentApiKey = RemoteConfigManager.apiKey
        requestBuilder.header("X-Api-Key", currentApiKey)

        // 2. Add Authorization Bearer header if user token is available
        if (!idToken.isNullOrEmpty()) {
            requestBuilder.header("Authorization", "Bearer $idToken")
        }

        // 3. Dynamic Base URL construction & clean URI joining (eliminating duplicate slashes like /api//planes.php)
        val currentBaseUrl = RemoteConfigManager.serverUrl.toHttpUrlOrNull()
        if (currentBaseUrl != null) {
            val oldUrl = originalRequest.url
            val currentBasePath = currentBaseUrl.encodedPath.trimEnd('/')
            val originalPath = oldUrl.encodedPath

            // Strip duplicate /api prefix if base URL already includes /api
            val relativePath = if (currentBasePath.endsWith("/api") && originalPath.startsWith("/api/")) {
                originalPath.removePrefix("/api")
            } else {
                originalPath
            }

            val combinedPath = ("$currentBasePath/$relativePath")
                .replace(Regex("/{2,}"), "/")
            val cleanPath = if (combinedPath.startsWith("/")) combinedPath else "/$combinedPath"

            val newUrl = oldUrl.newBuilder()
                .scheme(currentBaseUrl.scheme)
                .host(currentBaseUrl.host)
                .port(currentBaseUrl.port)
                .encodedPath(cleanPath)
                .build()

            requestBuilder.url(newUrl)
        }

        val finalRequest = requestBuilder.build()
        val requestUrl = finalRequest.url.toString()

        Log.d("KinemaxNetwork", "--> HTTP ${finalRequest.method} $requestUrl [ApiKey: ${finalRequest.header("X-Api-Key")}]")

        // 4. Proceed with request & log response body or error
        val response = try {
            chain.proceed(finalRequest)
        } catch (e: Exception) {
            val networkErrMsg = "Error de conexión ($requestUrl): ${e.localizedMessage ?: e.message}"
            Log.e("KinemaxNetwork", networkErrMsg, e)
            NetworkErrorTracker.setError(networkErrMsg)
            throw e
        }

        val responseCode = response.code

        if (response.isSuccessful) {
            val responseBody = response.body
            val responseContent = if (responseBody != null) {
                try {
                    val source = responseBody.source()
                    source.request(Long.MAX_VALUE)
                    val buffer = source.buffer.clone()
                    buffer.readString(Charsets.UTF_8)
                } catch (e: Exception) {
                    "Error reading body: ${e.message}"
                }
            } else {
                ""
            }
            Log.d("KinemaxNetwork", "<-- HTTP $responseCode SUCCESS [$requestUrl]")
            Log.d("KinemaxNetwork", "    CONFIRMED DATA LOADED: $responseContent")
        } else {
            val errorBodyStr = try {
                val responseBody = response.body
                if (responseBody != null) {
                    val source = responseBody.source()
                    source.request(Long.MAX_VALUE)
                    val buffer = source.buffer.clone()
                    buffer.readString(Charsets.UTF_8)
                } else {
                    "No error body"
                }
            } catch (e: Exception) {
                "Error reading error body: ${e.message}"
            }
            val httpErrMsg = "HTTP $responseCode en $requestUrl\nDetalle: $errorBodyStr"
            Log.e("KinemaxNetwork", "<-- HTTP ERROR $responseCode [$requestUrl]")
            Log.e("KinemaxNetwork", "    HTTP Error Body: $errorBodyStr")
            NetworkErrorTracker.setError(httpErrMsg)
        }

        return response
    }
}
