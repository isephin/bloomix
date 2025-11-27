package com.example.bloomix

/**
 * MLProcessor: Simulates the machine learning analysis for Bloomix.
 * * UPDATE: Improved "Mixed State" handling. If both positive and negative emotions are present,
 * the system biases towards NEUTRAL (Mixed) rather than forcing a hard Positive/Negative label.
 */
object MLProcessor {

    // Define lists for scoring
    private val positiveKeywords = setOf("happy", "joy", "great", "love", "excited", "calm", "good", "blessed", "wonderful")
    private val negativeKeywords = setOf("sad", "tired", "angry", "stressed", "bored", "confused", "annoyed", "bad", "terrible", "shocked")

    /**
     * 1. Weighted Sentiment Analysis
     * Calculates a score based on BOTH the journal text AND the selected emotion chips.
     */
    private fun analyzeSentiment(text: String, selectedEmotions: List<String>): Sentiment {
        var score = 0
        val lowerText = text.lowercase()

        // 1. Score the Text
        val words = lowerText.split("\\s+".toRegex())
        for (word in words) {
            if (word in positiveKeywords) score += 1
            if (word in negativeKeywords) score -= 1
        }

        // 2. Score the Selected Emotions
        var hasPositive = false
        var hasNegative = false

        for (emotion in selectedEmotions) {
            when (emotion.lowercase()) {
                "happy", "excited", "loved", "calm", "zinnia", "marigold", "rose", "lotus" -> {
                    score += 2
                    hasPositive = true
                }
                "sad", "angry", "tired", "stressed", "annoyed", "bored", "confused", "shocked" -> {
                    score -= 2
                    hasNegative = true
                }
            }
        }

        // 3. Determine Final Sentiment with "Mixed State" Logic
        val isMixed = hasPositive && hasNegative

        return if (isMixed) {
            // If emotions are mixed, we widen the threshold.
            // It takes a much stronger score to override the "Complexity" and label it purely Positive or Negative.
            when {
                score > 5 -> Sentiment.POSITIVE // Overwhelmingly positive despite some negative
                score < -5 -> Sentiment.NEGATIVE // Overwhelmingly negative despite some positive
                else -> Sentiment.NEUTRAL // "Mixed" or "Complex" falls here
            }
        } else {
            // Standard thresholds for non-mixed states
            when {
                score > 2 -> Sentiment.POSITIVE
                score < -2 -> Sentiment.NEGATIVE
                else -> Sentiment.NEUTRAL
            }
        }
    }

    /**
     * 2. Mood Category Classification
     */
    private fun classifyOverallMoodCategory(selectedEmotions: List<String>, sentiment: Sentiment): String {
        val hasPositive = selectedEmotions.any { it in listOf("happy", "excited", "loved", "calm") }
        val hasNegative = selectedEmotions.any { it in listOf("sad", "angry", "tired", "stressed", "annoyed", "bored") }

        return when {
            hasPositive && hasNegative -> "Complex Emotional Landscape" // Mixed inputs
            sentiment == Sentiment.POSITIVE -> "High-Energy Positive Focus"
            sentiment == Sentiment.NEGATIVE -> "Processing Difficult Emotions"
            else -> "Balanced and Contemplative"
        }
    }

    /**
     * 3. Reflection Generation
     */
    private fun generateReflection(sentiment: Sentiment, category: String, selectedEmotions: List<String>): Pair<String, String> {
        val uniqueEmotions = selectedEmotions.distinct().joinToString(", ") { it.replaceFirstChar { char -> char.uppercase() } }

        val prompt: String
        val microAction: String

        // For "Mixed" states (Neutral sentiment but with Complex category), we give a specific prompt
        if (category == "Complex Emotional Landscape") {
            prompt = "You're navigating a mix of $uniqueEmotions. What is one way you can honor both sides of your experience today?"
            microAction = "Sit for 2 minutes and simply breathe, acknowledging all your feelings without judgment."
            return Pair(prompt, microAction)
        }

        when (sentiment) {
            Sentiment.POSITIVE -> {
                prompt = "What specifically about feeling $uniqueEmotions made today stand out?"
                microAction = "Write down one moment of joy from today and keep it nearby."
            }
            Sentiment.NEGATIVE -> {
                prompt = "You're holding a lot ($uniqueEmotions). What is one thing you can let go of tonight?"
                microAction = "Practice 4-7-8 breathing for one minute to reset your nervous system."
            }
            Sentiment.NEUTRAL -> {
                prompt = "With a sense of $uniqueEmotions, what does 'balance' look like for you right now?"
                microAction = "Find a quiet spot and observe your surroundings for 5 minutes."
            }
        }
        return Pair(prompt, microAction)
    }

    /**
     * Main function to process the entry.
     */
    fun processEntry(journalText: String, selectedEmotions: List<String>): AnalysisResult {
        val sentiment = analyzeSentiment(journalText, selectedEmotions)
        val category = classifyOverallMoodCategory(selectedEmotions, sentiment)
        val (prompt, microActionDesc) = generateReflection(sentiment, category, selectedEmotions)

        return AnalysisResult(
            sentiment = sentiment,
            overallMoodCategory = category,
            reflectionPrompt = prompt,
            suggestedMicroActions = listOf(MicroAction("General", microActionDesc))
        )
    }
}