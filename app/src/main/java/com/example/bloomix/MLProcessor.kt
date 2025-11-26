package com.example.bloomix

/**
 * MLProcessor: Simulates the machine learning analysis for Bloomix.
 *
 * In a real application, the functions below would utilize:
 * 1. Naïve Bayes/TensorFlow Lite model for Sentiment Analysis.
 * 2. SVM/TensorFlow Lite model for Mood Category Classification.
 * 3. Gemini API for generating the sophisticated Reflection Prompt and Micro Actions.
 */
object MLProcessor {

    /**
     * 1. Simulates Naïve Bayes Sentiment Analysis.
     * In a real app, this would tokenise the text and run it through a trained NB model.
     */
    private fun analyzeSentiment(text: String): Sentiment {
        val lowerText = text.lowercase()
        // Simple heuristic stub for demonstration:
        return when {
            lowerText.contains("happy") || lowerText.contains("joy") || lowerText.contains("great") || lowerText.contains("love") -> Sentiment.POSITIVE
            lowerText.contains("sad") || lowerText.contains("tired") || lowerText.contains("angry") || lowerText.contains("stressed") -> Sentiment.NEGATIVE
            else -> Sentiment.NEUTRAL
        }
    }

    /**
     * 2. Simulates SVM Mood Category Classification.
     * In a real app, this would use a trained SVM model on a combination of selected moods and sentiment.
     */
    private fun classifyOverallMoodCategory(selectedEmotions: List<String>, sentiment: Sentiment): String {
        val hasPositive = selectedEmotions.any { it in listOf("happy", "excited", "loved", "calm") }
        val hasNegative = selectedEmotions.any { it in listOf("sad", "angry", "tired", "bored", "confused", "annoyed", "stressed", "shocked") }

        return when {
            hasPositive && !hasNegative -> "High-Energy Positive Focus"
            hasPositive && hasNegative -> "Complex Emotional Landscape"
            !hasPositive && hasNegative -> when (sentiment) {
                Sentiment.NEGATIVE -> "Low-Energy Challenging Focus"
                Sentiment.NEUTRAL -> "Contemplative & Unsettled"
                Sentiment.POSITIVE -> "High-Energy Positive Focus" // Should not happen with negative emotions selected
            }
            else -> "Neutral Observation"
        }
    }

    /**
     * 3. Simulates LLM Reflection Prompt and Micro-Action Generation.
     * In a real app, this would use the Gemini API to generate the text.
     */
    private fun generateReflection(sentiment: Sentiment, category: String, selectedEmotions: List<String>): Pair<String, String> {
        val emotionsList = selectedEmotions.joinToString(", ")

        val prompt: String
        val microAction: String

        when (sentiment) {
            Sentiment.POSITIVE -> {
                prompt = "What is the single best thing that contributed to your ${emotionsList} today, and how can you repeat it tomorrow?"
                microAction = "Spend 5 minutes reflecting on your success and celebrating it."
            }
            Sentiment.NEGATIVE -> {
                prompt = "What is the smallest step you can take tomorrow to address the root cause of your $emotionsList?"
                microAction = "Practice 4-7-8 breathing for one minute to reset your nervous system."
            }
            Sentiment.NEUTRAL -> {
                prompt = "If you could pick one word to describe the underlying theme of this entry, what would it be?"
                microAction = "Find a quiet spot and observe your surroundings for 5 minutes."
            }
        }
        return Pair(prompt, microAction)
    }

    /**
     * Main function to process the entry and return the complete analysis result.
     */
    fun processEntry(journalText: String, selectedEmotions: List<String>): AnalysisResult {
        // 1. Sentiment Analysis (Naïve Bayes Stub)
        val sentiment = analyzeSentiment(journalText)

        // 2. Mood Classification (SVM Stub)
        val category = classifyOverallMoodCategory(selectedEmotions, sentiment)

        // 3. Reflection Generation (LLM Stub)
        val (prompt, microActionDesc) = generateReflection(sentiment, category, selectedEmotions)

        // 4. Create the final result object
        return AnalysisResult(
            sentiment = sentiment,
            overallMoodCategory = category,
            reflectionPrompt = prompt,
            suggestedMicroActions = listOf(MicroAction("General", microActionDesc))
            // flowerPetalData removed as it is not a property of AnalysisResult
        )
    }
}