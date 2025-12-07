package com.example.bloomix

/**
 * Represents a single "Rule" or "Entry" in our reflection engine.
 * The system checks if the user's input matches the criteria (tags, keywords, sentiment) defined here.
 * It acts as a bridge between the raw analysis results and the user-facing content.
 */
data class ReflectionEntry(
    val tags: List<String>,       // List of emotion tags this prompt applies to (e.g., "sad", "lonely")
    val keywords: List<String>,   // Specific words to look for in the user's journal text (e.g., "breakup")
    val sentiment: Sentiment,     // The general sentiment required for this match (e.g., NEGATIVE)
    val prompt: String,           // The personalized question/reflection to display to the user
    val microAction: String       // The small, actionable task to suggest
)

/**
 * Singleton object that acts as the "Knowledge Base" for the AI.
 * It contains the data and the logic to retrieve the best match.
 * This centralized object allows for easy updates to the content without changing the core logic.
 */
object ReflectionData {

    // A hardcoded "Database" of all possible responses the AI can give.
    // This replaces the need for an external API (like ChatGPT), making the app work offline.
    // Each entry is a specific rule that maps emotions/text to a thoughtful response.
    val dataset = listOf(
        // ==========================================
        // HAPPY / POSITIVE / HIGH ENERGY
        // ==========================================

        // Category: Achievement / Work / School
        // Focuses on celebrating success and reinforcing positive momentum.
        ReflectionEntry(
            listOf("happy", "excited", "proud"),
            listOf("work", "project", "job", "career", "promotion"),
            Sentiment.POSITIVE,
            "It sounds like you're thriving in your efforts. How can you carry this momentum into your next challenge?",
            "Write down one professional win from today."
        ),
        ReflectionEntry(
            listOf("happy", "proud", "confident"),
            listOf("school", "exam", "test", "grade", "study", "passed"),
            Sentiment.POSITIVE,
            "Your hard work is paying off. What specific study habit or mindset helped you succeed today?",
            "Take a 15-minute break to do something purely for fun."
        ),
        ReflectionEntry(
            listOf("excited", "happy"),
            listOf("goal", "dream", "plan", "future", "idea"),
            Sentiment.POSITIVE,
            "This excitement is a signal. What is one small step you can take tomorrow to keep moving toward this dream?",
            "Create a vision board or write down your next 3 steps."
        ),
        ReflectionEntry(
            listOf("happy", "motivated"),
            listOf("finish", "done", "complete", "task", "cleaned"),
            Sentiment.POSITIVE,
            "Completion brings such relief. Take a moment to acknowledge that you did what you set out to do.",
            "Physically cross the item off your list with a bold pen."
        ),

        // NEW: Tired but Happy (Positive Fatigue)
        // Addresses the "good tired" feeling after a productive or fun day.
        ReflectionEntry(
            listOf("happy", "tired", "satisfied", "proud"),
            listOf("long day", "busy", "work", "gym", "party", "social"),
            Sentiment.POSITIVE,
            "A 'good tired' is the sign of a day fully lived. You gave your energy to things that matter.",
            "Rest deeply tonight knowing you did enough."
        ),

        // Category: Relationships / Connection
        // Focuses on gratitude for social bonds and deepening connections.
        ReflectionEntry(
            listOf("loved", "happy", "grateful"),
            listOf("friend", "bestie", "hangout", "laugh", "funny"),
            Sentiment.POSITIVE,
            "Laughter is medicine. How did connecting with your friend change your perspective today?",
            "Send a text saying 'I appreciate you' to them right now."
        ),
        ReflectionEntry(
            listOf("loved", "calm", "safe"),
            listOf("partner", "boyfriend", "girlfriend", "spouse", "date", "love"),
            Sentiment.POSITIVE,
            "Feeling safe and loved is a beautiful baseline. What is one thing you appreciate about your partner today?",
            "Plan a small surprise for them this week."
        ),
        ReflectionEntry(
            listOf("loved", "grateful"),
            listOf("family", "mom", "dad", "sister", "brother", "home", "parents"),
            Sentiment.POSITIVE,
            "Family can be a source of great strength. What family tradition or memory are you grateful for today?",
            "Call a family member just to say hello."
        ),
        ReflectionEntry(
            listOf("happy", "connected"),
            listOf("talk", "chat", "conversation", "call", "message"),
            Sentiment.POSITIVE,
            "Good conversations feed the soul. What was the most memorable thing said today?",
            "Write down one quote from your conversation."
        ),
        ReflectionEntry(
            listOf("grateful", "loved"),
            listOf("pet", "dog", "cat", "puppy", "kitten"),
            Sentiment.POSITIVE,
            "Animals offer the purest kind of love. How did your pet make you smile today?",
            "Give your pet an extra 5 minutes of undivided attention."
        ),

        // Category: Nature / Peace
        // Encourages mindfulness and appreciation of the environment.
        ReflectionEntry(
            listOf("calm", "happy", "peaceful"),
            listOf("morning", "walk", "nature", "sun", "outside", "park", "sky"),
            Sentiment.POSITIVE,
            "Nature often grounds us. What small detail in your environment brought you peace today?",
            "Take a photo of something beautiful tomorrow."
        ),
        ReflectionEntry(
            listOf("calm", "relaxed"),
            listOf("rain", "book", "coffee", "tea", "cozy", "reading"),
            Sentiment.POSITIVE,
            "Slow moments are necessary for recharge. How can you protect this feeling of coziness for the rest of the week?",
            "Read 5 more pages of your book."
        ),
        ReflectionEntry(
            listOf("peaceful", "calm"),
            listOf("night", "stars", "moon", "quiet", "sleep"),
            Sentiment.POSITIVE,
            "The night offers a unique stillness. What thoughts come to you when the world is quiet?",
            "Spend 2 minutes looking out the window before bed."
        ),

        // Category: Self-Care / Body
        // Validates physical health and self-care routines.
        ReflectionEntry(
            listOf("grateful", "happy"),
            listOf("food", "meal", "dinner", "lunch", "cooked", "delicious", "ate"),
            Sentiment.POSITIVE,
            "Nourishing our bodies is a form of self-love. What was the best flavor you experienced today?",
            "Savor your next meal without distractions for the first 5 minutes."
        ),
        ReflectionEntry(
            listOf("strong", "proud", "happy"),
            listOf("gym", "workout", "run", "exercise", "yoga", "sport"),
            Sentiment.POSITIVE,
            "Moving your body changes your mind. How do you feel physically right now compared to before you moved?",
            "Stretch for 5 minutes before bed."
        ),
        ReflectionEntry(
            listOf("clean", "fresh"),
            listOf("shower", "bath", "clean", "routine", "skin"),
            Sentiment.POSITIVE,
            "Cleaning the body often cleanses the mind. How do you feel now that you've reset?",
            "Put on your most comfortable clothes."
        ),
        ReflectionEntry(
            listOf("creative", "inspired"),
            listOf("art", "draw", "paint", "write", "music", "sing"),
            Sentiment.POSITIVE,
            "Creativity is the soul speaking. What did you express today that words couldn't say?",
            "Keep your creative work visible on your desk."
        ),

        // Category: Travel / Adventure
        // Focuses on new experiences and celebrating special moments.
        ReflectionEntry(
            listOf("excited", "joyful", "curious"),
            listOf("travel", "trip", "vacation", "journey", "flight", "drive"),
            Sentiment.POSITIVE,
            "New experiences expand our world. What are you most looking forward to discovering on this trip?",
            "Look up one interesting fact about your destination."
        ),
        ReflectionEntry(
            listOf("happy", "grateful"),
            listOf("birthday", "party", "gift", "celebrate", "cake"),
            Sentiment.POSITIVE,
            "Celebrations mark the passage of time with joy. What is your wish for the coming year?",
            "Take a photo of yourself to remember this happy moment."
        ),

        // ==========================================
        // SAD / NEGATIVE / LOW ENERGY
        // ==========================================

        // Category: Sadness / Loneliness
        // Offers comfort and gentle suggestions for connection during hard times.
        ReflectionEntry(
            listOf("sad", "lonely", "isolated"),
            listOf("friend", "alone", "miss", "breakup", "left", "ignore"),
            Sentiment.NEGATIVE,
            "Loneliness is just a signal for connection. Who is one person you haven't spoken to in a while that feels safe?",
            "Reach out to an old friend just to say hello."
        ),
        ReflectionEntry(
            listOf("sad", "grief", "heartbroken"),
            listOf("loss", "miss", "remember", "gone", "death", "died"),
            Sentiment.NEGATIVE,
            "Grief is love with nowhere to go. What is a favorite memory you have of what you lost?",
            "Light a candle or sit quietly for a moment in honor of your memory."
        ),
        ReflectionEntry(
            listOf("sad", "tired"),
            listOf("crying", "tears", "heavy", "dark", "hopeless"),
            Sentiment.NEGATIVE,
            "It's okay not to be okay. If you could speak kindly to yourself right now, what would you say?",
            "Wrap yourself in a warm blanket and just breathe."
        ),
        ReflectionEntry(
            listOf("sad", "disappointed"),
            listOf("fail", "mistake", "wrong", "bad", "stupid"),
            Sentiment.NEGATIVE,
            "Mistakes are proof that you are trying. What is one lesson you can take from this without judging yourself?",
            "Write 'I am learning' on a piece of paper."
        ),
        ReflectionEntry(
            listOf("sad", "insecure"),
            listOf("ugly", "fat", "body", "look", "mirror"),
            Sentiment.NEGATIVE,
            "Your worth is not defined by your appearance. What is one thing your body allows you to DO that you are grateful for?",
            "Put a sticky note on your mirror that says 'I am enough'."
        ),

        // NEW: Empty / Numb
        // Validates the feeling of emptiness as a form of rest, rather than just sadness.
        ReflectionEntry(
            listOf("tired", "bored", "sad"),
            listOf("empty", "numb", "nothing", "void", "blank"),
            Sentiment.NEGATIVE,
            "Sometimes feeling nothing is a way for your mind to rest from feeling too much. Can you just exist for a moment?",
            "Lay down and stare at the ceiling for 2 minutes."
        ),

        // Category: Stress / Overwhelm
        // Provides grounding techniques for anxiety and pressure.
        ReflectionEntry(
            listOf("stressed", "overwhelmed", "panic"),
            listOf("time", "busy", "deadline", "schedule", "too much", "late"),
            Sentiment.NEGATIVE,
            "You are carrying a lot. What is one task you can realistically drop, delay, or delegate today?",
            "Take a 5-minute break to do absolutely nothing."
        ),
        ReflectionEntry(
            listOf("anxious", "scared", "nervous"),
            listOf("future", "unknown", "worry", "what if", "scared"),
            Sentiment.NEGATIVE,
            "Anxiety often tries to predict the future. What is one thing you know for sure is true right now in this room?",
            "Write down 3 things you can control and 3 things you cannot."
        ),
        ReflectionEntry(
            listOf("shocked", "confused"),
            listOf("news", "unexpected", "change", "happened", "sudden"),
            Sentiment.NEGATIVE,
            "Sudden changes are jarring. What is one anchor in your life that remains stable?",
            "Drink a glass of water slowly to ground yourself."
        ),
        ReflectionEntry(
            listOf("stressed", "tired"),
            listOf("money", "bill", "cost", "expensive", "broke"),
            Sentiment.NEGATIVE,
            "Financial stress is heavy. Instead of the big picture, what is one small budget choice you can make today?",
            "Track your spending for just today."
        ),

        // NEW: Specific Stress Triggers
        // Addresses environmental stressors that often go unnoticed but cause significant anxiety.
        ReflectionEntry(
            listOf("stressed", "annoyed", "tired"),
            listOf("noise", "loud", "crowd", "people", "pressure"),
            Sentiment.NEGATIVE,
            "Environmental overstimulation can be draining. Is there a quiet space you can retreat to for just 5 minutes?",
            "Put on headphones with white noise or rain sounds."
        ),

        // Category: Anger / Frustration
        // Helps process anger constructively without suppressing it.
        ReflectionEntry(
            listOf("angry", "annoyed", "frustrated"),
            listOf("boss", "work", "colleague", "email", "meeting", "coworker"),
            Sentiment.NEGATIVE,
            "Work frustration is valid. Is this a temporary hurdle or a sign of a boundary that needs setting?",
            "Close your eyes and visualize leaving work stress at the door."
        ),
        ReflectionEntry(
            listOf("angry", "betrayed"),
            listOf("lie", "friend", "trust", "secret", "fake"),
            Sentiment.NEGATIVE,
            "Anger often protects us from hurt. What boundary was crossed today that made you feel this way?",
            "Write an angry letter, then tear it up immediately."
        ),
        ReflectionEntry(
            listOf("annoyed", "bored"),
            listOf("traffic", "line", "waiting", "slow", "driving"),
            Sentiment.NEGATIVE,
            "Patience is hard when we feel wasted time. Can you reframe this waiting time as 'me time'?",
            "Listen to one of your favorite songs."
        ),
        ReflectionEntry(
            listOf("angry", "annoyed"),
            listOf("noise", "loud", "neighbor", "quiet"),
            Sentiment.NEGATIVE,
            "External chaos can disrupt inner peace. Can you put on headphones or find a quieter room for 10 minutes?",
            "Listen to white noise or rain sounds."
        ),

        // Category: Physical / Fatigue
        // Connects physical sensations to emotional states.
        ReflectionEntry(
            listOf("tired", "exhausted"),
            listOf("sleep", "night", "bed", "insomnia", "awake", "tired"),
            Sentiment.NEGATIVE,
            "Rest is productive too. If you could give yourself permission to just 'be' tonight, what would that look like?",
            "Turn off your phone 30 minutes before bed tonight."
        ),
        ReflectionEntry(
            listOf("tired", "sick"),
            listOf("pain", "headache", "sick", "ill", "body", "stomach"),
            Sentiment.NEGATIVE,
            "Your body needs you right now. What is the kindest thing you can do for your physical self?",
            "Drink a large glass of water and lie down."
        ),

        // NEW: Somatic Symptoms of Stress
        // Helps users recognize when their body is signaling emotional distress.
        ReflectionEntry(
            listOf("stressed", "anxious", "tired"),
            listOf("chest", "tension", "tight", "breath", "shake", "shaking"),
            Sentiment.NEGATIVE,
            "Your body is holding onto tension right now. Let's try to release it physically.",
            "Do a quick body scan: unclench your jaw and drop your shoulders."
        ),

        // ==========================================
        // MIXED / NEUTRAL / COMPLEX
        // ==========================================

        // Category: Indecision
        // Helps users navigate uncertainty.
        ReflectionEntry(
            listOf("confused", "anxious", "uncertain"),
            listOf("decision", "choice", "future", "path", "option"),
            Sentiment.NEUTRAL,
            "Big decisions are heavy. If you trusted your gut instinct without logic for a second, what does it say?",
            "Write down the best-case scenario for your decision."
        ),

        // Category: Boredom / Routine
        // Reframes boredom as an opportunity.
        ReflectionEntry(
            listOf("bored", "calm"),
            listOf("nothing", "routine", "same", "day", "normal", "boring"),
            Sentiment.NEUTRAL,
            "Sometimes quiet days are necessary for growth. Is there a small hobby you've been meaning to pick up?",
            "Read 5 pages of a book you've been ignoring."
        ),
        ReflectionEntry(
            listOf("bored", "tired"),
            listOf("watch", "tv", "movie", "show", "scroll"),
            Sentiment.NEUTRAL,
            "Passive consumption can sometimes drain us more. Would creating something small make you feel better?",
            "Doodle or write a short poem."
        ),

        // Category: Nostalgia
        // Honors past memories without getting stuck in them.
        ReflectionEntry(
            listOf("nostalgic", "sad", "happy"),
            listOf("past", "childhood", "memory", "old", "photo", "remember"),
            Sentiment.NEUTRAL,
            "Looking back can be bittersweet. What lesson from that time are you carrying with you today?",
            "Look at an old photo that brings you a smile."
        ),

        // Category: Mixed Emotions
        // Validates the complexity of feeling multiple things at once.
        ReflectionEntry(
            listOf("happy", "sad"),
            listOf("change", "moving", "leaving", "goodbye", "graduate"),
            Sentiment.NEUTRAL,
            "It's fully possible to hold joy and grief at the same time. How are these two feelings co-existing for you right now?",
            "Place one hand on your heart and acknowledge both feelings."
        ),
        ReflectionEntry(
            listOf("excited", "nervous"),
            listOf("start", "begin", "new", "job", "school", "interview"),
            Sentiment.NEUTRAL,
            "Excitement and nervousness are two sides of the same coin. What is the best thing that could happen?",
            "Visualize yourself succeeding for 1 minute."
        ),
        ReflectionEntry(
            listOf("tired", "happy"),
            listOf("party", "event", "social", "people", "crowd"),
            Sentiment.NEUTRAL,
            "A 'good tired' is a sign of a day well spent. What drained your energy but filled your cup today?",
            "Do a gentle stretch to release physical tension before bed."
        ),

        // NEW: Bittersweet / Growth
        // Specific prompt for the "Complex Emotional Landscape" category involving personal growth.
        ReflectionEntry(
            listOf("happy", "sad", "confused"),
            listOf("growing", "older", "time", "fast", "birthday"),
            Sentiment.NEUTRAL,
            "Growth often involves leaving things behind. What is one thing you are glad to take with you into this new chapter?",
            "Write a one-sentence wish for your future self."
        ),

        // NEW: Complex Anxiety (Excited but Anxious)
        // Addresses high-stakes situations where positive and negative feelings mix.
        ReflectionEntry(
            listOf("excited", "anxious", "scared"),
            listOf("new job", "date", "launch", "presentation", "big day"),
            Sentiment.NEUTRAL,
            "It's normal to feel fear when you care about the outcome. Can you channel that nervous energy into excitement?",
            "Stand in a 'power pose' for 30 seconds."
        ),

        // NEW: Relief and Sadness
        // Addresses the complex feeling of ending something difficult.
        ReflectionEntry(
            listOf("relieved", "sad", "tired"),
            listOf("over", "ended", "breakup", "quit", "finished"),
            Sentiment.NEUTRAL,
            "Ending something can bring both relief and sadness. Allow yourself to feel the weight lift, even if it leaves a space behind.",
            "Take a deep breath and say 'It is done'."
        ),

        // ==========================================
        // FALLBACKS (Generic but thoughtful)
        // Used when keywords don't match, but a single emotion tag is present.
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

        // Ultimate Fallback: The catch-all if absolutely no match is found.
        ReflectionEntry(
            listOf(), listOf(), Sentiment.NEUTRAL,
            "Take a moment to simply breathe and be present with whatever you are feeling.",
            "Take three deep breaths."
        )
    )

    /**
     * The Smart Search Algorithm.
     * It scores every entry in the dataset to find the "Best Fit" for the user's current situation.
     * The scoring system prioritizes keyword matches (context) over simple emotion tag matches.
     */
    fun getReflectionFor(emotions: List<String>, sentiment: Sentiment, journalText: String): ReflectionEntry {
        // Normalize text for easier matching (lowercase and trim whitespace)
        val lowerText = journalText.lowercase().trim()
        val isTextEmpty = lowerText.isEmpty()

        // Iterate through the entire dataset and find the one with the highest "Score"
        // maxByOrNull returns the item that produces the highest value from the selector block.
        val bestMatch = dataset.maxByOrNull { entry ->

            // RULE: If user didn't write any text, instantly disqualify prompts that require keywords.
            // This prevents the AI from Hallucinating a topic (like "work") when no text exists.
            if (isTextEmpty && entry.keywords.isNotEmpty()) return@maxByOrNull -100

            var score = 0

            // Criterion 1: Sentiment Match (Weight: 10)
            // Does the prompt match the general vibe (Positive/Negative)?
            if (entry.sentiment == sentiment) score += 10

            // Criterion 2: Emotion Tags (Weight: 5 per match)
            // Does the prompt handle the specific emotions the user selected?
            val emotionMatches = entry.tags.count { emotions.contains(it) }
            score += (emotionMatches * 5)

            // Criterion 3: Keyword Search (Weight: 15 per match - HIGHEST PRIORITY)
            // Did the user mention specific topics like "friend", "exam", or "sleep"?
            // This is prioritized because it makes the reflection feel much more personal.
            if (!isTextEmpty) {
                val keywordMatches = entry.keywords.count { lowerText.contains(it) }
                score += (keywordMatches * 15)
            }

            score
        }

        // Return the winning entry, or the generic fallback (last item) if something goes wrong.
        return bestMatch ?: dataset.last()
    }
}