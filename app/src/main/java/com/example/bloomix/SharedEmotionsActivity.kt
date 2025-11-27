package com.example.bloomix

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SharedEmotionsActivity : AppCompatActivity() {

    private lateinit var sharedContainer: LinearLayout
    private lateinit var selected: ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.shared_emotions)

        sharedContainer = findViewById(R.id.sharedContainer)
        selected = intent.getStringArrayListExtra("selected") ?: arrayListOf()

        loadChips()

        findViewById<Button>(R.id.btnContinue).setOnClickListener {
            // FIX: Send the updated list back to EmotionActivity
            val resultIntent = Intent()
            resultIntent.putStringArrayListExtra("updated_selection", selected)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }

    /** Load chips into the screen */
    private fun loadChips() {
        sharedContainer.removeAllViews()

        // Update instruction text based on list size
        val instruction = findViewById<TextView>(R.id.tvInstruction)
        if (selected.isEmpty()) {
            instruction.text = "No emotions selected."
        } else {
            instruction.text = "Tap an emotion to remove it."
        }

        for (emotion in selected.toList()) {
            // FIX: Now using the correct layout we just created
            val chip = layoutInflater.inflate(R.layout.view_emotion_chip, sharedContainer, false)

            val img = chip.findViewById<ImageView>(R.id.chipImage)
            val txt = chip.findViewById<TextView>(R.id.chipLabel)

            val drawableRes = resources.getIdentifier("${emotion}_chip", "drawable", packageName)
            if (drawableRes != 0) img.setImageResource(drawableRes)

            txt.text = emotion.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

            chip.setOnClickListener {
                selected.remove(emotion)
                loadChips() // Refresh the view
            }

            sharedContainer.addView(chip)
        }
    }
}