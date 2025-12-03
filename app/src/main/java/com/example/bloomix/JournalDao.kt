package com.example.bloomix

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface JournalDao {

    // Saves entry. If dateKey exists, it overwrites it (Edit functionality)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: JournalEntry)

    // Get a specific day (for Calendar clicks)
    @Query("SELECT * FROM journal_table WHERE dateKey = :dateKey")
    suspend fun getEntryByDate(dateKey: String): JournalEntry?

    // Get all entries for a specific month (for History & Calendar grid)
    @Query("SELECT * FROM journal_table WHERE year = :year AND month = :month ORDER BY day DESC")
    suspend fun getEntriesForMonth(year: Int, month: Int): List<JournalEntry>

    // Get ALL entries (useful for Stats or Backups)
    @Query("SELECT * FROM journal_table")
    suspend fun getAllEntries(): List<JournalEntry>

    // Delete a specific entry
    @Query("DELETE FROM journal_table WHERE dateKey = :dateKey")
    suspend fun deleteEntry(dateKey: String)
}