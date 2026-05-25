package com.example.medicitaapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FormulaRequestDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequest(request: FormulaRequestEntity): Long

    @Query("SELECT * FROM formula_requests ORDER BY fechaCreacion DESC")
    suspend fun getAllRequests(): List<FormulaRequestEntity>

    @Query("SELECT * FROM formula_requests WHERE estado = :estado ORDER BY fechaCreacion DESC")
    suspend fun getRequestsByStatus(estado: String): List<FormulaRequestEntity>

    @Query("SELECT * FROM formula_requests WHERE userDocumento = :documento ORDER BY fechaCreacion DESC")
    suspend fun getRequestsByUser(documento: String): List<FormulaRequestEntity>

    @Query("SELECT * FROM formula_requests WHERE id = :requestId LIMIT 1")
    suspend fun getRequestById(requestId: Int): FormulaRequestEntity?

    @Query("""
    UPDATE formula_requests 
    SET estado = :estado,
        validacionIA = :validacionIA,
        mensajeValidacion = :mensajeValidacion,
        observacionesValidacion = :observacionesValidacion
    WHERE id = :requestId
""")
    suspend fun updateRequestWithValidation(
        requestId: Int,
        estado: String,
        validacionIA: Boolean,
        mensajeValidacion: String,
        observacionesValidacion: String
    )

    @Query("""
        UPDATE formula_requests 
        SET estado = :estado,
            comentarioFarmaceuta = :comentario,
            turno = :turno,
            ubicacion = :ubicacion
        WHERE id = :requestId
    """)
    suspend fun updateRequestStatus(
        requestId: Int,
        estado: String,
        comentario: String,
        turno: String,
        ubicacion: String
    )

    @Query("""
    UPDATE formula_requests 
    SET validacionIA = :validacionIA,
        mensajeValidacion = :mensajeValidacion,
        observacionesValidacion = :observacionesValidacion
    WHERE id = :requestId
""")
    suspend fun updateValidationResult(
        requestId: Int,
        validacionIA: Boolean,
        mensajeValidacion: String,
        observacionesValidacion: String
    )
}