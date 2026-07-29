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
     * Chat interactivo con el Asistente Musical IA de Vibefy.
     * Permite al usuario hablar con la IA, pedir canciones por estado de ánimo,
     * actividad o estilo, y devuelve recomendaciones reales del catálogo.
     */
    data class AiChatResponse(
        val message: String,
        val recommendedSongs: List<Song> = emptyList()
    )

    suspend fun chatWithAi(
        userQuery: String,
        conversationHistory: List<Pair<String, String>>, // list of (sender, text)
        candidateCatalog: List<Song>,
        currentSong: Song? = null
    ): Result<AiChatResponse> = withContext(Dispatchers.IO) {
        try {
            val queryLower = userQuery.lowercase().trim()

            // Sort candidate catalog by relevance to user query
            val sortedCatalog = candidateCatalog.sortedByDescending { song ->
                var score = 0
                val artistLower = song.artista.lowercase()
                val titleLower = song.titulo.lowercase()
                val genreLower = song.generoNombre.lowercase()

                if (artistLower.isNotBlank() && (queryLower.contains(artistLower) || artistLower.contains(queryLower))) score += 100
                if (titleLower.isNotBlank() && (queryLower.contains(titleLower) || titleLower.contains(queryLower))) score += 60
                if (genreLower.isNotBlank() && (queryLower.contains(genreLower) || genreLower.contains(queryLower))) score += 30
                val queryWords = queryLower.split(Regex("\\s+")).filter { it.length > 2 }
                queryWords.forEach { word ->
                    if (artistLower.contains(word)) score += 25
                    if (titleLower.contains(word)) score += 15
                }
                if (currentSong != null && song.generoNombre.equals(currentSong.generoNombre, ignoreCase = true)) score += 5
                score
            }

            val distinctArtists = candidateCatalog.map { it.artista }.filter { it.isNotBlank() }.distinct().sorted()
            val distinctGenres = candidateCatalog.map { it.generoNombre }.filter { it.isNotBlank() }.distinct().sorted()

            val candidatesJsonArray = JSONArray()
            sortedCatalog.take(120).forEach { candidate ->
                val obj = JSONObject().apply {
                    put("id", candidate.id)
                    put("titulo", candidate.titulo)
                    put("artista", candidate.artista)
                    put("genero", candidate.generoNombre)
                    put("album", candidate.album ?: "")
                }
                candidatesJsonArray.put(obj)
            }

            val historyPrompt = if (conversationHistory.isNotEmpty()) {
                val sb = StringBuilder("Historial reciente del chat:\n")
                conversationHistory.takeLast(6).forEach { (sender, text) ->
                    sb.append("- ").append(if (sender == "user") "Usuario" else "Asistente").append(": ").append(text).append("\n")
                }
                sb.toString()
            } else ""

            val currentSongPrompt = if (currentSong != null) {
                "Canción reproduciéndose actualmente: '${currentSong.titulo}' de ${currentSong.artista} (${currentSong.generoNombre})."
            } else "No hay canción sonando en este momento."

            val prompt = """
                Eres el Asistente Musical de Inteligencia Artificial de Vibefy, una app de streaming de música líder.
                Tu trabajo es conversar de forma cálida, experta, entusiasta y amigable con el usuario en español sobre cualquier tema musical, artistas o recomendaciones.
                
                $historyPrompt
                $currentSongPrompt

                Mensaje actual del usuario: "$userQuery"

                ARTISTAS DISPONIBLES EN EL CATÁLOGO DE VIBEFY:
                ${distinctArtists.joinToString(", ")}

                GÉNEROS DISPONIBLES:
                ${distinctGenres.joinToString(", ")}

                CATÁLOGO REAL DE CANCIONES Y SUS IDs EN VIBEFY (JSON):
                $candidatesJsonArray

                INSTRUCCIONES DE RESPUESTA:
                1. Responde al mensaje del usuario de manera natural, cercana y profesional (1 a 3 párrafos).
                2. Si el usuario pide canciones de un ARTISTA específico (ej. Peso Pluma, Bad Bunny, Karol G, Feid, Fuerza Regida, etc.), de un GÉNERO, época o estado de ánimo, o pregunta qué escuchar, busca en el catálogo e incluye de 1 a 5 IDs de canciones del catálogo que coincidan exactamente en "recommendedSongIds".
                3. Si el usuario pregunta opiniones o información sobre un artista o canción, conversa como un experto musical entusiasta e incluye canciones del artista que tengamos en el catálogo.
                4. NO INVENTES IDs fuera de la lista de candidatos.

                MANDATO DE FORMATO: Responde obligatoriamente en formato JSON con la siguiente estructura:
                {
                   "message": "Tu respuesta conversacional en español aquí",
                   "recommendedSongIds": [id1, id2]
                }
            """.trimIndent()

            val schema = JSONObject().apply {
                put("type", "OBJECT")
                put("properties", JSONObject().apply {
                    put("message", JSONObject().put("type", "STRING"))
                    put("recommendedSongIds", JSONObject().apply {
                        put("type", "ARRAY")
                        put("items", JSONObject().put("type", "INTEGER"))
                    })
                })
                put("required", JSONArray().apply {
                    put("message")
                    put("recommendedSongIds")
                })
            }

            val responseText = callGeminiApi(prompt = prompt, jsonSchema = schema)
            val jsonObj = JSONObject(responseText)
            val replyMessage = jsonObj.optString("message", "¡Hola! Estoy listo para ayudarte a descubrir excelente música.")
            val idsArr = jsonObj.optJSONArray("recommendedSongIds") ?: JSONArray()
            val recommendedIds = mutableListOf<Long>()
            for (i in 0 until idsArr.length()) {
                recommendedIds.add(idsArr.optLong(i))
            }

            var matchedSongs = recommendedIds.mapNotNull { id -> candidateCatalog.find { it.id == id } }

            // If Gemini returned no IDs or empty matches, do a direct search in catalog
            if (matchedSongs.isEmpty()) {
                val directMatches = candidateCatalog.filter { song ->
                    val artistMatch = song.artista.isNotBlank() && (queryLower.contains(song.artista.lowercase()) || song.artista.lowercase().contains(queryLower))
                    val titleMatch = song.titulo.isNotBlank() && (queryLower.contains(song.titulo.lowercase()) || song.titulo.lowercase().contains(queryLower))
                    artistMatch || titleMatch
                }
                if (directMatches.isNotEmpty()) {
                    matchedSongs = directMatches.take(4)
                }
            }

            Result.success(AiChatResponse(message = replyMessage, recommendedSongs = matchedSongs))
        } catch (e: Exception) {
            Log.e("GeminiAiService", "Error in chatWithAi", e)
            val queryLower = userQuery.lowercase().trim()
            val directMatches = candidateCatalog.filter { song ->
                (song.artista.isNotBlank() && (queryLower.contains(song.artista.lowercase()) || song.artista.lowercase().contains(queryLower))) ||
                (song.titulo.isNotBlank() && (queryLower.contains(song.titulo.lowercase()) || song.titulo.lowercase().contains(queryLower)))
            }
            val fallbackSongs = if (directMatches.isNotEmpty()) {
                directMatches.take(4)
            } else {
                candidateCatalog.filter {
                    currentSong != null && it.generoNombre.equals(currentSong.generoNombre, ignoreCase = true) && it.id != currentSong.id
                }.take(3)
            }
            Result.success(
                AiChatResponse(
                    message = "¡Hola! Tuve una pequeña interferencia de señal con la IA, pero aquí tienes unas canciones destacadas que encajan con lo que buscas.",
                    recommendedSongs = fallbackSongs.ifEmpty { candidateCatalog.shuffled().take(3) }
                )
            )
        }
    }

    /**
     * Modo DJ IA Inteligente: Genera locuciones avanzadas de DJ con anuncios de canciones,
     * estilo de locutor personalizable y comentarios de transición de ritmo.
     */
    suspend fun generateSmartDjCommentary(
        currentSong: Song,
        nextSong: Song? = null,
        djStyle: String = "Radio FM Enérgico",
        timeOfDay: String = "día"
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val nextSongInfo = if (nextSong != null) {
                "Siguiente canción en cola: '${nextSong.titulo}' de ${nextSong.artista} (${nextSong.generoNombre})."
            } else "No hay canción siguiente definida en la cola."

            val prompt = """
                Actúa como un DJ profesional de radio en vivo en la app Vibefy.
                
                Estilo de DJ seleccionado: '$djStyle'
                Momento del día: '$timeOfDay'

                Canción actual a sonar/sonando:
                - Título: '${currentSong.titulo}'
                - Artista: '${currentSong.artista}'
                - Género: '${currentSong.generoNombre}'
                - Álbum: '${currentSong.album ?: "Sencillo"}'

                $nextSongInfo

                Genera un anuncio de DJ enérgico, fresco e inteligente de 2 a 3 frases (máximo 45 palabras) para la audiencia.
                
                Instrucciones:
                - Usa un tono acorde con el estilo '$djStyle' (ej. enérgico para radio urbana/pop, relajado para jazz/chill, vibrante para club).
                - Haz un breve comentario sobre la vibra de la canción o del artista, y anuncia lo que se viene.
                - Sin acotaciones de guión, sin paréntesis, sin emojis.
                - Idioma: Español listo para lectura por síntesis de voz (Text-to-Speech).
            """.trimIndent()

            val responseText = callGeminiApi(prompt)
            val cleanCommentary = responseText.replace(Regex("\\(.*?\\)"), "").trim()
            if (cleanCommentary.isNotBlank()) {
                Result.success(cleanCommentary)
            } else {
                Result.success("¡Atención vibers! Seguimos con el mejor sonido. Escuchamos ${currentSong.titulo} de ${currentSong.artista}.")
            }
        } catch (e: Exception) {
            Log.e("GeminiAiService", "Error in generateSmartDjCommentary", e)
            Result.success("¡Estás en la mejor sintonía de Vibefy! Disfruta de ${currentSong.titulo} de ${currentSong.artista}.")
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
