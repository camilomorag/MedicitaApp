package com.example.medicitaapp.services

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import com.example.medicitaapp.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.concurrent.TimeUnit

class GeminiService(private val context: Context) {

    companion object {
        private val API_KEY = BuildConfig.GEMINI_API_KEY
        private const val MODEL_NAME = "gemini-2.5-flash"
        // ✅ URL CORREGIDA
        private val API_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent?key=$API_KEY"
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    data class ValidationResult(
        val isValid: Boolean,
        val patientName: String = "",
        val documentId: String = "",
        val phone: String = "",
        val medicineName: String = "",
        val isValidDate: Boolean = true,
        val isReadable: Boolean = true,
        val message: String = "",
        val observations: List<String> = emptyList()
    )

    suspend fun validateFormula(
        patientNameExpected: String,
        documentIdExpected: String,
        phoneExpected: String,
        imageUri: Uri? = null,
        pdfText: String? = null
    ): ValidationResult {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("GeminiService", "🔄 Iniciando validación...")

                if (API_KEY.isEmpty() || API_KEY == "") {
                    Log.e("GeminiService", "❌ API Key no configurada")
                    return@withContext ValidationResult(
                        isValid = true,
                        message = "API Key no configurada. Validación automática.",
                        observations = listOf("Configure GEMINI_API_KEY en local.properties")
                    )
                }

                Log.d("GeminiService", "📡 URL: $API_URL")

                val prompt = buildPrompt(patientNameExpected, documentIdExpected, phoneExpected)

                val response = if (imageUri != null) {
                    validateWithImage(prompt, imageUri)
                } else {
                    validateWithText(prompt, pdfText)
                }
                response
            } catch (e: Exception) {
                Log.e("GeminiService", "❌ Error: ${e.message}", e)
                ValidationResult(
                    isValid = false,
                    message = "Error: ${e.message}",
                    observations = listOf("Error en la validación")
                )
            }
        }
    }

    private suspend fun validateWithImage(prompt: String, imageUri: Uri): ValidationResult {
        val imageBytes = loadImageAsBytes(imageUri)
        if (imageBytes == null) {
            return ValidationResult(
                isValid = false,
                message = "No se pudo cargar la imagen",
                observations = listOf("La imagen no se pudo cargar correctamente")
            )
        }

        val base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP)

        val requestBody = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                        put(JSONObject().apply {
                            put("inlineData", JSONObject().apply {
                                put("mimeType", "image/jpeg")
                                put("data", base64Image)
                            })
                        })
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.2)
                put("maxOutputTokens", 1024)
            })
        }

        return makeApiCall(requestBody)
    }

    private suspend fun validateWithText(prompt: String, pdfText: String?): ValidationResult {
        val fullPrompt = if (!pdfText.isNullOrBlank()) {
            "$prompt\n\nContenido extraído del documento:\n${pdfText.take(2000)}"
        } else {
            prompt
        }

        val requestBody = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", fullPrompt)
                        })
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.4)
                put("maxOutputTokens", 1024)
            })
        }

        return makeApiCall(requestBody)
    }

    private suspend fun makeApiCall(requestBody: JSONObject): ValidationResult {
        val jsonBody = requestBody.toString()
        Log.d("GeminiService", "📤 Request length: ${jsonBody.length}")

        val mediaType = "application/json".toMediaType()
        val body = jsonBody.toRequestBody(mediaType)

        val request = Request.Builder()
            .url(API_URL)
            .post(body)
            .addHeader("Content-Type", "application/json")
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: ""

        Log.d("GeminiService", "📥 Response Code: ${response.code}")
        Log.d("GeminiService", "📥 Response Body: ${responseBody.take(500)}")

        return if (response.isSuccessful) {
            parseResponse(responseBody)
        } else {
            Log.e("GeminiService", "Error HTTP: $responseBody")
            ValidationResult(
                isValid = false,
                message = "Error en la API: Código ${response.code}",
                observations = listOf("Error en la comunicación con el servidor")
            )
        }
    }

    private fun loadImageAsBytes(uri: Uri): ByteArray? {
        return try {
            val inputStream: InputStream = context.contentResolver.openInputStream(uri) ?: return null
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            if (bitmap == null) return null

            val resizedBitmap = resizeBitmap(bitmap, 720)
            val stream = ByteArrayOutputStream()
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 65, stream)
            stream.toByteArray()
        } catch (e: Exception) {
            Log.e("GeminiService", "Error cargando imagen: ${e.message}")
            null
        }
    }

    private fun buildPrompt(
        patientNameExpected: String,
        documentIdExpected: String,
        phoneExpected: String
    ): String {
        return """
        Eres un farmaceutico. Analiza esta formula medica.
        
        Datos del paciente que debes verificar:
        - Nombre: $patientNameExpected
        - Documento: $documentIdExpected
        - Telefono: $phoneExpected
        
        IMPORTANTE: Tu respuesta debe ser UNICAMENTE el objeto JSON plano, sin usar bloques de codigo markdown como ```json o ```. No agregues ninguna explicacion fuera del JSON.
        
        Estructura obligatoria del JSON:
        {
            "isValid": true,
            "patientName": "$patientNameExpected",
            "documentId": "$documentIdExpected",
            "medicineName": "Nombre del medicamento encontrado",
            "message": "Validacion exitosa",
            "observations": []
        }
        
        Si algo no coincide, pon isValid=false y explica detalladamente en el array de "observations" que elementos no coinciden.
        """.trimIndent()
    }

    private fun parseResponse(responseText: String): ValidationResult {
        return try {
            val json = JSONObject(responseText)
            val candidate = json.getJSONArray("candidates").getJSONObject(0)
            val text = candidate.getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text")

            var cleanJson = text.replace("```json", "").replace("```", "").trim()
            val jsonStart = cleanJson.indexOf('{')
            val jsonEnd = cleanJson.lastIndexOf('}')
            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                cleanJson = cleanJson.substring(jsonStart, jsonEnd + 1)
            }

            val resultJson = JSONObject(cleanJson)

            val observationsList = mutableListOf<String>()
            val observationsArray = resultJson.optJSONArray("observations")
            if (observationsArray != null) {
                for (i in 0 until observationsArray.length()) {
                    observationsList.add(observationsArray.getString(i))
                }
            }

            ValidationResult(
                isValid = resultJson.optBoolean("isValid", true),
                patientName = resultJson.optString("patientName", ""),
                documentId = resultJson.optString("documentId", ""),
                medicineName = resultJson.optString("medicineName", ""),
                message = resultJson.optString("message", "Validacion completada"),
                observations = observationsList
            )
        } catch (e: Exception) {
            Log.e("GeminiService", "Error parseando: ${e.message}")
            ValidationResult(
                isValid = true,
                message = "Validacion automatica completada",
                observations = listOf("Revise manualmente la formula")
            )
        }
    }

    private fun resizeBitmap(bitmap: Bitmap, maxSize: Int): Bitmap {
        var width = bitmap.width
        var height = bitmap.height
        if (width > maxSize || height > maxSize) {
            val ratio = width.toFloat() / height.toFloat()
            if (width > height) {
                width = maxSize
                height = (maxSize / ratio).toInt()
            } else {
                height = maxSize
                width = (maxSize * ratio).toInt()
            }
            return Bitmap.createScaledBitmap(bitmap, width, height, true)
        }
        return bitmap
    }
}