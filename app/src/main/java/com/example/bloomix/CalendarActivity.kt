package com.example.bloomix

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.collections.LinkedHashMap
import kotlin.math.abs

class CalendarActivity : AppCompatActivity(), DayAdapter.OnDayClickListener {

    // UI Components for the calendar grid and title
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DayAdapter
    private lateinit var tvMonthTitle: TextView

    // List that holds the data for every cell in the grid (empty slots + actual days)
    private var daysList = mutableListOf<DayModel>()

    // Tracks the currently displayed year and month
    private var currentYear = Calendar.getInstance().get(Calendar.YEAR)
    private var currentMonth = Calendar.getInstance().get(Calendar.MONTH)

    // Code to identify when we return from the EmotionActivity
    private val REQUEST_EMOTION = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        // Initialize UI references
        recyclerView = findViewById(R.id.recyclerViewCalendar)
        tvMonthTitle = findViewById(R.id.tvMonthTitle)

        // Set up the grid to have 7 columns (one for each day of the week)
        recyclerView.layoutManager = GridLayoutManager(this, 7)

        // Initialize the adapter with an empty list first; data will be loaded in updateCalendar()
        adapter = DayAdapter(daysList, emptyMap(), this)
        recyclerView.adapter = adapter

        // Load the initial data for the current month
        updateCalendar()

        // --- NAVIGATION LISTENERS ---
        // Go to previous month
        findViewById<ImageView>(R.id.btnPrev).setOnClickListener {
            changeMonth(-1)
        }

        // Go to next month
        findViewById<ImageView>(R.id.btnNext).setOnClickListener {
            changeMonth(1)
        }

        // Open History Screen
        findViewById<ImageView>(R.id.btnHistory).setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            intent.putExtra("month", currentMonth)
            intent.putExtra("year", currentYear)
            startActivity(intent)
        }

        // Open Settings Dialog
        findViewById<ImageView>(R.id.iconSettings).setOnClickListener {
            showSettingsDialog()
        }

        // Open Stats/Charts Screen
        findViewById<ImageView>(R.id.iconBarChart).setOnClickListener {
            val intent = Intent(this, StatsActivity::class.java)
            intent.putExtra("month", currentMonth)
            intent.putExtra("year", currentYear)
            startActivity(intent)
        }

        // --- SWIPE GESTURE SETUP ---
        // Allows the user to swipe left/right on the screen to change months
        val swipeListener = object : OnSwipeTouchListener(this@CalendarActivity) {
            override fun onSwipeRight() { changeMonth(-1) }
            override fun onSwipeLeft() { changeMonth(1) }
        }
        // Attach listener to both the list and the background for better sensitivity
        recyclerView.setOnTouchListener(swipeListener)
        findViewById<View>(android.R.id.content).setOnTouchListener(swipeListener)
    }

    // Refresh the calendar when the user returns to this screen (e.g. after journaling)
    override fun onResume() {
        super.onResume()
        updateCalendar()
    }

    // --- DIALOGS ---

    private fun showSettingsDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_settings)

        // Retrieve nickname from SharedPreferences
        val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
        val tvNicknameLabel = dialog.findViewById<TextView>(R.id.tvNicknameLabel)
        val currentNickname = prefs.getString("user_nickname", "Bloomix User")
        tvNicknameLabel.text = "Nickname: $currentNickname"

        dialog.findViewById<ImageView>(R.id.btnBack).setOnClickListener { dialog.dismiss() }

        // Open the Rename dialog
        dialog.findViewById<TextView>(R.id.btnChangeNickname).setOnClickListener {
            dialog.dismiss()
            showChangeNicknameDialog()
        }

        // Handle Logout: Clears flags so user cannot click "Back" to return to Calendar
        dialog.findViewById<Button>(R.id.btnLogout).setOnClickListener {
            val intent = Intent(this, SignUp::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            dialog.dismiss()
            finish()
        }

        // Transparent background for the rounded corner effect
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val width = (resources.displayMetrics.widthPixels * 0.9).toInt()
        dialog.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.show()
    }

    private fun showChangeNicknameDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_change_nickname)
        val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
        val etNewNickname = dialog.findViewById<EditText>(R.id.etNewNickname)
        etNewNickname.setText(prefs.getString("user_nickname", "Bloomix User"))

        dialog.findViewById<Button>(R.id.btnCancel).setOnClickListener {
            dialog.dismiss()
            showSettingsDialog() // Go back to main settings
        }

        // Save the new nickname to SharedPreferences
        dialog.findViewById<Button>(R.id.btnSave).setOnClickListener {
            val newNickname = etNewNickname.text.toString().trim()
            if (newNickname.isNotEmpty()) {
                prefs.edit().putString("user_nickname", newNickname).apply()
                Toast.makeText(this, "Nickname saved!", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                showSettingsDialog()
            } else {
                Toast.makeText(this, "Nickname cannot be empty.", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val width = (resources.displayMetrics.widthPixels * 0.9).toInt()
        dialog.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.show()
    }

    // Logic to handle month transitions (including year wrapping)
    private fun changeMonth(offset: Int) {
        currentMonth += offset
        if (currentMonth < 0) {
            currentMonth = 11 // Go to December
            currentYear--     // Previous year
        } else if (currentMonth > 11) {
            currentMonth = 0  // Go to January
            currentYear++     // Next year
        }
        updateCalendar()
    }

    // --- CORE LOGIC: REFRESH GRID ---
    private fun updateCalendar() {
        // Update the top title (e.g., "November 2025")
        tvMonthTitle.text = getMonthName(currentMonth) + " " + currentYear

        // Calculate the grid structure (days + empty placeholders)
        generateMonthDays(currentYear, currentMonth)

        // Fetch saved flowers from the Database asynchronously
        lifecycleScope.launch {
            val entries = AppDatabase.getDatabase(applicationContext)
                .journalDao()
                .getEntriesForMonth(currentYear, currentMonth)

            // Create a map: DateKey ("2025-11-05") -> FlowerKey ("rose")
            val map = LinkedHashMap<String, String>()
            entries.forEach { entry ->
                map[entry.dateKey] = entry.flowerKey
            }

            // Pass the data to the adapter to render flower icons
            adapter.updateFlowers(map)
            adapter.notifyDataSetChanged()
        }
    }

    // Calculates the "Empty Cells" needed before the 1st of the month
    private fun generateMonthDays(year: Int, month: Int) {
        daysList.clear()
        val cal = Calendar.getInstance()
        cal.set(year, month, 1) // Set to the 1st of the month

        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 1=Sun, 2=Mon...
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        // Calculate how many blank spaces to add before day 1
        val offset = when (firstDayOfWeek) {
            Calendar.SUNDAY -> 6 // Optional: Adjust based on your preferred week start
            else -> firstDayOfWeek - 2
        }

        // Add empty placeholders
        for (i in 0 until offset) daysList.add(DayModel(null, null))

        // Add actual days
        for (day in 1..daysInMonth) {
            val dateKey = "$year-${month + 1}-$day"
            daysList.add(DayModel(day, dateKey))
        }

        // Pad the end of the grid so it looks complete (optional, pure aesthetic)
        while (daysList.size % 7 != 0) daysList.add(DayModel(null, null))
    }

    // --- CLICK HANDLER ---
    override fun onDayClicked(dateKey: String) {
        // 1. Prevent journaling for future days
        if (isFutureDate(dateKey)) {
            Toast.makeText(this, "You cannot journal for future dates!", Toast.LENGTH_SHORT).show()
            return
        }

        // 2. Check Database: Does an entry already exist for this day?
        lifecycleScope.launch {
            val existingEntry = AppDatabase.getDatabase(applicationContext)
                .journalDao()
                .getEntryByDate(dateKey)

            if (existingEntry != null) {
                // ENTRY EXISTS -> Open the Result/View screen
                val intent = Intent(this@CalendarActivity, FlowerResultActivity::class.java)
                intent.putExtra("selectedDate", dateKey)
                intent.putExtra("flower_key", existingEntry.flowerKey)
                intent.putExtra("journal_text", existingEntry.journalText)
                intent.putExtra("sentiment", existingEntry.sentiment)
                intent.putExtra("category", existingEntry.moodCategory)
                intent.putExtra("reflection", existingEntry.reflection)
                intent.putExtra("micro_action_desc", existingEntry.microAction)

                val emotionsList = ArrayList(existingEntry.emotions.split(","))
                intent.putStringArrayListExtra("selected", emotionsList)

                startActivity(intent)
            } else {
                // NO ENTRY -> Open Emotion Selection (Start new journal)
                val intent = Intent(this@CalendarActivity, EmotionActivity::class.java)
                intent.putExtra("selectedDate", dateKey)
                startActivityForResult(intent, REQUEST_EMOTION)
            }
        }
    }

    // Helper to validate dates
    private fun isFutureDate(dateKey: String): Boolean {
        return try {
            val sdf = SimpleDateFormat("yyyy-M-d", Locale.getDefault())
            val selectedDate = sdf.parse(dateKey) ?: return false
            val selectedCal = Calendar.getInstance()
            selectedCal.time = selectedDate
            val today = Calendar.getInstance()

            // Compare Year, Month, Day logic
            if (selectedCal.get(Calendar.YEAR) > today.get(Calendar.YEAR)) return true
            if (selectedCal.get(Calendar.YEAR) < today.get(Calendar.YEAR)) return false
            if (selectedCal.get(Calendar.MONTH) > today.get(Calendar.MONTH)) return true
            if (selectedCal.get(Calendar.MONTH) < today.get(Calendar.MONTH)) return false
            selectedCal.get(Calendar.DAY_OF_MONTH) > today.get(Calendar.DAY_OF_MONTH)
        } catch (e: Exception) { false }
    }

    // Helper for Activity Result (Legacy support)
    @Deprecated("Deprecated in Android")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_EMOTION && resultCode == Activity.RESULT_OK) {
            updateCalendar()
        }
    }

    private fun getMonthName(monthIndex: Int): String {
        return listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")[monthIndex]
    }

    // --- SWIPE LISTENER HELPER CLASS ---
    // Detects fling gestures to navigate months
    open class OnSwipeTouchListener(ctx: Context) : View.OnTouchListener {
        private val gestureDetector = GestureDetector(ctx, GestureListener())

        override fun onTouch(v: View, event: MotionEvent): Boolean {
            return gestureDetector.onTouchEvent(event)
        }

        private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
            private val SWIPE_THRESHOLD = 100
            private val SWIPE_VELOCITY_THRESHOLD = 100

            override fun onDown(e: MotionEvent): Boolean { return false }

            override fun onFling(e1: MotionEvent?, e2: MotionEvent, vX: Float, vY: Float): Boolean {
                if (e1 == null) return false
                try {
                    val diffY = e2.y - e1.y
                    val diffX = e2.x - e1.x
                    // Detect horizontal swipe
                    if (abs(diffX) > abs(diffY)) {
                        if (abs(diffX) > SWIPE_THRESHOLD && abs(vX) > SWIPE_VELOCITY_THRESHOLD) {
                            if (diffX > 0) onSwipeRight() else onSwipeLeft()
                            return true
                        }
                    }
                } catch (e: Exception) { e.printStackTrace() }
                return false
            }
        }
        open fun onSwipeRight() {}
        open fun onSwipeLeft() {}
    }
}