package com.example.bloomix

import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
// import android.widget.Button // REMOVED: No longer using the old buttons
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

    // Fixed: this defines the flower KEY used by FlowerData
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
    private val selected = linkedSetOf<String>() // keeps emotion order
    private lateinit var selectedBar: LinearLayout // Reference to the bottom bar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emotion)

        val selectedContainer = findViewById<LinearLayout>(R.id.selectedContainer)

        // REMOVED: btnShared and btnJournal are removed from the layout.
        // val btnShared = findViewById<Button>(R.id.btnShared)
        // val btnJournal = findViewById<Button>(R.id.btnJournal)

        findViewById<ImageView>(R.id.btn_back).setOnClickListener { finish() }

        // Get new UI elements
        val llSharedEmotions = findViewById<LinearLayout>(R.id.ll_shared_emotions)
        val btnMixEmotions = findViewById<ImageView>(R.id.btn_mix_emotions) // ImageButton is usually handled as ImageView
        selectedBar = findViewById<LinearLayout>(R.id.selectedBar)


        // Connect emotion icons and listeners
        for (emo in emotions) {
            val resId = resources.getIdentifier("em_$emo", "id", packageName)
            if (resId == 0) continue

            val iv = findViewById<ImageView>(resId)
            viewIdToEmotion[resId] = emo

            iv.alpha = 1f

            iv.setOnClickListener {
                toggleEmotionSelection(iv, emo, selectedContainer)
            }
        }

        // 1. & 4. New: Listener for the Shared Emotions container (to show/hide the pop-up/bottom bar)
        llSharedEmotions.setOnClickListener {
            if (selected.isNotEmpty()) {
                // Toggle visibility of the selected emotions bar
                selectedBar.visibility = if (selectedBar.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            }
        }

        // 2. New: Listener for Mix Emotions button (takes over the old btnJournal logic)
        btnMixEmotions.setOnClickListener {
            val chosenFlowerKey = determineFlowerKey()
            val intent = Intent(this, JournalActivity::class.java)

            intent.putStringArrayListExtra("selected_emotions", ArrayList(selected))
            intent.putExtra("flower_key", chosenFlowerKey)

            startActivity(intent)
        }
    }

    private fun toggleEmotionSelection(
        iv: ImageView,
        emotion: String,
        selectedContainer: LinearLayout
    ) {
        if (selected.contains(emotion)) {
            selected.remove(emotion)
            iv.clearColorFilter()
            iv.alpha = 1f
            removeChipFor(emotion, selectedContainer)
        } else {
            selected.add(emotion)
            iv.setColorFilter(Color.parseColor("#99FFFFFF"), PorterDuff.Mode.SRC_ATOP)
            iv.alpha = 0.85f
            addChipFor(emotion, selectedContainer)
        }

        // 3. Update the count every time an emotion is toggled
        updateSelectedBarCount()

        // REMOVED: Automatic showing of selectedBar is now handled by ll_shared_emotions click.
    }

    // New function to update the emotion count
    private fun updateSelectedBarCount() {
        // R.id.tv_emotion_count is the new TextView inside ll_shared_emotions
        val countTextView = findViewById<TextView>(R.id.tv_emotion_count)
        countTextView.text = "(${selected.size})"

        // If the selection becomes empty, hide the bottom bar for cleanup
        if (selected.isEmpty()) {
            selectedBar.visibility = View.GONE
        }
    }

    private fun addChipFor(emotion: String, selectedContainer: LinearLayout) {
        // R.layout.view_emotion_chip must exist for this to work
        val chip = layoutInflater.inflate(R.layout.view_emotion_chip, selectedContainer, false)
        val img = chip.findViewById<ImageView>(R.id.chipImage)
        val txt = chip.findViewById<TextView>(R.id.chipLabel)

        val drawRes = resources.getIdentifier("${emotion}_chip", "drawable", packageName)
        if (drawRes != 0) img.setImageResource(drawRes)

        txt.text = emotion.replaceFirstChar { it.uppercase() }
        chip.tag = "chip_$emotion"

        chip.setOnClickListener {
            selected.remove(emotion)
            val ivId = resources.getIdentifier("em_$emotion", "id", packageName)
            if (ivId != 0) {
                val iv = findViewById<ImageView>(ivId)
                iv.clearColorFilter()
                iv.alpha = 1f
            }
            selectedContainer.removeView(chip)

            // Update count after removing the chip
            updateSelectedBarCount()

            // REMOVED: Automatic visibility logic from here
        }

        selectedContainer.addView(chip)

        // Update count after adding the chip
        updateSelectedBarCount()
    }

    private fun removeChipFor(emotion: String, selectedContainer: LinearLayout) {
        val tag = "chip_$emotion"
        selectedContainer.findViewWithTag<View>(tag)?.let {
            selectedContainer.removeView(it)
        }
    }


    private fun determineFlowerKey(): String {
        if (selected.isEmpty()) return "white_daisy"
        val main = selected.first() // first selected emotion
        return flowerMap[main] ?: "white_daisy"
    }
}