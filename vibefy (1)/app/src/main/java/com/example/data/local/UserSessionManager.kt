package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "kinemax_user_prefs")

class UserSessionManager(private val context: Context) {

    companion object {
        private val ID_TOKEN = stringPreferencesKey("id_token")
        private val USER_ID = stringPreferencesKey("user_id")
        private val USER_NAME = stringPreferencesKey("user_name")
        private val USER_EMAIL = stringPreferencesKey("user_email")
        private val ES_PREMIUM = booleanPreferencesKey("es_premium")
        private val QUITA_ANUNCIOS = booleanPreferencesKey("quita_anuncios")
        private val PERMITE_DESCARGAS = booleanPreferencesKey("permite_descargas")
        private val PLAN = stringPreferencesKey("user_plan")
        private val ANUNCIOS_INTERVALO = stringPreferencesKey("anuncios_intervalo")

        @Volatile
        private var INSTANCE: UserSessionManager? = null

        fun getInstance(context: Context): UserSessionManager {
            return INSTANCE ?: synchronized(this) {
                val instance = UserSessionManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }

        // Static memory cache of latest token for fast sync access in OkHttp Interceptor
        @Volatile
        var cachedToken: String? = null
    }

    val idTokenFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        val token = prefs[ID_TOKEN]
        cachedToken = token
        token
    }

    val userFlow: Flow<User?> = context.dataStore.data.map { prefs ->
        val id = prefs[USER_ID] ?: return@map null
        User(
            id = id,
            nombre = prefs[USER_NAME] ?: "",
            email = prefs[USER_EMAIL] ?: "",
            esPremium = prefs[ES_PREMIUM] ?: false,
            quitaAnuncios = prefs[QUITA_ANUNCIOS] ?: false,
            permiteDescargas = prefs[PERMITE_DESCARGAS] ?: false,
            plan = prefs[PLAN] ?: "Gratuito"
        )
    }

    suspend fun saveSession(idToken: String, user: User) {
        cachedToken = idToken
        context.dataStore.edit { prefs ->
            prefs[ID_TOKEN] = idToken
            prefs[USER_ID] = user.id
            prefs[USER_NAME] = user.nombre
            prefs[USER_EMAIL] = user.email
            prefs[ES_PREMIUM] = user.esPremium
            prefs[QUITA_ANUNCIOS] = user.quitaAnuncios
            prefs[PERMITE_DESCARGAS] = user.permiteDescargas
            prefs[PLAN] = user.plan ?: "Gratuito"
        }
    }

    suspend fun updateToken(idToken: String) {
        cachedToken = idToken
        context.dataStore.edit { prefs ->
            prefs[ID_TOKEN] = idToken
        }
    }

    suspend fun clearSession() {
        cachedToken = null
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }
}
