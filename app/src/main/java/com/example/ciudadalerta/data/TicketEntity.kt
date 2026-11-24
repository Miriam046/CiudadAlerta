package com.example.ciudadalerta.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ticket_incidencia")
data class TicketEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val remoteId: Int? = null, // id_ticket real generado por PostgreSQL en Supabase
    val descripcion: String,
    val fecha: Long = System.currentTimeMillis(),
    val fechaActualizacion: Long = fecha,
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val fotoRutaLocal: String? = null,
    val fotoUrlRemota: String? = null, // URL pública en Supabase Storage (bucket evidencias)
    val categoriaId: Int = 1, // 1: Bache, 2: Alumbrado, 3: Agua/Fuga, 4: Seguridad, 5: Basura
    val estatusId: Int = 1,   // 1: Registrado, 2: En Evaluación, 3: En Reparación, 4: Solucionado
    val esUrgente: Boolean = false,
    val isSynced: Boolean = false,
    val isDeleted: Boolean = false,
    val cuadrillaAsignada: String? = null,
    val solucionadoPor: String? = null
) {
    /** Mejor ruta disponible para mostrar la evidencia (local primero, si no URL de Supabase). */
    val bestPhotoSource: String?
        get() = fotoRutaLocal?.takeIf { it.isNotBlank() } ?: fotoUrlRemota?.takeIf { it.isNotBlank() }
}
