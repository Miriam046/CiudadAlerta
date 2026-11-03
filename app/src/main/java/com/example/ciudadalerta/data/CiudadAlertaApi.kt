package com.example.ciudadalerta.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

data class TicketDto(
    val id: Int?,
    val descripcion: String,
    val latitud: Double,
    val longitud: Double,
    val estatusId: Int,
    val solucionadoPor: String? = null
)

interface CiudadAlertaApi {
    @GET("tickets")
    suspend fun getTickets(): Response<List<TicketDto>>

    @POST("tickets")
    suspend fun createTicket(@Body ticket: TicketDto): Response<TicketDto>

    @PUT("tickets/{id}")
    suspend fun updateTicket(@Path("id") id: Int, @Body ticket: TicketDto): Response<TicketDto>

    @DELETE("tickets/{id}")
    suspend fun deleteTicket(@Path("id") id: Int): Response<Unit>
}