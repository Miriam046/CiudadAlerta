package com.example.ciudadalerta.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ciudadalerta.R
import com.example.ciudadalerta.data.SessionManager
import com.example.ciudadalerta.data.TicketRepository
import com.example.ciudadalerta.domain.model.UserRole
import com.example.ciudadalerta.ui.components.UiText
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
    val isLoading: Boolean = false,
    val errorMessage: UiText? = null
)

class LoginViewModel(
    private val sessionManager: SessionManager,
    private val repository: TicketRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onModeSelected(registerMode: Boolean) {
        _uiState.update { it.copy(isRegisterMode = registerMode, errorMessage = null) }
    }

    fun onFullNameChanged(text: String) {
        _uiState.update { it.copy(fullName = text, errorMessage = null) }
    }

    fun onEmailChanged(text: String) {
        _uiState.update { it.copy(email = text, errorMessage = null) }
    }

    fun onPasswordChanged(text: String) {
        _uiState.update { it.copy(password = text, errorMessage = null) }
    }

    fun enterApp(onSuccess: () -> Unit) {
        val current = _uiState.value
        val email = current.email.trim()
        val password = current.password.trim()
        val name = current.fullName.trim()

        if (current.isLoading) return

        if (email.isBlank()) {
            _uiState.update { it.copy(errorMessage = UiText.StringResource(R.string.err_login_empty_email)) }
            return
        }
        if (password.isBlank()) {
            _uiState.update { it.copy(errorMessage = UiText.StringResource(R.string.err_login_empty_password)) }
            return
        }
        if (current.isRegisterMode && name.isBlank()) {
            _uiState.update { it.copy(errorMessage = UiText.StringResource(R.string.err_login_empty_name)) }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                if (current.isRegisterMode) {
                    repository?.registerUserInCloud(
                        name = name,
                        email = email,
                        password = password,
                        role = UserRole.CIUDADANO
                    )
                    sessionManager.signInWithRole(
                        role = UserRole.CIUDADANO,
                        name = name,
                        email = email
                    )
                    _uiState.update { it.copy(isLoading = false) }
                    onSuccess()
                } else {
                    val isAdminCred = (email.equals("admin@ciudadalerta.gob", ignoreCase = true) ||
                            email.equals("gestor@ciudadalerta.gob", ignoreCase = true) ||
                            email.equals("admin", ignoreCase = true)) && password == "admin123"

                    val isTechnicianCred = email.equals("cuadrilla@ciudadalerta.gob", ignoreCase = true) && password == "admin123"

                    val resolvedRole: UserRole
                    val resolvedName: String

                    when {
                        isAdminCred -> {
                            resolvedRole = UserRole.GESTOR_MUNICIPAL
                            resolvedName = "Administrador Municipal"
                        }
                        isTechnicianCred -> {
                            resolvedRole = UserRole.TECNICO_REPARACION
                            resolvedName = "Técnico de Reparación"
                        }
                        else -> {
                            val cloudUser = repository?.resolveUserFromCloud(email)
                            if (cloudUser == null) {
                                _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        errorMessage = UiText.StringResource(R.string.err_login_invalid_user)
                                    )
                                }
                                return@launch
                            }

                            resolvedRole = when (cloudUser.idRol) {
                                1 -> UserRole.CIUDADANO
                                2 -> UserRole.TECNICO_REPARACION
                                3 -> UserRole.GESTOR_MUNICIPAL
                                else -> {
                                    _uiState.update {
                                        it.copy(
                                            isLoading = false,
                                            errorMessage = UiText.StringResource(
                                                R.string.err_login_invalid_role,
                                                cloudUser.idRol
                                            )
                                        )
                                    }
                                    return@launch
                                }
                            }

                            resolvedName = cloudUser.nombreCompleto.takeIf { it.isNotBlank() }
                                ?: name.ifBlank { resolvedRole.defaultName }
                        }
                    }

                    sessionManager.signInWithRole(
                        role = resolvedRole,
                        name = resolvedName,
                        email = email
                    )
                    _uiState.update { it.copy(isLoading = false) }
                    onSuccess()
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = UiText.StringResource(
                            R.string.err_login_generic,
                            e.localizedMessage ?: ""
                        )
                    )
                }
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