package com.example.bloomix

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FlowerResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flower_result)

        // Get Data
        val journalText = intent.getStringExtra("journal_text") ?: ""
        val flowerKey = intent.getStringExtra("flower_key") ?: "white_daisy"
        val selectedEmotions = intent.getStringArrayListExtra("selected") ?: arrayListOf()
        val dateKey = intent.getStringExtra("selectedDate")

        val sentiment = intent.getStringExtra("sentiment") ?: "NEUTRAL"
        val category = intent.getStringExtra("category") ?: "Complex"
        val reflectionPrompt = intent.getStringExtra("reflection") ?: ""
        val microActionDesc = intent.getStringExtra("micro_action_desc") ?: ""

        val flowerInfo = FlowerData.flowers[flowerKey] ?: FlowerData.flowers["white_daisy"]!!

        // --- BIND UI ---

        // 1. Header (Date)
        // FIX: Improved parsing for "yyyy-M-d" and display "yyyy.MM.dd"
        val displayDate = if (dateKey != null) {
            try {
                // Calendar passes format "yyyy-M-d" (e.g. 2025-11-5)
                val parser = SimpleDateFormat("yyyy-M-d", Locale.getDefault())
                var date = try {
                    parser.parse(dateKey)
                } catch (e: Exception) {
                    // Fallback for zero-padded dates
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateKey)
                }

                // Show full date with day
                val formatter = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
                formatter.format(date ?: Date())
            } catch (e: Exception) {
                SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).format(Date())
            }
        } else {
            SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).format(Date())
        }
        findViewById<TextView>(R.id.tvDate).text = displayDate

        findViewById<ImageButton>(R.id.btnClose).setOnClickListener {
            val intent = Intent(this, CalendarActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        // --- CUSTOM DELETE DIALOG ---
        findViewById<ImageButton>(R.id.btnMenu).setOnClickListener {
            showCustomDeleteDialog(dateKey)
        }

        // 2. Flower Card
        findViewById<ImageView>(R.id.flowerImage).setImageResource(flowerInfo.drawable)
        findViewById<TextView>(R.id.flowerName).text = flowerInfo.name
        findViewById<TextView>(R.id.flowerKeywords).text = flowerInfo.keywords
        findViewById<TextView>(R.id.flowerLanguage).text = flowerInfo.flowerLanguage

        // 3. Sentiment Card
        findViewById<TextView>(R.id.tvSentiment).text = "Sentiment: $sentiment"
        findViewById<TextView>(R.id.tvCategory).text = "Overall mood: $category"
        findViewById<TextView>(R.id.tvReflectionPrompt).text = reflectionPrompt

        // 4. Micro Action Card
        findViewById<TextView>(R.id.microAction).text = microActionDesc

        // 5. Emotion Stats
        generateEmotionStats(selectedEmotions)

        // 6. Journal Card
        findViewById<TextView>(R.id.journalEntry).text = journalText
    }

    private fun showCustomDeleteDialog(dateKey: String?) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_delete_entry) // Use the new layout
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnDelete = dialog.findViewById<TextView>(R.id.btnDeleteAction)
        btnDelete.setOnClickListener {
            if (dateKey != null) {
                deleteEntry(dateKey)
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Cannot delete: Date unknown", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun deleteEntry(dateKey: String) {
        val prefs = getSharedPreferences("journal_data", MODE_PRIVATE)
        prefs.edit().apply {
            remove("flower_$dateKey")
            remove("journal_$dateKey")
            remove("sentiment_$dateKey")
            remove("category_$dateKey")
            remove("reflection_$dateKey")
            remove("micro_action_desc_$dateKey")
        }.apply()

        Toast.makeText(this, "Entry deleted", Toast.LENGTH_SHORT).show()

        val intent = Intent(this, CalendarActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    private fun generateEmotionStats(emotions: ArrayList<String>) {
        val container = findViewById<LinearLayout>(R.id.emotionsContainer)
        if (emotions.isEmpty()) return

        val counts = emotions.groupingBy { it }.eachCount()
        val total = emotions.size.toFloat()

        counts.forEach { (emotion, count) ->
            val percentage = ((count / total) * 100).toInt()

            val label = TextView(this)
            label.text = "${emotion.replaceFirstChar { it.uppercase() }} $percentage%"
            label.textSize = 14f
            label.setTextColor(Color.parseColor("#555555"))
            label.typeface = android.graphics.Typeface.MONOSPACE

            val progressBar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal)
            progressBar.max = 100
            progressBar.progress = percentage
            progressBar.progressTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#F4C2C2"))
            progressBar.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                20
            ).apply { bottomMargin = 24 }

            container.addView(label)
            container.addView(progressBar)
        }
    }
}