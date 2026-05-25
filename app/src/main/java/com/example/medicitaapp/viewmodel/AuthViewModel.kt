package com.example.medicitaapp.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import com.example.medicitaapp.data.*
import com.example.medicitaapp.services.NotificationService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val userDao = db.userDao()
    private val formulaRequestDao = db.formulaRequestDao()
    private val notificationDao = db.notificationDao()
    private val sessionManager = SessionManager(application)
    private val medicineDao = db.medicineDao()

    lateinit var notificationService: NotificationService

    var currentUser by mutableStateOf<UserEntity?>(null)
        private set

    var isPharmacistLoggedIn by mutableStateOf(false)
        private set

    init {
        restoreSession()
    }

    fun initNotificationService(context: Context) {
        notificationService = NotificationService(context)
    }

    private fun restoreSession() {
        isPharmacistLoggedIn = sessionManager.isPharmacistLoggedIn()
        val documento = sessionManager.getUserDocumento()
        if (documento != null) {
            // Se cargará luego con login
        }
    }

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
        if (user != null) {
            sessionManager.saveUserSession(user.documento)
            isPharmacistLoggedIn = false
        }
        return user != null
    }

    fun loginAsPharmacist() {
        isPharmacistLoggedIn = true
        currentUser = null
        sessionManager.savePharmacistSession()
    }

    suspend fun submitFormulaRequest(
        formulaUri: String,
        formulaType: String
    ): Result<String> {
        return try {
            val user = currentUser ?: return Result.failure(Exception("No hay usuario logueado"))

            val request = FormulaRequestEntity(
                userDocumento = user.documento,
                userNombre = user.nombre,
                formulaUri = formulaUri,
                formulaType = formulaType,
                medicamento = "Pendiente de validación",
                estado = "pendiente"
            )

            formulaRequestDao.insertRequest(request)

            // 📢 Notificación para el usuario
            notificationService.showFormulaSubmittedNotification("Pendiente de validación")

            // Guardar notificación en DB
            notificationDao.insertNotification(
                NotificationEntity(
                    userDocumento = user.documento,
                    title = "📄 Fórmula enviada",
                    message = "Tu fórmula ha sido enviada correctamente y está pendiente de revisión."
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

        // 📢 Notificación al usuario sobre el cambio de estado
        notificationService.showFormulaStatusNotification(
            estado = estado,
            medicamento = request.medicamento,
            turno = turno,
            ubicacion = ubicacion
        )

        val title = when (estado) {
            "aceptada" -> "✅ Fórmula aceptada"
            "rechazada" -> "❌ Fórmula rechazada"
            "aplazada" -> "⏰ Fórmula aplazada"
            "lista" -> "🎉 Medicamento listo"
            else -> "Actualización de solicitud"
        }

        val message = when (estado) {
            "aceptada" -> "Su fórmula fue aceptada.${if (turno.isNotBlank()) " Turno: $turno" else ""}"
            "rechazada" -> "Su fórmula fue rechazada. Comuníquese con el farmaceuta."
            "aplazada" -> "Su fórmula fue aplazada. Revise observaciones."
            "lista" -> "Su medicamento está listo para reclamar. Ubicación: $ubicacion"
            else -> "Su solicitud fue actualizada."
        }

        notificationDao.insertNotification(
            NotificationEntity(
                userDocumento = request.userDocumento,
                title = title,
                message = message
            )
        )
    }

    // ============================================
    // FUNCIONES PARA CARGAR MEDICAMENTOS DESDE API
    // ============================================

    suspend fun loadMedicinesFromApi(): List<MedicineEntity> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("AuthViewModel", "🔄 Cargando medicamentos desde API...")

                val medicines = mutableListOf<MedicineEntity>()
                var offset = 0
                val limit = 500
                var hasMore = true

                while (hasMore && medicines.size < 2000) {
                    val response = RetrofitClient.instance.getMedicines(
                        limit = limit,
                        offset = offset
                    )

                    if (response.isEmpty()) {
                        hasMore = false
                        Log.d("AuthViewModel", "🏁 No hay más medicamentos en la API")
                    } else {
                        response.forEach { apiMedicine ->
                            if (!apiMedicine.producto.isNullOrBlank()) {
                                medicines.add(
                                    MedicineEntity(
                                        expediente = apiMedicine.expediente ?: "N/A",
                                        producto = apiMedicine.producto!!.take(150),
                                        titular = apiMedicine.titular ?: "No especificado",
                                        registroSanitario = apiMedicine.registrosanitario ?: "N/A",
                                        fechaExpedicion = apiMedicine.fechaexpedicion?.take(4) ?: "",
                                        fechaVencimiento = apiMedicine.fechavencimiento ?: "",
                                        estadoRegistro = apiMedicine.estadoregistro ?: "Vigente",
                                        descripcion = buildString {
                                            if (!apiMedicine.principioactivo.isNullOrBlank()) {
                                                append("Principio activo: ${apiMedicine.principioactivo}\n")
                                            }
                                            if (!apiMedicine.viaadministracion.isNullOrBlank()) {
                                                append("Vía: ${apiMedicine.viaadministracion}\n")
                                            }
                                            if (!apiMedicine.formafarmaceutica.isNullOrBlank()) {
                                                append("Forma: ${apiMedicine.formafarmaceutica}")
                                            }
                                        }.take(200),
                                        estadoComercial = "Activo",
                                        unidad = apiMedicine.unidad ?: "U",
                                        disponible = apiMedicine.estadoregistro?.equals("Vigente", ignoreCase = true) ?: true,
                                        viaAdministracion = apiMedicine.viaadministracion ?: "",
                                        formaFarmaceutica = apiMedicine.formafarmaceutica ?: "",
                                        principioActivo = apiMedicine.principioactivo?.take(100) ?: "",
                                        atc = apiMedicine.atc ?: "",
                                        concentracion = apiMedicine.concentracion ?: ""
                                    )
                                )
                            }
                        }

                        offset += limit
                        Log.d("AuthViewModel", "📦 Cargados ${medicines.size} medicamentos hasta ahora...")
                    }
                }

                Log.d("AuthViewModel", "✅ Total medicamentos cargados desde API: ${medicines.size}")
                medicines
            } catch (e: Exception) {
                Log.e("AuthViewModel", "❌ Error cargando desde API: ${e.message}", e)
                emptyList()
            }
        }
    }

    suspend fun preloadMedicinesIfNeeded(context: Context) {
        try {
            val current = medicineDao.getAllMedicines()
            Log.d("AuthViewModel", "📊 Medicamentos en DB: ${current.size}")

            if (current.isEmpty()) {
                Log.d("AuthViewModel", "🔄 No hay medicamentos en DB, cargando desde API...")

                val apiMedicines = loadMedicinesFromApi()

                if (apiMedicines.isNotEmpty()) {
                    medicineDao.insertAll(apiMedicines)
                    Log.d("AuthViewModel", "✅ ${apiMedicines.size} medicamentos guardados desde API")
                } else {
                    Log.w("AuthViewModel", "⚠️ API falló, intentando con CSV...")
                    val csvMedicines = MedicineCsvLoader.loadMedicinesFromCsv(context)
                    if (csvMedicines.isNotEmpty()) {
                        medicineDao.insertAll(csvMedicines)
                        Log.d("AuthViewModel", "✅ ${csvMedicines.size} medicamentos guardados desde CSV")
                    } else {
                        Log.e("AuthViewModel", "❌ No se pudo cargar medicamentos desde API ni CSV")
                    }
                }
            } else {
                Log.d("AuthViewModel", "✅ Ya hay ${current.size} medicamentos en DB")
            }
        } catch (e: Exception) {
            Log.e("AuthViewModel", "❌ Error en preloadMedicinesIfNeeded: ${e.message}", e)
        }
    }

    suspend fun getAllMedicines(): List<MedicineEntity> {
        return medicineDao.getAllMedicines()
    }

    suspend fun searchMedicines(query: String): List<MedicineEntity> {
        return if (query.isBlank()) {
            medicineDao.getAllMedicines()
        } else {
            medicineDao.searchMedicines(query)
        }
    }

    suspend fun getMedicineById(medicineId: Int): MedicineEntity? {
        return medicineDao.getMedicineById(medicineId)
    }

    fun logout() {
        currentUser = null
        isPharmacistLoggedIn = false
        sessionManager.clearSession()
    }
}