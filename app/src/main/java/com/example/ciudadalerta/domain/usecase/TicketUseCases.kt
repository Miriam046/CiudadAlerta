package com.example.ciudadalerta.domain.usecase

import com.example.ciudadalerta.data.TicketEntity
import com.example.ciudadalerta.data.TicketRepository
import com.example.ciudadalerta.domain.model.TicketStatus
import kotlinx.coroutines.flow.Flow

/**
 * Capa de Dominio (Domain Layer): Casos de Uso encapsulados según la arquitectura de 3 capas.
 */
class ObserveTicketsUseCase(private val repository: TicketRepository) {
    operator fun invoke(): Flow<List<TicketEntity>> = repository.allTickets
}

class CreateTicketUseCase(private val repository: TicketRepository) {
    suspend operator fun invoke(
        descripcion: String,
        latitud: Double,
        longitud: Double,
        fotoRuta: String?,
        categoriaId: Int,
        esUrgente: Boolean
    ) {
        require(descripcion.isNotBlank()) { "La descripción no puede estar vacía." }
        repository.addTicket(
            descripcion = descripcion.trim(),
            lat = latitud,
            lon = longitud,
            fotoRuta = fotoRuta,
            categoriaId = categoriaId,
            estatusId = TicketStatus.REGISTRADO.id,
            esUrgente = esUrgente
        )
    }
}

class AdvanceTicketStatusUseCase(private val repository: TicketRepository) {
    suspend operator fun invoke(
        ticket: TicketEntity,
        nextStatus: TicketStatus,
        cuadrillaAsignada: String? = ticket.cuadrillaAsignada,
        solucionadoPor: String? = null
    ) {
        repository.updateTicketStatus(
            ticket = ticket,
            newEstatusId = nextStatus.id,
            cuadrillaAsignada = cuadrillaAsignada,
            solucionadoPor = if (nextStatus == TicketStatus.SOLUCIONADO) solucionadoPor else null
        )
    }
}

class SyncTicketsUseCase(private val repository: TicketRepository) {
    suspend operator fun invoke(): Int = repository.syncPendingTickets()
}
