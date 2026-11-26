package com.example.ciudadalerta.domain.model

import androidx.annotation.StringRes
import com.example.ciudadalerta.R

/**
 * Ciclo de vida de 4 fases según el diagrama de flujo de Ciudad Alerta:
 * 1: Registrado -> 2: En Evaluación Técnica -> 3: Mantenimiento en Progreso -> 4: Solucionado
 */
enum class TicketStatus(val id: Int, @StringRes val labelRes: Int) {
    REGISTRADO(1, R.string.status_registered),
    EN_EVALUACION(2, R.string.status_evaluation),
    EN_REPARACION(3, R.string.status_in_progress),
    SOLUCIONADO(4, R.string.status_solved);

    companion object {
        fun fromId(id: Int): TicketStatus = entries.firstOrNull { it.id == id } ?: REGISTRADO
    }
}

enum class TicketCategory(
    val id: Int,
    @StringRes val shortLabelRes: Int,
    @StringRes val fullLabelRes: Int
) {
    BACHE(1, R.string.cat_pothole, R.string.cat_pothole_full),
    ALUMBRADO(2, R.string.cat_lighting, R.string.cat_lighting_full),
    AGUA(3, R.string.cat_water, R.string.cat_water_full),
    SEGURIDAD(4, R.string.cat_security, R.string.cat_security_full),
    BASURA(5, R.string.cat_trash, R.string.cat_trash_full);

    companion object {
        fun fromId(id: Int): TicketCategory = entries.firstOrNull { it.id == id } ?: BACHE
    }
}

enum class MaintenanceCrew(
    val dbValue: String,
    @param:StringRes val labelRes: Int
) {
    VIALIDAD("Cuadrilla Vialidad Norte", R.string.crew_roads),
    ALUMBRADO("Cuadrilla Alumbrado Público", R.string.crew_lighting),
    HIDRAULICA("Brigada Hidráulica Municipal", R.string.crew_water),
    SEGURIDAD("Patrulla Seguridad Urbana", R.string.crew_security),
    LIMPIA("Brigada Limpia y Residuos", R.string.crew_waste);

    companion object {
        fun fromDbValue(value: String?): MaintenanceCrew? =
            entries.firstOrNull { it.dbValue == value }
    }
}

object MaintenanceCrews {
    val availableCrews: List<String> = MaintenanceCrew.entries.map { it.dbValue }

    fun defaultForCategory(categoriaId: Int): String = when (categoriaId) {
        1 -> availableCrews[0]
        2 -> availableCrews[1]
        3 -> availableCrews[2]
        4 -> availableCrews[3]
        5 -> availableCrews[4]
        else -> availableCrews[0]
    }
}
