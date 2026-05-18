package com.example.medicitaapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medicines")
data class MedicineEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val expediente: String,
    val producto: String,
    val titular: String,
    val registroSanitario: String,
    val fechaExpedicion: String,
    val fechaVencimiento: String,
    val estadoRegistro: String,
    val descripcion: String,
    val estadoComercial: String,
    val unidad: String,
    val disponible: Boolean = true
)