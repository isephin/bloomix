package com.example.bloomix

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val rvHistory = findViewById<RecyclerView>(R.id.rvHistory)
        val title = findViewById<TextView>(R.id.tvHistoryTitle)

        // Set current month/year in title (Optional dynamic logic)
        val sdf = SimpleDateFormat("yyyy.MM", Locale.getDefault())
        title.text = sdf.format(Date())

        btnBack.setOnClickListener { finish() }

        // Fetch Data
        val historyList = getAllEntries()

        // Setup Recycler View
        rvHistory.layoutManager = LinearLayoutManager(this)
        rvHistory.adapter = HistoryAdapter(historyList) { item ->
            // On Click: Open Result View for that day
            val intent = Intent(this, FlowerResultActivity::class.java)

            // Fetch saved details to pass forward
            val prefs = getSharedPreferences("journal_data", MODE_PRIVATE)
            val journalText = prefs.getString("journal_${item.dateKey}", "")
            val sentiment = prefs.getString("sentiment_${item.dateKey}", "NEUTRAL")
            val category = prefs.getString("category_${item.dateKey}", "Complex")
            val reflection = prefs.getString("reflection_${item.dateKey}", "")
            val microAction = prefs.getString("micro_action_desc_${item.dateKey}", "")

            intent.putExtra("selectedDate", item.dateKey)
            intent.putExtra("flower_key", getFlowerKeyFromName(item.flowerName))
            intent.putExtra("journal_text", journalText)
            intent.putExtra("sentiment", sentiment)
            intent.putExtra("category", category)
            intent.putExtra("reflection", reflection)
            intent.putExtra("micro_action_desc", microAction)
            intent.putStringArrayListExtra("selected", ArrayList(item.emotions))

            startActivity(intent)
        }
    }

    private fun getAllEntries(): List<HistoryItem> {
        val prefs = getSharedPreferences("journal_data", MODE_PRIVATE)
        val allEntries = prefs.all
        val list = mutableListOf<HistoryItem>()

        val parser = SimpleDateFormat("yyyy-M-d", Locale.getDefault())
        val dayFormatter = SimpleDateFormat("d", Locale.getDefault()) // "1", "31"
        val nameFormatter = SimpleDateFormat("EEE", Locale.getDefault()) // "Mon", "Sun"
        val headerFormatter = SimpleDateFormat("yyyy.MM", Locale.getDefault()) // "2025.09"

        // Filter keys to find only flowers (which indicate an entry exists)
        allEntries.keys.forEach { key ->
            if (key.startsWith("flower_")) {
                val dateKey = key.removePrefix("flower_") // "2025-11-05"
                val flowerKey = prefs.getString(key, "white_daisy") ?: "white_daisy"

                // Get emotions list
                val emotionsStr = prefs.getString("emotions_$dateKey", "")
                val emotions = if (emotionsStr.isNullOrEmpty()) emptyList() else emotionsStr.split(",")

                // Parse Date
                try {
                    val date = try {
                        parser.parse(dateKey)
                    } catch (e: Exception) {
                        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateKey)
                    } ?: Date()

                    val flowerInfo = FlowerData.flowers[flowerKey]

                    list.add(HistoryItem(
                        dateKey = dateKey,
                        dateObj = date,
                        dayNumber = dayFormatter.format(date),
                        dayName = nameFormatter.format(date),
                        monthYear = headerFormatter.format(date),
                        flowerName = flowerInfo?.name ?: "Flower",
                        flowerResId = flowerInfo?.drawable ?: R.drawable.white_daisy,
                        emotions = emotions
                    ))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        // Return sorted by date (Newest first)
        return list.sortedByDescending { it.dateObj }
    }

    // Helper to reverse lookup key from name
    private fun getFlowerKeyFromName(name: String): String {
        return FlowerData.flowers.entries.find { it.value.name == name }?.key ?: "white_daisy"
    }
}