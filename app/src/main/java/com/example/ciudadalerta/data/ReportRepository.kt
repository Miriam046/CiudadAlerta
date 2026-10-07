package com.example.ciudadalerta.data

interface ReportRepository {
    fun getReports(): List<String>
}

class MockReportRepository : ReportRepository {
    override fun getReports(): List<String> {
        return listOf("Bache en Av. Principal", "Lámpara fundida en el parque", "Fuga de agua")
    }
}