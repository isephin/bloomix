package com.example.bloomix

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class FlowerResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flower_result)

        // --- FIX: Receiving "journal_text" matches the sender now ---
        val journalText = intent.getStringExtra("journal_text") ?: ""
        val flowerKey = intent.getStringExtra("flower_key") ?: "white_daisy"

        // Receive Analysis Data
        val sentiment = intent.getStringExtra("sentiment") ?: "NEUTRAL"
        val category = intent.getStringExtra("category") ?: "Complex Emotional Landscape"
        val reflectionPrompt = intent.getStringExtra("reflection") ?: "How are you truly feeling right now?"
        val microActionDesc = intent.getStringExtra("micro_action_desc") ?: "Take a mindful pause today."

        // Get flower info
        val flowerInfo = FlowerData.flowers[flowerKey] ?: FlowerData.flowers["white_daisy"]!!

        // UI References
        val flowerImg = findViewById<ImageView>(R.id.flowerImage)
        val flowerName = findViewById<TextView>(R.id.flowerName)
        val flowerDesc = findViewById<TextView>(R.id.flowerDescription)

        // AI Result UI References
        val tvSentiment = findViewById<TextView>(R.id.tvSentiment)
        val tvCategory = findViewById<TextView>(R.id.tvCategory)
        val tvReflectionPrompt = findViewById<TextView>(R.id.tvReflectionPrompt)
        val microActionView = findViewById<TextView>(R.id.microAction)

        val journalView = findViewById<TextView>(R.id.journalEntry)

        // Populate UI
        flowerImg.setImageResource(flowerInfo.drawable)
        flowerName.text = flowerInfo.name

        // Show the AI Category as the main description
        flowerDesc.text = "Your Bloomix Analysis: $category"

        // Populate AI Card
        tvSentiment.text = "Sentiment: $sentiment"
        tvCategory.text = "Overall Mood: $category"
        tvReflectionPrompt.text = reflectionPrompt
        microActionView.text = "Micro-Action: $microActionDesc"

        // Populate Journal Entry
        journalView.text = journalText

        // --- FIX: Logic for the DONE button to return to Calendar ---
        val btnDone = findViewById<Button>(R.id.btnBack)
        btnDone.setOnClickListener {
            val intent = Intent(this, CalendarActivity::class.java)
            // Clear the back stack so the user can't press 'back' to return to this result screen
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
    }
}