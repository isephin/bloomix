package com.example.bloomix

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SharedEmotionsActivity : AppCompatActivity() {

    private lateinit var sharedContainer: LinearLayout
    private lateinit var selected: ArrayList<String>

    // Store dateKey to pass it along
    private var selectedDateKey: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.shared_emotions)

        // Retrieve data passed from EmotionActivity
        sharedContainer = findViewById(R.id.sharedContainer)
        selected = intent.getStringArrayListExtra("selected") ?: arrayListOf()
        selectedDateKey = intent.getStringExtra("selectedDate") // Get the date key!

        loadChips()

        findViewById<Button>(R.id.btnContinue).setOnClickListener {
            // FIX: Don't just finish(). Start JournalActivity directly.
            val intent = Intent(this, JournalActivity::class.java)

            // Pass the filtered emotions list
            intent.putStringArrayListExtra("selected_emotions", selected)

            // Pass the date key so Journal knows which day we are editing
            intent.putExtra("selectedDate", selectedDateKey)

            // We also need to determine the flower key again based on the FINAL list
            val flowerKey = determineFlowerKey()
            intent.putExtra("flower_key", flowerKey)

            startActivity(intent)

            // Optional: Finish this activity so coming back doesn't show it
            // You might also want to finish EmotionActivity, but that requires
            // clearing the stack or using flags. For now, simple forward nav is safest.
        }
    }

    /** Load chips into the screen */
    private fun loadChips() {
        sharedContainer.removeAllViews()

        val instruction = findViewById<TextView>(R.id.tvInstruction)
        if (selected.isEmpty()) {
            instruction.text = "No emotions selected."
        } else {
            instruction.text = "Tap an emotion to remove it."
        }

        for (emotion in selected.toList()) {
            val chip = layoutInflater.inflate(R.layout.view_emotion_chip, sharedContainer, false)

            val img = chip.findViewById<ImageView>(R.id.chipImage)
            val txt = chip.findViewById<TextView>(R.id.chipLabel)
            val countBadge = chip.findViewById<TextView>(R.id.chipCount)
            if (countBadge != null) countBadge.visibility = View.GONE

            val drawableRes = resources.getIdentifier("${emotion}_chip", "drawable", packageName)
            if (drawableRes != 0) img.setImageResource(drawableRes)

            txt.text = emotion.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

            chip.setOnClickListener {
                selected.remove(emotion)
                loadChips()
            }

            sharedContainer.addView(chip)
        }
    }

    // Helper to calculate flower key again (since list might have changed)
    private fun determineFlowerKey(): String {
        if (selected.isEmpty()) return "white_daisy"

        // Copy the map logic from EmotionActivity or move to a shared helper
        // For simplicity, I'll recreate the map here. Ideally, put this in a FlowerHelper object.
        val flowerMap = mapOf(
            "happy" to listOf("marigold", "morning_glory", "dahlia"),
            "sad" to listOf("bluebell", "hydrangea", "lilac"),
            "angry" to listOf("snapdragon", "black_rose"),
            "tired" to listOf("anemone", "aloe_vera", "lavender"),
            "bored" to listOf("white_daisy", "pansy", "cornflower"),
            "confused" to listOf("wisteria", "iris"),
            "loved" to listOf("rose", "gardenia", "camellia", "carnation"),
            "calm" to listOf("lotus", "lily_of_the_valley", "white_rose"),
            "excited" to listOf("zinnia", "freesia"),
            "stressed" to listOf("black_rose", "edelweiss", "chamomile"),
            "annoyed" to listOf("azalea", "red_tulip"),
            "shocked" to listOf("iris", "cherry_blossom")
        )

        val mostFrequent = selected.groupingBy { it }.eachCount().maxByOrNull { it.value }?.key
        val possibleFlowers = flowerMap[mostFrequent]

        return if (possibleFlowers != null && possibleFlowers.isNotEmpty()) {
            possibleFlowers.random()
        } else {
            flowerMap.values.flatten().random()
        }
    }
}