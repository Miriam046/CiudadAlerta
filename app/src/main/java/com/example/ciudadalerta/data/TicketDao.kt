package com.example.ciudadalerta.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TicketDao {
    @Query("SELECT * FROM ticket_incidencia WHERE isDeleted = 0 ORDER BY fecha DESC")
    fun getAllActiveTickets(): Flow<List<TicketEntity>>

    @Query("SELECT * FROM ticket_incidencia WHERE id = :id AND isDeleted = 0 LIMIT 1")
    fun getTicketById(id: Int): Flow<TicketEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTicket(ticket: TicketEntity): Long

    @Query("SELECT * FROM ticket_incidencia WHERE isSynced = 0 AND isDeleted = 0")
    suspend fun getUnsyncedCreatedTickets(): List<TicketEntity>

    @Query("SELECT * FROM ticket_incidencia WHERE isDeleted = 1")
    suspend fun getPendingDeletedTickets(): List<TicketEntity>

    @Update
    suspend fun updateTicket(ticket: TicketEntity)

    @Query("UPDATE ticket_incidencia SET isDeleted = 1, isSynced = 0 WHERE id = :id")
    suspend fun softDeleteTicket(id: Int)

    @Query("DELETE FROM ticket_incidencia WHERE id = :id")
    suspend fun hardDeleteTicket(id: Int)
}