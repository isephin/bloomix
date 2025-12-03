package com.example.bloomix

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope // <--- ADDED THIS
import kotlinx.coroutines.launch          // <--- ADDED THIS

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

        // 2. SAVE ALL DATA (Now using Room Database)
        selectedDateKey?.let { dateKey ->
            // Use lifecycleScope to launch a background coroutine
            lifecycleScope.launch {
                try {
                    // Parse the dateKey "2025-11-5" to get Year/Month/Day integers
                    val parts = dateKey.split("-")
                    // Safety check: ensure we have at least 3 parts
                    if (parts.size >= 3) {
                        val y = parts[0].toInt()
                        val m = parts[1].toInt()
                        val d = parts[2].toInt()
                        val timestamp = System.currentTimeMillis()

                        val entry = JournalEntry(
                            dateKey = dateKey,
                            timestamp = timestamp,
                            year = y,
                            month = m - 1, // Store as 0-indexed to match Calendar logic
                            day = d,
                            flowerKey = chosenFlowerKey,
                            journalText = journalText,
                            emotions = selectedEmotions.joinToString(","),
                            sentiment = analysisResult.sentiment.name,
                            moodCategory = analysisResult.overallMoodCategory,
                            reflection = analysisResult.reflectionPrompt,
                            microAction = microActionDesc
                        )

                        // Save to Database
                        AppDatabase.getDatabase(applicationContext).journalDao().insertEntry(entry)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    // Optional: Toast.makeText(this@JournalActivity, "Error saving", Toast.LENGTH_SHORT).show()
                }
            }
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

        // Tell the next screen this is a NEW entry
        intent.putExtra("is_new_entry", true)

        startActivity(intent)
        finish()
    }
}