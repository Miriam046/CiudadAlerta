package com.example.ciudadalerta.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ciudadalerta.R
import com.example.ciudadalerta.data.SessionManager
import com.example.ciudadalerta.data.SupabaseConfig
import com.example.ciudadalerta.data.TicketEntity
import com.example.ciudadalerta.data.TicketRepository
import com.example.ciudadalerta.domain.model.TicketStatus
import com.example.ciudadalerta.domain.model.UserRole
import com.example.ciudadalerta.domain.usecase.ObserveTicketsUseCase
import com.example.ciudadalerta.domain.usecase.SyncTicketsUseCase
import com.example.ciudadalerta.ui.components.UiText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FeedUiState(
    val tickets: List<TicketEntity> = emptyList(),
    val totalIncidencias: Int = 0,
    val enProcesoCount: Int = 0,
    val solucionadasCount: Int = 0,
    val pendientesSyncCount: Int = 0,
    val urgentesCount: Int = 0,
    val tasaResolucion: Int = 0,
    val currentRole: UserRole = UserRole.CIUDADANO,
    val userName: String = "",
    val isDarkMode: Boolean = false,
    val isSyncing: Boolean = false,
    val syncMessage: UiText? = null,
    val searchQuery: String = "",
    val selectedCategoryFilter: Int? = null,
    val onlyUrgentFilter: Boolean = false,
    val selectedTicketId: Int? = null,
    val isLoading: Boolean = true
)

class FeedViewModel(
    private val repository: TicketRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val observeTicketsUseCase = ObserveTicketsUseCase(repository)
    private val syncTicketsUseCase = SyncTicketsUseCase(repository)

    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    init {
        observeTickets()
        observeSession()
        viewModelScope.launch {
            if (SupabaseConfig.isConfigured) {
                repository.refreshFromCloud()
            }
        }
    }

    private fun observeTickets() {
        viewModelScope.launch {
            observeTicketsUseCase()
                .catch { _uiState.update { it.copy(isLoading = false) } }
                .collect { tickets ->
                    val total = tickets.size
                    val solved = tickets.count { it.estatusId == TicketStatus.SOLUCIONADO.id }
                    val inProcess = tickets.count {
                        it.estatusId == TicketStatus.EN_EVALUACION.id ||
                            it.estatusId == TicketStatus.EN_REPARACION.id
                    }
                    val rate = if (total > 0) ((solved * 100f) / total).toInt() else 0

                    _uiState.update { current ->
                        val currentSelected = current.selectedTicketId
                        val validSelected = if (currentSelected != null && tickets.any { it.id == currentSelected }) {
                            currentSelected
                        } else {
                            tickets.firstOrNull()?.id
                        }
                        current.copy(
                            tickets = tickets,
                            totalIncidencias = total,
                            enProcesoCount = inProcess,
                            solucionadasCount = solved,
                            pendientesSyncCount = tickets.count { !it.isSynced },
                            urgentesCount = tickets.count { it.esUrgente && it.estatusId != TicketStatus.SOLUCIONADO.id },
                            tasaResolucion = rate,
                            selectedTicketId = validSelected,
                            isLoading = false
                        )
                    }
                }
        }
    }

    private fun observeSession() {
        viewModelScope.launch {
            sessionManager.currentRole.collect { role ->
                _uiState.update { it.copy(currentRole = role) }
            }
        }
        viewModelScope.launch {
            sessionManager.userName.collect { name ->
                _uiState.update { it.copy(userName = name) }
            }
        }
        viewModelScope.launch {
            sessionManager.isDarkMode.collect { dark ->
                _uiState.update { it.copy(isDarkMode = dark) }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onCategoryFilterSelected(categoryId: Int?) {
        _uiState.update { it.copy(selectedCategoryFilter = categoryId) }
    }

    fun onUrgentFilterSelected(onlyUrgent: Boolean) {
        _uiState.update { it.copy(onlyUrgentFilter = onlyUrgent) }
    }

    fun onSelectTicket(ticketId: Int) {
        _uiState.update { it.copy(selectedTicketId = ticketId) }
    }

    fun toggleDarkMode() {
        sessionManager.toggleDarkMode()
    }

    fun logout() {
        sessionManager.logout()
    }

    fun clearSyncMessage() {
        _uiState.update { it.copy(syncMessage = null) }
    }

    fun syncPending() {
        if (_uiState.value.isSyncing) return
        viewModelScope.launch {
            if (!SupabaseConfig.isConfigured) {
                _uiState.update {
                    it.copy(syncMessage = UiText.StringResource(R.string.msg_supabase_not_configured))
                }
                return@launch
            }
            _uiState.update { it.copy(isSyncing = true, syncMessage = null) }
            val syncedCount = syncTicketsUseCase()
            val msg = if (syncedCount > 0) {
                UiText.PluralResource(R.plurals.msg_sync_changes_count, syncedCount, syncedCount)
            } else {
                UiText.StringResource(R.string.msg_sync_up_to_date)
            }
            _uiState.update { it.copy(isSyncing = false, syncMessage = msg) }
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
