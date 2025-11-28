package com.example.bloomix

// import android.app.Activity // COMMENTED: No longer needed as we removed startActivityForResult
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
// Removed the unused Button import, as we are now using ImageView/LinearLayout
// import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class EmotionActivity : AppCompatActivity() {

    private val emotions = listOf(
        "happy","sad","excited","angry",
        "tired","bored","confused","loved",
        "calm","shocked","annoyed","stressed"
    )

    private val flowerMap = mapOf(
        "happy" to "marigold",
        "sad" to "bluebell",
        "angry" to "snapdragon",
        "tired" to "anemone",
        "bored" to "white_daisy",
        "confused" to "wisteria",
        "loved" to "rose",
        "calm" to "lotus",
        "excited" to "zinnia",
        "stressed" to "black_rose",
        "annoyed" to "azalea",
        "shocked" to "iris"
    )

    private val viewIdToEmotion = mutableMapOf<Int, String>()
    // Changed to ArrayList to explicitly support duplicates and order
    private val selected = ArrayList<String>()
    private var selectedDateKey: String? = null

    // private val REQUEST_CODE_SHARED = 100 // COMMENTED: No longer needed
    private lateinit var selectedBar: LinearLayout // Reference to the bottom bar
    private lateinit var selectedContainer: LinearLayout // New: Reference to the chip container

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emotion)

        selectedDateKey = intent.getStringExtra("selectedDate")

        // Initialized here
        selectedContainer = findViewById<LinearLayout>(R.id.selectedContainer)

        // --- OBSOLETE VIEW REFERENCES (Kept for comment history) ---
        // REMOVED: val btnShared = findViewById<Button>(R.id.btnShared)
        // REMOVED: val btnJournal = findViewById<Button>(R.id.btnJournal)
        // ----------------------------------------------------------

        // New UI elements
        val llSharedEmotions = findViewById<LinearLayout>(R.id.ll_shared_emotions)
        val btnMixEmotions = findViewById<ImageView>(R.id.btn_mix_emotions)
        selectedBar = findViewById<LinearLayout>(R.id.selectedBar)


        // Connect emotion icons and listeners
        for (emo in emotions) {
            val resId = resources.getIdentifier("em_$emo", "id", packageName)
            if (resId == 0) continue

            val iv = findViewById<ImageView>(resId)
            viewIdToEmotion[resId] = emo

            iv.alpha = 1f

            iv.setOnClickListener {
                // Always ADD, now handled by refreshSelectedChips to prevent redundancy
                addEmotion(iv, emo)
            }
        }

        // Listener for the back button
        findViewById<ImageView>(R.id.btn_back).setOnClickListener { finish() }

        // 1. UPDATED: Listener for the Shared Emotions container (toggle bottom bar visibility)
        llSharedEmotions.setOnClickListener {
            if (selected.isNotEmpty()) {
                // Toggle visibility of the selected emotions bar
                selectedBar.visibility = if (selectedBar.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            }
        }

        // Logic for Mix Emotions button (sends data to JournalActivity)
        btnMixEmotions.setOnClickListener {
            val chosenFlowerKey = determineFlowerKey()
            val intent = Intent(this, JournalActivity::class.java)

            intent.putStringArrayListExtra("selected_emotions", selected)
            intent.putExtra("flower_key", chosenFlowerKey)
            intent.putExtra("selectedDate", selectedDateKey)

            startActivity(intent)
        }
    }

    // COMMENTED: This function is no longer needed as we removed the redirection to SharedEmotionsActivity.
    /*
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SHARED && resultCode == Activity.RESULT_OK) {
            // ... (Logic to update selected list and refresh UI) ...
        }
    }
    */

    private fun addEmotion(
        iv: ImageView,
        emotion: String
    ) {
        // 1. Add to list (duplicates allowed for counting)
        selected.add(emotion)

        // 2. Visually highlight the big icon (it stays highlighted as long as count > 0)
        iv.setColorFilter(Color.parseColor("#99FFFFFF"), PorterDuff.Mode.SRC_ATOP)
        iv.alpha = 0.85f

        // 3. 2. UPDATED: Refresh the entire bottom bar (unique chips + counts)
        refreshSelectedChips()

        selectedBar.visibility = View.VISIBLE
    }

    // Function to update the emotion count in ll_shared_emotions
    private fun updateSelectedBarCount() {
        val countTextView = findViewById<TextView>(R.id.tv_emotion_count)
        countTextView.text = "(${selected.size})"

        // Hide the bottom bar if the list is empty
        if (selected.isEmpty()) {
            selectedBar.visibility = View.GONE
        }
    }

    /**
     * NEW CORE LOGIC: Clears the bottom bar and rebuilds it with unique chips and their counts.
     */
    private fun refreshSelectedChips() {
        // 1. Clear existing views
        selectedContainer.removeAllViews()

        // 2. Calculate counts for all selected emotions
        val emotionCounts = selected.groupingBy { it }.eachCount()

        // 3. Rebuild chips for each unique emotion
        emotionCounts.forEach { (emotion, count) ->
            // Assume R.layout.view_emotion_chip exists and contains R.id.chipImage, R.id.chipLabel, and R.id.chipCount
            val chip = layoutInflater.inflate(R.layout.view_emotion_chip, selectedContainer, false)
            val img = chip.findViewById<ImageView>(R.id.chipImage)
            val txt = chip.findViewById<TextView>(R.id.chipLabel)

            // This TextView shows the count badge (e.g., '2' for two 'Happy' selections)
            val countBadge = chip.findViewById<TextView>(R.id.chipCount)
            countBadge.text = count.toString()

            // Set icon and label
            val drawRes = resources.getIdentifier("${emotion}_chip", "drawable", packageName)
            if (drawRes != 0) img.setImageResource(drawRes)
            txt.text = emotion.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

            // 4. Click Listener to decrement count (remove one instance of the emotion)
            chip.setOnClickListener {
                // Remove ONE instance of this emotion string
                selected.remove(emotion)

                // Recalculate and re-render the entire bar to show the new count
                refreshSelectedChips()

                // Check if we need to turn off the highlight on the main icon
                // Only turn off if the list NO LONGER contains this emotion
                if (!selected.contains(emotion)) {
                    val ivId = resources.getIdentifier("em_$emotion", "id", packageName)
                    if (ivId != 0) {
                        val iv = findViewById<ImageView>(ivId)
                        iv.clearColorFilter()
                        iv.alpha = 1f
                    }
                }

                // COMMENTED OUT: Removed the line that was forcing the bar visible on chip removal.
                // selectedBar.visibility = if (selected.isNotEmpty()) View.VISIBLE else View.GONE
            }

            selectedContainer.addView(chip)
        }

        // 5. Update the main counter and bar visibility (only sets to GONE when empty)
        updateSelectedBarCount()
    }

    // COMMENTED: Obsolete function, replaced by refreshSelectedChips()
    /*
    private fun addChipFor(emotion: String, selectedContainer: LinearLayout) {
        // ... original redundant chip creation logic ...
    }
    */

    // COMMENTED: Obsolete function
    /*
    private fun removeChipFor(emotion: String, selectedContainer: LinearLayout) {
        // ... original chip removal logic ...
    }
    */

    private fun determineFlowerKey(): String {
        if (selected.isEmpty()) return "white_daisy"

        // Find the most frequent emotion
        val mostFrequent = selected.groupingBy { it }.eachCount().maxByOrNull { it.value }?.key
        return flowerMap[mostFrequent] ?: "white_daisy"
    }
}