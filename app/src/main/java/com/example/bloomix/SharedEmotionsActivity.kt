package com.example.bloomix

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SharedEmotionsActivity : AppCompatActivity() {

    // The horizontal or vertical container where we add the emotion chips dynamically
    private lateinit var sharedContainer: LinearLayout

    // The list of emotions passed from the previous screen
    private lateinit var selected: ArrayList<String>

    // We store the date key (e.g., "2025-11-20") to pass it forward to the Journal screen
    private var selectedDateKey: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.shared_emotions)

        // 1. Initialize the container view
        sharedContainer = findViewById(R.id.sharedContainer)

        // 2. Retrieve the list of emotions sent from EmotionActivity
        // If null (shouldn't happen), default to an empty list to avoid crashes.
        selected = intent.getStringArrayListExtra("selected") ?: arrayListOf()

        // 3. Retrieve the date key so we don't lose track of which day we are editing
        selectedDateKey = intent.getStringExtra("selectedDate")

        // 4. Populate the screen with the emotion chips
        loadChips()

        // 5. Setup the "Continue" button listener
        findViewById<Button>(R.id.btnContinue).setOnClickListener {
            // We are moving to the Journaling screen now
            val intent = Intent(this, JournalActivity::class.java)

            // Pass the final filtered list of emotions (user might have deleted some here)
            intent.putStringArrayListExtra("selected_emotions", selected)

            // Pass the date key so JournalActivity knows where to save the entry
            intent.putExtra("selectedDate", selectedDateKey)

            // --- KEY CHANGE: Use the centralized FlowerData logic ---
            // Instead of calculating the flower locally, we ask our helper object.
            // This ensures the logic is identical to what we used in EmotionActivity.
            val flowerKey = FlowerData.determineFlower(selected)
            intent.putExtra("flower_key", flowerKey)

            startActivity(intent)
            // Note: We don't call finish() here so the user could technically press "Back"
            // from JournalActivity to come back here and edit emotions again.
        }
    }

    /** * Renders the emotion chips into the container.
     * Clears the view first so we don't duplicate items if called multiple times.
     */
    private fun loadChips() {
        sharedContainer.removeAllViews()

        val instruction = findViewById<TextView>(R.id.tvInstruction)

        // Update the helper text based on whether the list is empty
        if (selected.isEmpty()) {
            instruction.text = "No emotions selected."
        } else {
            instruction.text = "Tap an emotion to remove it."
        }

        // Create a copy of the list (.toList()) to avoid ConcurrentModificationException
        // while iterating and removing items.
        for (emotion in selected.toList()) {
            // Inflate the standard emotion chip layout
            val chip = layoutInflater.inflate(R.layout.view_emotion_chip, sharedContainer, false)

            val img = chip.findViewById<ImageView>(R.id.chipImage)
            val txt = chip.findViewById<TextView>(R.id.chipLabel)

            // Hide the red count badge since we are just listing them here
            val countBadge = chip.findViewById<TextView>(R.id.chipCount)
            if (countBadge != null) countBadge.visibility = View.GONE

            // Dynamically load the correct image resource (e.g., "happy_chip")
            val drawableRes = resources.getIdentifier("${emotion}_chip", "drawable", packageName)
            if (drawableRes != 0) img.setImageResource(drawableRes)

            // Format text: "happy" -> "Happy"
            txt.text = emotion.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

            // Set Click Listener: Remove this emotion from the list
            chip.setOnClickListener {
                selected.remove(emotion)
                // Reload the list to update the UI immediately
                loadChips()
            }

            sharedContainer.addView(chip)
        }
    }

    // REMOVED: private fun determineFlowerKey()
    // This logic was deleted because we now use FlowerData.determineFlower()
}