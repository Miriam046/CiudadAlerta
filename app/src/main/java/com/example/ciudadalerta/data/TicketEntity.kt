package com.example.ciudadalerta.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ticket_incidencia")
data class TicketEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val descripcion: String,
    val fecha: Long = System.currentTimeMillis(),
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val fotoRutaLocal: String? = null,
    val categoriaId: Int = 1, // 1: Bache, 2: Alumbrado, 3: Agua/Fuga, 4: Seguridad, 5: Basura
    val estatusId: Int = 1,   // 1: Registrado, 2: Evaluación, 3: Solucionado
    val isSynced: Boolean = false, // Bandera para sincronización offline-first
    val isDeleted: Boolean = false, // Soft delete para sincronización offline
    val solucionadoPor: String? = null // Encargado que solucionó el ticket
)