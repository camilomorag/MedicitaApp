package com.example.medicitaapp.data

import android.content.Context
import android.util.Log

object MedicineCsvLoader {

    fun loadMedicinesFromCsv(
        context: Context,
        fileName: String = "medicamentos.csv"
    ): List<MedicineEntity> {
        val medicines = mutableListOf<MedicineEntity>()

        return try {
            val inputStream = context.assets.open(fileName)
            val lines = inputStream.bufferedReader(Charsets.UTF_8).readLines()

            if (lines.isEmpty()) return medicines

            // Saltar encabezado
            for (i in 1 until lines.size) {
                val line = lines[i]

                if (line.isBlank()) continue

                // En estos CSV normalmente el separador es ;
                val parts = line.split(";")

                // Leemos hasta índice 15, así que mínimo 16 columnas
                if (parts.size >= 16) {
                    medicines.add(
                        MedicineEntity(
                            expediente = parts.getOrNull(0)?.trim().orEmpty(),
                            producto = parts.getOrNull(1)?.trim().orEmpty(),
                            titular = parts.getOrNull(2)?.trim().orEmpty(),
                            registroSanitario = parts.getOrNull(3)?.trim().orEmpty(),
                            fechaExpedicion = parts.getOrNull(4)?.trim().orEmpty(),
                            fechaVencimiento = parts.getOrNull(5)?.trim().orEmpty(),
                            estadoRegistro = parts.getOrNull(6)?.trim().orEmpty(),
                            descripcion = parts.getOrNull(10)?.trim().orEmpty(),
                            estadoComercial = parts.getOrNull(11)?.trim().orEmpty(),
                            unidad = parts.getOrNull(15)?.trim().orEmpty(),
                            disponible = true
                        )
                    )
                }
            }

            medicines
        } catch (e: Exception) {
            Log.e("MedicineCsvLoader", "Error leyendo CSV: ${e.message}", e)
            emptyList()
        }
    }
}