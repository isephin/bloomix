package com.example.bloomix

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Calendar
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
        // NOTE: DayModel, DayAdapter, and loadFlowersMap() depend on external files not provided,
        // but this structure is correct for the available context.
        adapter = DayAdapter(daysList, loadFlowersMap(), this)
        recyclerView.adapter = adapter

        updateCalendar()

        findViewById<ImageView>(R.id.btnPrev).setOnClickListener {
            changeMonth(-1)
        }

        findViewById<ImageView>(R.id.btnNext).setOnClickListener {
            changeMonth(1)
        }

        // Listener for the Settings icon
        findViewById<ImageView>(R.id.iconSettings).setOnClickListener {
            showSettingsDialog()
        }
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
            // NOTE: Assuming DayModel is correctly defined for null days
            daysList.add(DayModel(null, null))
        }

        for (day in 1..daysInMonth) {
            val dateKey = "$year-${month + 1}-$day"
            // NOTE: Assuming DayModel is correctly defined for numbered days
            daysList.add(DayModel(day, dateKey))
        }

        while (daysList.size % 7 != 0) {
            // NOTE: Assuming DayModel is correctly defined for null days
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
        // 1. Check if we already have data for this day
        val prefs = getSharedPreferences("journal_data", MODE_PRIVATE)
        val existingFlower = prefs.getString("flower_$dateKey", null)

        if (existingFlower != null) {
            // 2. ENTRY EXISTS: Load saved data and go directly to results
            val journalText = prefs.getString("journal_$dateKey", "")
            val sentiment = prefs.getString("sentiment_$dateKey", "NEUTRAL")

            val intent = Intent(this, FlowerResultActivity::class.java)
            intent.putExtra("flower_key", existingFlower)
            intent.putExtra("journal_text", journalText)
            intent.putExtra("sentiment", sentiment)

            startActivity(intent)
        } else {
            // 3. NO ENTRY: Start a new journal entry
            val intent = Intent(this, EmotionActivity::class.java)
            intent.putExtra("selectedDate", dateKey)
            startActivityForResult(intent, REQUEST_EMOTION)
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

    // =================================================================================
    // DIALOG LOGIC FUNCTIONS (Moved outside showSettingsDialog)
    // =================================================================================

    /**
     * Helper function to fetch the user's nickname from SharedPreferences
     * and update the label in the given Dialog.
     */
    private fun updateNicknameDisplay(dialog: Dialog) {
        val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
        val currentNickname = prefs.getString("user_nickname", "User")
        dialog.findViewById<TextView>(R.id.tvNicknameLabel)?.text = "Nickname: $currentNickname"
    }


    /**
     * Shows a dialog allowing the user to input and save a new nickname.
     * Updates the original settings dialog upon successful save.
     */
    private fun showChangeNicknameDialog(settingsDialog: Dialog) {
        val changeDialog = Dialog(this)
        // NOTE: Assumes R.layout.dialog_change_nickname exists
        changeDialog.setContentView(R.layout.dialog_change_nickname)

        val etNewNickname = changeDialog.findViewById<EditText>(R.id.etNewNickname)
        val btnCancel = changeDialog.findViewById<Button>(R.id.btnCancel)
        val btnSave = changeDialog.findViewById<Button>(R.id.btnSave)

        // Load current nickname to pre-fill the EditText
        val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
        val currentNickname = prefs.getString("user_nickname", "")
        etNewNickname.setText(currentNickname)

        // Setup Listeners
        btnCancel.setOnClickListener {
            changeDialog.dismiss()
        }

        btnSave.setOnClickListener {
            val newNickname = etNewNickname.text.toString().trim()
            if (newNickname.isNotEmpty()) {
                // 1. Save new nickname to SharedPreferences
                prefs.edit().putString("user_nickname", newNickname).apply()

                // 2. Update the nickname label in the main settings dialog
                updateNicknameDisplay(settingsDialog)

                Toast.makeText(this, "Nickname updated to $newNickname", Toast.LENGTH_SHORT).show()
                changeDialog.dismiss()
            } else {
                Toast.makeText(this, "Nickname cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }

        // Set the dialog to be wider
        changeDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val width = (resources.displayMetrics.widthPixels * 0.9).toInt()
        changeDialog.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)

        changeDialog.show()
    }


    /**
     * Main function to display the settings dialog.
     */
    private fun showSettingsDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_settings)

        // Set up the close/back button
        val btnBack = dialog.findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener {
            dialog.dismiss()
        }

        // Set the current nickname display
        updateNicknameDisplay(dialog)

        // Set up the Change Nickname button
        val btnChangeNickname = dialog.findViewById<TextView>(R.id.btnChangeNickname)
        btnChangeNickname.setOnClickListener {
            // Call the nickname input dialog, passing the main settings dialog as a reference
            showChangeNicknameDialog(dialog)
        }

        // Set up the Log Out button
        val btnLogout = dialog.findViewById<Button>(R.id.btnLogout)
        btnLogout.setOnClickListener {
            // 1. CLEAR LOGIN/SESSION DATA
            val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
            prefs.edit().clear().apply()

            // 2. NAVIGATE TO SIGN UP/LOGIN SCREEN
            // IMPORTANT: Change SignUp::class.java if your activity name is different
            val intent = Intent(this, SignUp::class.java)

            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent)

            dialog.dismiss()
            finish()
        }

        // Set the dialog width
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val width = (resources.displayMetrics.widthPixels * 0.9).toInt()
        dialog.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)

        dialog.show()
    }
}