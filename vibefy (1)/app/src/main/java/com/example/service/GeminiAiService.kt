package com.example.service

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiAiService {

    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    /**
     * Devuelve la API key real configurada, o null si no hay ninguna configurada.
     * Ya NO cae en una key falsa — es preferible un error claro a una llamada
     * que siempre va a fallar en silencio contra la API real de Google.
     */
    private fun getApiKey(): String? {
        return try {
            val key = BuildConfig.GEMINI_API_KEY
            if (key.isNullOrBlank() || key == "TU_GEMINI_API_KEY_AQUI" || key == "MY_GEMINI_API_KEY") {
                null
            } else key
        } catch (e: Exception) {
            null
        }
    }

    private val sinApiKeyError = Exception(
        "Gemini API key no configurada. Copia .env.example a .env y pon tu key real " +
        "(gratis en https://aistudio.google.com/apikey), o si estás en AI Studio revisa " +
        "la sección de secretos del proyecto."
    )

    /**
     * Explicar esta canción: Explicación breve de la temática, contexto y curiosidades.
     */
    suspend fun explainSong(songTitle: String, artist: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val prompt = """
                Proporciona una explicación breve, atrapante y clara (2 a 3 párrafos cortos) sobre la temática, el significado, el contexto cultural o curiosidades de la canción '$songTitle' del artista '$artist'.
                Instrucciones importantes:
                - No reproduzcas letras completas de la canción por derechos de autor.
                - Mantén un tono entusiasta de crítico musical para una app de streaming llamada Vibefy.
                - Responde en idioma español.
            """.trimIndent()

            val responseText = callGeminiApi(prompt)
            if (responseText.isNotBlank()) {
                Result.success(responseText)
            } else {
                Result.failure(Exception("Gemini devolvió una respuesta vacía. Intenta de nuevo."))
            }
        } catch (e: Exception) {
            Log.e("GeminiAiService", "Error in explainSong", e)
            Result.failure(e)
        }
    }

    /**
     * Recomendado para ti: Manda la canción actual + candidatos reales del catálogo a Gemini para que elija.
     */
    suspend fun getSmartRecommendations(currentSong: Song, candidateCatalog: List<Song>): Result<List<Song>> = withContext(Dispatchers.IO) {
        try {
            val candidatesJsonArray = JSONArray()
            candidateCatalog.take(30).forEach { candidate ->
                if (candidate.id != currentSong.id) {
                    val obj = JSONObject().apply {
                        put("id", candidate.id)
                        put("titulo", candidate.titulo)
                        put("artista", candidate.artista)
                        put("genero", candidate.generoNombre)
                    }
                    candidatesJsonArray.put(obj)
                }
            }

            val prompt = """
                Actúa como un DJ inteligente y curador musical profesional.
                Canción actual del usuario:
                - Título: '${currentSong.titulo}'
                - Artista: '${currentSong.artista}'
                - Género: '${currentSong.generoNombre}'

                Lista de candidatos REALES disponibles en la plataforma Vibefy (JSON):
                ${candidatesJsonArray}

                MANDATO ESTRICTO: Selecciona entre 3 y 5 canciones de la lista de candidatos que mejor combinen con la canción actual por género, ritmo o vibra.
                NO INVENTES canciones fuera de la lista de candidatos.
            """.trimIndent()

            // Fuerza a Gemini a responder JSON puro (sin ```json ni texto extra alrededor),
            // usando un schema estricto en vez de parsear texto libre.
            val responseText = callGeminiApi(
                prompt = prompt,
                jsonSchema = JSONObject().apply {
                    put("type", "ARRAY")
                    put("items", JSONObject().put("type", "INTEGER"))
                }
            )

            val selectedIds = mutableListOf<Long>()
            try {
                val arr = JSONArray(responseText)
                for (i in 0 until arr.length()) {
                    selectedIds.add(arr.optLong(i))
                }
            } catch (e: Exception) {
                // Respaldo por si el modelo no siguió el schema al pie de la letra
                responseText.substringAfter("[").substringBeforeLast("]")
                    .split(",").map { it.trim() }
                    .forEach { idStr -> idStr.toLongOrNull()?.let { selectedIds.add(it) } }
            }

            val recommendedSongs = candidateCatalog.filter { selectedIds.contains(it.id) }
            if (recommendedSongs.isNotEmpty()) {
                Result.success(recommendedSongs)
            } else {
                // Respaldo: filtra candidatos por mismo género si Gemini no devolvió nada usable
                val fallback = candidateCatalog.filter {
                    it.id != currentSong.id && it.generoNombre.equals(currentSong.generoNombre, ignoreCase = true)
                }.take(5)
                Result.success(fallback)
            }
        } catch (e: Exception) {
            Log.e("GeminiAiService", "Error in getSmartRecommendations", e)
            // Ante error de red/API, igual intenta dar algo útil por género en vez de dejar la pantalla vacía
            val fallback = candidateCatalog.filter {
                it.id != currentSong.id && it.generoNombre.equals(currentSong.generoNombre, ignoreCase = true)
            }.take(5)
            if (fallback.isNotEmpty()) Result.success(fallback) else Result.failure(e)
        }
    }

    /**
     * Modo DJ IA: Genera un texto corto tipo locutor (2-3 frases) presentando la canción.
     */
    suspend fun generateDjIntro(songTitle: String, artist: String, genre: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val prompt = """
                Actúa como un DJ locutor de radio joven y enérgico de la app Vibefy.
                Genera una presentación súper corta de 2 a 3 frases (máximo 40 palabras) para introducir la canción que está a punto de sonar:
                - Título: '$songTitle'
                - Artista: '$artist'
                - Género: '$genre'

                Reglas:
                - Sé natural, directo y entusiasta.
                - No incluyas emoticonos ni acotaciones de guión entre paréntesis como '(música de fondo)'.
                - Escribe en español listo para ser leído por un motor de texto a voz (Text-to-Speech).
            """.trimIndent()

            val responseText = callGeminiApi(prompt)
            val cleanIntro = responseText.replace(Regex("\\(.*?\\)"), "").trim()
            if (cleanIntro.isNotBlank()) {
                Result.success(cleanIntro)
            } else {
                Result.success("¡Atención vibers! A continuación escucharemos $songTitle de $artist. ¡Que lo disfrutes!")
            }
        } catch (e: Exception) {
            Log.e("GeminiAiService", "Error generating DJ intro", e)
            // El modo DJ nunca debe romper la reproducción: si Gemini falla, sigue con un intro genérico.
            Result.success("¡Estás en Vibefy! Disfruta de $songTitle de $artist.")
        }
    }

    /**
     * @param jsonSchema si se manda, fuerza a Gemini a responder JSON válido siguiendo ese
     * esquema (responseMimeType application/json), evitando el problema típico de que el
     * modelo envuelva el JSON en backticks de markdown y rompa el parseo.
     */
    private fun callGeminiApi(prompt: String, jsonSchema: JSONObject? = null): String {
        val apiKey = getApiKey() ?: throw sinApiKeyError
        val requestUrl = "$BASE_URL?key=$apiKey"

        val jsonBody = JSONObject().apply {
            val contents = JSONArray().apply {
                val contentObj = JSONObject().apply {
                    val parts = JSONArray().apply {
                        put(JSONObject().put("text", prompt))
                    }
                    put("parts", parts)
                }
                put(contentObj)
            }
            put("contents", contents)

            if (jsonSchema != null) {
                put("generationConfig", JSONObject().apply {
                    put("responseMimeType", "application/json")
                    put("responseSchema", jsonSchema)
                })
            }
        }

        val request = Request.Builder()
            .url(requestUrl)
            .post(jsonBody.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
            .build()

        val response = okHttpClient.newCall(request).execute()
        val responseBodyString = response.body?.string() ?: ""

        if (!response.isSuccessful) {
            Log.e("GeminiAiService", "API call failed code ${response.code}: $responseBodyString")
            throw Exception("Gemini respondió HTTP ${response.code}: ${responseBodyString.take(300)}")
        }

        val jsonResponse = JSONObject(responseBodyString)
        val candidates = jsonResponse.optJSONArray("candidates")
        if (candidates != null && candidates.length() > 0) {
            val candidate = candidates.getJSONObject(0)
            val content = candidate.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            if (parts != null && parts.length() > 0) {
                return parts.getJSONObject(0).optString("text", "")
            }
        }

        // Si Gemini bloqueó la respuesta (safety filters) u otra causa, dilo explícitamente
        val blockReason = jsonResponse.optJSONObject("promptFeedback")?.optString("blockReason")
        if (!blockReason.isNullOrBlank()) {
            throw Exception("Gemini bloqueó la respuesta: $blockReason")
        }
        return ""
    }
}
