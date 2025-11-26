package com.example.bloomix

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
            finish()  // go back to EmotionActivity, user can hit Journal next
        }
    }

    /** Load chips into the screen */
    private fun loadChips() {
        sharedContainer.removeAllViews()

        for (emotion in selected.toList()) {
            val chip = layoutInflater.inflate(R.layout.view_emotion_chip, sharedContainer, false)

            val img = chip.findViewById<ImageView>(R.id.chipImage)
            val txt = chip.findViewById<TextView>(R.id.chipLabel)

            val drawableRes = resources.getIdentifier("${emotion}_chip", "drawable", packageName)
            if (drawableRes != 0) img.setImageResource(drawableRes)

            txt.text = emotion.replaceFirstChar { it.uppercase() }

            chip.setOnClickListener {
                selected.remove(emotion)
                loadChips()
            }

            sharedContainer.addView(chip)
        }
    }
}
