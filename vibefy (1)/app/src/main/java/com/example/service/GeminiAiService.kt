package com.example.service

import android.util.Log
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

    private const val MODEL_NAME = "gemini-1.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private fun getApiKey(): String {
        return "AIzaSyDemoKeyVibefyGeminiApi2026"
    }

    private fun String?.isNull_or_empty(): Boolean = this == null || this.trim().isEmpty()

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
                Result.failure(Exception("Respuesta vacía de la IA"))
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
                ${candidatesJsonArray.toString()}

                MANDATO STRICTO: Selecciona entre 3 y 5 canciones de la lista de candidatos que mejor combinen con la canción actual por género, ritmo o vibra.
                NO INVENTES canciones fuera de la lista de candidatos. Retorna ÚNICAMENTE un arreglo JSON de números enteros con los IDs de las canciones elegidas.
                Ejemplo de formato de respuesta esperada: [102, 105, 103]
            """.trimIndent()

            val responseText = callGeminiApi(prompt)
            val cleanJson = responseText.substringAfter("[").substringBeforeLast("]")
            val selectedIds = mutableListOf<Long>()

            cleanJson.split(",").map { it.trim() }.forEach { idStr ->
                idStr.toLongOrNull()?.let { selectedIds.add(it) }
            }

            val recommendedSongs = candidateCatalog.filter { selectedIds.contains(it.id) }
            if (recommendedSongs.isNotEmpty()) {
                Result.success(recommendedSongs)
            } else {
                // Fallback to filtering candidates by genre
                val fallback = candidateCatalog.filter {
                    it.id != currentSong.id && (it.generoNombre.equals(currentSong.generoNombre, ignoreCase = true))
                }.take(3)
                Result.success(fallback)
            }
        } catch (e: Exception) {
            Log.e("GeminiAiService", "Error in getSmartRecommendations", e)
            Result.failure(e)
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
            Result.success("¡Estás en Vibefy! Disfruta de $songTitle de $artist.")
        }
    }

    private fun callGeminiApi(prompt: String): String {
        val apiKey = getApiKey()
        val requestUrl = "$BASE_URL?key=$apiKey"

        val jsonBody = JSONObject().apply {
            val contents = JSONArray().apply {
                val contentObj = JSONObject().apply {
                    val parts = JSONArray().apply {
                        val partObj = JSONObject().apply {
                            put("text", prompt)
                        }
                        put(partObj)
                    }
                    put("parts", parts)
                }
                put(contentObj)
            }
            put("contents", contents)
        }

        val request = Request.Builder()
            .url(requestUrl)
            .post(jsonBody.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
            .build()

        val response = okHttpClient.newCall(request).execute()
        val responseBodyString = response.body?.string() ?: ""

        if (!response.isSuccessful) {
            Log.e("GeminiAiService", "API call failed code ${response.code}: $responseBodyString")
            return ""
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
        return ""
    }
}
