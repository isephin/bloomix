package com.example.bloomix

/**
 * Represents a single entry in our "Dataset" of AI responses.
 */
data class ReflectionEntry(
    val tags: List<String>,       // Emotions this prompt applies to (e.g., "sad", "tired")
    val keywords: List<String>,   // Keywords in journal text this matches (e.g., "work", "sleep", "friend")
    val sentiment: Sentiment,     // The general sentiment this fits
    val prompt: String,           // The personalized question
    val microAction: String       // The small suggested action
)

object ReflectionData {

    val dataset = listOf(
        // --- HAPPY / POSITIVE ---
        ReflectionEntry(
            listOf("happy", "excited"), listOf("work", "project", "job", "career"), Sentiment.POSITIVE,
            "It sounds like you're thriving in your efforts. How can you carry this momentum into your next challenge?",
            "Write down one professional win from today."
        ),
        ReflectionEntry(
            listOf("loved", "happy"), listOf("friend", "family", "partner", "mom", "dad"), Sentiment.POSITIVE,
            "Connection is such a powerful fuel. How did this person make you feel seen today?",
            "Send a text saying 'I appreciate you' to them right now."
        ),
        ReflectionEntry(
            listOf("calm", "happy"), listOf("morning", "walk", "nature", "sun"), Sentiment.POSITIVE,
            "Nature often grounds us. What small detail in your environment brought you peace today?",
            "Take a photo of something beautiful tomorrow."
        ),
        // Fallback Happy
        ReflectionEntry(
            listOf("happy", "excited"), listOf(), Sentiment.POSITIVE,
            "Your energy is contagious today! How can you channel this excitement into a passion you care about?",
            "Share your good news with one friend right now."
        ),

        // --- SAD / NEGATIVE ---
        ReflectionEntry(
            listOf("sad", "tired"), listOf("sleep", "night", "bed", "insomnia"), Sentiment.NEGATIVE,
            "Rest is productive too. If you could give yourself permission to just 'be' tonight, what would that look like?",
            "Turn off your phone 30 minutes before bed tonight."
        ),
        ReflectionEntry(
            listOf("angry", "annoyed"), listOf("boss", "work", "colleague", "email"), Sentiment.NEGATIVE,
            "Work frustration is valid. Is this a temporary hurdle or a sign of a boundary that needs setting?",
            "Close your eyes and visualize leaving work stress at the door."
        ),
        ReflectionEntry(
            listOf("sad", "lonely"), listOf("friend", "alone", "miss"), Sentiment.NEGATIVE,
            "Loneliness is just a signal for connection. Who is one person you haven't spoken to in a while?",
            "Reach out to an old friend just to say hello."
        ),
        // Fallback Sad
        ReflectionEntry(
            listOf("sad", "tired"), listOf(), Sentiment.NEGATIVE,
            "It sounds like a heavy day. If you were treating yourself like a small child right now, what would you do for them?",
            "Put on your most comfortable clothes and drink a glass of water."
        ),

        // --- MIXED / NEUTRAL ---
        ReflectionEntry(
            listOf("confused", "anxious"), listOf("decision", "choice", "future"), Sentiment.NEUTRAL,
            "Big decisions are heavy. If you trusted your gut instinct without logic for a second, what does it say?",
            "Write down the best-case scenario for your decision."
        ),
        // Fallback Mixed
        ReflectionEntry(
            listOf("happy", "sad"), listOf(), Sentiment.NEUTRAL,
            "It's fully possible to hold joy and grief at the same time. How are these two feelings co-existing for you right now?",
            "Place one hand on your heart and acknowledge both feelings."
        )
    )

    /**
     * Smart Search: Finds the best prompt based on Emotions, Sentiment, AND Journal Text.
     */
    fun getReflectionFor(emotions: List<String>, sentiment: Sentiment, journalText: String): ReflectionEntry {
        val lowerText = journalText.lowercase()

        // Score every entry in the dataset
        val bestMatch = dataset.maxByOrNull { entry ->
            var score = 0

            // 1. Match Sentiment (High priority)
            if (entry.sentiment == sentiment) score += 10

            // 2. Match Emotions (Medium priority)
            val emotionMatches = entry.tags.count { emotions.contains(it) }
            score += (emotionMatches * 5)

            // 3. Match Journal Keywords (Highest priority for personalization)
            val keywordMatches = entry.keywords.count { lowerText.contains(it) }
            score += (keywordMatches * 15) // High weight ensures text context wins

            score
        }

        // Return the best match, or a generic default if the score is too low
        return bestMatch ?: ReflectionEntry(
            listOf(), listOf(), Sentiment.NEUTRAL,
            "Take a moment to simply breathe and be present with whatever you are feeling.",
            "Take three deep breaths."
        )
    }
}