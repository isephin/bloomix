package com.example.bloomix

import android.content.Intent
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs

class HistoryActivity : AppCompatActivity() {

    private var currentYear = 0
    private var currentMonth = 0
    private lateinit var title: TextView
    private lateinit var rvHistory: RecyclerView
    private lateinit var tvEmptyState: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        val cal = Calendar.getInstance()
        // Get month/year from Intent or default to Today
        currentYear = intent.getIntExtra("year", cal.get(Calendar.YEAR))
        currentMonth = intent.getIntExtra("month", cal.get(Calendar.MONTH))

        title = findViewById(R.id.tvHistoryTitle)
        rvHistory = findViewById(R.id.rvHistory)
        tvEmptyState = findViewById(R.id.tvEmptyState)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        findViewById<ImageButton>(R.id.btnPrevMonth).setOnClickListener { changeMonth(-1) }
        findViewById<ImageButton>(R.id.btnNextMonth).setOnClickListener { changeMonth(1) }

        // Setup Recycler View
        rvHistory.layoutManager = LinearLayoutManager(this)

        // Add Swipe Listener
        val swipeListener = object : OnSwipeTouchListener(this@HistoryActivity) {
            override fun onSwipeRight() {
                changeMonth(-1) // Previous Month
            }
            override fun onSwipeLeft() {
                changeMonth(1) // Next Month
            }
        }
        rvHistory.setOnTouchListener(swipeListener)
        // Allow swiping on empty areas too
        findViewById<View>(android.R.id.content).setOnTouchListener(swipeListener)

        loadEntriesForMonth()
    }

    private fun changeMonth(offset: Int) {
        currentMonth += offset
        if (currentMonth < 0) {
            currentMonth = 11
            currentYear--
        } else if (currentMonth > 11) {
            currentMonth = 0
            currentYear++
        }
        loadEntriesForMonth()
    }

    // --- OPTIMIZATION: Load data in Background Thread ---
    private fun loadEntriesForMonth() {
        // Update Title immediately (UI operation)
        title.text = "$currentYear.${String.format("%02d", currentMonth + 1)}"

        // Switch to Background Thread for heavy lifting
        CoroutineScope(Dispatchers.IO).launch {
            val historyList = getEntriesFor(currentYear, currentMonth)

            // Switch back to Main Thread to update UI
            withContext(Dispatchers.Main) {
                if (historyList.isEmpty()) {
                    rvHistory.visibility = View.GONE
                    try {
                        tvEmptyState.visibility = View.VISIBLE
                    } catch (e: Exception) {}
                } else {
                    rvHistory.visibility = View.VISIBLE
                    try {
                        tvEmptyState.visibility = View.GONE
                    } catch (e: Exception) {}

                    rvHistory.adapter = HistoryAdapter(historyList) { item ->
                        openResult(item)
                    }
                }
            }
        }
    }

    private fun openResult(item: HistoryItem) {
        val intent = Intent(this, FlowerResultActivity::class.java)

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

    // Heavy processing (Moved to background by loadEntriesForMonth)
    private fun getEntriesFor(year: Int, month: Int): List<HistoryItem> {
        val prefs = getSharedPreferences("journal_data", MODE_PRIVATE)
        val allEntries = prefs.all
        val list = mutableListOf<HistoryItem>()

        val dayFormatter = SimpleDateFormat("d", Locale.getDefault())
        val nameFormatter = SimpleDateFormat("EEE", Locale.getDefault())
        val headerFormatter = SimpleDateFormat("yyyy.MM", Locale.getDefault())
        val parser = SimpleDateFormat("yyyy-M-d", Locale.getDefault())

        // Search Prefix: "flower_2025-11-"
        val searchPrefix = "flower_$year-${month + 1}-"

        allEntries.keys.forEach { key ->
            if (key.startsWith(searchPrefix)) {
                val dateKey = key.removePrefix("flower_")
                val flowerKey = prefs.getString(key, "white_daisy") ?: "white_daisy"
                val emotionsStr = prefs.getString("emotions_$dateKey", "")
                val emotions = if (emotionsStr.isNullOrEmpty()) emptyList() else emotionsStr.split(",")

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
        return list.sortedByDescending { it.dateObj }
    }

    private fun getFlowerKeyFromName(name: String): String {
        return FlowerData.flowers.entries.find { it.value.name == name }?.key ?: "white_daisy"
    }

    // --- Inner Class for Swipe Detection ---
    open class OnSwipeTouchListener(ctx: android.content.Context) : View.OnTouchListener {
        private val gestureDetector = GestureDetector(ctx, GestureListener())

        override fun onTouch(v: View, event: MotionEvent): Boolean {
            return gestureDetector.onTouchEvent(event)
        }

        private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
            private val SWIPE_THRESHOLD = 100
            private val SWIPE_VELOCITY_THRESHOLD = 100

            override fun onDown(e: MotionEvent): Boolean {
                return true
            }

            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                if (e1 == null) return false
                val diffY = e2.y - e1.y
                val diffX = e2.x - e1.x
                if (abs(diffX) > abs(diffY)) {
                    if (abs(diffX) > SWIPE_THRESHOLD && abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            onSwipeRight()
                        } else {
                            onSwipeLeft()
                        }
                        return true
                    }
                }
                return false
            }
        }

        open fun onSwipeRight() {}
        open fun onSwipeLeft() {}
    }
}