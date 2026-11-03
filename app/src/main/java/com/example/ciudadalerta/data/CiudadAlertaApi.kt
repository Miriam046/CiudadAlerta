package com.example.ciudadalerta.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

data class TicketDto(
    val id: Int?,
    val descripcion: String,
    val latitud: Double,
    val longitud: Double,
    val estatusId: Int
)

interface CiudadAlertaApi {
    @GET("tickets")
    suspend fun getTickets(): Response<List<TicketDto>>

    @POST("tickets")
    suspend fun createTicket(@Body ticket: TicketDto): Response<TicketDto>
}