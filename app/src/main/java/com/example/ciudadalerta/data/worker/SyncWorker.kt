package com.example.ciudadalerta.data.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.ciudadalerta.data.AppDatabase
import com.example.ciudadalerta.data.LocalTicketRepository
import com.example.ciudadalerta.data.NetworkTicketRepository
import com.example.ciudadalerta.data.RetrofitClient
import com.example.ciudadalerta.data.TicketRepository

class SyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val database = AppDatabase.getDatabase(applicationContext)
        val localRepo = LocalTicketRepository(database.ticketDao())
        val networkRepo = NetworkTicketRepository(RetrofitClient.apiService)
        val repository = TicketRepository(localRepo, networkRepo, applicationContext)

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
