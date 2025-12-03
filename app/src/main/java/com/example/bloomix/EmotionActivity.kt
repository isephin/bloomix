package com.example.bloomix

import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class EmotionActivity : AppCompatActivity() {

    private val emotions = listOf(
        "happy","sad","excited","angry",
        "tired","bored","confused","loved",
        "calm","shocked","annoyed","stressed"
    )

    // REMOVED: private val flowerMap ... (Logic moved to FlowerData.kt)

    private val viewIdToEmotion = mutableMapOf<Int, String>()
    private val selected = ArrayList<String>() // ArrayList supports duplicates for counting
    private var selectedDateKey: String? = null

    // UI References
    private lateinit var selectedBar: LinearLayout
    private lateinit var selectedContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emotion)

        selectedDateKey = intent.getStringExtra("selectedDate")

        // Initialize UI References
        selectedContainer = findViewById(R.id.selectedContainer)
        selectedBar = findViewById(R.id.selectedBar) // The bottom bar that is initially hidden

        val llSharedEmotions = findViewById<LinearLayout>(R.id.ll_shared_emotions) // The button to toggle the bar
        val btnMixEmotions = findViewById<ImageButton>(R.id.btn_mix_emotions)
        val btnBack = findViewById<ImageButton>(R.id.btn_back)

        // Connect emotion icons and listeners
        for (emo in emotions) {
            val resId = resources.getIdentifier("em_$emo", "id", packageName)
            if (resId == 0) continue

            val iv = findViewById<ImageView>(resId)
            viewIdToEmotion[resId] = emo

            iv.alpha = 1f

            iv.setOnClickListener {
                addEmotion(iv, emo)
            }
        }

        // Back Button
        btnBack.setOnClickListener { finish() }

        // --- TOGGLE LOGIC (Restored from your snippet) ---
        // The bar does NOT pop up automatically. It only toggles when this is clicked.
        llSharedEmotions.setOnClickListener {
            if (selected.isNotEmpty()) {
                // Toggle visibility: If visible -> Hide. If hidden -> Show.
                selectedBar.visibility = if (selectedBar.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            }
        }

        // --- MIX EMOTIONS LOGIC (Proceed to Journal) ---
        btnMixEmotions.setOnClickListener {
            if (selected.isEmpty()) {
                Toast.makeText(this, "Please select at least one emotion.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Use FlowerData to decide the flower
            val chosenFlowerKey = FlowerData.determineFlower(selected)

            val intent = Intent(this, JournalActivity::class.java)
            intent.putStringArrayListExtra("selected_emotions", selected)
            intent.putExtra("flower_key", chosenFlowerKey)
            intent.putExtra("selectedDate", selectedDateKey)

            startActivity(intent)
            finish() // Close screen
        }
    }

    private fun addEmotion(iv: ImageView, emotion: String) {
        // 1. Add to list
        selected.add(emotion)

        // 2. Visually highlight the big icon
        iv.setColorFilter(Color.parseColor("#99FFFFFF"), PorterDuff.Mode.SRC_ATOP)
        iv.alpha = 0.85f

        // 3. Update the chips (data), but DO NOT force the bar to appear.
        refreshSelectedChips()

        // Note: We removed "selectedBar.visibility = View.VISIBLE" so it stays hidden until clicked.
    }

    private fun updateSelectedBarCount() {
        val countTextView = findViewById<TextView>(R.id.tv_emotion_count)
        // Update the text like "(3)"
        countTextView.text = "(${selected.size})"

        // If list is empty, we must hide the bar because there is nothing to show
        if (selected.isEmpty()) {
            selectedBar.visibility = View.GONE
        }
    }

    /**
     * Rebuilds the chip list. Handles grouping (e.g. "Happy (2)").
     */
    private fun refreshSelectedChips() {
        // 1. Clear existing
        selectedContainer.removeAllViews()

        // 2. Group counts: { "happy": 2, "sad": 1 }
        val emotionCounts = selected.groupingBy { it }.eachCount()

        // 3. Create Chips
        emotionCounts.forEach { (emotion, count) ->
            val chip = layoutInflater.inflate(R.layout.view_emotion_chip, selectedContainer, false)
            val img = chip.findViewById<ImageView>(R.id.chipImage)
            val txt = chip.findViewById<TextView>(R.id.chipLabel)
            val countBadge = chip.findViewById<TextView>(R.id.chipCount)

            // Set Image
            val drawRes = resources.getIdentifier("${emotion}_chip", "drawable", packageName)
            if (drawRes != 0) img.setImageResource(drawRes)

            // Set Text
            txt.text = emotion.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

            // Set Badge Count (Only if > 1)
            if (count > 1) {
                countBadge.text = count.toString()
                countBadge.visibility = View.VISIBLE
            } else {
                countBadge.visibility = View.GONE
            }

            // Click to remove
            chip.setOnClickListener {
                selected.remove(emotion) // Remove one instance

                // If completely removed, reset the main icon highlight
                if (!selected.contains(emotion)) {
                    val ivId = resources.getIdentifier("em_$emotion", "id", packageName)
                    if (ivId != 0) {
                        val iv = findViewById<ImageView>(ivId)
                        iv.clearColorFilter()
                        iv.alpha = 1f
                    }
                }
                refreshSelectedChips()
            }
            selectedContainer.addView(chip)
        }

        // 4. Update the count text (and hide bar if empty)
        updateSelectedBarCount()
    }
}