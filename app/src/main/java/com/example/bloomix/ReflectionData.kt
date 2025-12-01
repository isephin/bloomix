package com.example.bloomix

/**
 * Represents a single entry in our "Dataset" of AI responses.
 */
data class ReflectionEntry(
    val tags: List<String>,       // Emotions this prompt applies to
    val keywords: List<String>,   // Keywords in journal text this matches
    val sentiment: Sentiment,     // The general sentiment this fits
    val prompt: String,           // The personalized question
    val microAction: String       // The small suggested action
)

object ReflectionData {

    val dataset = listOf(
        // ==========================================
        // HAPPY / POSITIVE / HIGH ENERGY
        // ==========================================

        // Achievement / Work / School
        ReflectionEntry(
            listOf("happy", "excited", "proud"), listOf("work", "project", "job", "career", "promotion"), Sentiment.POSITIVE,
            "It sounds like you're thriving in your efforts. How can you carry this momentum into your next challenge?",
            "Write down one professional win from today."
        ),
        ReflectionEntry(
            listOf("happy", "proud", "confident"), listOf("school", "exam", "test", "grade", "study"), Sentiment.POSITIVE,
            "Your hard work is paying off. What specific study habit or mindset helped you succeed today?",
            "Take a 15-minute break to do something purely for fun."
        ),
        ReflectionEntry(
            listOf("excited", "happy"), listOf("goal", "dream", "plan", "future"), Sentiment.POSITIVE,
            "This excitement is a signal. What is one small step you can take tomorrow to keep moving toward this dream?",
            "Create a vision board or write down your next 3 steps."
        ),

        // Relationships / Connection
        ReflectionEntry(
            listOf("loved", "happy", "grateful"), listOf("friend", "bestie", "hangout", "laugh"), Sentiment.POSITIVE,
            "Laughter is medicine. How did connecting with your friend change your perspective today?",
            "Send a text saying 'I appreciate you' to them right now."
        ),
        ReflectionEntry(
            listOf("loved", "calm", "safe"), listOf("partner", "boyfriend", "girlfriend", "spouse", "date"), Sentiment.POSITIVE,
            "Feeling safe and loved is a beautiful baseline. What is one thing you appreciate about your partner today?",
            "Plan a small surprise for them this week."
        ),
        ReflectionEntry(
            listOf("loved", "grateful"), listOf("family", "mom", "dad", "sister", "brother", "home"), Sentiment.POSITIVE,
            "Family can be a source of great strength. What family tradition or memory are you grateful for today?",
            "Call a family member just to say hello."
        ),

        // Nature / Peace
        ReflectionEntry(
            listOf("calm", "happy", "peaceful"), listOf("morning", "walk", "nature", "sun", "outside", "park"), Sentiment.POSITIVE,
            "Nature often grounds us. What small detail in your environment brought you peace today?",
            "Take a photo of something beautiful tomorrow."
        ),
        ReflectionEntry(
            listOf("calm", "relaxed"), listOf("rain", "book", "coffee", "tea", "cozy"), Sentiment.POSITIVE,
            "Slow moments are necessary for recharge. How can you protect this feeling of coziness for the rest of the week?",
            "Read 5 more pages of your book."
        ),

        // Self-Care / Body
        ReflectionEntry(
            listOf("grateful", "happy"), listOf("food", "meal", "dinner", "lunch", "cooked"), Sentiment.POSITIVE,
            "Nourishing our bodies is a form of self-love. What was the best flavor you experienced today?",
            "Savor your next meal without distractions for the first 5 minutes."
        ),
        ReflectionEntry(
            listOf("strong", "proud", "happy"), listOf("gym", "workout", "run", "exercise", "yoga"), Sentiment.POSITIVE,
            "Moving your body changes your mind. How do you feel physically right now compared to before you moved?",
            "Stretch for 5 minutes before bed."
        ),

        // Travel / Adventure
        ReflectionEntry(
            listOf("excited", "joyful", "curious"), listOf("travel", "trip", "vacation", "journey", "flight"), Sentiment.POSITIVE,
            "New experiences expand our world. What are you most looking forward to discovering on this trip?",
            "Look up one interesting fact about your destination."
        ),

        // ==========================================
        // SAD / NEGATIVE / LOW ENERGY
        // ==========================================

        // Sadness / Loneliness
        ReflectionEntry(
            listOf("sad", "lonely", "isolated"), listOf("friend", "alone", "miss", "breakup", "left"), Sentiment.NEGATIVE,
            "Loneliness is just a signal for connection. Who is one person you haven't spoken to in a while that feels safe?",
            "Reach out to an old friend just to say hello."
        ),
        ReflectionEntry(
            listOf("sad", "grief", "heartbroken"), listOf("loss", "miss", "remember", "gone", "death"), Sentiment.NEGATIVE,
            "Grief is love with nowhere to go. What is a favorite memory you have of what you lost?",
            "Light a candle or sit quietly for a moment in honor of your memory."
        ),
        ReflectionEntry(
            listOf("sad", "tired"), listOf("crying", "tears", "heavy", "dark"), Sentiment.NEGATIVE,
            "It's okay not to be okay. If you could speak kindly to yourself right now, what would you say?",
            "Wrap yourself in a warm blanket and just breathe."
        ),

        // Stress / Overwhelm
        ReflectionEntry(
            listOf("stressed", "overwhelmed", "panic"), listOf("time", "busy", "deadline", "schedule", "too much"), Sentiment.NEGATIVE,
            "You are carrying a lot. What is one task you can realistically drop, delay, or delegate today?",
            "Take a 5-minute break to do absolutely nothing."
        ),
        ReflectionEntry(
            listOf("anxious", "scared", "nervous"), listOf("future", "unknown", "worry", "what if"), Sentiment.NEGATIVE,
            "Anxiety often tries to predict the future. What is one thing you know for sure is true right now in this room?",
            "Write down 3 things you can control and 3 things you cannot."
        ),
        ReflectionEntry(
            listOf("shocked", "confused"), listOf("news", "unexpected", "change", "happened"), Sentiment.NEGATIVE,
            "Sudden changes are jarring. What is one anchor in your life that remains stable?",
            "Drink a glass of water slowly to ground yourself."
        ),

        // Anger / Frustration
        ReflectionEntry(
            listOf("angry", "annoyed", "frustrated"), listOf("boss", "work", "colleague", "email", "meeting"), Sentiment.NEGATIVE,
            "Work frustration is valid. Is this a temporary hurdle or a sign of a boundary that needs setting?",
            "Close your eyes and visualize leaving work stress at the door."
        ),
        ReflectionEntry(
            listOf("angry", "betrayed"), listOf("lie", "friend", "trust", "secret"), Sentiment.NEGATIVE,
            "Anger often protects us from hurt. What boundary was crossed today that made you feel this way?",
            "Write an angry letter, then tear it up immediately."
        ),
        ReflectionEntry(
            listOf("annoyed", "bored"), listOf("traffic", "line", "waiting", "slow"), Sentiment.NEGATIVE,
            "Patience is hard when we feel wasted time. Can you reframe this waiting time as 'me time'?",
            "Listen to one of your favorite songs."
        ),

        // Physical / Fatigue
        ReflectionEntry(
            listOf("tired", "exhausted"), listOf("sleep", "night", "bed", "insomnia", "awake"), Sentiment.NEGATIVE,
            "Rest is productive too. If you could give yourself permission to just 'be' tonight, what would that look like?",
            "Turn off your phone 30 minutes before bed tonight."
        ),
        ReflectionEntry(
            listOf("tired", "sick"), listOf("pain", "headache", "sick", "ill", "body"), Sentiment.NEGATIVE,
            "Your body needs you right now. What is the kindest thing you can do for your physical self?",
            "Drink a large glass of water and lie down."
        ),

        // ==========================================
        // MIXED / NEUTRAL / COMPLEX
        // ==========================================

        // Indecision
        ReflectionEntry(
            listOf("confused", "anxious", "uncertain"), listOf("decision", "choice", "future", "path"), Sentiment.NEUTRAL,
            "Big decisions are heavy. If you trusted your gut instinct without logic for a second, what does it say?",
            "Write down the best-case scenario for your decision."
        ),

        // Boredom / Routine
        ReflectionEntry(
            listOf("bored", "calm"), listOf("nothing", "routine", "same", "day", "normal"), Sentiment.NEUTRAL,
            "Sometimes quiet days are necessary for growth. Is there a small hobby you've been meaning to pick up?",
            "Read 5 pages of a book you've been ignoring."
        ),

        // Nostalgia
        ReflectionEntry(
            listOf("nostalgic", "sad", "happy"), listOf("past", "childhood", "memory", "old", "photo"), Sentiment.NEUTRAL,
            "Looking back can be bittersweet. What lesson from that time are you carrying with you today?",
            "Look at an old photo that brings you a smile."
        ),

        // Mixed Emotions
        ReflectionEntry(
            listOf("happy", "sad"), listOf("change", "moving", "leaving", "goodbye"), Sentiment.NEUTRAL,
            "It's fully possible to hold joy and grief at the same time. How are these two feelings co-existing for you right now?",
            "Place one hand on your heart and acknowledge both feelings."
        ),
        ReflectionEntry(
            listOf("excited", "nervous"), listOf("start", "begin", "new", "job", "school"), Sentiment.NEUTRAL,
            "Excitement and nervousness are two sides of the same coin. What is the best thing that could happen?",
            "Visualize yourself succeeding for 1 minute."
        ),
        ReflectionEntry(
            listOf("tired", "happy"), listOf("party", "event", "social", "people"), Sentiment.NEUTRAL,
            "A 'good tired' is a sign of a day well spent. What drained your energy but filled your cup today?",
            "Do a gentle stretch to release physical tension before bed."
        ),

        // ==========================================
        // FALLBACKS (Generic but thoughtful)
        // ==========================================
        ReflectionEntry(
            listOf("happy"), listOf(), Sentiment.POSITIVE,
            "Your joy is valid and beautiful. What caused this spark of happiness?",
            "Smile at yourself in the mirror."
        ),
        ReflectionEntry(
            listOf("sad"), listOf(), Sentiment.NEGATIVE,
            "Heavy feelings are visitors, not residents. What do you need most right now?",
            "Take three deep breaths."
        ),
        ReflectionEntry(
            listOf("calm"), listOf(), Sentiment.NEUTRAL,
            "Peace is a rare gift. How can you extend this feeling into tomorrow?",
            "Sit in silence for 2 minutes."
        ),
        ReflectionEntry(
            listOf(), listOf(), Sentiment.NEUTRAL,
            "Take a moment to simply breathe and be present with whatever you are feeling.",
            "Take three deep breaths."
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
        return bestMatch ?: dataset.last()
    }
}