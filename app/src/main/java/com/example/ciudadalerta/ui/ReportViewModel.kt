package com.example.ciudadalerta.ui

import androidx.lifecycle.ViewModel
import com.example.ciudadalerta.data.MockReportRepository
import com.example.ciudadalerta.data.ReportRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ReportViewModel : ViewModel() {
    // Instanciamos el repositorio mockeado por ahora
    private val repository: ReportRepository = MockReportRepository()

    // Estado de la UI
    private val _reports = MutableStateFlow(repository.getReports())
    val reports: StateFlow<List<String>> = _reports.asStateFlow()

    fun addReport(report: String) {
        val currentList = _reports.value.toMutableList()
        currentList.add(report)
        _reports.value = currentList
    }
}