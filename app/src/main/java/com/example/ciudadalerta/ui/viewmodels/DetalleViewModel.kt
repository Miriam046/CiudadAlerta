package com.example.ciudadalerta.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ciudadalerta.R
import com.example.ciudadalerta.data.SessionManager
import com.example.ciudadalerta.data.TicketEntity
import com.example.ciudadalerta.data.TicketRepository
import com.example.ciudadalerta.domain.model.MaintenanceCrew
import com.example.ciudadalerta.domain.model.MaintenanceCrews
import com.example.ciudadalerta.domain.model.TicketStatus
import com.example.ciudadalerta.domain.model.UserRole
import com.example.ciudadalerta.domain.usecase.AdvanceTicketStatusUseCase
import com.example.ciudadalerta.ui.components.UiText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DetalleUiState(
    val ticket: TicketEntity? = null,
    val selectedCrew: String = MaintenanceCrews.availableCrews.first(),
    val currentRole: UserRole = UserRole.CIUDADANO,
    val encargadoEmail: String? = null,
    val isLoading: Boolean = true,
    val isUpdatingStatus: Boolean = false,
    val actionFeedback: UiText? = null,
    val errorMessage: UiText? = null
)

class DetalleViewModel(
    private val repository: TicketRepository,
    private val sessionManager: SessionManager,
    private val ticketId: Int
) : ViewModel() {

    private val advanceStatusUseCase = AdvanceTicketStatusUseCase(repository)

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
                    val crew = ticket?.cuadrillaAsignada
                        ?: ticket?.let { MaintenanceCrews.defaultForCategory(it.categoriaId) }
                        ?: current.selectedCrew
                    current.copy(
                        ticket = ticket,
                        selectedCrew = crew,
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
            sessionManager.encargadoEmail.collect { email ->
                _uiState.update { it.copy(encargadoEmail = email) }
            }
        }
    }

    fun onCrewSelected(crew: String) {
        _uiState.update { it.copy(selectedCrew = crew) }
    }

    fun clearFeedback() {
        _uiState.update { it.copy(actionFeedback = null, errorMessage = null) }
    }

    fun markAsEnEvaluacion() {
        updatePhase(
            nextStatus = TicketStatus.EN_EVALUACION,
            feedbackBuilder = { id, crew ->
                UiText.StringResource(R.string.msg_ticket_in_evaluation, id, localizedCrew(crew))
            }
        )
    }

    fun markAsEnReparacion() {
        updatePhase(
            nextStatus = TicketStatus.EN_REPARACION,
            feedbackBuilder = { id, crew ->
                UiText.StringResource(R.string.msg_ticket_in_repair, id, localizedCrew(crew))
            }
        )
    }

    fun markAsSolucionado() {
        val encargadoEmail = sessionManager.encargadoEmail.value ?: _uiState.value.currentRole.defaultEmail
        updatePhase(
            nextStatus = TicketStatus.SOLUCIONADO,
            solucionadoPor = encargadoEmail,
            feedbackBuilder = { id, _ ->
                UiText.StringResource(R.string.msg_ticket_solved, id)
            }
        )
    }

    private fun localizedCrew(crew: String): UiText =
        MaintenanceCrew.fromDbValue(crew)?.let { UiText.StringResource(it.labelRes) }
            ?: UiText.DynamicString(crew)

    private fun updatePhase(
        nextStatus: TicketStatus,
        solucionadoPor: String? = null,
        feedbackBuilder: (Int, String) -> UiText
    ) {
        val currentTicket = _uiState.value.ticket ?: return
        val crew = _uiState.value.selectedCrew
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdatingStatus = true, actionFeedback = null, errorMessage = null) }
            try {
                advanceStatusUseCase(
                    ticket = currentTicket,
                    nextStatus = nextStatus,
                    cuadrillaAsignada = crew,
                    solucionadoPor = solucionadoPor
                )
                _uiState.update {
                    it.copy(
                        isUpdatingStatus = false,
                        actionFeedback = feedbackBuilder(currentTicket.id, crew)
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isUpdatingStatus = false,
                        errorMessage = UiText.StringResource(
                            R.string.err_detail_update_failed,
                            e.localizedMessage ?: ""
                        )
                    )
                }
            }
        }
    }

    fun syncWithCloud() {
        val currentTicket = _uiState.value.ticket ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdatingStatus = true, actionFeedback = null) }
            val ok = repository.syncSingleTicket(currentTicket)
            _uiState.update {
                it.copy(
                    isUpdatingStatus = false,
                    actionFeedback = if (ok) {
                        UiText.StringResource(R.string.msg_ticket_synced, currentTicket.id)
                    } else {
                        UiText.StringResource(R.string.msg_ticket_saved_offline)
                    }
                )
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
                        errorMessage = UiText.StringResource(
                            R.string.err_detail_delete_failed,
                            e.localizedMessage ?: ""
                        )
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
