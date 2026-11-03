package com.example.ciudadalerta.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ciudadalerta.data.TicketEntity
import com.example.ciudadalerta.data.TicketRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DetalleUiState(
    val ticket: TicketEntity? = null,
    val isLoading: Boolean = true,
    val isUpdatingStatus: Boolean = false,
    val errorMessage: String? = null
)

class DetalleViewModel(
    private val repository: TicketRepository,
    private val ticketId: Int
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetalleUiState())
    val uiState: StateFlow<DetalleUiState> = _uiState.asStateFlow()

    init {
        loadTicket()
    }

    private fun loadTicket() {
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

    fun updateTicketStatus(newStatusId: Int) {
        val currentTicket = _uiState.value.ticket ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdatingStatus = true) }
            try {
                repository.updateTicketStatus(currentTicket, newStatusId)
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
}

class DetalleViewModelFactory(
    private val repository: TicketRepository,
    private val ticketId: Int
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return DetalleViewModel(repository, ticketId) as T
    }
}