package com.example.medicitaapp.data

import android.content.Context

class SessionManager(context: Context) {

    private val prefs = context.getSharedPreferences("medicita_session", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_USER_DOCUMENTO = "user_documento"
        private const val KEY_IS_PHARMACIST = "is_pharmacist"
    }

    fun saveUserSession(documento: String) {
        prefs.edit()
            .putString(KEY_USER_DOCUMENTO, documento)
            .putBoolean(KEY_IS_PHARMACIST, false)
            .apply()
    }

    fun savePharmacistSession() {
        prefs.edit()
            .putString(KEY_USER_DOCUMENTO, null)
            .putBoolean(KEY_IS_PHARMACIST, true)
            .apply()
    }

    fun getUserDocumento(): String? {
        return prefs.getString(KEY_USER_DOCUMENTO, null)
    }

    fun isPharmacistLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_PHARMACIST, false)
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}