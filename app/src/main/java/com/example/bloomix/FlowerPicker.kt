package com.example.bloomix

object FlowerPicker {

    data class Flower(
        val name: String,
        val image: String,       // drawable name WITHOUT extension
        val description: String,
        val action: String
    )

    fun pickFlower(emotions: List<String>): Flower {

        // Choose the first (dominant) emotion
        val main = emotions.firstOrNull() ?: "neutral"

        return when (main) {

            "happy" -> Flower(
                "Snapdragon",
                "snapdragon",
                "Bright and warm, like your positivity.",
                "Spread your light today by sending someone a compliment."
            )

            "sad" -> Flower(
                "Bluebell",
                "bluebell",
                "Gentle and soft, symbolizing emotional sensitivity.",
                "Be kind to yourself — try writing 1 thing you’re grateful for."
            )

            "angry" -> Flower(
                "Red Rose",
                "red_rose",
                "Passionate and fiery, representing intense emotion.",
                "Take a deep breath — hold for 4 seconds, release for 6."
            )

            "tired" -> Flower(
                "Lavender",
                "lavender",
                "Calming and soothing, reflecting your need for rest.",
                "Do a 3-minute body relaxation scan."
            )

            "bored" -> Flower(
                "Daisy",
                "daisy",
                "Simple and light, symbolizing a desire for freshness.",
                "Try something new for 5 minutes today."
            )

            "confused" -> Flower(
                "Orchid",
                "orchid",
                "Mysterious yet beautiful — complexity can be meaningful.",
                "Write down your top 3 thoughts to gain clarity."
            )

            "stressed" -> Flower(
                "Chamomile",
                "chamomile",
                "A calming plant often associated with peace and recovery.",
                "Take one short break today. Even 2 minutes helps."
            )

            "shocked" -> Flower(
                "Magnolia",
                "magnolia",
                "Strong yet delicate — reflecting unexpected events.",
                "Ground yourself by naming 3 things you can see."
            )

            "calm" -> Flower(
                "Lotus",
                "lotus",
                "Centered and peaceful, symbolizing inner balance.",
                "Continue nurturing your peace — take a slow breath."
            )

            "loved" -> Flower(
                "Rose Pink",
                "rose_pink",
                "Warm, affectionate, and caring.",
                "Send a message to someone you appreciate today."
            )

            else -> Flower(
                "Wildflower",
                "wildflower",
                "A mix of emotions — complex but beautiful.",
                "Take a mindful pause today."
            )
        }
    }
}
