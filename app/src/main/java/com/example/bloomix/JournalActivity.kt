package com.example.bloomix

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class JournalActivity : AppCompatActivity() {

    private var selectedDateKey: String? = null
    private var chosenFlowerKey: String = "white_daisy"
    private var selectedEmotions: ArrayList<String> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_journal)

        // 1. Retrieve data
        selectedDateKey = intent.getStringExtra("selectedDate")
        chosenFlowerKey = intent.getStringExtra("flower_key") ?: "white_daisy"
        // Change "selected_emotions" to "selected" to match the previous screen
        selectedEmotions = intent.getStringArrayListExtra("selected") ?: arrayListOf()

        // 2. Setup Buttons
        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        findViewById<ImageView>(R.id.btnBloomOpen).setOnClickListener {
            if (validateInput()) {
                showBloomPopup()
            }
        }
    }

    private fun validateInput(): Boolean {
        val text = findViewById<EditText>(R.id.etJournal).text.toString()
        if (text.isBlank()) {
            Toast.makeText(this, "Please write something first!", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun showBloomPopup() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_let_it_bloom)

        // Fix for transparent corners
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialog.findViewById<Button>(R.id.btnClosePopup).setOnClickListener {
            dialog.dismiss()
        }

        dialog.findViewById<Button>(R.id.btnLetItBloom).setOnClickListener {
            dialog.dismiss() // Dismiss first
            openFlowerResultScreen() // Then process
        }

        dialog.show()
    }

    private fun openFlowerResultScreen() {
        val journalText = findViewById<EditText>(R.id.etJournal).text.toString()

        // --- STEP 1: AI PROCESSING ---
        // Ensure MLProcessor returns the AnalysisResult data class defined above
        val analysisResult = MLProcessor.processEntry(journalText, selectedEmotions)

        // Safely get the micro-action description
        val microActionDesc = analysisResult.suggestedMicroActions.firstOrNull()?.description
            ?: "Take a moment to breathe."

        // --- STEP 2: DATABASE SAVE ---
        selectedDateKey?.let { dateKey ->
            lifecycleScope.launch {
                try {
                    val parts = dateKey.split("-")
                    if (parts.size >= 3) {
                        val y = parts[0].toInt()
                        val m = parts[1].toInt()
                        val d = parts[2].toInt()

                        // Ensure JournalEntry matches your updated Entity definition
                        val entry = JournalEntry(
                            dateKey = dateKey,
                            timestamp = System.currentTimeMillis(),
                            year = y,
                            month = m - 1,
                            day = d,
                            flowerKey = chosenFlowerKey,
                            journalText = journalText,
                            emotions = selectedEmotions.joinToString(","),
                            sentiment = analysisResult.sentiment.name,
                            moodCategory = analysisResult.overallMoodCategory,
                            reflection = analysisResult.reflectionPrompt,
                            microAction = microActionDesc
                        )

                        AppDatabase.getDatabase(applicationContext).journalDao().insertEntry(entry)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    // Optional: Run on UI thread to show toast if save fails
                }
            }
        }

        // --- STEP 3: NAVIGATE TO RESULT ---
        val intent = Intent(this, FlowerResultActivity::class.java)

        intent.putExtra("selectedDate", selectedDateKey)
        intent.putExtra("journal_text", journalText)
        intent.putExtra("flower_key", chosenFlowerKey)

        // FIXED: Key matches onCreate now
        intent.putStringArrayListExtra("selected_emotions", selectedEmotions)

        intent.putExtra("sentiment", analysisResult.sentiment.name)
        intent.putExtra("category", analysisResult.overallMoodCategory)
        intent.putExtra("reflection", analysisResult.reflectionPrompt)
        intent.putExtra("micro_action_desc", microActionDesc)
        intent.putExtra("is_new_entry", true)

        startActivity(intent)
        finish()
    }
}