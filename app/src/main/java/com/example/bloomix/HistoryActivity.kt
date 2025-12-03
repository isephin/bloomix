package com.example.bloomix

import android.content.Intent
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs

class HistoryActivity : AppCompatActivity() {

    // Track which month we are viewing (defaults to current if not passed)
    private var currentYear = 0
    private var currentMonth = 0

    // UI References
    private lateinit var title: TextView
    private lateinit var rvHistory: RecyclerView
    private lateinit var tvEmptyState: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        // 1. Get the year/month passed from CalendarActivity
        val cal = Calendar.getInstance()
        currentYear = intent.getIntExtra("year", cal.get(Calendar.YEAR))
        currentMonth = intent.getIntExtra("month", cal.get(Calendar.MONTH))

        // 2. Initialize Views
        title = findViewById(R.id.tvHistoryTitle)
        rvHistory = findViewById(R.id.rvHistory)
        tvEmptyState = findViewById(R.id.tvEmptyState) // "No entries" text

        // 3. Setup Buttons
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        // Month Navigation Buttons
        findViewById<ImageButton>(R.id.btnPrevMonth).setOnClickListener { changeMonth(-1) }
        findViewById<ImageButton>(R.id.btnNextMonth).setOnClickListener { changeMonth(1) }

        // 4. Setup RecyclerView (The List)
        rvHistory.layoutManager = LinearLayoutManager(this)

        // 5. Setup Swipe Gestures (Left/Right swipe to change month)
        val swipeListener = object : OnSwipeTouchListener(this@HistoryActivity) {
            override fun onSwipeRight() { changeMonth(-1) }
            override fun onSwipeLeft() { changeMonth(1) }
        }
        // Attach listener to both list and background
        rvHistory.setOnTouchListener(swipeListener)
        findViewById<View>(android.R.id.content).setOnTouchListener(swipeListener)

        // 6. Initial Data Load
        loadEntriesForMonth()
    }

    /**
     * Refresh the list when the activity comes into view.
     * Essential if the user clicked an item, deleted it, and came back.
     */
    override fun onResume() {
        super.onResume()
        loadEntriesForMonth()
    }

    /**
     * Handles logic to switch months, including wrapping December <-> January.
     */
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

    /**
     * Fetches data from Room Database and updates the UI.
     */
    private fun loadEntriesForMonth() {
        // Update Title (e.g., "2025.11")
        title.text = "$currentYear.${String.format("%02d", currentMonth + 1)}"

        // Launch Coroutine for Database access
        lifecycleScope.launch {
            val entries = AppDatabase.getDatabase(applicationContext)
                .journalDao()
                .getEntriesForMonth(currentYear, currentMonth)

            // Handle Empty State
            if (entries.isEmpty()) {
                rvHistory.visibility = View.GONE
                tvEmptyState.visibility = View.VISIBLE
            } else {
                rvHistory.visibility = View.VISIBLE
                tvEmptyState.visibility = View.GONE

                // Convert Database Entities (JournalEntry) to UI Models (HistoryItem)
                // This formatting logic happens here to keep the Adapter clean.
                val historyItems = entries.map { entry ->
                    val date = try {
                        SimpleDateFormat("yyyy-M-d", Locale.getDefault()).parse(entry.dateKey)
                    } catch (e: Exception) { Date() } ?: Date()

                    val dayFormatter = SimpleDateFormat("d", Locale.getDefault())
                    val nameFormatter = SimpleDateFormat("EEE", Locale.getDefault()) // "Mon", "Tue"
                    val headerFormatter = SimpleDateFormat("yyyy.MM", Locale.getDefault())

                    // Safe lookup for flower info
                    val flowerName = FlowerData.flowers[entry.flowerKey]?.name ?: "Flower"
                    val flowerRes = FlowerData.flowers[entry.flowerKey]?.drawable ?: R.drawable.white_daisy
                    val emotionList = if (entry.emotions.isNotEmpty()) entry.emotions.split(",") else emptyList()

                    HistoryItem(
                        dateKey = entry.dateKey,
                        dateObj = date,
                        dayNumber = dayFormatter.format(date),
                        dayName = nameFormatter.format(date),
                        monthYear = headerFormatter.format(date),
                        flowerName = flowerName,
                        flowerResId = flowerRes,
                        emotions = emotionList
                    )
                }

                // Attach Adapter to RecyclerView
                rvHistory.adapter = HistoryAdapter(historyItems) { item ->
                    openResult(item)
                }
            }
        }
    }

    /**
     * Click Handler: Opens the full details for a specific entry.
     */
    private fun openResult(item: HistoryItem) {
        // We need to fetch the FULL details (journal text, reflection) from DB again
        // because HistoryItem serves a lightweight version for the list.
        lifecycleScope.launch {
            val entry = AppDatabase.getDatabase(applicationContext)
                .journalDao()
                .getEntryByDate(item.dateKey)

            if (entry != null) {
                val intent = Intent(this@HistoryActivity, FlowerResultActivity::class.java)
                intent.putExtra("selectedDate", entry.dateKey)
                intent.putExtra("flower_key", entry.flowerKey)
                intent.putExtra("journal_text", entry.journalText)
                intent.putExtra("sentiment", entry.sentiment)
                intent.putExtra("category", entry.moodCategory)
                intent.putExtra("reflection", entry.reflection)
                intent.putExtra("micro_action_desc", entry.microAction)
                intent.putStringArrayListExtra("selected", ArrayList(item.emotions))
                startActivity(intent)
            }
        }
    }

    // --- SWIPE LISTENER HELPER (Standard Android Boilerplate) ---
    open class OnSwipeTouchListener(ctx: android.content.Context) : View.OnTouchListener {
        private val gestureDetector = GestureDetector(ctx, GestureListener())
        override fun onTouch(v: View, event: MotionEvent): Boolean { return gestureDetector.onTouchEvent(event) }
        private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
            private val SWIPE_THRESHOLD = 100
            private val SWIPE_VELOCITY_THRESHOLD = 100
            override fun onDown(e: MotionEvent): Boolean { return true }
            override fun onFling(e1: MotionEvent?, e2: MotionEvent, vX: Float, vY: Float): Boolean {
                if (e1 == null) return false
                val diffY = e2.y - e1.y
                val diffX = e2.x - e1.x
                if (abs(diffX) > abs(diffY)) {
                    if (abs(diffX) > SWIPE_THRESHOLD && abs(vX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) onSwipeRight() else onSwipeLeft()
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