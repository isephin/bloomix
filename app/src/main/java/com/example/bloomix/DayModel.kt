package com.example.bloomix

data class DayModel(
    val dayNumber: Int?,   // The actual day (1-31). If null, this is an empty "padding" cell.
    val dateKey: String?   // The unique ID string "2025-11-20" used to look up journal entries.
)