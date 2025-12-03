package com.example.bloomix

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "journal_table")
data class JournalEntry(
    @PrimaryKey val dateKey: String, // format: "2025-11-05" - Keeps your existing ID system
    val timestamp: Long,             // For easy sorting
    val year: Int,                   // For fast filtering by Year
    val month: Int,                  // For fast filtering by Month
    val day: Int,

    // The Content
    val flowerKey: String,
    val journalText: String,
    val emotions: String,            // Stored as "happy,tired,excited" (Comma separated)

    // AI Analysis Results
    val sentiment: String,
    val moodCategory: String,
    val reflection: String,
    val microAction: String
)