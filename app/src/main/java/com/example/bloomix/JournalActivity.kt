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

        dialog.findViewById<Button>(R.id.btnClosePopup).setOnClickListener {
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

        // 1. Run the AI Analysis
        val analysisResult = MLProcessor.processEntry(journalText, selectedEmotions)

        val microActionDesc = analysisResult.suggestedMicroActions.firstOrNull()?.description
            ?: "Take a mindful pause today."

        // 2. SAVE ALL DATA
        selectedDateKey?.let { dateKey ->
            val prefs = getSharedPreferences("journal_data", MODE_PRIVATE)
            prefs.edit().apply {
                putString("flower_$dateKey", chosenFlowerKey)
                putString("journal_$dateKey", journalText)
                putString("sentiment_$dateKey", analysisResult.sentiment.name)
                putString("category_$dateKey", analysisResult.overallMoodCategory)
                putString("reflection_$dateKey", analysisResult.reflectionPrompt)
                putString("micro_action_desc_$dateKey", microActionDesc)
                putString("emotions_$dateKey", selectedEmotions.joinToString(","))
            }.apply()
        }

        // 3. Start Result Screen
        val intent = Intent(this, FlowerResultActivity::class.java)

        intent.putExtra("selectedDate", selectedDateKey)
        intent.putExtra("journal_text", journalText)
        intent.putExtra("flower_key", chosenFlowerKey)
        intent.putStringArrayListExtra("selected", selectedEmotions)

        intent.putExtra("sentiment", analysisResult.sentiment.name)
        intent.putExtra("category", analysisResult.overallMoodCategory)
        intent.putExtra("reflection", analysisResult.reflectionPrompt)
        intent.putExtra("micro_action_desc", microActionDesc)

        // --- CRITICAL FIX: Tell the next screen this is a NEW entry ---
        intent.putExtra("is_new_entry", true)

        startActivity(intent)
        finish()
    }
}