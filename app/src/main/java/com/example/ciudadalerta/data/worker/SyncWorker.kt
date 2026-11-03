package com.example.ciudadalerta.data.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.ciudadalerta.data.AppDatabase
import com.example.ciudadalerta.data.CiudadAlertaApi
import com.example.ciudadalerta.data.TicketDto

class SyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val database = AppDatabase.getDatabase(applicationContext)
        val dao = database.ticketDao()
        // Opcional: Instancia de Retrofit (o mock API)
        val api: CiudadAlertaApi? = null

        return try {
            // 1. Sincronizar tickets creados/actualizados no sincronizados
            val unsyncedTickets = dao.getUnsyncedCreatedTickets()
            for (ticket in unsyncedTickets) {
                if (api != null) {
                    val dto = TicketDto(
                        id = if (ticket.id > 0) ticket.id else null,
                        descripcion = ticket.descripcion,
                        latitud = ticket.latitud,
                        longitud = ticket.longitud,
                        estatusId = ticket.estatusId
                    )
                    val response = api.createTicket(dto)
                    if (response.isSuccessful) {
                        dao.updateTicket(ticket.copy(isSynced = true))
                    }
                } else {
                    // Marcamos como sincronizado localmente para simulación
                    dao.updateTicket(ticket.copy(isSynced = true))
                }
            }

            // 2. Sincronizar tickets eliminados (soft delete) y purgarlos localmente
            val pendingDeleted = dao.getPendingDeletedTickets()
            for (ticket in pendingDeleted) {
                if (api != null && ticket.id > 0) {
                    try {
                        api.deleteTicket(ticket.id)
                    } catch (e: Exception) {
                        Log.e("SyncWorker", "Error al borrar ticket en API: ${e.localizedMessage}")
                    }
                }
                // Purga física en Room
                dao.hardDeleteTicket(ticket.id)
            }

            Result.success()
        } catch (e: Exception) {
            Log.e("SyncWorker", "Error durante sincronización en segundo plano: ${e.localizedMessage}")
            Result.retry()
        }
    }
}