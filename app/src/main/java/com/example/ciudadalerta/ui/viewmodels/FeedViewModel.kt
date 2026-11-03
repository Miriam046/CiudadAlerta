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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FeedUiState(
    val tickets: List<TicketEntity> = emptyList(),
    val totalIncidencias: Int = 0,
    val solucionadasCount: Int = 0,
    val isEncargadoLoggedIn: Boolean = false,
    val isSyncing: Boolean = false,
    val searchQuery: String = "",
    val selectedCategoryFilter: Int? = null,
    val isLoading: Boolean = true
)

class FeedViewModel(
    private val repository: TicketRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    init {
        observeTickets()
        observeSession()
    }

    private fun observeTickets() {
        viewModelScope.launch {
            repository.allTickets
                .catch { _uiState.update { it.copy(isLoading = false) } }
                .collect { tickets ->
                    _uiState.update { current ->
                        current.copy(
                            tickets = tickets,
                            totalIncidencias = tickets.size,
                            solucionadasCount = tickets.count { it.estatusId == 3 },
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

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onCategoryFilterSelected(categoryId: Int?) {
        _uiState.update { it.copy(selectedCategoryFilter = categoryId) }
    }

    fun logout() {
        sessionManager.logout()
    }

    fun syncPending() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true) }
            repository.syncPendingTickets()
            _uiState.update { it.copy(isSyncing = false) }
        }
    }
}

class FeedViewModelFactory(
    private val repository: TicketRepository,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return FeedViewModel(repository, sessionManager) as T
    }
}