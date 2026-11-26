package com.example.ciudadalerta.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ciudadalerta.R
import com.example.ciudadalerta.data.TicketRepository
import com.example.ciudadalerta.domain.usecase.CreateTicketUseCase
import com.example.ciudadalerta.ui.components.UiText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReportarUiState(
    val descripcion: String = "",
    val categoriaId: Int = 1,
    val latitud: Double = 19.432608,
    val longitud: Double = -99.133209,
    val isRealGps: Boolean = false,
    val fotoRuta: String? = null,
    val isUrgent: Boolean = false,
    val isSaving: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: UiText? = null
)

class ReportarViewModel(private val repository: TicketRepository) : ViewModel() {

    private val createTicketUseCase = CreateTicketUseCase(repository)

    private val _uiState = MutableStateFlow(ReportarUiState())
    val uiState: StateFlow<ReportarUiState> = _uiState.asStateFlow()

    fun onDescripcionChanged(newText: String) {
        _uiState.update { it.copy(descripcion = newText, errorMessage = null) }
    }

    fun onCategoriaSelected(id: Int) {
        _uiState.update { it.copy(categoriaId = id) }
    }

    fun onLocationChanged(lat: Double, lon: Double, fromHardware: Boolean = false) {
        _uiState.update { it.copy(latitud = lat, longitud = lon, isRealGps = fromHardware) }
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

        _uiState.update { current ->
            if (current.isSaving || current.isSuccess) {
                shouldReturn = true
                return@update current
            }
            if (current.descripcion.isBlank()) {
                shouldReturn = true
                return@update current.copy(
                    errorMessage = UiText.StringResource(R.string.err_report_empty_desc)
                )
            }

            currentDesc = current.descripcion
            currentLat = current.latitud
            currentLon = current.longitud
            currentFoto = current.fotoRuta
            currentCat = current.categoriaId
            isUrg = current.isUrgent

            current.copy(isSaving = true, errorMessage = null)
        }

        if (shouldReturn) return

        viewModelScope.launch {
            try {
                createTicketUseCase(
                    descripcion = currentDesc,
                    latitud = currentLat,
                    longitud = currentLon,
                    fotoRuta = currentFoto,
                    categoriaId = currentCat,
                    esUrgente = isUrg
                )
                _uiState.update { it.copy(isSaving = true, isSuccess = true) }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = UiText.StringResource(
                            R.string.err_report_save_failed,
                            e.localizedMessage ?: ""
                        )
                    )
                }
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
