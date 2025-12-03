package com.example.bloomix

/**
 * Core Data Models for Bloomix, supporting the MLProcessor.
 * This file defines the structures used to hold the results of the AI analysis.
 */

// 1. Enumeration for Sentiment (Positive, Neutral, Negative)
// The Naïve Bayes classifier outputs one of these three values.
enum class Sentiment {
    POSITIVE,
    NEUTRAL,
    NEGATIVE
}

// 2. Data structure for Micro-Actions
// A simple suggestion (e.g., "Take a breath") generated based on the user's mood.
data class MicroAction(
    val type: String,       // Category: "Rest", "Organize", etc.
    val description: String // The actual text displayed to the user.
)

// 3. Complete AI Analysis Results
// This object bundles everything the AI produces so it can be passed to the Result Screen easily.
data class AnalysisResult(
    // The basic feeling detected (Happy/Sad/Neutral)
    val sentiment: Sentiment,

    // The specific mood category detected by the SVM (e.g., "High-Energy Positive Focus")
    val overallMoodCategory: String,

    // The personalized question derived from ReflectionData
    val reflectionPrompt: String,

    // The list of suggested small tasks
    val suggestedMicroActions: List<MicroAction>
)