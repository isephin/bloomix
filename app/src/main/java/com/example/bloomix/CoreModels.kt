package com.example.bloomix

enum class Sentiment {
    POSITIVE, NEUTRAL, NEGATIVE
}

data class MicroAction(
    val type: String,
    val description: String
)

data class AnalysisResult(
    val sentiment: Sentiment,
    val overallMoodCategory: String,
    val reflectionPrompt: String,
    val suggestedMicroActions: List<MicroAction>
)