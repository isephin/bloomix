package com.example.bloomix

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class EmotionActivity : AppCompatActivity() {

    // List of supported emotions. These match the IDs in our layout (e.g., em_happy).
    private val emotions = listOf(
        "happy","sad","excited","angry",
        "tired","bored","confused","loved",
        "calm","shocked","annoyed","stressed"
    )

    // Maps the Android View ID (int) to the emotion string name (e.g., R.id.em_happy -> "happy")
    private val viewIdToEmotion = mutableMapOf<Int, String>()

    // Stores the list of emotions the user has selected so far
    private val selected = ArrayList<String>()

    // Holds the date passed from the Calendar, so we know which day we are journaling for
    private var selectedDateKey: String? = null

    // Request code to identify when the user comes back from the "Shared/Review" screen
    private val REQUEST_CODE_SHARED = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emotion)

        // 1. Retrieve the date key passed from CalendarActivity
        selectedDateKey = intent.getStringExtra("selectedDate")

        // 2. Check if we have pre-existing emotions passed back (e.g., if editing an entry)
        val existingEmotions = intent.getStringArrayListExtra("selected_emotions")
        if (existingEmotions != null) {
            selected.addAll(existingEmotions)
        }

        // 3. Initialize UI references
        val selectedContainer = findViewById<LinearLayout>(R.id.selectedContainer) // The bar at the bottom
        val btnReviewShared = findViewById<LinearLayout>(R.id.ll_shared_emotions) // The "Shared Emotions" button
        val btnMixEmotions = findViewById<ImageButton>(R.id.btn_mix_emotions)     // The "Flower Vase" button (Next)
        val btnBack = findViewById<ImageButton>(R.id.btn_back)                    // Back button
        val tvEmotionCount = findViewById<TextView>(R.id.tv_emotion_count)        // Text showing "(3)" count

        // 4. If we have existing emotions (edit mode), populate the UI chips immediately
        if (selected.isNotEmpty()) {
            selected.forEach { emotion ->
                addChipFor(emotion, selectedContainer)
            }
            updateCount(tvEmotionCount)
        }

        // 5. Loop through all supported emotions to set up their click listeners
        for (emo in emotions) {
            // Dynamically find the ID (e.g., R.id.em_happy) based on the string name
            val resId = resources.getIdentifier("em_$emo", "id", packageName)
            if (resId == 0) continue // Skip if ID is not found

            val iv = findViewById<ImageView>(resId)
            viewIdToEmotion[resId] = emo

            // If this emotion is already selected, visually dim it to indicate selection
            if (selected.contains(emo)) {
                iv.setColorFilter(Color.parseColor("#99FFFFFF"), PorterDuff.Mode.SRC_ATOP)
                iv.alpha = 0.85f
            } else {
                // Otherwise ensure it looks normal
                iv.alpha = 1f
            }

            // Set the click listener for the emotion icon
            iv.setOnClickListener {
                addEmotion(iv, emo, selectedContainer)
                updateCount(tvEmotionCount)
            }
        }

        // 6. Listener for "Shared Emotions" button (Review screen)
        btnReviewShared.setOnClickListener {
            val intent = Intent(this, SharedEmotionsActivity::class.java)
            // Pass the current list of selected emotions
            intent.putStringArrayListExtra("selected", selected)
            // Pass date key so shared activity can forward it if needed
            intent.putExtra("selectedDate", selectedDateKey)
            // Start activity and wait for result (in case user deletes chips there)
            startActivityForResult(intent, REQUEST_CODE_SHARED)
        }

        // 7. Listener for "Mix Emotions" (Next / Flower Vase) button
        btnMixEmotions.setOnClickListener {
            // Validation: User must select at least one emotion
            if (selected.isEmpty()) {
                Toast.makeText(this, "Please select at least one emotion to bloom.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // --- KEY CHANGE: Use the centralized FlowerData logic to pick the flower ---
            val chosenFlowerKey = FlowerData.determineFlower(selected)

            // Prepare to move to JournalActivity
            val intent = Intent(this, JournalActivity::class.java)
            intent.putStringArrayListExtra("selected_emotions", selected)
            intent.putExtra("flower_key", chosenFlowerKey) // Pass the determined flower
            intent.putExtra("selectedDate", selectedDateKey)

            startActivity(intent)

            // Finish this activity so the user can't go "Back" to it easily after journaling
            finish()
        }

        // 8. Back button listener
        btnBack.setOnClickListener {
            finish()
        }
    }

    /**
     * Handles the result when returning from SharedEmotionsActivity.
     * Use case: User deleted some emotions in the review screen.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SHARED && resultCode == Activity.RESULT_OK) {
            // Get the updated list from the result intent
            val updatedList = data?.getStringArrayListExtra("updated_selection")
            if (updatedList != null) {
                // Clear current local selection
                selected.clear()
                val selectedContainer = findViewById<LinearLayout>(R.id.selectedContainer)
                selectedContainer.removeAllViews()

                // Reset all main grid icons to "unselected" state first
                viewIdToEmotion.forEach { (id, _) ->
                    val iv = findViewById<ImageView>(id)
                    iv.clearColorFilter()
                    iv.alpha = 1f
                }

                // Re-populate based on the updated list
                updatedList.forEach { emo ->
                    val resId = resources.getIdentifier("em_$emo", "id", packageName)
                    val iv = if (resId != 0) findViewById<ImageView>(resId) else null

                    selected.add(emo)
                    addChipFor(emo, selectedContainer) // Re-add chips to bottom bar

                    // Visually dim the icon again since it is still selected
                    iv?.setColorFilter(Color.parseColor("#99FFFFFF"), PorterDuff.Mode.SRC_ATOP)
                    iv?.alpha = 0.85f
                }

                val tvEmotionCount = findViewById<TextView>(R.id.tv_emotion_count)
                updateCount(tvEmotionCount)
            }
        }
    }

    /** Helper to update the "(3)" count text. */
    private fun updateCount(tv: TextView?) {
        tv?.text = "(${selected.size})"
    }

    /** Adds an emotion to the list and dims the icon. */
    private fun addEmotion(iv: ImageView, emotion: String, selectedContainer: LinearLayout) {
        selected.add(emotion)
        // Dim the icon to show it is active
        iv.setColorFilter(Color.parseColor("#99FFFFFF"), PorterDuff.Mode.SRC_ATOP)
        iv.alpha = 0.85f
        // Create the small chip in the bottom bar
        addChipFor(emotion, selectedContainer)
    }

    /** Dynamically creates a chip view (small icon + text) for the bottom bar. */
    private fun addChipFor(emotion: String, selectedContainer: LinearLayout) {
        val chip = layoutInflater.inflate(R.layout.view_emotion_chip, selectedContainer, false)
        val img = chip.findViewById<ImageView>(R.id.chipImage)
        val txt = chip.findViewById<TextView>(R.id.chipLabel)

        // Load the chip image resource
        val drawRes = resources.getIdentifier("${emotion}_chip", "drawable", packageName)
        if (drawRes != 0) img.setImageResource(drawRes)

        // Capitalize the first letter (e.g., "happy" -> "Happy")
        txt.text = emotion.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        chip.tag = "chip_$emotion"

        // Clicking the chip removes it from selection
        chip.setOnClickListener {
            selected.remove(emotion)
            selectedContainer.removeView(chip)

            // If the emotion is fully removed (not in list anymore), reset the main grid icon
            if (!selected.contains(emotion)) {
                val ivId = resources.getIdentifier("em_$emotion", "id", packageName)
                if (ivId != 0) {
                    val iv = findViewById<ImageView>(ivId)
                    iv.clearColorFilter() // Remove dimming
                    iv.alpha = 1f
                }
            }

            val tvEmotionCount = findViewById<TextView>(R.id.tv_emotion_count)
            updateCount(tvEmotionCount)
        }
        selectedContainer.addView(chip)
    }
}