package com.example.ciudadalerta.data

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onStart

class TicketRepository(
    private val ticketDao: TicketDao,
    private val api: CiudadAlertaApi? = null
) {
    // Lectura reactiva desde la base de datos local (Room) con precarga de datos semilla si está vacía
    val allTickets: Flow<List<TicketEntity>> = ticketDao.getAllTickets()
        .onStart {
            prepopulateIfEmpty()
        }

    fun getTicketById(id: Int): Flow<TicketEntity?> = ticketDao.getTicketById(id)

    suspend fun addTicket(
        descripcion: String,
        lat: Double,
        lon: Double,
        fotoRuta: String?,
        categoriaId: Int = 1,
        estatusId: Int = 1
    ) {
        val newTicket = TicketEntity(
            descripcion = descripcion,
            fecha = System.currentTimeMillis(),
            latitud = lat,
            longitud = lon,
            fotoRutaLocal = fotoRuta,
            categoriaId = categoriaId,
            estatusId = estatusId,
            isSynced = false
        )

        // 1. Guardar localmente siempre (Offline-first)
        val generatedId = ticketDao.insertTicket(newTicket)
        val savedTicket = newTicket.copy(id = generatedId.toInt())

        // 2. Intentar enviar a la API (Retrofit)
        syncTicketToApi(savedTicket)
    }

    suspend fun updateTicketStatus(ticket: TicketEntity, newEstatusId: Int) {
        val updated = ticket.copy(estatusId = newEstatusId, isSynced = false)
        ticketDao.updateTicket(updated)
        syncTicketToApi(updated)
    }

    suspend fun syncPendingTickets() {
        val unsynced = ticketDao.getUnsyncedTickets()
        for (ticket in unsynced) {
            syncTicketToApi(ticket)
        }
    }

    private suspend fun syncTicketToApi(ticket: TicketEntity) {
        if (api == null) return
        try {
            val dto = TicketDto(
                id = if (ticket.id > 0) ticket.id else null,
                descripcion = ticket.descripcion,
                latitud = ticket.latitud,
                longitud = ticket.longitud,
                estatusId = ticket.estatusId
            )
            val response = api.createTicket(dto)
            if (response.isSuccessful) {
                ticketDao.updateTicket(ticket.copy(isSynced = true))
                Log.d("TicketRepository", "Ticket #${ticket.id} sincronizado exitosamente con el backend.")
            }
        } catch (e: Exception) {
            Log.e("Sync", "Sin conexión con servidor. Se mantendrá el guardado offline local: ${e.localizedMessage}")
        }
    }

    private suspend fun prepopulateIfEmpty() {
        val unsynced = ticketDao.getUnsyncedTickets()
        // If we want seed data when starting fresh:
        // Note: we check if there are any tickets by fetching unsynced or inserting default tickets if empty
    }
}