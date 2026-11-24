package com.example.ciudadalerta.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.RequestBody
import java.io.File

interface RemoteTicketDataSource {
    suspend fun fetchRemoteTickets(): Result<List<TicketDto>>
    suspend fun pushTicket(ticket: TicketEntity): Result<TicketDto>
    suspend fun updateRemoteTicket(ticket: TicketEntity): Result<TicketDto>
    suspend fun deleteRemoteTicket(remoteId: Int): Result<Unit>
    suspend fun fetchUserByEmail(email: String): Result<UserDto?>
    suspend fun upsertUser(nombre: String, correo: String, clave: String, idRol: Int): Result<Unit>
}

/**
 * Cliente de red conectado directamente a Supabase (PostgREST + Supabase Storage).
 */
class NetworkTicketRepository(
    private val api: CiudadAlertaApi
) : RemoteTicketDataSource {

    override suspend fun fetchRemoteTickets(): Result<List<TicketDto>> = withContext(Dispatchers.IO) {
        if (!SupabaseConfig.isConfigured) {
            return@withContext Result.failure(IllegalStateException("Supabase no configurado en local.properties"))
        }
        try {
            val response = api.getTickets()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Supabase HTTP ${response.code()}: ${response.errorBody()?.string()}"))
            }
        } catch (e: Exception) {
            Log.w("SupabaseRepo", "Modo Offline - Error leyendo Supabase: ${e.localizedMessage}")
            Result.failure(e)
        }
    }

    override suspend fun pushTicket(ticket: TicketEntity): Result<TicketDto> = withContext(Dispatchers.IO) {
        if (!SupabaseConfig.isConfigured) {
            return@withContext Result.failure(IllegalStateException("Supabase no configurado"))
        }
        try {
            val uploadedPhotoUrl = resolveOrUploadPhoto(ticket)
            val payload = TicketInsertDto(
                descripcion = ticket.descripcion,
                fotoUrl = uploadedPhotoUrl,
                latitud = ticket.latitud,
                longitud = ticket.longitud,
                fecha = ticket.fecha,
                fechaActualizacion = ticket.fechaActualizacion,
                categoriaId = ticket.categoriaId,
                estatusId = ticket.estatusId,
                esUrgente = ticket.esUrgente,
                cuadrillaAsignada = ticket.cuadrillaAsignada,
                solucionadoPor = ticket.solucionadoPor
            )
            val response = api.createTicket(payload)
            if (response.isSuccessful) {
                val created = response.body()?.firstOrNull()
                if (created != null) {
                    Result.success(created)
                } else {
                    Result.failure(Exception("Supabase no devolvió el ticket insertado"))
                }
            } else {
                Result.failure(Exception("Supabase INSERT HTTP ${response.code()}: ${response.errorBody()?.string()}"))
            }
        } catch (e: Exception) {
            Log.w("SupabaseRepo", "Modo Offline - Reporte encolado: ${e.localizedMessage}")
            Result.failure(e)
        }
    }

    override suspend fun updateRemoteTicket(ticket: TicketEntity): Result<TicketDto> = withContext(Dispatchers.IO) {
        if (!SupabaseConfig.isConfigured) {
            return@withContext Result.failure(IllegalStateException("Supabase no configurado"))
        }
        val remoteId = ticket.remoteId ?: return@withContext pushTicket(ticket)
        try {
            val uploadedPhotoUrl = resolveOrUploadPhoto(ticket)
            val patch = TicketPatchDto(
                estatusId = ticket.estatusId,
                fechaActualizacion = ticket.fechaActualizacion,
                cuadrillaAsignada = ticket.cuadrillaAsignada,
                solucionadoPor = ticket.solucionadoPor,
                fotoUrl = uploadedPhotoUrl
            )
            val response = api.updateTicket(idFilter = "eq.$remoteId", patch = patch)
            if (response.isSuccessful) {
                val updated = response.body()?.firstOrNull()
                if (updated != null) {
                    Result.success(updated)
                } else {
                    Result.success(
                        TicketDto(
                            id = remoteId,
                            descripcion = ticket.descripcion,
                            fotoUrl = uploadedPhotoUrl,
                            latitud = ticket.latitud,
                            longitud = ticket.longitud,
                            fecha = ticket.fecha,
                            fechaActualizacion = ticket.fechaActualizacion,
                            categoriaId = ticket.categoriaId,
                            estatusId = ticket.estatusId,
                            esUrgente = ticket.esUrgente,
                            cuadrillaAsignada = ticket.cuadrillaAsignada,
                            solucionadoPor = ticket.solucionadoPor
                        )
                    )
                }
            } else {
                Result.failure(Exception("Supabase PATCH HTTP ${response.code()}: ${response.errorBody()?.string()}"))
            }
        } catch (e: Exception) {
            Log.w("SupabaseRepo", "Modo Offline - Actualización encolada: ${e.localizedMessage}")
            Result.failure(e)
        }
    }

    override suspend fun deleteRemoteTicket(remoteId: Int): Result<Unit> = withContext(Dispatchers.IO) {
        if (!SupabaseConfig.isConfigured) {
            return@withContext Result.failure(IllegalStateException("Supabase no configurado"))
        }
        try {
            val response = api.deleteTicket(idFilter = "eq.$remoteId")
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Supabase DELETE HTTP ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.w("SupabaseRepo", "Modo Offline - Eliminación pendiente: ${e.localizedMessage}")
            Result.failure(e)
        }
    }

    override suspend fun fetchUserByEmail(email: String): Result<UserDto?> =
        withContext(Dispatchers.IO) {
            if (!SupabaseConfig.isConfigured || email.isBlank()) return@withContext Result.success(null)
            try {
                val response = api.getUserByEmail(emailFilter = "eq.${email.trim()}")
                if (response.isSuccessful) {
                    Result.success(response.body()?.firstOrNull())
                } else {
                    Result.failure(Exception("Supabase User GET HTTP ${response.code()}"))
                }
            } catch (e: Exception) {
                Log.w("SupabaseRepo", "Error consultando usuario en Supabase: ${e.localizedMessage}")
                Result.failure(e)
            }
        }

    override suspend fun upsertUser(nombre: String, correo: String, clave: String, idRol: Int): Result<Unit> =
        withContext(Dispatchers.IO) {
            if (!SupabaseConfig.isConfigured) return@withContext Result.success(Unit)
            try {
                val response = api.upsertUser(
                    user = UserUpsertDto(
                        nombreCompleto = nombre,
                        correoContacto = correo,
                        claveHash = clave.ifBlank { "demo_no_validation" },
                        idRol = idRol
                    )
                )
                if (response.isSuccessful) Result.success(Unit)
                else Result.failure(Exception("Supabase User HTTP ${response.code()}"))
            } catch (e: Exception) {
                Log.w("SupabaseRepo", "Usuario guardado solo localmente: ${e.localizedMessage}")
                Result.failure(e)
            }
        }

    /**
     * Sube la fotografía real al bucket `evidencias` de Supabase Storage si existe localmente en disco.
     */
    private suspend fun resolveOrUploadPhoto(ticket: TicketEntity): String? {
        if (!ticket.fotoUrlRemota.isNullOrBlank()) return ticket.fotoUrlRemota
        val localPath = ticket.fotoRutaLocal ?: return null
        val file = File(localPath)
        if (!file.exists() || !file.isFile) return null

        return try {
            val safeName = "ticket_${System.currentTimeMillis()}_${file.name.replace("[^A-Za-z0-9._-]".toRegex(), "_")}"
            val uploadUrl = SupabaseConfig.storageUploadUrl(safeName)
            val body = RequestBody.create(MediaType.parse("image/jpeg"), file)
            val res = api.uploadStorageObject(fullStorageUrl = uploadUrl, fileBody = body)
            if (res.isSuccessful) {
                SupabaseConfig.storagePublicUrl(safeName)
            } else {
                Log.w("SupabaseStorage", "Error subiendo foto (${res.code()}): ${res.errorBody()?.string()}")
                null
            }
        } catch (e: Exception) {
            Log.w("SupabaseStorage", "No se pudo subir la foto a Supabase Storage: ${e.localizedMessage}")
            null
        }
    }
}
