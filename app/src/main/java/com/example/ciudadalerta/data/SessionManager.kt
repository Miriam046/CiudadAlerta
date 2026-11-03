package com.example.ciudadalerta.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SessionManager private constructor(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences("session_prefs", Context.MODE_PRIVATE)

    private val _isEncargadoLoggedIn = MutableStateFlow(prefs.getBoolean(KEY_IS_ENCARGADO, false))
    val isEncargadoLoggedIn: StateFlow<Boolean> = _isEncargadoLoggedIn.asStateFlow()

    fun loginEncargado(email: String, password: String): Boolean {
        // Validación de credenciales para el rol Encargado
        if (email.isNotBlank() && password.isNotBlank()) {
            prefs.edit().putBoolean(KEY_IS_ENCARGADO, true).apply()
            _isEncargadoLoggedIn.value = true
            return true
        }
        return false
    }

    fun logout() {
        prefs.edit().putBoolean(KEY_IS_ENCARGADO, false).apply()
        _isEncargadoLoggedIn.value = false
    }

    companion object {
        private const val KEY_IS_ENCARGADO = "is_encargado_logged_in"

        @Volatile
        private var INSTANCE: SessionManager? = null

        fun getInstance(context: Context): SessionManager {
            return INSTANCE ?: synchronized(this) {
                val instance = SessionManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}