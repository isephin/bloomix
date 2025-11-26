package com.example.bloomix

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class FlowerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flower_result)

        val flowerImage = findViewById<ImageView>(R.id.flowerImage)
        val flowerName = findViewById<TextView>(R.id.flowerName)
        val flowerDescription = findViewById<TextView>(R.id.flowerDescription)
        val microAction = findViewById<TextView>(R.id.microAction)
        val journalEntry = findViewById<TextView>(R.id.journalEntry)

        // Data sent from the previous screen
        val receivedFlowerKey = intent.getStringExtra("flower_key") ?: "rose"
        val journalText = intent.getStringExtra("journal_text") ?: ""

        val flower = FlowerData.flowers[receivedFlowerKey] ?: FlowerData.flowers["rose"]!!

        // Populate screen
        flowerImage.setImageResource(flower.drawable)
        flowerName.text = flower.name
        flowerDescription.text = flower.description
        microAction.text = flower.microAction
        journalEntry.text = journalText
    }
}
