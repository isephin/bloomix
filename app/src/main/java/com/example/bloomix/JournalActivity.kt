package com.example.bloomix

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class JournalActivity : AppCompatActivity() {

    // Store data passed from the previous screens (Emotion Selection)
    private var selectedDateKey: String? = null
    private var chosenFlowerKey: String = "white_daisy"
    private var selectedEmotions: ArrayList<String> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_journal)

        // 1. Retrieve data from Intent
        // dateKey: "2025-11-20"
        selectedDateKey = intent.getStringExtra("selectedDate")
        // flowerKey: Determined by FlowerData in the previous step
        chosenFlowerKey = intent.getStringExtra("flower_key") ?: "white_daisy"
        // emotions: List of strings like ["happy", "excited"]
        selectedEmotions = intent.getStringArrayListExtra("selected_emotions") ?: arrayListOf()

        // 2. Setup Back Button
        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        // 3. Setup "Bloom" Button (The floating flower icon)
        // Clicking this opens the confirmation popup before processing logic
        findViewById<ImageView>(R.id.btnBloomOpen).setOnClickListener {
            showBloomPopup()
        }
    }

    /**
     * Shows a confirmation dialog.
     * This prevents accidental submissions and adds a moment of pause ("Let it bloom").
     */
    private fun showBloomPopup() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_let_it_bloom)

        // Make the background transparent so the rounded corners of the popup look correct
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialog.findViewById<Button>(R.id.btnClosePopup).setOnClickListener {
            dialog.dismiss()
        }

        // The user confirms they are done writing
        dialog.findViewById<Button>(R.id.btnLetItBloom).setOnClickListener {
            openFlowerResultScreen()
            dialog.dismiss()
        }

        dialog.show()
    }

    /**
     * CORE FUNCTION:
     * 1. Runs the ML Analysis.
     * 2. Saves the entry to the Database.
     * 3. Navigates to the Result Screen.
     */
    private fun openFlowerResultScreen() {
        val journalText = findViewById<EditText>(R.id.etJournal).text.toString()

        // --- STEP 1: AI PROCESSING ---
        // We pass the raw text and the list of emotions to our ML engine.
        // It returns sentiment (Positive/Negative) and a reflection prompt.
        val analysisResult = MLProcessor.processEntry(journalText, selectedEmotions)

        // Extract the suggested micro-action (small task)
        val microActionDesc = analysisResult.suggestedMicroActions.firstOrNull()?.description
            ?: "Take a mindful pause today."

        // --- STEP 2: DATABASE SAVE ---
        selectedDateKey?.let { dateKey ->
            // Launch a coroutine to perform database operations off the main thread
            lifecycleScope.launch {
                try {
                    // Parse "2025-11-5" -> Year: 2025, Month: 11, Day: 5
                    val parts = dateKey.split("-")

                    if (parts.size >= 3) {
                        val y = parts[0].toInt()
                        val m = parts[1].toInt()
                        val d = parts[2].toInt()
                        val timestamp = System.currentTimeMillis()

                        // Create the Database Entity
                        val entry = JournalEntry(
                            dateKey = dateKey,
                            timestamp = timestamp,
                            year = y,
                            month = m - 1, // Store month as 0-indexed (0=Jan) to match Calendar logic
                            day = d,
                            flowerKey = chosenFlowerKey,
                            journalText = journalText,
                            emotions = selectedEmotions.joinToString(","), // Flatten list to string
                            sentiment = analysisResult.sentiment.name,
                            moodCategory = analysisResult.overallMoodCategory,
                            reflection = analysisResult.reflectionPrompt,
                            microAction = microActionDesc
                        )

                        // Insert into Room Database (overwrites if dateKey exists)
                        AppDatabase.getDatabase(applicationContext).journalDao().insertEntry(entry)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        // --- STEP 3: NAVIGATE TO RESULT ---
        val intent = Intent(this, FlowerResultActivity::class.java)

        // Pass all the data needed to display the result card immediately
        // (We pass it via Intent so the next screen doesn't have to wait for a DB query)
        intent.putExtra("selectedDate", selectedDateKey)
        intent.putExtra("journal_text", journalText)
        intent.putExtra("flower_key", chosenFlowerKey)
        intent.putStringArrayListExtra("selected", selectedEmotions)

        intent.putExtra("sentiment", analysisResult.sentiment.name)
        intent.putExtra("category", analysisResult.overallMoodCategory)
        intent.putExtra("reflection", analysisResult.reflectionPrompt)
        intent.putExtra("micro_action_desc", microActionDesc)

        // Flag to tell the result screen this is a brand new entry (vs viewing history)
        intent.putExtra("is_new_entry", true)

        startActivity(intent)
        finish() // Close JournalActivity so user can't go back to edit it
    }
}