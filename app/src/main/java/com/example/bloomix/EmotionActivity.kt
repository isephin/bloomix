package com.example.bloomix

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
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

    private val viewIdToEmotion = mutableMapOf<Int, String>()
    private val selected = ArrayList<String>()
    private var selectedDateKey: String? = null

    private val REQUEST_CODE_SHARED = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emotion)

        selectedDateKey = intent.getStringExtra("selectedDate")

        // Restore existing emotions if passed (e.g. from Calendar for editing)
        val existingEmotions = intent.getStringArrayListExtra("selected_emotions")
        if (existingEmotions != null) {
            selected.addAll(existingEmotions)
        }

        val selectedContainer = findViewById<LinearLayout>(R.id.selectedContainer)

        val btnReviewShared = findViewById<LinearLayout>(R.id.ll_shared_emotions)
        val btnMixEmotions = findViewById<ImageButton>(R.id.btn_mix_emotions)
        val btnBack = findViewById<ImageButton>(R.id.btn_back)
        val tvEmotionCount = findViewById<TextView>(R.id.tv_emotion_count)

        // If we restored emotions, populate the UI chips immediately
        if (selected.isNotEmpty()) {
            selected.forEach { emotion ->
                addChipFor(emotion, selectedContainer)
            }
            updateCount(tvEmotionCount)
        }

        for (emo in emotions) {
            val resId = resources.getIdentifier("em_$emo", "id", packageName)
            if (resId == 0) continue

            val iv = findViewById<ImageView>(resId)
            viewIdToEmotion[resId] = emo

            // If this emotion is in our restored list, dim the icon
            if (selected.contains(emo)) {
                iv.setColorFilter(Color.parseColor("#99FFFFFF"), PorterDuff.Mode.SRC_ATOP)
                iv.alpha = 0.85f
            } else {
                iv.alpha = 1f
            }

            iv.setOnClickListener {
                addEmotion(iv, emo, selectedContainer)
                updateCount(tvEmotionCount)
            }
        }

        btnReviewShared.setOnClickListener {
            val intent = Intent(this, SharedEmotionsActivity::class.java)
            intent.putStringArrayListExtra("selected", selected)
            startActivityForResult(intent, REQUEST_CODE_SHARED)
        }

        btnMixEmotions.setOnClickListener {
            val chosenFlowerKey = determineFlowerKey()
            val intent = Intent(this, JournalActivity::class.java)

            intent.putStringArrayListExtra("selected_emotions", selected)
            intent.putExtra("flower_key", chosenFlowerKey)
            intent.putExtra("selectedDate", selectedDateKey)

            startActivity(intent)
        }

        btnBack.setOnClickListener {
            finish()
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

                viewIdToEmotion.forEach { (id, _) ->
                    val iv = findViewById<ImageView>(id)
                    iv.clearColorFilter()
                    iv.alpha = 1f
                }

                updatedList.forEach { emo ->
                    val resId = resources.getIdentifier("em_$emo", "id", packageName)
                    val iv = if (resId != 0) findViewById<ImageView>(resId) else null

                    selected.add(emo)
                    addChipFor(emo, selectedContainer)

                    iv?.setColorFilter(Color.parseColor("#99FFFFFF"), PorterDuff.Mode.SRC_ATOP)
                    iv?.alpha = 0.85f
                }

                val selectedBar = findViewById<LinearLayout>(R.id.selectedBar)
                selectedBar.visibility = if (selected.isNotEmpty()) View.VISIBLE else View.GONE

                val tvEmotionCount = findViewById<TextView>(R.id.tv_emotion_count)
                updateCount(tvEmotionCount)
            }
        }
    }

    private fun updateCount(tv: TextView?) {
        tv?.text = "(${selected.size})"
    }

    private fun addEmotion(iv: ImageView, emotion: String, selectedContainer: LinearLayout) {
        selected.add(emotion)
        iv.setColorFilter(Color.parseColor("#99FFFFFF"), PorterDuff.Mode.SRC_ATOP)
        iv.alpha = 0.85f
        addChipFor(emotion, selectedContainer)

        val selectedBar = findViewById<LinearLayout>(R.id.selectedBar)
        selectedBar.visibility = if (selected.isNotEmpty()) View.VISIBLE else View.GONE
    }

    private fun addChipFor(emotion: String, selectedContainer: LinearLayout) {
        val chip = layoutInflater.inflate(R.layout.view_emotion_chip, selectedContainer, false)
        val img = chip.findViewById<ImageView>(R.id.chipImage)
        val txt = chip.findViewById<TextView>(R.id.chipLabel)

        val drawRes = resources.getIdentifier("${emotion}_chip", "drawable", packageName)
        if (drawRes != 0) img.setImageResource(drawRes)

        txt.text = emotion.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        chip.tag = "chip_$emotion"

        chip.setOnClickListener {
            selected.remove(emotion)
            selectedContainer.removeView(chip)

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

            val tvEmotionCount = findViewById<TextView>(R.id.tv_emotion_count)
            updateCount(tvEmotionCount)
        }
        selectedContainer.addView(chip)
    }

    private fun determineFlowerKey(): String {
        // 1. If list is empty, pick a TOTALLY RANDOM flower from all available
        if (selected.isEmpty()) {
            return flowerMap.values.flatten().random()
        }

        // 2. Find dominant emotion
        val mostFrequent = selected.groupingBy { it }.eachCount().maxByOrNull { it.value }?.key

        // 3. Get list for dominant emotion
        val possibleFlowers = flowerMap[mostFrequent]

        // 4. Return random from list OR random from ALL if something fails
        return if (possibleFlowers != null && possibleFlowers.isNotEmpty()) {
            possibleFlowers.random()
        } else {
            // Fallback: Pick any random flower from the map instead of just "white_daisy"
            flowerMap.values.flatten().random()
        }
    }
}