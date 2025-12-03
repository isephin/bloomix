package com.example.bloomix

import java.util.Date

data class HistoryItem(
    val dateKey: String,       // ID for database lookups when clicked
    val dateObj: Date,         // Helper for sorting the list chronologically
    val dayNumber: String,     // Formatted day: "05"
    val dayName: String,       // Formatted day name: "Mon"
    val monthYear: String,     // Formatted header: "2025.09"
    val flowerName: String,    // Display name: "Red Rose"
    val flowerResId: Int,      // The drawable resource ID for the flower image
    val emotions: List<String> // List of emotion names to generate the small icons
)