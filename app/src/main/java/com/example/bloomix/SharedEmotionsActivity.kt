package com.example.bloomix

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback // Required for the new back button logic
import androidx.appcompat.app.AppCompatActivity

class SharedEmotionsActivity : AppCompatActivity() {

    // References the container inside the ScrollView where we will add the emotion chips
    private lateinit var sharedContainer: LinearLayout

    // Holds the list of emotion strings passed from the previous screen
    private lateinit var selected: ArrayList<String>

    // Stores the date key (e.g., "2025-11-20") to ensure we pass it forward to the Journal screen
    private var selectedDateKey: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.shared_emotions)

        // Initialize the linear layout container where chips will be displayed
        sharedContainer = findViewById(R.id.sharedContainer)

        // Retrieve the list of emotions sent from EmotionActivity
        // We default to an empty list if the intent data is missing to prevent crashes
        selected = intent.getStringArrayListExtra("selected") ?: arrayListOf()

        // Retrieve the date string so we don't lose context of which day is being edited
        selectedDateKey = intent.getStringExtra("selectedDate")

        // Populate the screen with the emotion chips based on the 'selected' list
        loadChips()

        // Registers a callback to handle the system "Back" button press
        // This replaces the deprecated onBackPressed() override
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Prepare the result intent to send the updated list back to EmotionActivity
                val resultIntent = Intent()
                resultIntent.putStringArrayListExtra("updated_selection", selected)

                // Set the result as OK and pass the data
                setResult(Activity.RESULT_OK, resultIntent)

                // Close this activity and return to the previous screen
                finish()
            }
        })

        // Sets up the listener for the "Continue" button
        findViewById<Button>(R.id.btnContinue).setOnClickListener {
            // Create intent to move to the Journaling screen
            val intent = Intent(this, JournalActivity::class.java)

            // Pass the filtered list of emotions (in case the user removed some here)
            intent.putStringArrayListExtra("selected_emotions", selected)

            // Pass the date key so JournalActivity knows where to save the entry
            intent.putExtra("selectedDate", selectedDateKey)

            // Use the centralized FlowerData logic to determine which flower matches these emotions
            val flowerKey = FlowerData.determineFlower(selected)
            intent.putExtra("flower_key", flowerKey)

            // Start the Journal activity
            startActivity(intent)
            // We do not call finish() here, allowing the user to come back if they press Back from Journal
        }
    }

    /**
     * Dynamically adds chip views to the layout based on the selected emotions.
     * It groups duplicate emotions (e.g., "Happy, Happy") into a single chip with a counter.
     */
    private fun loadChips() {
        // Remove all existing views to prevent duplication when this function is called multiple times
        sharedContainer.removeAllViews()

        // Update the instructional text based on whether any emotions are selected
        val instruction = findViewById<TextView>(R.id.tvInstruction)
        if (selected.isEmpty()) {
            instruction.text = "No emotions selected."
        } else {
            instruction.text = "Tap an emotion to reduce its count."
        }

        // Calculate the frequency of each emotion in the list
        // Example: ["happy", "happy", "sad"] becomes map: {"happy"=2, "sad"=1}
        val groupedEmotions = selected.groupingBy { it }.eachCount()

        // Iterate through each unique emotion and its count to create the UI chips
        for ((emotion, count) in groupedEmotions) {
            // Inflate the XML layout for a single emotion chip
            val chip = layoutInflater.inflate(R.layout.view_emotion_chip, sharedContainer, false)

            // Bind the views inside the chip layout
            val img = chip.findViewById<ImageView>(R.id.chipImage)
            val txt = chip.findViewById<TextView>(R.id.chipLabel)
            val countBadge = chip.findViewById<TextView>(R.id.chipCount)

            // Dynamically find and set the image resource for the emotion (e.g., "happy_chip")
            val drawableRes = resources.getIdentifier("${emotion}_chip", "drawable", packageName)
            if (drawableRes != 0) img.setImageResource(drawableRes)

            // Capitalize the emotion name for display (e.g., "happy" -> "Happy")
            txt.text = emotion.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

            // Logic to show or hide the red count badge
            // It is visible only if the user selected this emotion more than once
            if (count > 1) {
                countBadge.text = count.toString()
                countBadge.visibility = View.VISIBLE
            } else {
                countBadge.visibility = View.GONE
            }

            // Set a click listener to remove one instance of the emotion when tapped
            chip.setOnClickListener {
                // Removes the first occurrence of this emotion string from the main list
                selected.remove(emotion)

                // Recursively call loadChips to refresh the UI with the new counts
                loadChips()
            }

            // Add the constructed chip to the main container
            sharedContainer.addView(chip)
        }
    }
}