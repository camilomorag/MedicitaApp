package com.example.medicitaapp.viewmodel

import android.app.Application
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import com.example.medicitaapp.data.AppDatabase
import com.example.medicitaapp.data.FormulaRequestEntity
import com.example.medicitaapp.data.NotificationEntity
import com.example.medicitaapp.data.UserEntity

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val userDao = db.userDao()
    private val formulaRequestDao = db.formulaRequestDao()
    private val notificationDao = db.notificationDao()

    var currentUser by mutableStateOf<UserEntity?>(null)
        private set

    suspend fun register(
        nombre: String,
        documento: String,
        telefono: String,
        password: String
    ): Result<String> {
        return try {
            val existing = userDao.getUserByDocumento(documento)

            if (existing != null) {
                Result.failure(Exception("El usuario ya existe"))
            } else {
                userDao.insertUser(
                    UserEntity(
                        nombre = nombre,
                        documento = documento,
                        telefono = telefono,
                        password = password
                    )
                )
                Result.success("Usuario registrado")
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(documento: String, password: String): Boolean {
        val user = userDao.login(documento, password)
        currentUser = user
        return user != null
    }

    suspend fun submitFormulaRequest(
        formulaUri: String,
        formulaType: String,
        medicamento: String
    ): Result<String> {
        return try {
            val user = currentUser ?: return Result.failure(Exception("No hay usuario logueado"))

            val request = FormulaRequestEntity(
                userDocumento = user.documento,
                userNombre = user.nombre,
                formulaUri = formulaUri,
                formulaType = formulaType,
                medicamento = medicamento,
                estado = "pendiente"
            )

            formulaRequestDao.insertRequest(request)

            notificationDao.insertNotification(
                NotificationEntity(
                    userDocumento = user.documento,
                    title = "Solicitud enviada",
                    message = "Su fórmula fue enviada correctamente y está pendiente de revisión."
                )
            )

            Result.success("Solicitud enviada")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserRequests(): List<FormulaRequestEntity> {
        val user = currentUser ?: return emptyList()
        return formulaRequestDao.getRequestsByUser(user.documento)
    }

    suspend fun getAllRequests(): List<FormulaRequestEntity> {
        return formulaRequestDao.getAllRequests()
    }

    suspend fun getRequestById(requestId: Int): FormulaRequestEntity? {
        return formulaRequestDao.getRequestById(requestId)
    }

    suspend fun getUserNotifications(): List<NotificationEntity> {
        val user = currentUser ?: return emptyList()
        return notificationDao.getNotificationsByUser(user.documento)
    }

    suspend fun updateRequestAsPharmacist(
        requestId: Int,
        estado: String,
        comentario: String = "",
        turno: String = "",
        ubicacion: String = ""
    ) {
        val request = formulaRequestDao.getRequestById(requestId) ?: return

        formulaRequestDao.updateRequestStatus(
            requestId = requestId,
            estado = estado,
            comentario = comentario,
            turno = turno,
            ubicacion = ubicacion
        )

        val message = when (estado) {
            "aceptada" -> "Su fórmula fue aceptada."
            "rechazada" -> "Su fórmula fue rechazada."
            "aplazada" -> "Su fórmula fue aplazada. Revise observaciones."
            "lista" -> "Su medicamento está listo para reclamar."
            else -> "Su solicitud fue actualizada."
        }

        notificationDao.insertNotification(
            NotificationEntity(
                userDocumento = request.userDocumento,
                title = "Actualización de solicitud",
                message = if (turno.isNotBlank()) {
                    "$message Turno: $turno. Ubicación: $ubicacion"
                } else {
                    message
                }
            )
        )
    }

    fun logout() {
        currentUser = null
    }
}