package com.example.bloomix

import java.util.Date

data class HistoryItem(
    val dateKey: String,      // "2025-11-05"
    val dateObj: Date,        // For sorting
    val dayNumber: String,    // "05"
    val dayName: String,      // "Mon"
    val monthYear: String,    // "2025.09"
    val flowerName: String,   // "Red Rose"
    val flowerResId: Int,     // Drawable ID
    val emotions: List<String> // List of emotion names
)