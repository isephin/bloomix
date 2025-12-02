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
            listOf("happy", "proud", "confident"), listOf("school", "exam", "test", "grade", "study", "passed"), Sentiment.POSITIVE,
            "Your hard work is paying off. What specific study habit or mindset helped you succeed today?",
            "Take a 15-minute break to do something purely for fun."
        ),
        ReflectionEntry(
            listOf("excited", "happy"), listOf("goal", "dream", "plan", "future", "idea"), Sentiment.POSITIVE,
            "This excitement is a signal. What is one small step you can take tomorrow to keep moving toward this dream?",
            "Create a vision board or write down your next 3 steps."
        ),
        ReflectionEntry(
            listOf("happy", "motivated"), listOf("finish", "done", "complete", "task", "cleaned"), Sentiment.POSITIVE,
            "Completion brings such relief. Take a moment to acknowledge that you did what you set out to do.",
            "Physically cross the item off your list with a bold pen."
        ),

        // Relationships / Connection
        ReflectionEntry(
            listOf("loved", "happy", "grateful"), listOf("friend", "bestie", "hangout", "laugh", "funny"), Sentiment.POSITIVE,
            "Laughter is medicine. How did connecting with your friend change your perspective today?",
            "Send a text saying 'I appreciate you' to them right now."
        ),
        ReflectionEntry(
            listOf("loved", "calm", "safe"), listOf("partner", "boyfriend", "girlfriend", "spouse", "date", "love"), Sentiment.POSITIVE,
            "Feeling safe and loved is a beautiful baseline. What is one thing you appreciate about your partner today?",
            "Plan a small surprise for them this week."
        ),
        ReflectionEntry(
            listOf("loved", "grateful"), listOf("family", "mom", "dad", "sister", "brother", "home", "parents"), Sentiment.POSITIVE,
            "Family can be a source of great strength. What family tradition or memory are you grateful for today?",
            "Call a family member just to say hello."
        ),
        ReflectionEntry(
            listOf("happy", "connected"), listOf("talk", "chat", "conversation", "call", "message"), Sentiment.POSITIVE,
            "Good conversations feed the soul. What was the most memorable thing said today?",
            "Write down one quote from your conversation."
        ),
        ReflectionEntry(
            listOf("grateful", "loved"), listOf("pet", "dog", "cat", "puppy", "kitten"), Sentiment.POSITIVE,
            "Animals offer the purest kind of love. How did your pet make you smile today?",
            "Give your pet an extra 5 minutes of undivided attention."
        ),

        // Nature / Peace
        ReflectionEntry(
            listOf("calm", "happy", "peaceful"), listOf("morning", "walk", "nature", "sun", "outside", "park", "sky"), Sentiment.POSITIVE,
            "Nature often grounds us. What small detail in your environment brought you peace today?",
            "Take a photo of something beautiful tomorrow."
        ),
        ReflectionEntry(
            listOf("calm", "relaxed"), listOf("rain", "book", "coffee", "tea", "cozy", "reading"), Sentiment.POSITIVE,
            "Slow moments are necessary for recharge. How can you protect this feeling of coziness for the rest of the week?",
            "Read 5 more pages of your book."
        ),
        ReflectionEntry(
            listOf("peaceful", "calm"), listOf("night", "stars", "moon", "quiet", "sleep"), Sentiment.POSITIVE,
            "The night offers a unique stillness. What thoughts come to you when the world is quiet?",
            "Spend 2 minutes looking out the window before bed."
        ),

        // Self-Care / Body
        ReflectionEntry(
            listOf("grateful", "happy"), listOf("food", "meal", "dinner", "lunch", "cooked", "delicious", "ate"), Sentiment.POSITIVE,
            "Nourishing our bodies is a form of self-love. What was the best flavor you experienced today?",
            "Savor your next meal without distractions for the first 5 minutes."
        ),
        ReflectionEntry(
            listOf("strong", "proud", "happy"), listOf("gym", "workout", "run", "exercise", "yoga", "sport"), Sentiment.POSITIVE,
            "Moving your body changes your mind. How do you feel physically right now compared to before you moved?",
            "Stretch for 5 minutes before bed."
        ),
        ReflectionEntry(
            listOf("clean", "fresh"), listOf("shower", "bath", "clean", "routine", "skin"), Sentiment.POSITIVE,
            "Cleaning the body often cleanses the mind. How do you feel now that you've reset?",
            "Put on your most comfortable clothes."
        ),
        ReflectionEntry(
            listOf("creative", "inspired"), listOf("art", "draw", "paint", "write", "music", "sing"), Sentiment.POSITIVE,
            "Creativity is the soul speaking. What did you express today that words couldn't say?",
            "Keep your creative work visible on your desk."
        ),

        // Travel / Adventure
        ReflectionEntry(
            listOf("excited", "joyful", "curious"), listOf("travel", "trip", "vacation", "journey", "flight", "drive"), Sentiment.POSITIVE,
            "New experiences expand our world. What are you most looking forward to discovering on this trip?",
            "Look up one interesting fact about your destination."
        ),
        ReflectionEntry(
            listOf("happy", "grateful"), listOf("birthday", "party", "gift", "celebrate", "cake"), Sentiment.POSITIVE,
            "Celebrations mark the passage of time with joy. What is your wish for the coming year?",
            "Take a photo of yourself to remember this happy moment."
        ),

        // ==========================================
        // SAD / NEGATIVE / LOW ENERGY
        // ==========================================

        // Sadness / Loneliness
        ReflectionEntry(
            listOf("sad", "lonely", "isolated"), listOf("friend", "alone", "miss", "breakup", "left", "ignore"), Sentiment.NEGATIVE,
            "Loneliness is just a signal for connection. Who is one person you haven't spoken to in a while that feels safe?",
            "Reach out to an old friend just to say hello."
        ),
        ReflectionEntry(
            listOf("sad", "grief", "heartbroken"), listOf("loss", "miss", "remember", "gone", "death", "died"), Sentiment.NEGATIVE,
            "Grief is love with nowhere to go. What is a favorite memory you have of what you lost?",
            "Light a candle or sit quietly for a moment in honor of your memory."
        ),
        ReflectionEntry(
            listOf("sad", "tired"), listOf("crying", "tears", "heavy", "dark", "hopeless"), Sentiment.NEGATIVE,
            "It's okay not to be okay. If you could speak kindly to yourself right now, what would you say?",
            "Wrap yourself in a warm blanket and just breathe."
        ),
        ReflectionEntry(
            listOf("sad", "disappointed"), listOf("fail", "mistake", "wrong", "bad", "stupid"), Sentiment.NEGATIVE,
            "Mistakes are proof that you are trying. What is one lesson you can take from this without judging yourself?",
            "Write 'I am learning' on a piece of paper."
        ),
        ReflectionEntry(
            listOf("sad", "insecure"), listOf("ugly", "fat", "body", "look", "mirror"), Sentiment.NEGATIVE,
            "Your worth is not defined by your appearance. What is one thing your body allows you to DO that you are grateful for?",
            "Put a sticky note on your mirror that says 'I am enough'."
        ),

        // Stress / Overwhelm
        ReflectionEntry(
            listOf("stressed", "overwhelmed", "panic"), listOf("time", "busy", "deadline", "schedule", "too much", "late"), Sentiment.NEGATIVE,
            "You are carrying a lot. What is one task you can realistically drop, delay, or delegate today?",
            "Take a 5-minute break to do absolutely nothing."
        ),
        ReflectionEntry(
            listOf("anxious", "scared", "nervous"), listOf("future", "unknown", "worry", "what if", "scared"), Sentiment.NEGATIVE,
            "Anxiety often tries to predict the future. What is one thing you know for sure is true right now in this room?",
            "Write down 3 things you can control and 3 things you cannot."
        ),
        ReflectionEntry(
            listOf("shocked", "confused"), listOf("news", "unexpected", "change", "happened", "sudden"), Sentiment.NEGATIVE,
            "Sudden changes are jarring. What is one anchor in your life that remains stable?",
            "Drink a glass of water slowly to ground yourself."
        ),
        ReflectionEntry(
            listOf("stressed", "tired"), listOf("money", "bill", "cost", "expensive", "broke"), Sentiment.NEGATIVE,
            "Financial stress is heavy. Instead of the big picture, what is one small budget choice you can make today?",
            "Track your spending for just today."
        ),

        // Anger / Frustration
        ReflectionEntry(
            listOf("angry", "annoyed", "frustrated"), listOf("boss", "work", "colleague", "email", "meeting", "coworker"), Sentiment.NEGATIVE,
            "Work frustration is valid. Is this a temporary hurdle or a sign of a boundary that needs setting?",
            "Close your eyes and visualize leaving work stress at the door."
        ),
        ReflectionEntry(
            listOf("angry", "betrayed"), listOf("lie", "friend", "trust", "secret", "fake"), Sentiment.NEGATIVE,
            "Anger often protects us from hurt. What boundary was crossed today that made you feel this way?",
            "Write an angry letter, then tear it up immediately."
        ),
        ReflectionEntry(
            listOf("annoyed", "bored"), listOf("traffic", "line", "waiting", "slow", "driving"), Sentiment.NEGATIVE,
            "Patience is hard when we feel wasted time. Can you reframe this waiting time as 'me time'?",
            "Listen to one of your favorite songs."
        ),
        ReflectionEntry(
            listOf("angry", "annoyed"), listOf("noise", "loud", "neighbor", "quiet"), Sentiment.NEGATIVE,
            "External chaos can disrupt inner peace. Can you put on headphones or find a quieter room for 10 minutes?",
            "Listen to white noise or rain sounds."
        ),

        // Physical / Fatigue
        ReflectionEntry(
            listOf("tired", "exhausted"), listOf("sleep", "night", "bed", "insomnia", "awake", "tired"), Sentiment.NEGATIVE,
            "Rest is productive too. If you could give yourself permission to just 'be' tonight, what would that look like?",
            "Turn off your phone 30 minutes before bed tonight."
        ),
        ReflectionEntry(
            listOf("tired", "sick"), listOf("pain", "headache", "sick", "ill", "body", "stomach"), Sentiment.NEGATIVE,
            "Your body needs you right now. What is the kindest thing you can do for your physical self?",
            "Drink a large glass of water and lie down."
        ),

        // ==========================================
        // MIXED / NEUTRAL / COMPLEX
        // ==========================================

        // Indecision
        ReflectionEntry(
            listOf("confused", "anxious", "uncertain"), listOf("decision", "choice", "future", "path", "option"), Sentiment.NEUTRAL,
            "Big decisions are heavy. If you trusted your gut instinct without logic for a second, what does it say?",
            "Write down the best-case scenario for your decision."
        ),

        // Boredom / Routine
        ReflectionEntry(
            listOf("bored", "calm"), listOf("nothing", "routine", "same", "day", "normal", "boring"), Sentiment.NEUTRAL,
            "Sometimes quiet days are necessary for growth. Is there a small hobby you've been meaning to pick up?",
            "Read 5 pages of a book you've been ignoring."
        ),
        ReflectionEntry(
            listOf("bored", "tired"), listOf("watch", "tv", "movie", "show", "scroll"), Sentiment.NEUTRAL,
            "Passive consumption can sometimes drain us more. Would creating something small make you feel better?",
            "Doodle or write a short poem."
        ),

        // Nostalgia
        ReflectionEntry(
            listOf("nostalgic", "sad", "happy"), listOf("past", "childhood", "memory", "old", "photo", "remember"), Sentiment.NEUTRAL,
            "Looking back can be bittersweet. What lesson from that time are you carrying with you today?",
            "Look at an old photo that brings you a smile."
        ),

        // Mixed Emotions
        ReflectionEntry(
            listOf("happy", "sad"), listOf("change", "moving", "leaving", "goodbye", "graduate"), Sentiment.NEUTRAL,
            "It's fully possible to hold joy and grief at the same time. How are these two feelings co-existing for you right now?",
            "Place one hand on your heart and acknowledge both feelings."
        ),
        ReflectionEntry(
            listOf("excited", "nervous"), listOf("start", "begin", "new", "job", "school", "interview"), Sentiment.NEUTRAL,
            "Excitement and nervousness are two sides of the same coin. What is the best thing that could happen?",
            "Visualize yourself succeeding for 1 minute."
        ),
        ReflectionEntry(
            listOf("tired", "happy"), listOf("party", "event", "social", "people", "crowd"), Sentiment.NEUTRAL,
            "A 'good tired' is a sign of a day well spent. What drained your energy but filled your cup today?",
            "Do a gentle stretch to release physical tension before bed."
        ),

        // ==========================================
        // FALLBACKS (Generic but thoughtful)
        // ==========================================
        ReflectionEntry(
            listOf("happy"), listOf(), Sentiment.POSITIVE,
            "Your joy is a beautiful signal. What was the highlight of your day?",
            "Smile at yourself in the mirror for 5 seconds."
        ),
        ReflectionEntry(
            listOf("excited"), listOf(), Sentiment.POSITIVE,
            "Excitement is energy! What are you most looking forward to right now?",
            "Write down one goal you are excited about."
        ),
        ReflectionEntry(
            listOf("loved"), listOf(), Sentiment.POSITIVE,
            "Feeling loved is grounding. Who makes you feel safe to be yourself?",
            "Hug someone or something (like a pet or pillow)."
        ),
        ReflectionEntry(
            listOf("calm"), listOf(), Sentiment.POSITIVE,
            "Peace is a rare gift. How does your body feel when you are this calm?",
            "Sit in silence for 2 minutes and enjoy it."
        ),
        ReflectionEntry(
            listOf("sad"), listOf(), Sentiment.NEGATIVE,
            "Heavy feelings are visitors, not residents. What do you need most right now?",
            "Wrap yourself in a warm blanket."
        ),
        ReflectionEntry(
            listOf("tired"), listOf(), Sentiment.NEGATIVE,
            "Your energy is low. What is one thing you can say 'no' to today?",
            "Close your eyes for 60 seconds."
        ),
        ReflectionEntry(
            listOf("angry"), listOf(), Sentiment.NEGATIVE,
            "Anger protects us. What boundary felt crossed today?",
            "Squeeze a stress ball or pillow."
        ),
        ReflectionEntry(
            listOf("anxious"), listOf(), Sentiment.NEGATIVE,
            "Anxiety often worries about the future. What is true right now, in this moment?",
            "Name 3 things you can see in the room."
        ),
        ReflectionEntry(
            listOf("stressed"), listOf(), Sentiment.NEGATIVE,
            "Stress means you care about the outcome. What is the worst that could happen, and could you handle it?",
            "Take three deep breaths."
        ),
        ReflectionEntry(
            listOf("bored"), listOf(), Sentiment.NEGATIVE,
            "Boredom can be a call for creativity or rest. Which one do you think it is?",
            "Doodle on a piece of paper for 2 minutes."
        ),
        ReflectionEntry(
            listOf("confused"), listOf(), Sentiment.NEUTRAL,
            "Clarity often comes with time. What implies a 'maybe' in your life right now?",
            "Write down one question you have for yourself."
        ),
        ReflectionEntry(
            listOf("shocked"), listOf(), Sentiment.NEUTRAL,
            "Surprises can be overwhelming. Take a moment to let the information settle.",
            "Drink a glass of water slowly."
        ),

        // Ultimate Fallback
        ReflectionEntry(
            listOf(), listOf(), Sentiment.NEUTRAL,
            "Take a moment to simply breathe and be present with whatever you are feeling.",
            "Take three deep breaths."
        )
    )

    /**
     * Smart Search: Finds the best prompt.
     * FIX: If journalText is empty, it strictly avoids keyword-based prompts.
     */
    fun getReflectionFor(emotions: List<String>, sentiment: Sentiment, journalText: String): ReflectionEntry {
        val lowerText = journalText.lowercase().trim()
        val isTextEmpty = lowerText.isEmpty()

        val bestMatch = dataset.maxByOrNull { entry ->
            // CRITICAL FIX: If user didn't write anything, exclude prompts that require keywords
            if (isTextEmpty && entry.keywords.isNotEmpty()) return@maxByOrNull -100

            var score = 0

            // 1. Match Sentiment
            if (entry.sentiment == sentiment) score += 10

            // 2. Match Emotions
            val emotionMatches = entry.tags.count { emotions.contains(it) }
            score += (emotionMatches * 5)

            // 3. Match Journal Keywords (Only if text exists)
            if (!isTextEmpty) {
                val keywordMatches = entry.keywords.count { lowerText.contains(it) }
                score += (keywordMatches * 15)
            }

            score
        }

        return bestMatch ?: dataset.last()
    }
}