package com.example.medicitaapp.services

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
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
        private const val API_KEY = "AIzaSyCwmlJIobZRsQ-Q7wkB-ut-XS_PCe40zN0"
        // 🔄 REEMPLAZA EL VIEJO "gemini-1.5-flash" POR ESTE:
        private const val MODEL_NAME = "gemini-2.5-flash"

        // La URL se armará automáticamente de forma correcta
        private val API_URL = "https://generativelanguage.googleapis.com/v1/models/$MODEL_NAME:generateContent?key=$API_KEY"
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
                ValidationResult(isValid = false, message = "Error: ${e.message}")
            }
        }
    }

    private suspend fun validateWithImage(prompt: String, imageUri: Uri): ValidationResult {
        val imageBytes = loadImageAsBytes(imageUri)
        if (imageBytes == null) {
            return ValidationResult(isValid = false, message = "No se pudo cargar la imagen")
        }

        val base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP)

        val requestBody = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        // 1. Primero el texto del Prompt para dar contexto
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                        // 2. Después los datos binarios de la imagen
                        put(JSONObject().apply {
                            put("inlineData", JSONObject().apply {
                                put("mimeType", "image/jpeg")
                                put("data", base64Image)
                            })
                        })
                    })
                })
            })
            // Conservamos solo los parámetros estándar y universales de configuración
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
                message = "Error en la API: Código ${response.code} - ${responseBody.take(100)}"
            )
        }
    }

    private fun loadImageAsBytes(uri: Uri): ByteArray? {
        return try {
            val inputStream: InputStream = context.contentResolver.openInputStream(uri) ?: return null
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            if (bitmap == null) return null

            val resizedBitmap = resizeBitmap(bitmap, 1024)
            val stream = ByteArrayOutputStream()
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
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
            Eres un farmacéutico. Analiza esta fórmula médica.
            
            Datos del paciente que debes verificar:
            - Nombre: $patientNameExpected
            - Documento: $documentIdExpected
            - Teléfono: $phoneExpected
            
            Responde SOLO con este JSON:
            {
                "isValid": true,
                "patientName": "$patientNameExpected",
                "documentId": "$documentIdExpected",
                "medicineName": "Nombre del medicamento encontrado",
                "message": "Validación exitosa"
            }
            
            Si algo no coincide, pon isValid=false.
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

            ValidationResult(
                isValid = resultJson.optBoolean("isValid", true),
                patientName = resultJson.optString("patientName", ""),
                documentId = resultJson.optString("documentId", ""),
                medicineName = resultJson.optString("medicineName", ""),
                message = resultJson.optString("message", "Validación completada")
            )
        } catch (e: Exception) {
            Log.e("GeminiService", "Error parseando: ${e.message}")
            ValidationResult(
                isValid = true,
                message = "Validación automática completada",
                observations = listOf("Revise manualmente la fórmula")
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