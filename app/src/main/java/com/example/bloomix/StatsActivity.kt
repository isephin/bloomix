package com.example.bloomix

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.res.ResourcesCompat
import java.util.Calendar
import java.util.Locale

class StatsActivity : AppCompatActivity() {

    private val emotionColors = mapOf(
        "loved" to "#FF88AA", "annoyed" to "#FF6666", "angry" to "#FF3300",
        "stressed" to "#FFAA66", "happy" to "#FFDD66", "confused" to "#FFFF66",
        "excited" to "#66FF66", "bored" to "#66CCCC", "calm" to "#66CCCC",
        "sad" to "#3366FF", "shocked" to "#6633CC", "tired" to "#AAAAAA",
        "neutral" to "#A9A9A9"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)

        val cal = Calendar.getInstance()
        val month = intent.getIntExtra("month", cal.get(Calendar.MONTH))
        val year = intent.getIntExtra("year", cal.get(Calendar.YEAR))

        findViewById<TextView>(R.id.tvStatsDate).text = "$year.${String.format("%02d", month + 1)}"

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        loadStats(year, month)
    }

    private fun loadStats(year: Int, month: Int) {
        val prefs = getSharedPreferences("journal_data", MODE_PRIVATE)
        val allKeys = prefs.all.keys
        val prefix = "flower_$year-${month + 1}-"

        val flowerCounts = mutableMapOf<String, Int>()
        val emotionCounts = mutableMapOf<String, Int>()
        val dailyEmotionsMap = mutableMapOf<Int, List<String>>()
        val dailySentimentScores = mutableMapOf<Int, Int>()

        allKeys.filter { it.startsWith(prefix) }.forEach { key ->
            val dateKey = key.removePrefix("flower_")
            val day = dateKey.split("-").last().toIntOrNull() ?: 0

            val flowerKey = prefs.getString(key, "white_daisy") ?: "white_daisy"
            flowerCounts[flowerKey] = flowerCounts.getOrDefault(flowerKey, 0) + 1

            val emotionsStr = prefs.getString("emotions_$dateKey", "")
            val dayEmotions = if (!emotionsStr.isNullOrEmpty()) emotionsStr.split(",") else emptyList()

            dailyEmotionsMap[day] = dayEmotions

            var posCount = 0.0
            dayEmotions.forEach { emo ->
                val clean = emo.trim().lowercase()
                emotionCounts[clean] = emotionCounts.getOrDefault(clean, 0) + 1
                if (clean in listOf("happy", "excited", "loved", "calm", "grateful")) posCount++
            }

            val score = if (dayEmotions.isNotEmpty()) (posCount / dayEmotions.size * 100).toInt() else 0
            dailySentimentScores[day] = score
        }

        setupGardenList(flowerCounts)
        setupEmotionsList(emotionCounts)
        setupDailyChart(dailyEmotionsMap)
        setupPositiveIndex(dailySentimentScores)
    }

    // --- 1. GARDEN SETUP ---
    private fun setupGardenList(flowers: Map<String, Int>) {
        val container = findViewById<LinearLayout>(R.id.gardenContainer)
        container.removeAllViews()
        findViewById<TextView>(R.id.tvFlowerCount).text = "${flowers.values.sum()}"

        // Click on the whole card to open dialog
        findViewById<View>(R.id.cardGarden).setOnClickListener {
            showGardenDialog(flowers)
        }

        if (flowers.isEmpty()) {
            val empty = TextView(this)
            empty.text = "No flowers yet."
            applyGamjaFont(empty)
            container.addView(empty)
            return
        }

        flowers.entries.sortedByDescending { it.value }.forEach { (key, count) ->
            val card = createFlowerCard(key, count)
            container.addView(card)
        }
    }

    private fun showGardenDialog(flowers: Map<String, Int>) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_garden)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val width = (resources.displayMetrics.widthPixels * 0.95).toInt()
        dialog.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)

        val container = dialog.findViewById<LinearLayout>(R.id.gardenGrid)
        container.removeAllViews()

        dialog.findViewById<View>(R.id.btnCloseGarden)?.setOnClickListener {
            dialog.dismiss()
        }

        flowers.entries.sortedByDescending { it.value }.forEach { (key, count) ->
            val row = LinearLayout(this)
            row.orientation = LinearLayout.HORIZONTAL
            row.gravity = Gravity.CENTER_VERTICAL
            row.setPadding(0, 0, 0, 32)

            val img = ImageView(this)
            val info = FlowerData.flowers[key]
            img.setImageResource(info?.drawable ?: R.drawable.white_daisy)
            img.layoutParams = LinearLayout.LayoutParams(200, 200) // Very Big Image

            val textLayout = LinearLayout(this)
            textLayout.orientation = LinearLayout.VERTICAL
            textLayout.setPadding(32, 0, 0, 0)

            val name = TextView(this)
            name.text = info?.name ?: "Flower"
            name.textSize = 28f // Big Font
            applyGamjaFont(name)
            name.setTextColor(Color.DKGRAY)

            val countTxt = TextView(this)
            countTxt.text = "Collected: $count"
            countTxt.textSize = 20f
            applyGamjaFont(countTxt)
            countTxt.setTextColor(Color.GRAY)

            textLayout.addView(name)
            textLayout.addView(countTxt)

            row.addView(img)
            row.addView(textLayout)
            container.addView(row)
        }
        dialog.show()
    }

    private fun createFlowerCard(key: String, count: Int): View {
        val card = CardView(this)
        val params = LinearLayout.LayoutParams(320, 450) // Bigger Card
        params.marginEnd = 24
        card.layoutParams = params
        card.radius = 24f
        card.cardElevation = 0f
        card.setCardBackgroundColor(Color.parseColor("#F5F5F5"))

        val innerLayout = LinearLayout(this)
        innerLayout.orientation = LinearLayout.VERTICAL
        innerLayout.gravity = Gravity.CENTER
        innerLayout.setPadding(0, 24, 0, 24)

        val img = ImageView(this)
        val info = FlowerData.flowers[key]
        img.setImageResource(info?.drawable ?: R.drawable.white_daisy)
        img.layoutParams = LinearLayout.LayoutParams(160, 160)

        val name = TextView(this)
        name.text = info?.name ?: "Flower"
        name.gravity = Gravity.CENTER
        applyGamjaFont(name)
        name.textSize = 22f
        name.setTextColor(Color.DKGRAY)
        name.setPadding(0, 16, 0, 8)

        val badge = TextView(this)
        badge.text = "$count"
        badge.textSize = 14f
        badge.setTextColor(Color.WHITE)
        badge.gravity = Gravity.CENTER
        badge.background = ResourcesCompat.getDrawable(resources, R.drawable.rounded_red_badge, null)
        badge.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.GRAY)
        badge.layoutParams = LinearLayout.LayoutParams(70, 50)

        innerLayout.addView(img)
        innerLayout.addView(name)
        innerLayout.addView(badge)
        card.addView(innerLayout)
        return card
    }

    // --- 2. EMOTIONS LIST ---
    private fun setupEmotionsList(counts: Map<String, Int>) {
        val container = findViewById<LinearLayout>(R.id.emotionsListContainer)
        val btnMore = findViewById<TextView>(R.id.btnShowMore)
        container.removeAllViews()

        val total = counts.values.sum()
        val sorted = counts.entries.sortedByDescending { it.value }

        fun render(limit: Int) {
            container.removeAllViews()
            sorted.take(limit).forEach { (emo, count) ->
                val percent = if(total>0) (count * 100 / total) else 0
                container.addView(createEmotionRow(emo, count, percent))
            }
        }
        render(4)

        if (sorted.size > 4) {
            btnMore.visibility = View.VISIBLE
            var expanded = false
            btnMore.setOnClickListener {
                if (!expanded) {
                    render(sorted.size)
                    btnMore.text = "View Less ^"
                } else {
                    render(4)
                    btnMore.text = "View More v"
                }
                expanded = !expanded
            }
        } else {
            btnMore.visibility = View.GONE
        }
    }

    private fun createEmotionRow(emotion: String, count: Int, percent: Int): View {
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { bottomMargin = 24 }

        val headerRow = LinearLayout(this)
        headerRow.orientation = LinearLayout.HORIZONTAL
        headerRow.gravity = Gravity.CENTER_VERTICAL

        val icon = ImageView(this)
        var resId = resources.getIdentifier("em_$emotion", "drawable", packageName)
        if (resId == 0) resId = resources.getIdentifier("${emotion}_chip", "drawable", packageName)
        if (resId != 0) icon.setImageResource(resId)
        icon.layoutParams = LinearLayout.LayoutParams(70, 70).apply { marginEnd = 16 }

        val text = TextView(this)
        text.text = "${emotion.replaceFirstChar { it.uppercase() }} $count ($percent%)"
        text.textSize = 22f // Bigger Font
        text.setTextColor(Color.parseColor("#333333"))
        applyGamjaFont(text)

        headerRow.addView(icon)
        headerRow.addView(text)

        val pb = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal)
        pb.max = 100
        pb.progress = percent
        pb.progressTintList = android.content.res.ColorStateList.valueOf(getEmotionColor(emotion))
        val pbParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 20) // Thicker Bar
        pbParams.topMargin = 12
        pb.layoutParams = pbParams

        layout.addView(headerRow)
        layout.addView(pb)
        return layout
    }

    // --- 3. DAILY CHART (Thicker Bars) ---
    private fun setupDailyChart(dailyData: Map<Int, List<String>>) {
        val container = findViewById<LinearLayout>(R.id.dailyChartContainer)
        val avgTxt = findViewById<TextView>(R.id.tvAvgEmotions)
        container.removeAllViews()

        var totalDailyEmotions = 0
        if (dailyData.isNotEmpty()) {
            totalDailyEmotions = dailyData.values.sumOf { it.size }
            val avg = totalDailyEmotions / dailyData.size
            avgTxt.text = "avg. $avg"
        }

        for (d in 1..31) {
            val emotions = dailyData[d] ?: emptyList()
            if (emotions.isNotEmpty()) {
                container.addView(createStackedBar(d, emotions))
            }
        }
    }

    private fun createStackedBar(day: Int, emotions: List<String>): View {
        val barContainer = LinearLayout(this)
        barContainer.orientation = LinearLayout.VERTICAL
        barContainer.gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        val params = LinearLayout.LayoutParams(100, LinearLayout.LayoutParams.MATCH_PARENT) // Wider space
        params.marginEnd = 12
        barContainer.layoutParams = params

        val countText = TextView(this)
        countText.text = "${emotions.size}"
        countText.gravity = Gravity.CENTER
        countText.textSize = 14f
        applyGamjaFont(countText)
        barContainer.addView(countText)

        val stack = LinearLayout(this)
        stack.orientation = LinearLayout.VERTICAL
        val height = (emotions.size * 40).coerceAtMost(500) // Taller Bars
        stack.layoutParams = LinearLayout.LayoutParams(40, height) // Thicker Bars (40)

        emotions.forEach { emo ->
            val segment = View(this)
            segment.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f)
            segment.setBackgroundColor(getEmotionColor(emo.trim()))
            stack.addView(segment)
        }
        barContainer.addView(stack)

        val dayText = TextView(this)
        dayText.text = "$day"
        dayText.gravity = Gravity.CENTER
        dayText.textSize = 16f
        applyGamjaFont(dayText)
        dayText.setTextColor(Color.GRAY)
        barContainer.addView(dayText)

        return barContainer
    }

    // --- 4. POSITIVE INDEX ---
    private fun setupPositiveIndex(dailyScores: Map<Int, Int>) {
        val container = findViewById<LinearLayout>(R.id.positiveIndexContainer)
        val avgTxt = findViewById<TextView>(R.id.tvAvgPositive)
        container.removeAllViews()

        var totalScore = 0
        var scoredDays = 0

        if (dailyScores.isNotEmpty()) {
            totalScore = dailyScores.values.sum()
            scoredDays = dailyScores.size
            val avg = if (scoredDays > 0) totalScore / scoredDays else 0
            avgTxt.text = "avg. $avg%"
        }

        for (d in 1..31) {
            val score = dailyScores[d]
            if (score != null) {
                val color = if(score >= 50) "#66FF66" else "#FF6666"
                container.addView(createBar(d, score, color))
            }
        }
    }

    private fun createBar(label: Int, value: Int, colorHex: String): View {
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        val params = LinearLayout.LayoutParams(100, LinearLayout.LayoutParams.MATCH_PARENT)
        params.marginEnd = 12
        layout.layoutParams = params

        val valTxt = TextView(this)
        valTxt.text = "$value"
        valTxt.textSize = 12f
        applyGamjaFont(valTxt)
        valTxt.gravity = Gravity.CENTER

        val bar = View(this)
        val h = (value * 5).coerceAtMost(400).coerceAtLeast(15)
        bar.layoutParams = LinearLayout.LayoutParams(30, h) // Thicker bars (30)
        bar.background = ResourcesCompat.getDrawable(resources, R.drawable.rounded_corner_background, null)
        bar.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor(colorHex))

        val lblTxt = TextView(this)
        lblTxt.text = "$label"
        lblTxt.textSize = 16f
        applyGamjaFont(lblTxt)
        lblTxt.gravity = Gravity.CENTER

        layout.addView(valTxt)
        layout.addView(bar)
        layout.addView(lblTxt)
        return layout
    }

    private fun applyGamjaFont(textView: TextView) {
        try {
            textView.typeface = ResourcesCompat.getFont(this, R.font.gamja_flower)
        } catch (e: Exception) { }
    }

    private fun getEmotionColor(emotion: String): Int {
        val hex = emotionColors[emotion.trim().lowercase()] ?: "#F4C2C2"
        return Color.parseColor(hex)
    }

    private fun getMonthName(monthIndex: Int): String {
        return listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")[monthIndex]
    }
}