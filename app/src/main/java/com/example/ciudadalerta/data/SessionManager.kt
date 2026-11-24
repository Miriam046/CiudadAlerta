package com.example.ciudadalerta.data

import android.content.Context
import com.example.ciudadalerta.domain.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SessionManager private constructor(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences("session_prefs", Context.MODE_PRIVATE)

    private val _currentRole = MutableStateFlow(UserRole.fromKey(prefs.getString(KEY_USER_ROLE, UserRole.CIUDADANO.key)))
    val currentRole: StateFlow<UserRole> = _currentRole.asStateFlow()

    private val _userName = MutableStateFlow(prefs.getString(KEY_USER_NAME, UserRole.CIUDADANO.defaultName) ?: UserRole.CIUDADANO.defaultName)
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _encargadoEmail = MutableStateFlow(prefs.getString(KEY_USER_EMAIL, UserRole.CIUDADANO.defaultEmail))
    val encargadoEmail: StateFlow<String?> = _encargadoEmail.asStateFlow()

    private val _isEncargadoLoggedIn = MutableStateFlow(_currentRole.value.canUpdateRepairStatus)
    val isEncargadoLoggedIn: StateFlow<Boolean> = _isEncargadoLoggedIn.asStateFlow()

    private val _isDarkMode = MutableStateFlow(prefs.getBoolean(KEY_DARK_MODE, false))
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    /**
     * Inicia sesión o registra usuario sin validación estricta (modo demostración).
     */
    fun signInWithRole(role: UserRole, name: String, email: String) {
        val resolvedName = name.trim().ifBlank { role.defaultName }
        val resolvedEmail = email.trim().ifBlank { role.defaultEmail }

        prefs.edit()
            .putString(KEY_USER_ROLE, role.key)
            .putString(KEY_USER_NAME, resolvedName)
            .putString(KEY_USER_EMAIL, resolvedEmail)
            .apply()

        _currentRole.value = role
        _userName.value = resolvedName
        _encargadoEmail.value = resolvedEmail
        _isEncargadoLoggedIn.value = role.canUpdateRepairStatus
    }

    fun logout() {
        prefs.edit()
            .remove(KEY_USER_ROLE)
            .remove(KEY_USER_NAME)
            .remove(KEY_USER_EMAIL)
            .apply()
        _currentRole.value = UserRole.CIUDADANO
        _userName.value = UserRole.CIUDADANO.defaultName
        _encargadoEmail.value = null
        _isEncargadoLoggedIn.value = false
    }

    fun toggleDarkMode() {
        val next = !_isDarkMode.value
        prefs.edit().putBoolean(KEY_DARK_MODE, next).apply()
        _isDarkMode.value = next
    }

    companion object {
        private const val KEY_USER_ROLE = "user_role"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "encargado_email"
        private const val KEY_DARK_MODE = "pref_dark_mode"

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
