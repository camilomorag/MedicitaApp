package com.example.medicitaapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "formula_requests")
data class FormulaRequestEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userDocumento: String,
    val userNombre: String,
    val formulaUri: String,
    val formulaType: String, // "image" o "pdf"
    val medicamento: String,
    val estado: String = "pendiente", // pendiente, aceptada, rechazada, aplazada, lista
    val turno: String = "",
    val ubicacion: String = "",
    val comentarioFarmaceuta: String = "",
    val fechaCreacion: Long = System.currentTimeMillis()
)