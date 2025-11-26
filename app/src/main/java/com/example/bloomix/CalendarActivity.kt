package com.example.bloomix

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
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
        adapter = DayAdapter(daysList, loadFlowersMap(), this)
        recyclerView.adapter = adapter

        updateCalendar()

        findViewById<ImageView>(R.id.btnPrev).setOnClickListener {
            changeMonth(-1)
        }

        findViewById<ImageView>(R.id.btnNext).setOnClickListener {
            changeMonth(1)
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
        val intent = Intent(this, EmotionActivity::class.java)
        intent.putExtra("selectedDate", dateKey)
        startActivityForResult(intent, REQUEST_EMOTION)
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

