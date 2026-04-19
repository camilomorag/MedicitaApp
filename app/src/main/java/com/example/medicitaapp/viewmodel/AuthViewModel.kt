package com.example.medicitaapp.viewmodel

import android.app.Application
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import com.example.medicitaapp.data.AppDatabase
import com.example.medicitaapp.data.UserEntity

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val userDao = AppDatabase.getDatabase(application).userDao()

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

    fun logout() {
        currentUser = null
    }
}