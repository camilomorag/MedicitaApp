package com.example.medicitaapp.data

import android.content.Context
import android.util.Log

object MedicineCsvLoader {

    fun loadMedicinesFromCsv(
        context: Context,
        fileName: String = "medicamentos.csv"
    ): List<MedicineEntity> {
        val medicines = mutableListOf<MedicineEntity>()
        val seenProducts = mutableSetOf<String>()

        return try {
            Log.d("MedicineCsvLoader", "=== INICIANDO CARGA DE CSV: $fileName ===")

            // Verificar si el archivo existe
            val assetFileList = context.assets.list("")
            val fileExists = assetFileList?.contains(fileName) ?: false

            if (!fileExists) {
                Log.e("MedicineCsvLoader", "❌ El archivo $fileName NO existe en assets")
                Log.d("MedicineCsvLoader", "Archivos disponibles: ${assetFileList?.joinToString()}")
                return emptyList()
            }

            Log.d("MedicineCsvLoader", "✅ Archivo encontrado: $fileName")

            val inputStream = context.assets.open(fileName)
            val content = inputStream.bufferedReader(Charsets.UTF_8).readText()
            val lines = content.lines()

            if (lines.isEmpty()) {
                Log.e("MedicineCsvLoader", "El archivo está vacío")
                return emptyList()
            }

            // Detectar separador automáticamente
            val firstLine = lines[0]
            val separator = if (firstLine.contains(";")) ";" else if (firstLine.contains(",")) "," else ";"
            Log.d("MedicineCsvLoader", "🔍 Separador detectado: '$separator'")
            Log.d("MedicineCsvLoader", "📋 Encabezado: ${firstLine.take(200)}")

            // Identificar índices de columnas importantes
            val headerParts = firstLine.lowercase().split(separator)
            val productoIndex = headerParts.indexOfFirst { it.contains("producto") }
            val expedienteIndex = headerParts.indexOfFirst { it.contains("expediente") }
            val registroIndex = headerParts.indexOfFirst { it.contains("registrosanitario") }
            val titularIndex = headerParts.indexOfFirst { it.contains("titular") }
            val estadoIndex = headerParts.indexOfFirst { it.contains("estadoregistro") }
            val viaIndex = headerParts.indexOfFirst { it.contains("viaadministracion") }

            Log.d("MedicineCsvLoader", "📊 Índices - Producto:$productoIndex, Expediente:$expedienteIndex, Registro:$registroIndex")

            // Saltar encabezado
            for (i in 1 until lines.size) {
                val line = lines[i]
                if (line.isBlank()) continue

                val parts = line.split(separator)

                // Obtener valores según los índices detectados
                val producto = if (productoIndex >= 0 && productoIndex < parts.size)
                    parts[productoIndex].trim().replace("\"", "").replace("@", "®") else ""

                val expediente = if (expedienteIndex >= 0 && expedienteIndex < parts.size)
                    parts[expedienteIndex].trim().replace("\"", "") else ""

                val registroSanitario = if (registroIndex >= 0 && registroIndex < parts.size)
                    parts[registroIndex].trim().replace("\"", "") else ""

                val titular = if (titularIndex >= 0 && titularIndex < parts.size)
                    parts[titularIndex].trim().replace("\"", "") else "No especificado"

                val estadoRegistro = if (estadoIndex >= 0 && estadoIndex < parts.size)
                    parts[estadoIndex].trim().replace("\"", "") else "Vigente"

                val viaAdministracion = if (viaIndex >= 0 && viaIndex < parts.size)
                    parts[viaIndex].trim().replace("\"", "") else ""

                // Evitar duplicados
                if (producto.isNotBlank() && producto.length > 3 && !seenProducts.contains(producto)) {
                    seenProducts.add(producto)

                    medicines.add(
                        MedicineEntity(
                            expediente = if (expediente.isBlank()) "N/A" else expediente,
                            producto = producto.take(150),
                            titular = titular.take(100),
                            registroSanitario = if (registroSanitario.isBlank()) "N/A" else registroSanitario,
                            fechaExpedicion = "",
                            fechaVencimiento = "",
                            estadoRegistro = if (estadoRegistro.isBlank()) "Vigente" else estadoRegistro,
                            descripcion = if (viaAdministracion.isNotBlank()) "Vía: $viaAdministracion" else "Medicamento registrado",
                            estadoComercial = "Activo",
                            unidad = "U",
                            disponible = estadoRegistro.equals("Vigente", ignoreCase = true)
                        )
                    )
                }

                // Limitar a 5000 para evitar problemas de memoria
                if (medicines.size >= 5000) {
                    Log.d("MedicineCsvLoader", "Límite de 5000 medicamentos alcanzado")
                    break
                }

                // Mostrar progreso
                if (i % 10000 == 0) {
                    Log.d("MedicineCsvLoader", "📊 Procesadas $i líneas... (${medicines.size} medicamentos)")
                }
            }

            Log.d("MedicineCsvLoader", "=== CARGA COMPLETADA ===")
            Log.d("MedicineCsvLoader", "💊 Medicamentos únicos cargados: ${medicines.size}")

            if (medicines.isEmpty()) {
                Log.w("MedicineCsvLoader", "⚠️ No se cargaron medicamentos")
            }

            medicines
        } catch (e: Exception) {
            Log.e("MedicineCsvLoader", "❌ Error: ${e.message}", e)
            emptyList()
        }
    }
}