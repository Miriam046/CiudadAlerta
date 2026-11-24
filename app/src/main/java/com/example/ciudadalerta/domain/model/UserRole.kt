package com.example.ciudadalerta.domain.model

import androidx.annotation.StringRes
import com.example.ciudadalerta.R

/**
 * Roles de usuario disponibles en Ciudad Alerta.
 */
enum class UserRole(
    val key: String,
    @StringRes val titleRes: Int,
    @StringRes val subtitleRes: Int,
    val defaultEmail: String,
    val defaultName: String
) {
    CIUDADANO(
        key = "CIUDADANO",
        titleRes = R.string.role_citizen,
        subtitleRes = R.string.role_citizen_sub,
        defaultEmail = "ciudadano@ciudadalerta.mx",
        defaultName = "Vecino Ciudadano"
    ),
    TECNICO_REPARACION(
        key = "TECNICO_REPARACION",
        titleRes = R.string.role_technician,
        subtitleRes = R.string.role_technician_sub,
        defaultEmail = "cuadrilla@ciudadalerta.gob",
        defaultName = "Técnico de Reparación"
    ),
    GESTOR_MUNICIPAL(
        key = "GESTOR_MUNICIPAL",
        titleRes = R.string.role_manager,
        subtitleRes = R.string.role_manager_sub,
        defaultEmail = "gestor@ciudadalerta.gob",
        defaultName = "Gestor Municipal"
    );

    val canUpdateRepairStatus: Boolean
        get() = this == TECNICO_REPARACION || this == GESTOR_MUNICIPAL

    val canManageAndDelete: Boolean
        get() = this == GESTOR_MUNICIPAL

    companion object {
        fun fromKey(key: String?): UserRole =
            entries.firstOrNull { it.key == key } ?: CIUDADANO
    }
}
