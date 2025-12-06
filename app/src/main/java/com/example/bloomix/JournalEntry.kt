package com.example.bloomix

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "journal_table")
data class JournalEntry(
    @PrimaryKey val dateKey: String, // Format: "2025-10-24"
    val timestamp: Long,
    val year: Int,
    val month: Int,
    val day: Int,
    val flowerKey: String,
    val journalText: String,
    val emotions: String,

    // --- NEW AI FIELDS ---
    val sentiment: String,       // "POSITIVE", "NEGATIVE", etc.
    val moodCategory: String,    // "High-Energy Positive Focus", etc.
    val reflection: String,      // The question prompt
    val microAction: String      // The suggested action text
)