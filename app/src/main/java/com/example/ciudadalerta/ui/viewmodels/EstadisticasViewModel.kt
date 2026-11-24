package com.example.ciudadalerta.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ciudadalerta.data.TicketRepository
import com.example.ciudadalerta.domain.model.TicketStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CategoryMetric(
    val categoriaId: Int,
    val count: Int,
    val percentage: Float,
    val solvedCount: Int
)

data class EncargadoMetric(
    val email: String,
    val solvedCount: Int
)

data class EstadisticasUiState(
    val totalTickets: Int = 0,
    val registradosCount: Int = 0,
    val enEvaluacionCount: Int = 0,
    val enReparacionCount: Int = 0,
    val solucionadosCount: Int = 0,
    val urgentesCount: Int = 0,
    val syncedCount: Int = 0,
    val unsyncedCount: Int = 0,
    val conEvidenciaCount: Int = 0,
    val tasaResolucion: Int = 0,
    val categoryMetrics: List<CategoryMetric> = emptyList(),
    val encargadoMetrics: List<EncargadoMetric> = emptyList(),
    val isLoading: Boolean = true
)

class EstadisticasViewModel(
    private val repository: TicketRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EstadisticasUiState())
    val uiState: StateFlow<EstadisticasUiState> = _uiState.asStateFlow()

    init {
        observeStatistics()
    }

    private fun observeStatistics() {
        viewModelScope.launch {
            repository.allTickets.collect { tickets ->
                val total = tickets.size
                val registrados = tickets.count { it.estatusId == TicketStatus.REGISTRADO.id }
                val evaluacion = tickets.count { it.estatusId == TicketStatus.EN_EVALUACION.id }
                val reparacion = tickets.count { it.estatusId == TicketStatus.EN_REPARACION.id }
                val solucionados = tickets.count { it.estatusId == TicketStatus.SOLUCIONADO.id }
                val urgentes = tickets.count { it.esUrgente }
                val synced = tickets.count { it.isSynced }
                val unsynced = total - synced
                val conEvidencia = tickets.count { it.bestPhotoSource != null }
                val tasa = if (total > 0) ((solucionados * 100f) / total).toInt() else 0

                val categories = (1..5).map { catId ->
                    val catTickets = tickets.filter { it.categoriaId == catId }
                    val cCount = catTickets.size
                    val cPct = if (total > 0) cCount.toFloat() / total.toFloat() else 0f
                    CategoryMetric(
                        categoriaId = catId,
                        count = cCount,
                        percentage = cPct,
                        solvedCount = catTickets.count { it.estatusId == TicketStatus.SOLUCIONADO.id }
                    )
                }.sortedByDescending { it.count }

                val encargados = tickets
                    .filter { it.estatusId == TicketStatus.SOLUCIONADO.id && !it.solucionadoPor.isNullOrBlank() }
                    .groupBy { it.solucionadoPor!! }
                    .map { (email, list) -> EncargadoMetric(email, list.size) }
                    .sortedByDescending { it.solvedCount }

                _uiState.update {
                    EstadisticasUiState(
                        totalTickets = total,
                        registradosCount = registrados,
                        enEvaluacionCount = evaluacion,
                        enReparacionCount = reparacion,
                        solucionadosCount = solucionados,
                        urgentesCount = urgentes,
                        syncedCount = synced,
                        unsyncedCount = unsynced,
                        conEvidenciaCount = conEvidencia,
                        tasaResolucion = tasa,
                        categoryMetrics = categories,
                        encargadoMetrics = encargados,
                        isLoading = false
                    )
                }
            }
        }
    }
}

class EstadisticasViewModelFactory(
    private val repository: TicketRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return EstadisticasViewModel(repository) as T
    }
}
