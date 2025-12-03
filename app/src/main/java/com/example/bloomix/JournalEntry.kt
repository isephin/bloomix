package com.example.bloomix

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "journal_table")
data class JournalEntry(
    // The Primary Key uniquely identifies a row.
    // We use the date string "YYYY-MM-DD" because a user can only have one entry per day.
    @PrimaryKey val dateKey: String,

    val timestamp: Long,             // Used for sorting if needed
    val year: Int,                   // Separate columns for fast filtering by year
    val month: Int,                  // Separate columns for fast filtering by month
    val day: Int,

    // --- Content ---
    val flowerKey: String,           // The ID of the flower (e.g., "rose")
    val journalText: String,         // The user's written entry
    val emotions: String,            // Stored as a comma-separated string (e.g., "happy,tired")

    // --- AI Analysis Results ---
    val sentiment: String,           // "POSITIVE", "NEGATIVE", etc.
    val moodCategory: String,        // "High-Energy Positive Focus", etc.
    val reflection: String,          // The AI generated question
    val microAction: String          // The suggested small task
)