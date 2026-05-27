package com.example.medicitaapp.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.medicitaapp.data.*
import com.example.medicitaapp.services.GeminiService
import com.example.medicitaapp.services.NotificationService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val userDao = db.userDao()
    private val formulaRequestDao = db.formulaRequestDao()
    private val notificationDao = db.notificationDao()
    private val sessionManager = SessionManager(application)
    private val medicineDao = db.medicineDao()

    lateinit var notificationService: NotificationService
    private lateinit var geminiService: GeminiService

    var currentUser by mutableStateOf<UserEntity?>(null)
        private set

    var isPharmacistLoggedIn by mutableStateOf(false)
        private set

    var isRestoringSession by mutableStateOf(true)
        private set

    init {
        // Restaurar sesión de manera asíncrona
        viewModelScope.launch {
            restoreSessionInternal()
        }
    }

    fun initServices(context: Context) {
        notificationService = NotificationService(context)
        geminiService = GeminiService(context)
    }

    // ✅ MÉTODO TEMPORAL PARA LIMPIAR SESIÓN
    fun forceClearSession() {
        sessionManager.clearSession()
        currentUser = null
        isPharmacistLoggedIn = false
        Log.d("AuthViewModel", "✅ Sesión forzada limpiada")
    }

    // ✅ NUEVO MÉTODO restoreSession() que devuelve Boolean
    suspend fun restoreSession(): Boolean {
        isPharmacistLoggedIn = sessionManager.isPharmacistLoggedIn()
        if (isPharmacistLoggedIn) {
            Log.d("AuthViewModel", "✅ Sesión de farmaceuta restaurada")
            return true
        }
        val documento = sessionManager.getUserDocumento()
        if (documento != null) {
            currentUser = userDao.getUserByDocumento(documento)
            val restored = currentUser != null
            if (restored) {
                Log.d("AuthViewModel", "✅ Sesión de usuario restaurada: ${currentUser?.nombre}")
            } else {
                Log.d("AuthViewModel", "⚠️ Usuario no encontrado, sesión limpiada")
                sessionManager.clearSession()
            }
            return restored
        }
        Log.d("AuthViewModel", "No hay sesión guardada")
        return false
    }

    // ✅ MÉTODO INTERNO para restaurar sesión (original)
    private suspend fun restoreSessionInternal() {
        try {
            isRestoringSession = true
            restoreSession()
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Error restaurando sesión: ${e.message}", e)
        } finally {
            isRestoringSession = false
        }
    }

    // ============================================
    // REGISTRO Y LOGIN
    // ============================================

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
        return try {
            val user = userDao.login(documento, password)
            if (user != null) {
                currentUser = user
                isPharmacistLoggedIn = false
                sessionManager.saveUserSession(user.documento)
                Log.d("AuthViewModel", "✅ Login exitoso: ${user.nombre}")
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Error en login: ${e.message}", e)
            false
        }
    }

    fun loginAsPharmacist() {
        isPharmacistLoggedIn = true
        currentUser = null
        sessionManager.savePharmacistSession()
        Log.d("AuthViewModel", "✅ Login como farmaceuta")
    }

    // ============================================
    // RECUPERAR CONTRASEÑA
    // ============================================

    suspend fun getUserByDocumento(documento: String): UserEntity? {
        return userDao.getUserByDocumento(documento)
    }

    suspend fun updatePassword(documento: String, nuevaPassword: String) {
        userDao.updatePassword(documento, nuevaPassword)
    }

    // ============================================
    // FORMULAS CON VALIDACIÓN IA
    // ============================================

    suspend fun submitFormulaRequest(
        formulaUri: String,
        formulaType: String
    ): Result<String> {
        return try {
            val user = currentUser ?: return Result.failure(Exception("No hay usuario logueado"))

            val requestId = formulaRequestDao.insertRequest(
                FormulaRequestEntity(
                    userDocumento = user.documento,
                    userNombre = user.nombre,
                    userTelefono = user.telefono,
                    formulaUri = formulaUri,
                    formulaType = formulaType,
                    medicamento = "Pendiente de validacion",
                    estado = "pendiente"
                )
            ).toInt()

            val validationResult = geminiService.validateFormula(
                patientNameExpected = user.nombre,
                documentIdExpected = user.documento,
                phoneExpected = user.telefono,
                imageUri = if (formulaType == "image") Uri.parse(formulaUri) else null,
                pdfUri = if (formulaType == "pdf") Uri.parse(formulaUri) else null  // ✅ Cambiado
            )

            val mensajeValidacion = if (validationResult.isValid) {
                "✅ Validacion exitosa: ${validationResult.message}"
            } else {
                "❌ Validacion fallida: ${validationResult.message}"
            }
            val observacionesValidacion = validationResult.observations.joinToString("\n")

            formulaRequestDao.updateValidationResult(
                requestId = requestId,
                validacionIA = validationResult.isValid,
                mensajeValidacion = mensajeValidacion,
                observacionesValidacion = observacionesValidacion
            )

            if (!validationResult.isValid) {
                notificationService.showNewFormulaForPharmacistNotification(user.nombre)
            }

            notificationService.showFormulaSubmittedNotification("Pendiente de validacion")
            notificationDao.insertNotification(
                NotificationEntity(
                    userDocumento = user.documento,
                    title = if (validationResult.isValid) "📄 Formula enviada" else "⚠️ Formula con problemas",
                    message = mensajeValidacion
                )
            )

            val mensajeFinal = if (validationResult.isValid) {
                "✅ Formula valida. Enviada correctamente."
            } else {
                "⚠️ La IA detecto problemas. Un farmaceuta revisara tu formula."
            }

            Result.success(mensajeFinal)
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Error: ${e.message}", e)
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
            ubicacion = ubicacion,
            tiempoEspera = 0,
            posicionCola = 0
        )

        notificationService.showFormulaStatusNotification(
            estado = estado,
            medicamento = request.medicamento,
            turno = turno,
            ubicacion = ubicacion
        )

        val title = when (estado) {
            "aceptada" -> "✅ Formula aceptada"
            "rechazada" -> "❌ Formula rechazada"
            "aplazada" -> "⏰ Formula aplazada"
            "lista" -> "🎉 Medicamento listo"
            else -> "Actualizacion de solicitud"
        }

        val message = when (estado) {
            "aceptada" -> "Su formula fue aceptada.${if (turno.isNotBlank()) " Turno: $turno" else ""}"
            "rechazada" -> "Su formula fue rechazada. ${if (comentario.isNotBlank()) "Motivo: $comentario" else "Comuniquese con el farmaceuta."}"
            "aplazada" -> "Su formula fue aplazada. ${if (comentario.isNotBlank()) "Motivo: $comentario" else "Revise observaciones."}"
            "lista" -> "Su medicamento esta listo para reclamar. Ubicacion: $ubicacion"
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

    suspend fun updateRequestWithTurno(
        requestId: Int,
        estado: String,
        turno: String,
        ubicacion: String,
        tiempoEspera: Int,
        posicionCola: Int
    ) {
        val request = formulaRequestDao.getRequestById(requestId) ?: return

        formulaRequestDao.updateRequestStatus(
            requestId = requestId,
            estado = estado,
            comentario = "",
            turno = turno,
            ubicacion = ubicacion,
            tiempoEspera = tiempoEspera,
            posicionCola = posicionCola
        )

        notificationService.showFormulaStatusNotification(
            estado = estado,
            medicamento = request.medicamento,
            turno = turno,
            ubicacion = ubicacion
        )

        notificationDao.insertNotification(
            NotificationEntity(
                userDocumento = request.userDocumento,
                title = "🎫 Turno asignado",
                message = "Su turno es $turno en $ubicacion. Tiempo estimado: $tiempoEspera minutos. Posicion: $posicionCola°"
            )
        )
    }

    // ============================================
    // FUNCIONES PARA CARGAR MEDICAMENTOS
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
                                                append("Via: ${apiMedicine.viaadministracion}\n")
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
                    Log.w("AuthViewModel", "⚠️ API falló, insertando medicamentos de prueba...")
                    insertMockMedicines()
                }
            } else {
                Log.d("AuthViewModel", "✅ Ya hay ${current.size} medicamentos en DB")
            }
        } catch (e: Exception) {
            Log.e("AuthViewModel", "❌ Error en preloadMedicinesIfNeeded: ${e.message}", e)
        }
    }

    // ✅ DATOS DE PRUEBA PARA QUE LA APP NO FALLE
    private suspend fun insertMockMedicines() {
        val mockMedicines = listOf(
            MedicineEntity(
                expediente = "EXP-001",
                producto = "Paracetamol 500mg",
                titular = "Genfar",
                registroSanitario = "INVIMA 2024-001",
                fechaExpedicion = "2024",
                fechaVencimiento = "2026",
                estadoRegistro = "Vigente",
                descripcion = "Analgésico y antipirético",
                estadoComercial = "Activo",
                unidad = "Tableta",
                disponible = true,
                viaAdministracion = "Oral",
                formaFarmaceutica = "Tableta",
                principioActivo = "Paracetamol"
            ),
            MedicineEntity(
                expediente = "EXP-002",
                producto = "Ibuprofeno 400mg",
                titular = "MK",
                registroSanitario = "INVIMA 2024-002",
                fechaExpedicion = "2024",
                fechaVencimiento = "2026",
                estadoRegistro = "Vigente",
                descripcion = "Antiinflamatorio no esteroideo",
                estadoComercial = "Activo",
                unidad = "Tableta",
                disponible = true,
                viaAdministracion = "Oral",
                formaFarmaceutica = "Tableta",
                principioActivo = "Ibuprofeno"
            ),
            MedicineEntity(
                expediente = "EXP-003",
                producto = "Amoxicilina 500mg",
                titular = "Lab. Chile",
                registroSanitario = "INVIMA 2024-003",
                fechaExpedicion = "2024",
                fechaVencimiento = "2026",
                estadoRegistro = "Vigente",
                descripcion = "Antibiótico de amplio espectro",
                estadoComercial = "Activo",
                unidad = "Cápsula",
                disponible = true,
                viaAdministracion = "Oral",
                formaFarmaceutica = "Cápsula",
                principioActivo = "Amoxicilina"
            )
        )
        medicineDao.insertAll(mockMedicines)
        Log.d("AuthViewModel", "✅ ${mockMedicines.size} medicamentos de prueba insertados")
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

    // ✅ CORREGIDO: Cierre de sesión mejorado
    fun logout() {
        try {
            // Limpiar estado
            currentUser = null
            isPharmacistLoggedIn = false

            // Limpiar SharedPreferences
            sessionManager.clearSession()

            Log.d("AuthViewModel", "✅ Sesión cerrada correctamente")
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Error al cerrar sesión: ${e.message}", e)
        }
    }
}