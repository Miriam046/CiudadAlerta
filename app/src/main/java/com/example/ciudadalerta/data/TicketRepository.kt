package com.example.ciudadalerta.data

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.ciudadalerta.data.worker.SyncWorker
import kotlinx.coroutines.flow.Flow

class TicketRepository(
    private val ticketDao: TicketDao,
    private val api: CiudadAlertaApi? = null
) {
    // Fuente única de la verdad: tickets activos no eliminados
    val allTickets: Flow<List<TicketEntity>> = ticketDao.getAllActiveTickets()

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
            isSynced = false,
            isDeleted = false
        )

        // 1. Guardar localmente siempre (Offline-first)
        val generatedId = ticketDao.insertTicket(newTicket)
        val savedTicket = newTicket.copy(id = generatedId.toInt())

        // 2. Intentar enviar a la API
        syncTicketToApi(savedTicket)
    }

    suspend fun updateTicketStatus(ticket: TicketEntity, newEstatusId: Int) {
        val updated = ticket.copy(estatusId = newEstatusId, isSynced = false)
        ticketDao.updateTicket(updated)
        syncTicketToApi(updated)
    }

    suspend fun deleteTicket(id: Int) {
        // Soft delete para sincronización offline
        ticketDao.softDeleteTicket(id)
    }

    suspend fun syncPendingTickets() {
        val unsynced = ticketDao.getUnsyncedCreatedTickets()
        for (ticket in unsynced) {
            syncTicketToApi(ticket)
        }

        val pendingDeleted = ticketDao.getPendingDeletedTickets()
        for (ticket in pendingDeleted) {
            try {
                if (api != null && ticket.id > 0) {
                    api.deleteTicket(ticket.id)
                }
                ticketDao.hardDeleteTicket(ticket.id)
            } catch (e: Exception) {
                Log.e("Sync", "Error eliminando en API: ${e.localizedMessage}")
            }
        }
    }

    fun scheduleBackgroundSync(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueue(syncRequest)
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
            }
        } catch (e: Exception) {
            Log.e("Sync", "Sin conexión con servidor. Almacenado offline: ${e.localizedMessage}")
        }
    }
}