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
import androidx.lifecycle.lifecycleScope // <--- New Import
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch // <--- New Import
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.collections.LinkedHashMap
import kotlin.math.abs

class CalendarActivity : AppCompatActivity(), DayAdapter.OnDayClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DayAdapter
    private lateinit var tvMonthTitle: TextView

    private var daysList = mutableListOf<DayModel>()

    private var currentYear = Calendar.getInstance().get(Calendar.YEAR)
    private var currentMonth = Calendar.getInstance().get(Calendar.MONTH)

    private val REQUEST_EMOTION = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        recyclerView = findViewById(R.id.recyclerViewCalendar)
        tvMonthTitle = findViewById(R.id.tvMonthTitle)

        recyclerView.layoutManager = GridLayoutManager(this, 7)

        // Initialize with empty map, it will update in a second
        adapter = DayAdapter(daysList, emptyMap(), this)
        recyclerView.adapter = adapter

        updateCalendar()

        findViewById<ImageView>(R.id.btnPrev).setOnClickListener {
            changeMonth(-1)
        }

        findViewById<ImageView>(R.id.btnNext).setOnClickListener {
            changeMonth(1)
        }

        findViewById<ImageView>(R.id.btnHistory).setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            intent.putExtra("month", currentMonth)
            intent.putExtra("year", currentYear)
            startActivity(intent)
        }

        findViewById<ImageView>(R.id.iconSettings).setOnClickListener {
            showSettingsDialog()
        }

        findViewById<ImageView>(R.id.iconBarChart).setOnClickListener {
            val intent = Intent(this, StatsActivity::class.java)
            intent.putExtra("month", currentMonth)
            intent.putExtra("year", currentYear)
            startActivity(intent)
        }

        // --- SWIPE LISTENER ---
        val swipeListener = object : OnSwipeTouchListener(this@CalendarActivity) {
            override fun onSwipeRight() { changeMonth(-1) }
            override fun onSwipeLeft() { changeMonth(1) }
        }
        recyclerView.setOnTouchListener(swipeListener)
        findViewById<View>(android.R.id.content).setOnTouchListener(swipeListener)
    }

    override fun onResume() {
        super.onResume()
        updateCalendar()
    }

    // ... (Settings Dialog methods remain exactly the same as before) ...
    private fun showSettingsDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_settings)

        val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
        val tvNicknameLabel = dialog.findViewById<TextView>(R.id.tvNicknameLabel)
        val currentNickname = prefs.getString("user_nickname", "Bloomix User")
        tvNicknameLabel.text = "Nickname: $currentNickname"

        dialog.findViewById<ImageView>(R.id.btnBack).setOnClickListener { dialog.dismiss() }

        dialog.findViewById<TextView>(R.id.btnChangeNickname).setOnClickListener {
            dialog.dismiss()
            showChangeNicknameDialog()
        }

        dialog.findViewById<Button>(R.id.btnLogout).setOnClickListener {
            val intent = Intent(this, SignUp::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            dialog.dismiss()
            finish()
        }

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
            showSettingsDialog()
        }

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

    private fun changeMonth(offset: Int) {
        currentMonth += offset
        if (currentMonth < 0) {
            currentMonth = 11
            currentYear--
        } else if (currentMonth > 11) {
            currentMonth = 0
            currentYear++
        }
        updateCalendar()
    }

    // --- UPDATED: NOW USES DATABASE ---
    private fun updateCalendar() {
        tvMonthTitle.text = getMonthName(currentMonth) + " " + currentYear
        generateMonthDays(currentYear, currentMonth)

        // Launch a coroutine to fetch data from Room
        lifecycleScope.launch {
            val entries = AppDatabase.getDatabase(applicationContext)
                .journalDao()
                .getEntriesForMonth(currentYear, currentMonth) // Note: Room stores month 0-11

            val map = LinkedHashMap<String, String>()
            entries.forEach { entry ->
                map[entry.dateKey] = entry.flowerKey
            }

            adapter.updateFlowers(map)
            adapter.notifyDataSetChanged()
        }
    }

    private fun generateMonthDays(year: Int, month: Int) {
        daysList.clear()
        val cal = Calendar.getInstance()
        cal.set(year, month, 1)
        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val offset = when (firstDayOfWeek) {
            Calendar.SUNDAY -> 6
            else -> firstDayOfWeek - 2
        }
        for (i in 0 until offset) daysList.add(DayModel(null, null))
        for (day in 1..daysInMonth) {
            val dateKey = "$year-${month + 1}-$day"
            daysList.add(DayModel(day, dateKey))
        }
        while (daysList.size % 7 != 0) daysList.add(DayModel(null, null))
    }

    // --- UPDATED: NOW USES DATABASE ---
    override fun onDayClicked(dateKey: String) {
        if (isFutureDate(dateKey)) {
            Toast.makeText(this, "You cannot journal for future dates!", Toast.LENGTH_SHORT).show()
            return
        }

        // Check DB for existing entry
        lifecycleScope.launch {
            val existingEntry = AppDatabase.getDatabase(applicationContext)
                .journalDao()
                .getEntryByDate(dateKey)

            if (existingEntry != null) {
                // Entry Exists -> Go to Result
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
                // No Entry -> Go to Emotion Selection
                val intent = Intent(this@CalendarActivity, EmotionActivity::class.java)
                intent.putExtra("selectedDate", dateKey)
                startActivityForResult(intent, REQUEST_EMOTION)
            }
        }
    }

    private fun isFutureDate(dateKey: String): Boolean {
        return try {
            val sdf = SimpleDateFormat("yyyy-M-d", Locale.getDefault())
            val selectedDate = sdf.parse(dateKey) ?: return false
            val selectedCal = Calendar.getInstance()
            selectedCal.time = selectedDate
            val today = Calendar.getInstance()

            if (selectedCal.get(Calendar.YEAR) > today.get(Calendar.YEAR)) return true
            if (selectedCal.get(Calendar.YEAR) < today.get(Calendar.YEAR)) return false
            if (selectedCal.get(Calendar.MONTH) > today.get(Calendar.MONTH)) return true
            if (selectedCal.get(Calendar.MONTH) < today.get(Calendar.MONTH)) return false
            selectedCal.get(Calendar.DAY_OF_MONTH) > today.get(Calendar.DAY_OF_MONTH)
        } catch (e: Exception) { false }
    }

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

    // --- HELPER CLASS FOR SWIPE DETECTION (Same as before) ---
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