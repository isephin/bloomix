package com.example.bloomix

/**
 * Core Data Models for Bloomix, supporting the MLProcessor.
 * This file defines the structures used to hold the results of the AI analysis.
 */

// 1. Enumeration for Journal Sentiment Analysis (from Naïve Bayes)
enum class Sentiment {
    POSITIVE,
    NEUTRAL,
    NEGATIVE
}

// 2. Data structure for Micro-Actions (Suggested small tasks)
data class MicroAction(
    val type: String, // E.g., "Rest", "Organize", "Grounding"
    val description: String // E.g., "Take 10 minutes to sit quietly without your phone."
)

// 3. Complete AI Analysis Results (Combines Naïve Bayes, SVM, and LLM output)
data class AnalysisResult(
    // Naïve Bayes Sentiment Analysis (from journalText)
    val sentiment: Sentiment,

    // SVM Mood Category Classification (based on selectedMoods + journalText)
    val overallMoodCategory: String, // E.g., "High-Energy Positive Focus"

    // AI-Generated Output (Reflection Prompt and Micro-Actions)
    val reflectionPrompt: String,
    val suggestedMicroActions: List<MicroAction>
)