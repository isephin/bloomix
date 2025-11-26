package com.example.bloomix

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class JournalActivity : AppCompatActivity() {

    private var selectedDateKey: String? = null
    private var chosenFlowerKey: String = "white_daisy"
    private var selectedEmotions: ArrayList<String> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_journal)

        selectedDateKey = intent.getStringExtra("selectedDate")
        chosenFlowerKey = intent.getStringExtra("flower_key") ?: "white_daisy"
        selectedEmotions = intent.getStringArrayListExtra("selected_emotions") ?: arrayListOf()

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        findViewById<ImageView>(R.id.btnBloomOpen).setOnClickListener {
            showBloomPopup()
        }
    }

    private fun showBloomPopup() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_let_it_bloom)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialog.findViewById<ImageView>(R.id.btnClosePopup).setOnClickListener {
            dialog.dismiss()
        }

        dialog.findViewById<Button>(R.id.btnLetItBloom).setOnClickListener {
            openFlowerResultScreen()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun openFlowerResultScreen() {
        val journalText = findViewById<EditText>(R.id.etJournal).text.toString()

        // 1. Call the ML Processor to get the full analysis
        val analysisResult = MLProcessor.processEntry(journalText, selectedEmotions)

        // 2. Extract the micro-action description from the analysis result
        val microActionDesc = analysisResult.suggestedMicroActions.firstOrNull()?.description
            ?: FlowerData.flowers[chosenFlowerKey]?.microAction
            ?: "Take a mindful pause today."

        // Save to SharedPreferences
        selectedDateKey?.let { dateKey ->
            val prefs = getSharedPreferences("journal_data", MODE_PRIVATE)
            prefs.edit().apply {
                putString("flower_$dateKey", chosenFlowerKey)
                putString("journal_$dateKey", journalText)
                // Save AI analysis results for later retrieval (optional, but good practice)
                putString("sentiment_$dateKey", analysisResult.sentiment.name)
                putString("category_$dateKey", analysisResult.overallMoodCategory)
                putString("reflection_$dateKey", analysisResult.reflectionPrompt)
                putString("micro_action_desc_$dateKey", microActionDesc)
            }.apply()
        }

        // START FlowerResultActivity
        val intent = Intent(this, FlowerResultActivity::class.java)
        intent.putExtra("selected", selectedEmotions)
        intent.putExtra("journal_text", journalText) // Changed to "journal_text" to match previous convention
        intent.putExtra("flower_key", chosenFlowerKey)

        // PASS AI ANALYSIS RESULTS
        intent.putExtra("sentiment", analysisResult.sentiment.name)
        intent.putExtra("category", analysisResult.overallMoodCategory)
        intent.putExtra("reflection", analysisResult.reflectionPrompt)
        intent.putExtra("micro_action_desc", microActionDesc) // Pass the AI-suggested action

        startActivity(intent)
        finish()
    }
}