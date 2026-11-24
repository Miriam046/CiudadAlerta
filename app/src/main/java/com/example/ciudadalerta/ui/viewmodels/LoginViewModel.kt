package com.example.ciudadalerta.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ciudadalerta.data.SessionManager
import com.example.ciudadalerta.data.TicketRepository
import com.example.ciudadalerta.domain.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val isRegisterMode: Boolean = false,
    val fullName: String = "",
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false
)

class LoginViewModel(
    private val sessionManager: SessionManager,
    private val repository: TicketRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onModeSelected(registerMode: Boolean) {
        _uiState.update { it.copy(isRegisterMode = registerMode) }
    }

    fun onFullNameChanged(text: String) {
        _uiState.update { it.copy(fullName = text) }
    }

    fun onEmailChanged(text: String) {
        _uiState.update { it.copy(email = text) }
    }

    fun onPasswordChanged(text: String) {
        _uiState.update { it.copy(password = text) }
    }

    /**
     * Resuelve el rol automáticamente desde Supabase (`cuenta_usuario.id_rol`) al iniciar sesión.
     * En modo Registrarse, crea siempre una cuenta de Ciudadano (`id_rol = 1`) sin mostrar roles en la UI.
     */
    fun enterApp(onSuccess: () -> Unit) {
        val current = _uiState.value
        val email = current.email.trim()
        val name = current.fullName.trim()

        if (current.isLoading) return
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                if (current.isRegisterMode) {
                    val citizenEmail = email.ifBlank { UserRole.CIUDADANO.defaultEmail }
                    val citizenName = name.ifBlank { UserRole.CIUDADANO.defaultName }
                    repository?.registerUserInCloud(
                        name = citizenName,
                        email = citizenEmail,
                        password = current.password,
                        role = UserRole.CIUDADANO
                    )
                    sessionManager.signInWithRole(
                        role = UserRole.CIUDADANO,
                        name = citizenName,
                        email = citizenEmail
                    )
                } else {
                    val cloudUser = if (email.isNotBlank() && repository != null) {
                        repository.resolveUserFromCloud(email)
                    } else {
                        null
                    }
                    val resolvedRole = when (cloudUser?.idRol) {
                        1 -> UserRole.CIUDADANO
                        2 -> UserRole.TECNICO_REPARACION
                        3 -> UserRole.GESTOR_MUNICIPAL
                        else -> UserRole.CIUDADANO
                    }
                    val resolvedName = cloudUser?.nombreCompleto?.takeIf { it.isNotBlank() }
                        ?: name.ifBlank { resolvedRole.defaultName }
                    val resolvedEmail = cloudUser?.correoContacto?.takeIf { it.isNotBlank() }
                        ?: email.ifBlank { resolvedRole.defaultEmail }

                    sessionManager.signInWithRole(
                        role = resolvedRole,
                        name = resolvedName,
                        email = resolvedEmail
                    )
                }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
                onSuccess()
            }
        }
    }
}

class LoginViewModelFactory(
    private val sessionManager: SessionManager,
    private val repository: TicketRepository? = null
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LoginViewModel(sessionManager, repository) as T
    }
}
