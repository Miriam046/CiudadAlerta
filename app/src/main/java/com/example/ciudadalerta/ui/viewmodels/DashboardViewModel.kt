package com.example.ciudadalerta.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ciudadalerta.data.TicketEntity
import com.example.ciudadalerta.data.TicketRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DashboardUiState(
    val tickets: List<TicketEntity> = emptyList(),
    val totalAportaciones: Int = 0,
    val ticketsCriticos: Int = 0,
    val ticketsEnEvaluacion: Int = 0,
    val ticketsSolucionados: Int = 0,
    val unsyncedCount: Int = 0,
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val selectedCategoryFilter: Int? = null,
    val isSyncing: Boolean = false
)

class DashboardViewModel(private val repository: TicketRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadTickets()
    }

    private fun loadTickets() {
        viewModelScope.launch {
            repository.allTickets
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false) }
                }
                .collect { tickets ->
                    _uiState.update { current ->
                        current.copy(
                            tickets = tickets,
                            totalAportaciones = tickets.size,
                            ticketsCriticos = tickets.count { it.estatusId == 1 },
                            ticketsEnEvaluacion = tickets.count { it.estatusId == 2 },
                            ticketsSolucionados = tickets.count { it.estatusId == 3 },
                            unsyncedCount = tickets.count { !it.isSynced },
                            isLoading = false
                        )
                    }
                }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onCategoryFilterSelected(categoryId: Int?) {
        _uiState.update { it.copy(selectedCategoryFilter = categoryId) }
    }

    fun syncPending() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true) }
            repository.syncPendingTickets()
            _uiState.update { it.copy(isSyncing = false) }
        }
    }
}

class DashboardViewModelFactory(private val repository: TicketRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return DashboardViewModel(repository) as T
    }
}