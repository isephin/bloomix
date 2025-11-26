package com.example.bloomix

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class FlowerResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flower_result)

        // Receive from JournalActivity
        val flowerKey = intent.getStringExtra("flower_key") ?: "white_daisy"
        val journalText = intent.getStringExtra("journal_text") ?: ""

        // Get flower info from FlowerData
        val flowerInfo = FlowerData.flowers[flowerKey]
            ?: FlowerData.flowers["white_daisy"]!!

        // UI
        val flowerImg = findViewById<ImageView>(R.id.flowerImage)
        val flowerName = findViewById<TextView>(R.id.flowerName)
        val flowerDesc = findViewById<TextView>(R.id.flowerDescription)
        val microAction = findViewById<TextView>(R.id.microAction)
        val journalView = findViewById<TextView>(R.id.journalEntry)

        // Set UI
        flowerImg.setImageResource(flowerInfo.drawable)
        flowerName.text = flowerInfo.name
        flowerDesc.text = flowerInfo.description
        microAction.text = flowerInfo.microAction
        journalView.text = journalText
    }
}
