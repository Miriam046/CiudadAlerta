package com.example.ciudadalerta.data

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Url

/**
 * DTO completo que devuelve Supabase PostgREST para la tabla `ticket_incidencia`.
 */
data class TicketDto(
    @SerializedName("id_ticket") val id: Int?,
    @SerializedName("descripcion") val descripcion: String,
    @SerializedName("ruta_evidencia_url") val fotoUrl: String? = null,
    @SerializedName("coordenada_lat") val latitud: Double,
    @SerializedName("coordenada_lng") val longitud: Double,
    @SerializedName("registro_fecha") val fecha: Long = System.currentTimeMillis(),
    @SerializedName("actualizacion_fecha") val fechaActualizacion: Long = fecha,
    @SerializedName("id_categoria") val categoriaId: Int = 1,
    @SerializedName("id_estatus") val estatusId: Int = 1,
    @SerializedName("es_urgente") val esUrgente: Boolean = false,
    @SerializedName("cuadrilla_asignada") val cuadrillaAsignada: String? = null,
    @SerializedName("solucionado_por") val solucionadoPor: String? = null
)

/**
 * Payload de inserción en `ticket_incidencia` (sin id_ticket para que PostgreSQL genere BIGSERIAL).
 */
data class TicketInsertDto(
    @SerializedName("descripcion") val descripcion: String,
    @SerializedName("ruta_evidencia_url") val fotoUrl: String? = null,
    @SerializedName("coordenada_lat") val latitud: Double,
    @SerializedName("coordenada_lng") val longitud: Double,
    @SerializedName("registro_fecha") val fecha: Long,
    @SerializedName("actualizacion_fecha") val fechaActualizacion: Long,
    @SerializedName("id_categoria") val categoriaId: Int,
    @SerializedName("id_estatus") val estatusId: Int,
    @SerializedName("es_urgente") val esUrgente: Boolean,
    @SerializedName("cuadrilla_asignada") val cuadrillaAsignada: String? = null,
    @SerializedName("solucionado_por") val solucionadoPor: String? = null
)

/**
 * Payload de actualización parcial en `ticket_incidencia`.
 */
data class TicketPatchDto(
    @SerializedName("id_estatus") val estatusId: Int,
    @SerializedName("actualizacion_fecha") val fechaActualizacion: Long,
    @SerializedName("cuadrilla_asignada") val cuadrillaAsignada: String? = null,
    @SerializedName("solucionado_por") val solucionadoPor: String? = null,
    @SerializedName("ruta_evidencia_url") val fotoUrl: String? = null
)

/**
 * Payload de registro/upsert en `cuenta_usuario`.
 */
data class UserUpsertDto(
    @SerializedName("nombre_completo") val nombreCompleto: String,
    @SerializedName("correo_contacto") val correoContacto: String,
    @SerializedName("clave_hash") val claveHash: String = "demo_no_validation",
    @SerializedName("id_rol") val idRol: Int
)

data class UserDto(
    @SerializedName("id_usuario") val idUsuario: Int? = null,
    @SerializedName("nombre_completo") val nombreCompleto: String,
    @SerializedName("correo_contacto") val correoContacto: String,
    @SerializedName("id_rol") val idRol: Int
)

/**
 * Contrato REST real para Supabase PostgREST y Supabase Storage.
 */
interface CiudadAlertaApi {
    @GET("ticket_incidencia")
    suspend fun getTickets(
        @Query("select") select: String = "*",
        @Query("order") order: String = "es_urgente.desc,registro_fecha.desc"
    ): Response<List<TicketDto>>

    @Headers("Prefer: return=representation")
    @POST("ticket_incidencia")
    suspend fun createTicket(@Body ticket: TicketInsertDto): Response<List<TicketDto>>

    @Headers("Prefer: return=representation")
    @PATCH("ticket_incidencia")
    suspend fun updateTicket(
        @Query("id_ticket") idFilter: String,
        @Body patch: TicketPatchDto
    ): Response<List<TicketDto>>

    @DELETE("ticket_incidencia")
    suspend fun deleteTicket(
        @Query("id_ticket") idFilter: String
    ): Response<Unit>

    @GET("cuenta_usuario")
    suspend fun getUserByEmail(
        @Query("correo_contacto") emailFilter: String,
        @Query("select") select: String = "id_usuario,nombre_completo,correo_contacto,id_rol",
        @Query("limit") limit: Int = 1
    ): Response<List<UserDto>>

    @Headers("Prefer: resolution=merge-duplicates,return=minimal")
    @POST("cuenta_usuario")
    suspend fun upsertUser(
        @Query("on_conflict") onConflict: String = "correo_contacto",
        @Body user: UserUpsertDto
    ): Response<Unit>

    @POST
    suspend fun uploadStorageObject(
        @Url fullStorageUrl: String,
        @Header("x-upsert") upsert: String = "true",
        @Body fileBody: RequestBody
    ): Response<Unit>
}

object RetrofitClient {
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original = chain.request()
                val builder = original.newBuilder()
                if (SupabaseConfig.isConfigured) {
                    builder.header("apikey", SupabaseConfig.anonKey)
                    // Solo enviamos Authorization: Bearer cuando es JWT legacy (eyJ...);
                    // las llaves nuevas sb_publishable_... solo usan el header apikey.
                    if (SupabaseConfig.anonKey.startsWith("eyJ")) {
                        builder.header("Authorization", "Bearer ${SupabaseConfig.anonKey}")
                    }
                }
                chain.proceed(builder.build())
            }
            .build()
    }

    val apiService: CiudadAlertaApi by lazy {
        Retrofit.Builder()
            .baseUrl(SupabaseConfig.restBaseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CiudadAlertaApi::class.java)
    }
}
