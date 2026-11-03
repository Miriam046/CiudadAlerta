package com.example.ciudadalerta.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TicketDao {
    @Query("SELECT * FROM ticket_incidencia ORDER BY fecha DESC")
    fun getAllTickets(): Flow<List<TicketEntity>>

    @Query("SELECT * FROM ticket_incidencia WHERE id = :id LIMIT 1")
    fun getTicketById(id: Int): Flow<TicketEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTicket(ticket: TicketEntity): Long

    @Query("SELECT * FROM ticket_incidencia WHERE isSynced = 0")
    suspend fun getUnsyncedTickets(): List<TicketEntity>

    @Update
    suspend fun updateTicket(ticket: TicketEntity)

    @Query("DELETE FROM ticket_incidencia WHERE id = :id")
    suspend fun deleteTicketById(id: Int)
}