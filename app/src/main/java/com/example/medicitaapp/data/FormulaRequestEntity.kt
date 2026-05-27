package com.example.medicitaapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "formula_requests")
data class FormulaRequestEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userDocumento: String,
    val userNombre: String,
    val userTelefono: String = "",  // ✅ Campo agregado
    val formulaUri: String,
    val formulaType: String,
    val medicamento: String,
    val estado: String = "pendiente",
    val turno: String = "",
    val ubicacion: String = "",
    val tiempoEspera: Int = 0,      // ✅ Campo agregado
    val posicionCola: Int = 0,      // ✅ Campo agregado
    val comentarioFarmaceuta: String = "",
    val fechaCreacion: Long = System.currentTimeMillis(),
    val validacionIA: Boolean = false,
    val mensajeValidacion: String = "",
    val observacionesValidacion: String = ""
)