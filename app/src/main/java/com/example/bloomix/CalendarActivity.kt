package com.example.bloomix

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup // Required for dialog width logic
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.collections.LinkedHashMap

class CalendarActivity : AppCompatActivity(), DayAdapter.OnDayClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DayAdapter
    private lateinit var tvMonthTitle: TextView

    private var daysList = mutableListOf<DayModel>()

    private var currentYear = 2025
    private var currentMonth = Calendar.NOVEMBER   // 10

    private val REQUEST_EMOTION = 1001


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        recyclerView = findViewById(R.id.recyclerViewCalendar)
        tvMonthTitle = findViewById(R.id.tvMonthTitle)

        recyclerView.layoutManager = GridLayoutManager(this, 7)
        adapter = DayAdapter(daysList, loadFlowersMap(), this)
        recyclerView.adapter = adapter

        updateCalendar()

        findViewById<ImageView>(R.id.btnPrev).setOnClickListener {
            changeMonth(-1)
        }

        findViewById<ImageView>(R.id.btnNext).setOnClickListener {
            changeMonth(1)
        }

        // --- Navigation Button Click Listeners ---

        // Note: R.id.btnHistory is not in activity_calendar.xml, using R.id.iconTulip instead
        findViewById<ImageView>(R.id.btnHistory).setOnClickListener {
            // Assuming HistoryActivity is the correct target
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
        }

        // Settings Button (R.id.iconSettings)
        findViewById<ImageView>(R.id.iconSettings).setOnClickListener {
            showSettingsDialog()
        }

        // Statistics Button (R.id.iconBarChart)
        findViewById<ImageView>(R.id.iconBarChart).setOnClickListener {
            Toast.makeText(this, "Statistics feature coming soon!", Toast.LENGTH_SHORT).show()
        }
    }


    // --- Settings Dialog Methods ---

    private fun showSettingsDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_settings)

        val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
        val tvNicknameLabel = dialog.findViewById<TextView>(R.id.tvNicknameLabel)

        // Load and display current nickname
        val currentNickname = prefs.getString("user_nickname", "Bloomix User")
        tvNicknameLabel.text = "Nickname: $currentNickname"

        // Back button to dismiss dialog
        dialog.findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            dialog.dismiss()
        }

        // Change Nickname button
        dialog.findViewById<TextView>(R.id.btnChangeNickname).setOnClickListener {
            dialog.dismiss() // Dismiss settings dialog first
            showChangeNicknameDialog() // Show nickname dialog
        }

        // Logout button
        dialog.findViewById<Button>(R.id.btnLogout).setOnClickListener {
            val intent = Intent(this, SignUp::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            dialog.dismiss()
            finish()
        }

        // --- FIX: Set the dialog width programmatically ---
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val width = (resources.displayMetrics.widthPixels * 0.9).toInt()
        dialog.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        // ----------------------------------------------------

        dialog.show()
    }

    private fun showChangeNicknameDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_change_nickname)

        val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
        val etNewNickname = dialog.findViewById<EditText>(R.id.etNewNickname)

        // Pre-fill with current nickname
        val currentNickname = prefs.getString("user_nickname", "Bloomix User")
        etNewNickname.setText(currentNickname)

        dialog.findViewById<Button>(R.id.btnCancel).setOnClickListener {
            dialog.dismiss()
            showSettingsDialog() // Return to the settings dialog
        }

        dialog.findViewById<Button>(R.id.btnSave).setOnClickListener {
            val newNickname = etNewNickname.text.toString().trim()
            if (newNickname.isNotEmpty()) {
                prefs.edit().putString("user_nickname", newNickname).apply()
                Toast.makeText(this, "Nickname saved!", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                showSettingsDialog() // Refresh settings dialog with new nickname
            } else {
                Toast.makeText(this, "Nickname cannot be empty.", Toast.LENGTH_SHORT).show()
            }
        }

        // --- FIX: Set the dialog width programmatically ---
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val width = (resources.displayMetrics.widthPixels * 0.9).toInt()
        dialog.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        // ----------------------------------------------------

        dialog.show()
    }

    // --- Delete Entry Dialog Method ---
    fun showDeleteEntryDialog(dateKey: String) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_delete_entry)

        // Configure dialog to appear at the bottom
        val window = dialog.window
        window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        window?.setGravity(Gravity.BOTTOM)

        dialog.findViewById<TextView>(R.id.btnDeleteAction).setOnClickListener {
            val prefs = getSharedPreferences("journal_data", MODE_PRIVATE)
            prefs.edit()
                .remove("flower_$dateKey")
                .remove("journal_$dateKey")
                .remove("sentiment_$dateKey")
                .remove("category_$dateKey")
                .remove("reflection_$dateKey")
                .remove("micro_action_desc_$dateKey")
                .remove("emotions_$dateKey")
                .apply()

            Toast.makeText(this, "Entry for $dateKey deleted.", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
            updateCalendar() // Refresh the calendar view
        }
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


    private fun updateCalendar() {
        tvMonthTitle.text = getMonthName(currentMonth) + " " + currentYear
        generateMonthDays(currentYear, currentMonth)
        adapter.updateFlowers(loadFlowersMap())
        adapter.notifyDataSetChanged()
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

        for (i in 0 until offset) {
            daysList.add(DayModel(null, null))
        }

        for (day in 1..daysInMonth) {
            val dateKey = "$year-${month + 1}-$day"
            daysList.add(DayModel(day, dateKey))
        }

        while (daysList.size % 7 != 0) {
            daysList.add(DayModel(null, null))
        }
    }


    private fun loadFlowersMap(): Map<String, String> {
        val prefs = getSharedPreferences("journal_data", MODE_PRIVATE)
        val map = LinkedHashMap<String, String>()

        // Load ALL possible day keys in month
        for (d in 1..31) {
            val key = "$currentYear-${currentMonth + 1}-$d"
            prefs.getString("flower_$key", null)?.let { map[key] = it }
        }
        return map
    }


    override fun onDayClicked(dateKey: String) {
        // Block Future Dates
        if (isFutureDate(dateKey)) {
            Toast.makeText(this, "You cannot journal for future dates!", Toast.LENGTH_SHORT).show()
            return
        }

        val prefs = getSharedPreferences("journal_data", MODE_PRIVATE)
        val existingFlower = prefs.getString("flower_$dateKey", null)

        if (existingFlower != null) {
            // ENTRY EXISTS: Load ALL saved data
            val journalText = prefs.getString("journal_$dateKey", "")
            val sentiment = prefs.getString("sentiment_$dateKey", "NEUTRAL")
            val category = prefs.getString("category_$dateKey", "Complex Emotional Landscape")
            val reflection = prefs.getString("reflection_$dateKey", "Reflect on your day.")
            val microAction = prefs.getString("micro_action_desc_$dateKey", "Take a moment to breathe.")

            // Load and parse emotion list
            val emotionsStr = prefs.getString("emotions_$dateKey", "")
            val emotionsList = if (!emotionsStr.isNullOrEmpty()) {
                ArrayList(emotionsStr.split(","))
            } else {
                arrayListOf<String>()
            }

            val intent = Intent(this, FlowerResultActivity::class.java)
            intent.putExtra("selectedDate", dateKey)
            intent.putExtra("flower_key", existingFlower)
            intent.putExtra("journal_text", journalText)
            intent.putExtra("sentiment", sentiment)

            // Pass the restored AI data
            intent.putExtra("category", category)
            intent.putExtra("reflection", reflection)
            intent.putExtra("micro_action_desc", microAction)
            intent.putStringArrayListExtra("selected", emotionsList)

            startActivity(intent)

        } else {
            // NO ENTRY: Start a new journal entry
            val intent = Intent(this, EmotionActivity::class.java)
            intent.putExtra("selectedDate", dateKey)
            startActivityForResult(intent, REQUEST_EMOTION)
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
        } catch (e: Exception) {
            false
        }
    }


    @Deprecated("Deprecated in Android")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_EMOTION && resultCode == Activity.RESULT_OK) {
            updateCalendar()
        }
    }


    private fun getMonthName(monthIndex: Int): String {
        return listOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )[monthIndex]
    }
}