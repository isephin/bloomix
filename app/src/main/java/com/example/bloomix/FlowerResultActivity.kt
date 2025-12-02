package com.example.bloomix

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.Window
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FlowerResultActivity : AppCompatActivity() {

    // Define a map for emotion colors
    private val emotionColors = mapOf(
        "loved" to "#FF88AA", "annoyed" to "#FF6666", "angry" to "#FF3300",
        "stressed" to "#FFAA66", "happy" to "#FFDD66", "confused" to "#FFFF66",
        "excited" to "#66FF66", "bored" to "#66CCCC", "calm" to "#66CCCC",
        "sad" to "#3366FF", "shocked" to "#6633CC", "tired" to "#AAAAAA",
        "neutral" to "#A9A9A9"
    )

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
        val displayDate = if (dateKey != null) {
            try {
                val parser = SimpleDateFormat("yyyy-M-d", Locale.getDefault())
                var date = try { parser.parse(dateKey) } catch (e: Exception) { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateKey) }
                val formatter = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
                formatter.format(date ?: Date())
            } catch (e: Exception) {
                SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).format(Date())
            }
        } else {
            SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).format(Date())
        }
        findViewById<TextView>(R.id.tvDate).text = displayDate

        val prefsSettings = getSharedPreferences("app_settings", MODE_PRIVATE)
        val nickname = prefsSettings.getString("user_nickname", "")
        if (!nickname.isNullOrEmpty() && nickname != "Bloomix User") {
            findViewById<TextView>(R.id.labelFlowerOf).text = "$nickname's flower of\nthe day is a"
            findViewById<TextView>(R.id.labelMicroAction).text = "Micro-action for $nickname"
            findViewById<TextView>(R.id.labelJournal).text = "$nickname's Journal"
        }

        // --- NAVIGATION FIX ---
        // Just finish(). Do NOT start a new Intent.
        // This returns to the existing CalendarActivity instance (which is still on November).
        findViewById<ImageButton>(R.id.btnClose).setOnClickListener {
            finish()
        }

        findViewById<ImageButton>(R.id.btnMenu).setOnClickListener {
            showCustomDeleteDialog(dateKey)
        }

        // Populate Cards
        findViewById<ImageView>(R.id.flowerImage).setImageResource(flowerInfo.drawable)
        findViewById<TextView>(R.id.flowerName).text = flowerInfo.name
        findViewById<TextView>(R.id.flowerKeywords).text = flowerInfo.keywords
        findViewById<TextView>(R.id.flowerLanguage).text = flowerInfo.flowerLanguage
        findViewById<TextView>(R.id.tvSentiment).text = "Sentiment: $sentiment"
        findViewById<TextView>(R.id.tvCategory).text = "Overall mood: $category"
        findViewById<TextView>(R.id.tvReflectionPrompt).text = reflectionPrompt
        findViewById<TextView>(R.id.microAction).text = microActionDesc
        findViewById<TextView>(R.id.journalEntry).text = journalText

        generateEmotionStats(selectedEmotions)
    }

    private fun showCustomDeleteDialog(dateKey: String?) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_delete_entry)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnDelete = dialog.findViewById<TextView>(R.id.btnDeleteAction)
        btnDelete.setOnClickListener {
            if (dateKey != null) {
                deleteEntry(dateKey)
                dialog.dismiss()
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
            remove("emotions_$dateKey")
        }.apply()

        Toast.makeText(this, "Entry deleted", Toast.LENGTH_SHORT).show()
        // Just finish to go back to Calendar and let onResume refresh the view
        finish()
    }

    private fun generateEmotionStats(emotions: ArrayList<String>) {
        val container = findViewById<LinearLayout>(R.id.emotionsContainer)
        if (emotions.isEmpty()) return

        val counts = emotions.groupingBy { it.lowercase(Locale.getDefault()) }.eachCount()
        val total = emotions.size.toFloat()

        // Sort by count descending
        counts.entries.sortedByDescending { it.value }.forEach { (emotion, count) ->
            val percentage = ((count / total) * 100).toInt()
            val colorInt = getEmotionColor(emotion)

            val rowLayout = LinearLayout(this)
            rowLayout.orientation = LinearLayout.HORIZONTAL
            rowLayout.gravity = Gravity.CENTER_VERTICAL
            rowLayout.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 8 }

            val emotionIcon = ImageView(this)
            val iconParams = LinearLayout.LayoutParams(60, 60)
            iconParams.marginEnd = 16
            emotionIcon.layoutParams = iconParams

            var resId = resources.getIdentifier("em_$emotion", "drawable", packageName)
            if (resId == 0) resId = resources.getIdentifier("${emotion}_chip", "drawable", packageName)
            if (resId == 0) resId = resources.getIdentifier(emotion, "drawable", packageName)

            if (resId != 0) {
                emotionIcon.setImageResource(resId)
                rowLayout.addView(emotionIcon)
            }

            val label = TextView(this)
            label.text = "${emotion.replaceFirstChar { it.uppercase(Locale.getDefault()) }} $percentage%"
            label.textSize = 20f
            label.setTextColor(Color.parseColor("#555555"))
            try { label.typeface = ResourcesCompat.getFont(this, R.font.gamja_flower) } catch (e: Exception) {}

            rowLayout.addView(label)
            container.addView(rowLayout)

            val progressBar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal)
            progressBar.max = 100
            progressBar.progress = percentage
            progressBar.progressTintList = android.content.res.ColorStateList.valueOf(colorInt)
            progressBar.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 20
            ).apply { bottomMargin = 24 }

            container.addView(progressBar)
        }
    }

    private fun getEmotionColor(emotion: String): Int {
        val hexColor = emotionColors[emotion.lowercase(Locale.getDefault())] ?: "#F4C2C2"
        return Color.parseColor(hexColor)
    }
}