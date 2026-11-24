package com.example.ciudadalerta.data

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.ciudadalerta.data.worker.SyncWorker
import com.example.ciudadalerta.domain.model.TicketStatus
import com.example.ciudadalerta.domain.model.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

/**
 * Repositorio Principal Offline-First coordinando Room (SQLite) y Supabase (PostgREST + Storage).
 */
class TicketRepository(
    private val localDataSource: LocalTicketDataSource,
    private val remoteDataSource: RemoteTicketDataSource,
    private val appContext: Context? = null
) {
    constructor(
        ticketDao: TicketDao,
        api: CiudadAlertaApi = RetrofitClient.apiService,
        context: Context? = null
    ) : this(
        localDataSource = LocalTicketRepository(ticketDao),
        remoteDataSource = NetworkTicketRepository(api),
        appContext = context
    )

    val allTickets: Flow<List<TicketEntity>> = localDataSource.allActiveTickets

    fun getTicketById(id: Int): Flow<TicketEntity?> = localDataSource.getTicketById(id)

    suspend fun ensureSeedData() {
        // Si Supabase está configurado, primero intentamos traer los datos reales de la nube
        if (SupabaseConfig.isConfigured) {
            refreshFromCloud()
        }

        // Si sigue vacía (modo sin internet o Supabase aún sin configurar), cargamos semilla local
        if (localDataSource.getTicketCount() == 0) {
            val now = System.currentTimeMillis()
            val sampleTickets = listOf(
                TicketEntity(
                    descripcion = "Bache profundo en carril central de Av. Insurgentes frente a la parada de autobús; riesgo alto para motociclistas.",
                    fecha = now - 3600_000L * 28,
                    fechaActualizacion = now - 3600_000L * 28,
                    latitud = 19.4342,
                    longitud = -99.1386,
                    fotoRutaLocal = "evidencia_bache_av_insurgentes.jpg",
                    categoriaId = 1,
                    estatusId = TicketStatus.REGISTRADO.id,
                    esUrgente = true,
                    isSynced = !SupabaseConfig.isConfigured
                ),
                TicketEntity(
                    descripcion = "3 luminarias LED inoperantes en el andador peatonal del Parque Municipal, zona oscura por la noche.",
                    fecha = now - 3600_000L * 22,
                    fechaActualizacion = now - 3600_000L * 10,
                    latitud = 19.4289,
                    longitud = -99.1274,
                    fotoRutaLocal = "evidencia_alumbrado_parque.jpg",
                    categoriaId = 2,
                    estatusId = TicketStatus.EN_EVALUACION.id,
                    esUrgente = false,
                    isSynced = !SupabaseConfig.isConfigured,
                    cuadrillaAsignada = "Cuadrilla Alumbrado Público"
                ),
                TicketEntity(
                    descripcion = "Semáforo peatonal sin funcionar y cables expuestos en cruce escolar de Primaria Benito Juárez.",
                    fecha = now - 3600_000L * 16,
                    fechaActualizacion = now - 3600_000L * 4,
                    latitud = 19.4265,
                    longitud = -99.1350,
                    fotoRutaLocal = "evidencia_semaforo_escuela.jpg",
                    categoriaId = 4,
                    estatusId = TicketStatus.EN_REPARACION.id,
                    esUrgente = true,
                    isSynced = false,
                    cuadrillaAsignada = "Patrulla Seguridad Urbana"
                ),
                TicketEntity(
                    descripcion = "Fuga de agua potable en toma principal sobre Calle Reforma esq. Juárez, afectando presión en la colonia.",
                    fecha = now - 3600_000L * 36,
                    fechaActualizacion = now - 3600_000L * 6,
                    latitud = 19.4378,
                    longitud = -99.1412,
                    fotoRutaLocal = "evidencia_fuga_reforma.jpg",
                    categoriaId = 3,
                    estatusId = TicketStatus.SOLUCIONADO.id,
                    esUrgente = true,
                    isSynced = !SupabaseConfig.isConfigured,
                    cuadrillaAsignada = "Brigada Hidráulica Municipal",
                    solucionadoPor = "gestor@ciudadalerta.gob"
                ),
                TicketEntity(
                    descripcion = "Acumulación de basura y escombros en esquina del Mercado Municipal obstruye paso en banqueta.",
                    fecha = now - 3600_000L * 48,
                    fechaActualizacion = now - 3600_000L * 12,
                    latitud = 19.4310,
                    longitud = -99.1295,
                    fotoRutaLocal = "evidencia_basura_mercado.jpg",
                    categoriaId = 5,
                    estatusId = TicketStatus.SOLUCIONADO.id,
                    esUrgente = false,
                    isSynced = !SupabaseConfig.isConfigured,
                    cuadrillaAsignada = "Brigada Limpia y Residuos",
                    solucionadoPor = "cuadrilla@ciudadalerta.gob"
                )
            )
            sampleTickets.forEach { localDataSource.insertTicket(it) }
        }
    }

    /**
     * Descarga los reportes desde Supabase (`ticket_incidencia`) y los fusiona con Room.
     */
    suspend fun refreshFromCloud(): Int {
        if (!SupabaseConfig.isConfigured) return 0
        val result = remoteDataSource.fetchRemoteTickets()
        val remoteTickets = result.getOrNull() ?: return 0

        var merged = 0
        for (dto in remoteTickets) {
            val remoteId = dto.id ?: continue
            val existing = localDataSource.getTicketByRemoteId(remoteId)
            if (existing == null) {
                localDataSource.insertTicket(
                    TicketEntity(
                        remoteId = remoteId,
                        descripcion = dto.descripcion,
                        fecha = dto.fecha,
                        fechaActualizacion = dto.fechaActualizacion,
                        latitud = dto.latitud,
                        longitud = dto.longitud,
                        fotoRutaLocal = null,
                        fotoUrlRemota = dto.fotoUrl,
                        categoriaId = dto.categoriaId,
                        estatusId = dto.estatusId,
                        esUrgente = dto.esUrgente,
                        isSynced = true,
                        isDeleted = false,
                        cuadrillaAsignada = dto.cuadrillaAsignada,
                        solucionadoPor = dto.solucionadoPor
                    )
                )
                merged++
            } else if (existing.isSynced && !existing.isDeleted) {
                localDataSource.updateTicket(
                    existing.copy(
                        descripcion = dto.descripcion,
                        fechaActualizacion = dto.fechaActualizacion,
                        latitud = dto.latitud,
                        longitud = dto.longitud,
                        fotoUrlRemota = dto.fotoUrl ?: existing.fotoUrlRemota,
                        categoriaId = dto.categoriaId,
                        estatusId = dto.estatusId,
                        esUrgente = dto.esUrgente,
                        cuadrillaAsignada = dto.cuadrillaAsignada,
                        solucionadoPor = dto.solucionadoPor
                    )
                )
                merged++
            }
        }
        return merged
    }

    suspend fun addTicket(
        descripcion: String,
        lat: Double,
        lon: Double,
        fotoRuta: String?,
        categoriaId: Int = 1,
        estatusId: Int = TicketStatus.REGISTRADO.id,
        esUrgente: Boolean = false
    ) {
        val now = System.currentTimeMillis()
        val newTicket = TicketEntity(
            descripcion = descripcion,
            fecha = now,
            fechaActualizacion = now,
            latitud = lat,
            longitud = lon,
            fotoRutaLocal = fotoRuta,
            categoriaId = categoriaId,
            estatusId = estatusId,
            esUrgente = esUrgente,
            isSynced = false,
            isDeleted = false
        )

        val generatedId = localDataSource.insertTicket(newTicket)
        val savedTicket = newTicket.copy(id = generatedId.toInt())

        val synced = syncTicketToApi(savedTicket, isNew = true)
        if (!synced && appContext != null) {
            scheduleBackgroundSync(appContext)
        }
    }

    suspend fun updateTicketStatus(
        ticket: TicketEntity,
        newEstatusId: Int,
        cuadrillaAsignada: String? = ticket.cuadrillaAsignada,
        solucionadoPor: String? = null
    ) {
        val updated = ticket.copy(
            estatusId = newEstatusId,
            fechaActualizacion = System.currentTimeMillis(),
            cuadrillaAsignada = cuadrillaAsignada ?: ticket.cuadrillaAsignada,
            solucionadoPor = if (newEstatusId == TicketStatus.SOLUCIONADO.id) {
                solucionadoPor ?: ticket.solucionadoPor
            } else {
                null
            },
            isSynced = false
        )
        localDataSource.updateTicket(updated)
        val synced = syncTicketToApi(updated, isNew = updated.remoteId == null)
        if (!synced && appContext != null) {
            scheduleBackgroundSync(appContext)
        }
    }

    suspend fun deleteTicket(id: Int) {
        val existing = localDataSource.getTicketById(id).firstOrNull()
        localDataSource.softDeleteTicket(id)

        val remoteId = existing?.remoteId
        if (remoteId == null) {
            localDataSource.hardDeleteTicket(id)
            return
        }

        val result = remoteDataSource.deleteRemoteTicket(remoteId)
        if (result.isSuccess) {
            localDataSource.hardDeleteTicket(id)
        } else if (appContext != null) {
            scheduleBackgroundSync(appContext)
        }
    }

    suspend fun resolveUserFromCloud(email: String): UserDto? {
        return remoteDataSource.fetchUserByEmail(email).getOrNull()
    }

    suspend fun registerUserInCloud(name: String, email: String, password: String, role: UserRole) {
        val roleId = when (role) {
            UserRole.CIUDADANO -> 1
            UserRole.TECNICO_REPARACION -> 2
            UserRole.GESTOR_MUNICIPAL -> 3
        }
        remoteDataSource.upsertUser(
            nombre = name.ifBlank { role.defaultName },
            correo = email.ifBlank { role.defaultEmail },
            clave = password,
            idRol = roleId
        )
    }

    /**
     * Sincronización bidireccional completa con Supabase:
     * 1) Sube creaciones/ediciones locales + fotos a Storage
     * 2) Procesa eliminaciones pendientes
     * 3) Descarga cambios recientes desde Supabase
     */
    suspend fun syncPendingTickets(): Int {
        if (!SupabaseConfig.isConfigured) return 0
        var syncedCount = 0

        val unsynced = localDataSource.getUnsyncedCreatedTickets()
        for (ticket in unsynced) {
            if (syncTicketToApi(ticket, isNew = ticket.remoteId == null)) {
                syncedCount++
            }
        }

        val pendingDeleted = localDataSource.getPendingDeletedTickets()
        for (ticket in pendingDeleted) {
            val remoteId = ticket.remoteId
            if (remoteId == null) {
                localDataSource.hardDeleteTicket(ticket.id)
                syncedCount++
            } else {
                val result = remoteDataSource.deleteRemoteTicket(remoteId)
                if (result.isSuccess) {
                    localDataSource.hardDeleteTicket(ticket.id)
                    syncedCount++
                } else {
                    Log.w("Sync", "No se pudo borrar en Supabase ticket remoto #$remoteId")
                }
            }
        }

        refreshFromCloud()
        return syncedCount
    }

    suspend fun syncSingleTicket(ticket: TicketEntity): Boolean {
        return syncTicketToApi(ticket, isNew = ticket.remoteId == null)
    }

    fun scheduleBackgroundSync(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "ciudad_alerta_cloud_sync",
            ExistingWorkPolicy.REPLACE,
            syncRequest
        )
    }

    private suspend fun syncTicketToApi(ticket: TicketEntity, isNew: Boolean): Boolean {
        if (!SupabaseConfig.isConfigured) return false
        val result = if (isNew || ticket.remoteId == null) {
            remoteDataSource.pushTicket(ticket)
        } else {
            remoteDataSource.updateRemoteTicket(ticket)
        }

        val dto = result.getOrNull() ?: return false
        localDataSource.updateTicket(
            ticket.copy(
                remoteId = dto.id ?: ticket.remoteId,
                fotoUrlRemota = dto.fotoUrl ?: ticket.fotoUrlRemota,
                isSynced = true
            )
        )
        return true
    }
}
