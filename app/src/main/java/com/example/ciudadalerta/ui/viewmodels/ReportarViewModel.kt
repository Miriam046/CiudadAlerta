package com.example.ciudadalerta.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ciudadalerta.data.TicketRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReportarUiState(
    val descripcion: String = "",
    val categoriaId: Int = 1, // 1: Bache, 2: Alumbrado, 3: Agua, 4: Seguridad, 5: Basura
    val latitud: Double = 19.432608,
    val longitud: Double = -99.133209,
    val fotoRuta: String? = null,
    val isUrgent: Boolean = false,
    val isSaving: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

class ReportarViewModel(private val repository: TicketRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportarUiState())
    val uiState: StateFlow<ReportarUiState> = _uiState.asStateFlow()

    fun onDescripcionChanged(newText: String) {
        _uiState.update { it.copy(descripcion = newText, errorMessage = null) }
    }

    fun onCategoriaSelected(id: Int) {
        _uiState.update { it.copy(categoriaId = id) }
    }

    fun onLocationChanged(lat: Double, lon: Double) {
        _uiState.update { it.copy(latitud = lat, longitud = lon) }
    }

    fun onUrgentToggled(urgent: Boolean) {
        _uiState.update { it.copy(isUrgent = urgent) }
    }

    fun onFotoSelected(ruta: String?) {
        _uiState.update { it.copy(fotoRuta = ruta) }
    }

    fun submitReport(onSuccess: () -> Unit) {
        val current = _uiState.value
        if (current.descripcion.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Por favor ingresa una descripción del problema.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                // Si es urgente, estatus es 1 (Crítico/Registrado)
                val estatusId = if (current.isUrgent) 1 else 1
                repository.addTicket(
                    descripcion = current.descripcion.trim(),
                    lat = current.latitud,
                    lon = current.longitud,
                    fotoRuta = current.fotoRuta,
                    categoriaId = current.categoriaId,
                    estatusId = estatusId
                )
                _uiState.update { it.copy(isSaving = false, isSuccess = true) }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, errorMessage = "Error al guardar el reporte: ${e.localizedMessage}") }
            }
        }
    }
}

class ReportarViewModelFactory(private val repository: TicketRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ReportarViewModel(repository) as T
    }
}