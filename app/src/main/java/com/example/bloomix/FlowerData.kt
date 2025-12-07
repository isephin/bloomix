package com.example.bloomix

import android.content.Context

/**
 * Data class representing the visual and textual details of a single flower.
 * This holds the image resource ID and the meanings we display on the Result screen.
 */
data class FlowerInfo(
    val drawable: Int,        // The R.drawable ID for the flower image
    val name: String,         // Display name (e.g., "Red Rose")
    val keywords: String,     // Short summary (e.g., "Passion + Love")
    val flowerLanguage: String, // The longer, detailed description
    val microAction: String   // The small suggested task for the user
)

/**
 * Singleton object that acts as the central database for all Flower-related logic.
 * Using 'object' means we can access these functions anywhere without creating an instance.
 */
object FlowerData {

    // --- 1. OPTIMIZATION: Cache for Emotion IDs ---
    // This map stores resource IDs we've already looked up so we don't have to scan
    // the system resources every time (which can be slow).
    private val emotionCache = mutableMapOf<String, Int>()

    /**
     * Dynamically finds the drawable ID for an emotion name string (e.g., "happy").
     * It checks multiple naming conventions (e.g., "em_happy", "happy_chip", "happy").
     */
    fun getEmotionDrawable(context: Context, emotionName: String): Int {
        // Normalize the input string to lowercase to avoid case-sensitivity issues
        val key = emotionName.lowercase().trim()

        // If we found this ID before, return it immediately from our cache
        if (emotionCache.containsKey(key)) {
            return emotionCache[key]!!
        }

        // Try finding "em_happy" (the main icon style)
        var resId = context.resources.getIdentifier("em_$key", "drawable", context.packageName)

        // If not found, try finding "happy_chip" (the chip style)
        if (resId == 0) resId = context.resources.getIdentifier("${key}_chip", "drawable", context.packageName)

        // If still not found, try just "happy" (fallback)
        if (resId == 0) resId = context.resources.getIdentifier(key, "drawable", context.packageName)

        // If we found a valid ID, save it to the cache for next time
        if (resId != 0) {
            emotionCache[key] = resId
        }

        return resId
    }

    // --- 2. FLOWER DEFINITIONS ---
    // A hardcoded map connecting a unique key (e.g., "rose") to its full FlowerInfo data.
    val flowers = mapOf(
        "rose" to FlowerInfo(
            R.drawable.rose,
            "Red Rose",
            "Passion + Love + Devotion",
            "Red roses are the universal symbol of love, passion, and romance. They can also represent deep respect, courage, devotion, and beauty.",
            "Be honest with yourself about how you feel right now."
        ),
        "marigold" to FlowerInfo(
            R.drawable.marigold,
            "Marigold",
            "Warmth + Resilience + Courage",
            "Marigolds represent the power of the sun and the strength to overcome darkness. They symbolize the courage to be yourself and the warmth of positive energy.",
            "Send a kind message to someone who matters."
        ),
        "white_daisy" to FlowerInfo(
            R.drawable.white_daisy,
            "White Daisy",
            "Simplicity + New Beginnings",
            "Daisies symbolize innocence, purity, and true love. They remind us that every new day is a fresh start full of simple joys.",
            "Declutter one tiny thing around you."
        ),
        "bluebell" to FlowerInfo(
            R.drawable.bluebell,
            "Bluebell",
            "Calmness + Gratitude + Humility",
            "Bluebells are associated with humility and gratitude. Their drooping heads symbolize bowing in thanks, reminding us to find peace in what we have.",
            "Write down one thing you're thankful for."
        ),
        "snapdragon" to FlowerInfo(
            R.drawable.snapdragon,
            "Snapdragon",
            "Strength + Grace under Pressure",
            "Snapdragons represent growing in rocky soil. They symbolize the inner strength required to face challenges with grace and resilience.",
            "Stand tall for one moment today—literally."
        ),
        "anemone" to FlowerInfo(
            R.drawable.anemone,
            "Anemone",
            "Anticipation + Hope",
            "Anemones close their petals at night and reopen in the morning, symbolizing the anticipation of something better arriving soon.",
            "Try listing three things you're looking forward to today."
        ),
        "azalea" to FlowerInfo(
            R.drawable.azalea,
            "Azalea",
            "Temperance + Self-Care",
            "Azaleas remind us to take care of ourselves and find balance.",
            "Do one small act today that is just for you."
        ),
        "black_rose" to FlowerInfo(
            R.drawable.black_rose,
            "Black Rose",
            "Rebirth + New Beginnings",
            "A symbol of major change, turning away from the old to embrace the new.",
            "Release one thought you no longer need."
        ),
        "cherry_blossom" to FlowerInfo(
            R.drawable.cherry_blossom,
            "Cherry Blossom",
            "Renewal + Impermanence",
            "A reminder that life is beautiful but fleeting.",
            "Take a slow breath and reset your pace."
        ),
        "white_rose" to FlowerInfo(
            R.drawable.white_rose,
            "White Rose",
            "Purity + Innocence",
            "Symbolizes a clean slate and honest intentions.",
            "Let your mind rest for a minute—no pressure."
        ),
        "wisteria" to FlowerInfo(
            R.drawable.wisteria,
            "Wisteria",
            "Longevity + Immortality",
            "Represents long life and the endurance of love.",
            "Reach out to someone who makes you feel safe."
        ),
        "zinnia" to FlowerInfo(
            R.drawable.zinnia,
            "Zinnia",
            "Endurance + Lasting Affection",
            "Zinnias symbolize thoughts of absent friends and lasting affection.",
            "Recall one happy memory and let it warm you."
        ),
        "morning_glory" to FlowerInfo(
            R.drawable.morning_glory,
            "Morning Glory",
            "Affection + Mortality",
            "Represents love that is strong but fleeting, urging us to seize the day.",
            "Make a plan to wake up 15 minutes early tomorrow."
        ),
        "dahlia" to FlowerInfo(
            R.drawable.dahlia,
            "Dahlia",
            "Dignity + Elegance",
            "Symbolizes standing strong in your own values and inner strength.",
            "Wear something that makes you feel confident."
        ),
        "hydrangea" to FlowerInfo(
            R.drawable.hydrangea,
            "Hydrangea",
            "Gratitude + Understanding",
            "Represents heartfelt emotions and gratitude for being understood.",
            "Write a short thank-you note to someone."
        ),
        "lilac" to FlowerInfo(
            R.drawable.lilac,
            "Lilac",
            "First Love + Confidence",
            "Symbolizes the joy of youth and the confidence of innocence.",
            "Listen to a song that reminds you of a happy time."
        ),
        "aloe_vera" to FlowerInfo(
            R.drawable.aloe_vera,
            "Aloe Vera",
            "Healing + Protection",
            "Known for its healing properties, symbolizing recovery and protection.",
            "Drink a full glass of water to hydrate and heal."
        ),
        "lavender" to FlowerInfo(
            R.drawable.lavender,
            "Lavender",
            "Serenity + Grace",
            "Represents purity, silence, and calmness of mind.",
            "Take 10 seconds right now to breathe deeply."
        ),
        "pansy" to FlowerInfo(
            R.drawable.pansy,
            "Pansy",
            "Loving Thoughts",
            "Derived from the French word 'pensée' (thought), symbolizing remembrance.",
            "Send a text to a friend you haven't seen in a while."
        ),
        "cornflower" to FlowerInfo(
            R.drawable.cornflower_kornblume,
            "Cornflower",
            "Delicacy + Refinement",
            "Symbolizes simple beauty and refinement in nature.",
            "Notice one small, beautiful detail in your room."
        ),
        "gardenia" to FlowerInfo(
            R.drawable.gardenia,
            "Gardenia",
            "Secret Love + Joy",
            "Represents purity and the joy of secret admiration.",
            "Do something kind anonymously today."
        ),
        "carnation" to FlowerInfo(
            R.drawable.carnation,
            "Carnation",
            "Fascination + Distinction",
            "Represents deep love and fascination with the unique.",
            "Read one page of a book or article on a new topic."
        ),
        "lotus" to FlowerInfo(
            R.drawable.lotus,
            "Lotus",
            "Rebirth + Enlightenment",
            "Rising from the mud to bloom, symbolizing overcoming adversity.",
            "Acknowledge one challenge you've overcome."
        ),
        "lily_of_the_valley" to FlowerInfo(
            R.drawable.lily_of_the_valley,
            "Lily of the Valley",
            "Return of Happiness",
            "Symbolizes sweet humility and the return of happiness.",
            "Smile at a stranger or someone you live with today."
        ),
        "freesia" to FlowerInfo(
            R.drawable.freesia,
            "Freesia",
            "Trust + Friendship",
            "Represents innocence and thoughtfulness.",
            "Trust your gut instinct on one small decision today."
        ),
        "edelweiss" to FlowerInfo(
            R.drawable.edelweiss,
            "Edelweiss",
            "Courage + Devotion",
            "Symbolizes deep love and devotion, found in rugged places.",
            "Do one small thing that scares you today."
        ),
        "camellia" to FlowerInfo(
            R.drawable.camellia, // Keeping existing drawable reference
            "Camellia (Tsubaki)",
            "Admiration + Perfection",
            "In floral language, the red Camellia symbolizes deep love and admiration, while the white represents waiting and affection.",
            "Compliment yourself or someone else today."
        ),
        "iris" to FlowerInfo(
            R.drawable.iris,
            "Iris",
            "Wisdom + Hope",
            "Symbolizes wisdom and the hope of a new message.",
            "Pause and reflect before your next decision."
        ),
        "red_tulip" to FlowerInfo(
            R.drawable.red_tulip,
            "Red Tulip",
            "Perfect Love",
            "Represents true, deep, and perfect love.",
            "Do one gentle thing for yourself today."
        ),
        "sunflower" to FlowerInfo(
            R.drawable.sunflower,
            "Sunflower",
            "Adoration + Loyalty",
            "Sunflowers turn their heads to follow the sun. They symbolize unwavering faith and unconditional love.",
            "Stand in the sun for a moment and soak it in."
        )
    )

    // --- 3. NEW: CENTRALIZED MAPPING LOGIC ---
    // This defines which flowers can appear for a specific emotion.
    // e.g. If you feel "happy", you might get a marigold, morning_glory, or dahlia.
    private val emotionToFlowerMap = mapOf(
        "happy" to listOf("marigold", "morning_glory", "dahlia", "sunflower"), // Added Sunflower
        "sad" to listOf("bluebell", "hydrangea", "lilac"),
        "angry" to listOf("snapdragon", "black_rose"),
        "tired" to listOf("anemone", "aloe_vera", "lavender"),
        "bored" to listOf("white_daisy", "pansy", "cornflower"),
        "confused" to listOf("wisteria", "iris"),
        "loved" to listOf("rose", "gardenia", "camellia", "carnation", "sunflower"),
        "calm" to listOf("lotus", "lily_of_the_valley", "white_rose"),
        "excited" to listOf("zinnia", "freesia", "sunflower"),
        "stressed" to listOf("black_rose", "edelweiss", "camellia"),
        "annoyed" to listOf("azalea", "red_tulip"),
        "shocked" to listOf("iris", "cherry_blossom"),
    )

    /**
     * The Main Logic: Determines the final flower based on a list of emotions selected by the user.
     */
    fun determineFlower(selectedEmotions: List<String>): String {
        // Safety check: if the list is empty, return a random flower to prevent crashing
        if (selectedEmotions.isEmpty()) {
            return flowers.keys.random()
        }

        // 1. Find the most frequent emotion in the user's selection
        // "groupingBy { it }.eachCount()" turns ["happy", "happy", "sad"] into { "happy": 2, "sad": 1 }
        val mostFrequent = selectedEmotions
            .groupingBy { it }
            .eachCount()
            .maxByOrNull { it.value }?.key

        // 2. Look up the list of possible flowers for that dominant emotion
        val possibleFlowers = emotionToFlowerMap[mostFrequent]

        // 3. Return a random flower from that list, or a random fallback if something went wrong
        return if (possibleFlowers != null && possibleFlowers.isNotEmpty()) {
            possibleFlowers.random()
        } else {
            flowers.keys.random()
        }
    }

    /**
     * Helper to get the image ID for a specific flower name (e.g. "rose").
     */
    fun getDrawableForName(context: Context, flowerName: String): Int {
        val key = flowerName.lowercase().trim()
        val info = flowers[key]
        // Return 0 if the flower isn't found (which lets the UI handle empty states)
        return info?.drawable ?: 0
    }
}