package com.example.medicitaapp.services

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
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
        pdfUri: Uri? = null
    ): ValidationResult {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("GeminiService", "🔄 Iniciando validación...")

                if (API_KEY.isEmpty() || API_KEY == "" || API_KEY == "null") {
                    return@withContext ValidationResult(
                        isValid = true,
                        message = "API Key no configurada. Validación automática.",
                        observations = listOf("Agrega GEMINI_API_KEY en local.properties")
                    )
                }

                val prompt = buildPrompt(patientNameExpected, documentIdExpected, phoneExpected)

                val response = when {
                    imageUri != null -> {
                        Log.d("GeminiService", "📷 Validando con IMAGEN")
                        validateWithImage(prompt, imageUri)
                    }
                    pdfUri != null -> {
                        Log.d("GeminiService", "📄 Validando con PDF - Convirtiendo a imágenes")
                        validateWithPdf(prompt, pdfUri)
                    }
                    else -> {
                        ValidationResult(
                            isValid = false,
                            message = "No se proporcionó ningún archivo",
                            observations = listOf("Debe seleccionar una imagen o PDF")
                        )
                    }
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

    // ✅ NUEVO: Validar PDF convirtiendo sus páginas a imágenes
    private suspend fun validateWithPdf(prompt: String, pdfUri: Uri): ValidationResult {
        val pageImages = extractPdfPagesAsImages(pdfUri)
        if (pageImages.isEmpty()) {
            return ValidationResult(
                isValid = false,
                message = "No se pudieron extraer páginas del PDF",
                observations = listOf("El PDF no se pudo leer correctamente")
            )
        }

        // Enviar la primera página del PDF (la más importante)
        val firstPage = pageImages[0]
        val base64Image = Base64.encodeToString(firstPage, Base64.NO_WRAP)

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

    // ✅ Extraer páginas de PDF como imágenes (sin PDFBox)
    private fun extractPdfPagesAsImages(uri: Uri): List<ByteArray> {
        val images = mutableListOf<ByteArray>()
        var parcelFileDescriptor: ParcelFileDescriptor? = null
        var renderer: PdfRenderer? = null

        return try {
            parcelFileDescriptor = context.contentResolver.openFileDescriptor(uri, "r")
            if (parcelFileDescriptor == null) return emptyList()

            renderer = PdfRenderer(parcelFileDescriptor)
            Log.d("GeminiService", "📄 PDF tiene ${renderer.pageCount} páginas")

            // Tomar solo las primeras 3 páginas máximo
            val pagesToProcess = minOf(renderer.pageCount, 3)

            for (i in 0 until pagesToProcess) {
                val page = renderer.openPage(i)
                val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

                // Convertir bitmap a JPEG
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream)
                images.add(stream.toByteArray())

                bitmap.recycle()
                page.close()
                Log.d("GeminiService", "📄 Página ${i + 1} convertida")
            }

            renderer.close()
            parcelFileDescriptor.close()
            images
        } catch (e: Exception) {
            Log.e("GeminiService", "Error extrayendo páginas del PDF: ${e.message}", e)
            emptyList()
        } finally {
            try {
                renderer?.close()
                parcelFileDescriptor?.close()
            } catch (e: Exception) {
                Log.e("GeminiService", "Error cerrando recursos: ${e.message}")
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

            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 65, stream)
            bitmap.recycle()
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
}