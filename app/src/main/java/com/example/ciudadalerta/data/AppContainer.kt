package com.example.ciudadalerta.data

import android.content.Context

/**
 * Contenedor de Inyección de Dependencias a nivel de Aplicación.
 * Implementa la jerarquía:
 * Application -> Activity -> Compose -> ViewModel -> TicketRepository -> LocalTicketRepository / NetworkTicketRepository
 */
interface AppContainer {
    val database: AppDatabase
    val localTicketRepository: LocalTicketDataSource
    val networkTicketRepository: RemoteTicketDataSource
    val ticketRepository: TicketRepository
    val sessionManager: SessionManager
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    override val database: AppDatabase by lazy {
        AppDatabase.getDatabase(context)
    }

    override val localTicketRepository: LocalTicketDataSource by lazy {
        LocalTicketRepository(database.ticketDao())
    }

    override val networkTicketRepository: RemoteTicketDataSource by lazy {
        NetworkTicketRepository(RetrofitClient.apiService)
    }

    override val ticketRepository: TicketRepository by lazy {
        TicketRepository(
            localDataSource = localTicketRepository,
            remoteDataSource = networkTicketRepository,
            appContext = context.applicationContext
        )
    }

    override val sessionManager: SessionManager by lazy {
        SessionManager.getInstance(context)
    }
}
