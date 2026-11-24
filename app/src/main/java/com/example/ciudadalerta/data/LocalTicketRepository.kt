package com.example.ciudadalerta.data

import kotlinx.coroutines.flow.Flow

interface LocalTicketDataSource {
    val allActiveTickets: Flow<List<TicketEntity>>
    fun getTicketById(id: Int): Flow<TicketEntity?>
    suspend fun getTicketByRemoteId(remoteId: Int): TicketEntity?
    suspend fun getTicketCount(): Int
    suspend fun insertTicket(ticket: TicketEntity): Long
    suspend fun updateTicket(ticket: TicketEntity)
    suspend fun softDeleteTicket(id: Int)
    suspend fun hardDeleteTicket(id: Int)
    suspend fun getUnsyncedCreatedTickets(): List<TicketEntity>
    suspend fun getPendingDeletedTickets(): List<TicketEntity>
}

class LocalTicketRepository(
    private val ticketDao: TicketDao
) : LocalTicketDataSource {

    override val allActiveTickets: Flow<List<TicketEntity>> = ticketDao.getAllActiveTickets()

    override fun getTicketById(id: Int): Flow<TicketEntity?> = ticketDao.getTicketById(id)

    override suspend fun getTicketByRemoteId(remoteId: Int): TicketEntity? =
        ticketDao.getTicketByRemoteId(remoteId)

    override suspend fun getTicketCount(): Int = ticketDao.getTicketCount()

    override suspend fun insertTicket(ticket: TicketEntity): Long = ticketDao.insertTicket(ticket)

    override suspend fun updateTicket(ticket: TicketEntity) = ticketDao.updateTicket(ticket)

    override suspend fun softDeleteTicket(id: Int) = ticketDao.softDeleteTicket(id)

    override suspend fun hardDeleteTicket(id: Int) = ticketDao.hardDeleteTicket(id)

    override suspend fun getUnsyncedCreatedTickets(): List<TicketEntity> =
        ticketDao.getUnsyncedCreatedTickets()

    override suspend fun getPendingDeletedTickets(): List<TicketEntity> =
        ticketDao.getPendingDeletedTickets()
}
