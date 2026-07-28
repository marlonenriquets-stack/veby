package com.example.data.model

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson

class GenreAdapter {
    @FromJson
    fun fromJson(reader: JsonReader): Genre? {
        return when (reader.peek()) {
            JsonReader.Token.NULL -> {
                reader.nextNull<Genre>()
            }
            JsonReader.Token.STRING -> {
                val str = reader.nextString()
                if (str.isBlank()) null else Genre(id = 0L, nombre = str)
            }
            JsonReader.Token.BEGIN_OBJECT -> {
                reader.beginObject()
                var id = 0L
                var nombre = ""
                var imagenUrl: String? = null
                var numCanciones: Int? = 0
                val options = JsonReader.Options.of("id", "nombre", "imagen_url", "num_canciones")
                while (reader.hasNext()) {
                    when (reader.selectName(options)) {
                        0 -> {
                            id = try {
                                reader.nextLong()
                            } catch (_: Exception) {
                                try {
                                    reader.nextInt().toLong()
                                } catch (_: Exception) {
                                    reader.nextString().toLongOrNull() ?: 0L
                                }
                            }
                        }
                        1 -> nombre = if (reader.peek() == JsonReader.Token.NULL) { reader.nextNull<Void>(); "" } else reader.nextString()
                        2 -> imagenUrl = if (reader.peek() == JsonReader.Token.NULL) { reader.nextNull<Void>(); null } else reader.nextString()
                        3 -> numCanciones = if (reader.peek() == JsonReader.Token.NULL) {
                            reader.nextNull<Void>()
                            0
                        } else {
                            try {
                                reader.nextInt()
                            } catch (_: Exception) {
                                reader.nextString().toIntOrNull() ?: 0
                            }
                        }
                        else -> reader.skipValue()
                    }
                }
                reader.endObject()
                Genre(id = id, nombre = nombre, imagenUrl = imagenUrl, numCanciones = numCanciones)
            }
            else -> {
                reader.skipValue()
                null
            }
        }
    }

    @ToJson
    fun toJson(writer: JsonWriter, value: Genre?) {
        if (value == null) {
            writer.nullValue()
        } else {
            writer.beginObject()
            writer.name("id").value(value.id)
            writer.name("nombre").value(value.nombre)
            if (value.imagenUrl != null) {
                writer.name("imagen_url").value(value.imagenUrl)
            }
            writer.endObject()
        }
    }
}
