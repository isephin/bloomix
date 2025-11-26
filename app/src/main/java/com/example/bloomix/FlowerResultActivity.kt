package com.example.bloomix

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class FlowerResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // NOTE: The resource name is assumed to be 'activity_flower_result'
        setContentView(R.layout.activity_flower_result)

        // Receive from JournalActivity
        val flowerKey = intent.getStringExtra("flower_key") ?: "white_daisy"
        val journalText = intent.getStringExtra("journal") ?: "" // Changed to "journal" from "journal_text"

        // ----------------------------------------------------
        // NEW: RECEIVE ANALYSIS RESULT DATA
        // ----------------------------------------------------
        val sentiment = intent.getStringExtra("sentiment") ?: "NEUTRAL"
        val category = intent.getStringExtra("category") ?: "Complex Emotional Landscape"
        val reflectionPrompt = intent.getStringExtra("reflection") ?: "How are you truly feeling right now?"
        val microActionDesc = intent.getStringExtra("micro_action_desc") ?: "Take a mindful pause today."

        // Get flower info from FlowerData
        val flowerInfo = FlowerData.flowers[flowerKey]
            ?: FlowerData.flowers["white_daisy"]!!

        // UI Element Declarations (Fixing the "red" text issue)
        val flowerImg = findViewById<ImageView>(R.id.flowerImage)
        val flowerName = findViewById<TextView>(R.id.flowerName)
        val flowerDesc = findViewById<TextView>(R.id.flowerDescription)

        // --- NEW UI Elements for AI Results ---
        // Assuming your layout (activity_flower_result.xml) has these IDs:
        val tvSentiment = findViewById<TextView>(R.id.tvSentiment)
        val tvCategory = findViewById<TextView>(R.id.tvCategory)
        val tvReflectionPrompt = findViewById<TextView>(R.id.tvReflectionPrompt)
        val microActionView = findViewById<TextView>(R.id.microAction)

        val journalView = findViewById<TextView>(R.id.journalEntry)

        // Set UI
        flowerImg.setImageResource(flowerInfo.drawable)
        flowerName.text = flowerInfo.name

        // flowerDesc originally showed the hardcoded description, now it can show the category
        flowerDesc.text = "Your Bloomix Analysis: $category"

        // --- Set NEW AI Result Fields ---
        tvSentiment.text = "Sentiment: $sentiment"
        tvCategory.text = "Overall Mood: $category"
        tvReflectionPrompt.text = reflectionPrompt
        microActionView.text = "Micro-Action: $microActionDesc"

        // Original journal content
        journalView.text = journalText
    }
}