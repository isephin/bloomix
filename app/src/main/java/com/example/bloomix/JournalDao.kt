package com.example.bloomix

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface JournalDao {

    // Saves a journal entry.
    // OnConflictStrategy.REPLACE means if we save an entry with a dateKey that already exists
    // (e.g. "2025-11-20"), it overwrites the old one. This handles "Editing" automatically.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: JournalEntry)

    // Retrieve a single entry for a specific date (used when clicking a calendar day)
    @Query("SELECT * FROM journal_table WHERE dateKey = :dateKey")
    suspend fun getEntryByDate(dateKey: String): JournalEntry?

    // Retrieve all entries for a specific month (used for History list and Calendar icons)
    @Query("SELECT * FROM journal_table WHERE year = :year AND month = :month ORDER BY day DESC")
    suspend fun getEntriesForMonth(year: Int, month: Int): List<JournalEntry>

    // Retrieve EVERYTHING (used for stats or backups)
    @Query("SELECT * FROM journal_table")
    suspend fun getAllEntries(): List<JournalEntry>

    // Deletes a specific day's entry
    @Query("DELETE FROM journal_table WHERE dateKey = :dateKey")
    suspend fun deleteEntry(dateKey: String)
}