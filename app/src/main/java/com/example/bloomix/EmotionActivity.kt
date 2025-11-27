package com.example.bloomix

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import android.widget.Button
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

    private val REQUEST_CODE_SHARED = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emotion)

        selectedDateKey = intent.getStringExtra("selectedDate")

        val selectedContainer = findViewById<LinearLayout>(R.id.selectedContainer)
        val btnShared = findViewById<Button>(R.id.btnShared)
        val btnJournal = findViewById<Button>(R.id.btnJournal)

        // Connect emotion icons
        for (emo in emotions) {
            val resId = resources.getIdentifier("em_$emo", "id", packageName)
            if (resId == 0) continue

            val iv = findViewById<ImageView>(resId)
            viewIdToEmotion[resId] = emo

            iv.alpha = 1f

            iv.setOnClickListener {
                // CHANGED: Always ADD, never toggle off from the main icon
                addEmotion(iv, emo, selectedContainer)
            }
        }

        btnShared.setOnClickListener {
            val intent = Intent(this, SharedEmotionsActivity::class.java)
            intent.putStringArrayListExtra("selected", selected)
            startActivityForResult(intent, REQUEST_CODE_SHARED)
        }

        btnJournal.setOnClickListener {
            val chosenFlowerKey = determineFlowerKey()
            val intent = Intent(this, JournalActivity::class.java)

            intent.putStringArrayListExtra("selected_emotions", selected)
            intent.putExtra("flower_key", chosenFlowerKey)
            intent.putExtra("selectedDate", selectedDateKey)

            startActivity(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SHARED && resultCode == Activity.RESULT_OK) {
            val updatedList = data?.getStringArrayListExtra("updated_selection")
            if (updatedList != null) {
                selected.clear()
                val selectedContainer = findViewById<LinearLayout>(R.id.selectedContainer)
                selectedContainer.removeAllViews()

                // Reset all icons visually first
                viewIdToEmotion.forEach { (id, _) ->
                    val iv = findViewById<ImageView>(id)
                    iv.clearColorFilter()
                    iv.alpha = 1f
                }

                // Re-apply selections
                updatedList.forEach { emo ->
                    // Find the view to update its visual state
                    val resId = resources.getIdentifier("em_$emo", "id", packageName)
                    val iv = if (resId != 0) findViewById<ImageView>(resId) else null

                    // Add back to internal list and UI
                    selected.add(emo)
                    addChipFor(emo, selectedContainer)

                    // Update visual state (highlight if present)
                    iv?.setColorFilter(Color.parseColor("#99FFFFFF"), PorterDuff.Mode.SRC_ATOP)
                    iv?.alpha = 0.85f
                }

                val selectedBar = findViewById<LinearLayout>(R.id.selectedBar)
                selectedBar.visibility = if (selected.isNotEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    private fun addEmotion(
        iv: ImageView,
        emotion: String,
        selectedContainer: LinearLayout
    ) {
        // 1. Add to list (duplicates allowed now!)
        selected.add(emotion)

        // 2. Visually highlight the big icon (it stays highlighted as long as count > 0)
        iv.setColorFilter(Color.parseColor("#99FFFFFF"), PorterDuff.Mode.SRC_ATOP)
        iv.alpha = 0.85f

        // 3. Add the chip to the bottom bar
        addChipFor(emotion, selectedContainer)

        val selectedBar = findViewById<LinearLayout>(R.id.selectedBar)
        selectedBar.visibility = View.VISIBLE
    }

    private fun addChipFor(emotion: String, selectedContainer: LinearLayout) {
        val chip = layoutInflater.inflate(R.layout.view_emotion_chip, selectedContainer, false)
        val img = chip.findViewById<ImageView>(R.id.chipImage)
        val txt = chip.findViewById<TextView>(R.id.chipLabel)

        val drawRes = resources.getIdentifier("${emotion}_chip", "drawable", packageName)
        if (drawRes != 0) img.setImageResource(drawRes)

        txt.text = emotion.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

        // We use a unique tag for the view object itself, but since we have duplicates,
        // we can't use the tag to find *specific* chips easily later.
        // We rely on the object reference in the OnClickListener.
        chip.tag = "chip_$emotion"

        chip.setOnClickListener {
            // REMOVAL LOGIC: Only remove THIS specific instance
            selected.remove(emotion) // Removes the first occurrence of this string
            selectedContainer.removeView(chip)

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

            val selectedBar = findViewById<LinearLayout>(R.id.selectedBar)
            selectedBar.visibility = if (selected.isNotEmpty()) View.VISIBLE else View.GONE
        }

        selectedContainer.addView(chip)
    }

    // removeChipFor is no longer needed since we remove views directly in the OnClickListener
    // within addChipFor.

    private fun determineFlowerKey(): String {
        if (selected.isEmpty()) return "white_daisy"

        // Find the most frequent emotion
        val mostFrequent = selected.groupingBy { it }.eachCount().maxByOrNull { it.value }?.key
        return flowerMap[mostFrequent] ?: "white_daisy"
    }
}