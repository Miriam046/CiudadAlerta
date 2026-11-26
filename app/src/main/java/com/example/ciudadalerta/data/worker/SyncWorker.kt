package com.example.ciudadalerta.data.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.ciudadalerta.CiudadAlertaApplication
import com.example.ciudadalerta.data.DefaultAppContainer

class SyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val container = (applicationContext as? CiudadAlertaApplication)?.container
            ?: DefaultAppContainer(applicationContext)
        val repository = container.ticketRepository

        return try {
            val count = repository.syncPendingTickets()
            Log.i("SyncWorker", "Sincronización en segundo plano completada ($count registros procesados).")
            Result.success()
        } catch (e: Exception) {
            Log.e("SyncWorker", "Error durante sincronización en segundo plano: ${e.localizedMessage}")
            Result.retry()
        }
    }
}
