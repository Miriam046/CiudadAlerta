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
        var shouldReturn = false
        var currentDesc = ""
        var currentLat = 0.0
        var currentLon = 0.0
        var currentFoto: String? = null
        var currentCat = 1
        var isUrg = false
        
        // Actualización atómica del estado para asegurar que solo un clic pase la validación
        _uiState.update { current ->
            if (current.isSaving || current.isSuccess) {
                shouldReturn = true
                return@update current
            }
            if (current.descripcion.isBlank()) {
                shouldReturn = true
                return@update current.copy(errorMessage = "Por favor ingresa una descripción del problema.")
            }
            
            currentDesc = current.descripcion
            currentLat = current.latitud
            currentLon = current.longitud
            currentFoto = current.fotoRuta
            currentCat = current.categoriaId
            isUrg = current.isUrgent
            
            current.copy(isSaving = true)
        }

        if (shouldReturn) return

        viewModelScope.launch {
            try {
                // Si es urgente, estatus es 1 (Crítico/Registrado)
                val estatusId = if (isUrg) 1 else 1
                repository.addTicket(
                    descripcion = currentDesc.trim(),
                    lat = currentLat,
                    lon = currentLon,
                    fotoRuta = currentFoto,
                    categoriaId = currentCat,
                    estatusId = estatusId
                )
                // Se mantiene isSuccess = true e isSaving = true para bloquear cualquier toque adicional durante la transición/cierre de la pantalla
                _uiState.update { it.copy(isSaving = true, isSuccess = true) }
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