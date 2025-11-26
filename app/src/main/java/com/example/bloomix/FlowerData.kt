package com.example.bloomix

import android.content.Context

data class FlowerInfo(
    val drawable: Int,
    val name: String,
    val description: String,
    val microAction: String
)

object FlowerData {

    val flowers = mapOf(
        "anemone" to FlowerInfo(
            R.drawable.anemone,
            "Anemone",
            "Symbolizes anticipation and positive hope.",
            "Try listing three things you're looking forward to today."
        ),
        "azalea" to FlowerInfo(
            R.drawable.azalea,
            "Azalea",
            "Represents self-care and emotional balance.",
            "Do one small act today that is just for you."
        ),
        "black_rose" to FlowerInfo(
            R.drawable.black_rose,
            "Black Rose",
            "A symbol of transformation and emotional strength.",
            "Release one thought you no longer need."
        ),
        "bluebell" to FlowerInfo(
            R.drawable.bluebell,
            "Bluebell",
            "Represents calmness and gratitude.",
            "Write down one thing you're thankful for."
        ),
        "cherry_blossom" to FlowerInfo(
            R.drawable.cherry_blossom,
            "Cherry Blossom",
            "A reminder of renewal and gentle beginnings.",
            "Take a slow breath and reset your pace."
        ),
        "iris" to FlowerInfo(
            R.drawable.iris,
            "Iris",
            "Symbol of wisdom, clarity, and insight.",
            "Pause and reflect before your next decision."
        ),
        "lavender" to FlowerInfo(
            R.drawable.lavender,
            "Lavender",
            "Brings calm and emotional relaxation.",
            "Take 10 seconds right now to breathe deeply."
        ),
        "lotus" to FlowerInfo(
            R.drawable.lotus,
            "Lotus",
            "A symbol of rising above challenges.",
            "Acknowledge one challenge you've overcome."
        ),
        "marigold" to FlowerInfo(
            R.drawable.marigold,
            "Marigold",
            "Represents warmth, resilience, and courage.",
            "Send a kind message to someone who matters."
        ),
        "red_tulip" to FlowerInfo(
            R.drawable.red_tulip,
            "Red Tulip",
            "Represents love, affection, and kindness.",
            "Do one gentle thing for yourself today."
        ),
        "rose" to FlowerInfo(
            R.drawable.rose,
            "Rose",
            "A classic symbol of love and emotional honesty.",
            "Be honest with yourself about how you feel right now."
        ),
        "snapdragon" to FlowerInfo(
            R.drawable.snapdragon,
            "Snapdragon",
            "Symbolizes resilience and confidence.",
            "Stand tall for one moment today—literally."
        ),
        "white_daisy" to FlowerInfo(
            R.drawable.white_daisy,
            "White Daisy",
            "Represents simplicity and new beginnings.",
            "Declutter one tiny thing around you."
        ),
        "white_rose" to FlowerInfo(
            R.drawable.white_rose,
            "White Rose",
            "Symbol of clarity and emotional purity.",
            "Let your mind rest for a minute—no pressure."
        ),
        "wisteria" to FlowerInfo(
            R.drawable.wisteria,
            "Wisteria",
            "Represents friendship and gentle support.",
            "Reach out to someone who makes you feel safe."
        ),
        "zinnia" to FlowerInfo(
            R.drawable.zinnia,
            "Zinnia",
            "Symbolizes joy, remembrance, and positivity.",
            "Recall one happy memory and let it warm you."
        )
    )


    fun getDrawableForName(context: Context, flowerName: String): Int {
        val key = flowerName.lowercase()
        val info = flowers[key]
        return info?.drawable ?: 0
    }
}

