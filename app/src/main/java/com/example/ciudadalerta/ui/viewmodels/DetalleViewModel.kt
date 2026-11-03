package com.example.ciudadalerta.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ciudadalerta.data.SessionManager
import com.example.ciudadalerta.data.TicketEntity
import com.example.ciudadalerta.data.TicketRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DetalleUiState(
    val ticket: TicketEntity? = null,
    val isEncargadoLoggedIn: Boolean = false,
    val isLoading: Boolean = true,
    val isUpdatingStatus: Boolean = false,
    val errorMessage: String? = null
)

class DetalleViewModel(
    private val repository: TicketRepository,
    private val sessionManager: SessionManager,
    private val ticketId: Int
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetalleUiState())
    val uiState: StateFlow<DetalleUiState> = _uiState.asStateFlow()

    init {
        observeTicket()
        observeSession()
    }

    private fun observeTicket() {
        viewModelScope.launch {
            repository.getTicketById(ticketId).collect { ticket ->
                _uiState.update { current ->
                    current.copy(
                        ticket = ticket,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun observeSession() {
        viewModelScope.launch {
            sessionManager.isEncargadoLoggedIn.collect { isLoggedIn ->
                _uiState.update { it.copy(isEncargadoLoggedIn = isLoggedIn) }
            }
        }
    }

    fun markAsSolucionado() {
        val currentTicket = _uiState.value.ticket ?: return
        val encargadoEmail = sessionManager.encargadoEmail.value
        
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdatingStatus = true) }
            try {
                repository.updateTicketStatus(currentTicket, 3, solucionadoPor = encargadoEmail) // 3: Solucionado
                _uiState.update { it.copy(isUpdatingStatus = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isUpdatingStatus = false,
                        errorMessage = "Error al actualizar estado: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun deleteReport(onSuccess: () -> Unit) {
        val currentTicket = _uiState.value.ticket ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdatingStatus = true) }
            try {
                repository.deleteTicket(currentTicket.id)
                _uiState.update { it.copy(isUpdatingStatus = false) }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isUpdatingStatus = false,
                        errorMessage = "Error al eliminar reporte: ${e.localizedMessage}"
                    )
                }
            }
        }
    }
}

class DetalleViewModelFactory(
    private val repository: TicketRepository,
    private val sessionManager: SessionManager,
    private val ticketId: Int
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return DetalleViewModel(repository, sessionManager, ticketId) as T
    }
}