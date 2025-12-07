package com.example.bloomix

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.GestureDetector
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import kotlin.math.abs

class StatsActivity : AppCompatActivity() {

    // Helper map for emotion colors (used in the charts)
    private val emotionColors = mapOf(
        "loved" to "#FF88AA", "annoyed" to "#FF6666", "angry" to "#FF3300",
        "stressed" to "#FFAA66", "happy" to "#FFDD66", "confused" to "#FFFF66",
        "excited" to "#66FF66", "bored" to "#66CCCC", "calm" to "#66CCCC",
        "sad" to "#3366FF", "shocked" to "#6633CC", "tired" to "#AAAAAA",
        "neutral" to "#A9A9A9"
    )

    private var currentMonth = 0
    private var currentYear = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)

        // 1. Get the target month/year from Intent (defaults to today)
        val cal = Calendar.getInstance()
        currentMonth = intent.getIntExtra("month", cal.get(Calendar.MONTH))
        currentYear = intent.getIntExtra("year", cal.get(Calendar.YEAR))

        // 2. Setup Navigation Buttons
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<ImageButton>(R.id.btnPrevMonth).setOnClickListener { changeMonth(-1) }
        findViewById<ImageButton>(R.id.btnNextMonth).setOnClickListener { changeMonth(1) }

        // 3. Setup Swipe Gestures
        val swipeListener = object : OnSwipeTouchListener(this@StatsActivity) {
            override fun onSwipeRight() { changeMonth(-1) }
            override fun onSwipeLeft() { changeMonth(1) }
        }
        // Attach to main layout and scrollview to catch touches everywhere
        findViewById<View>(R.id.statsRoot).setOnTouchListener(swipeListener)
        findViewById<ScrollView>(R.id.statsScrollView).setOnTouchListener(swipeListener)

        // 4. Initial Load
        loadStats()
    }

    /**
     * Logic to handle month navigation (wrapping Dec -> Jan)
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
        loadStats()
    }

    /**
     * CORE FUNCTION:
     * 1. Fetches data from DB.
     * 2. Processes statistics (Counts, Sentiment Scores) in background.
     * 3. Updates the UI on the main thread.
     */
    private fun loadStats() {
        // Update Header
        findViewById<TextView>(R.id.tvStatsDate).text = "$currentYear.${String.format("%02d", currentMonth + 1)}"

        // Start Coroutine
        lifecycleScope.launch {
            // STEP 1: Fetch raw data (I/O operation)
            val entries = AppDatabase.getDatabase(applicationContext)
                .journalDao()
                .getEntriesForMonth(currentYear, currentMonth)

            // STEP 2: Process Heavy Logic (CPU operation) on DEFAULT dispatcher
            withContext(Dispatchers.Default) {
                val flowerCounts = mutableMapOf<String, Int>()
                val emotionCounts = mutableMapOf<String, Int>()
                val dailyEmotionsMap = mutableMapOf<Int, List<String>>()
                val dailySentimentScores = mutableMapOf<Int, Int>()

                // REMOVED: combinedJournalText StringBuilder

                entries.forEach { entry ->
                    // Count flowers for the Garden view
                    flowerCounts[entry.flowerKey] = flowerCounts.getOrDefault(entry.flowerKey, 0) + 1

                    // Parse emotions string ("happy,sad") into list
                    val dayEmotions = if (entry.emotions.isNotEmpty()) entry.emotions.split(",") else emptyList()
                    dailyEmotionsMap[entry.day] = dayEmotions

                    // REMOVED: combinedJournalText appending

                    // Calculate a simple "Positivity Score" for the day (0-100)
                    var posCount = 0.0
                    dayEmotions.forEach { emo ->
                        val clean = emo.trim().lowercase()
                        emotionCounts[clean] = emotionCounts.getOrDefault(clean, 0) + 1
                        if (clean in listOf("happy", "excited", "loved", "calm", "grateful")) posCount++
                    }
                    // Score = % of positive emotions in that day
                    val score = if (dayEmotions.isNotEmpty()) (posCount / dayEmotions.size * 100).toInt() else 0
                    dailySentimentScores[entry.day] = score
                }

                // REMOVED: MLProcessor.extractKeyWords call

                // STEP 3: Update UI on MAIN dispatcher
                withContext(Dispatchers.Main) {
                    setupGardenList(flowerCounts)
                    setupEmotionsList(emotionCounts)
                    setupDailyChart(dailyEmotionsMap)
                    setupPositiveIndex(dailySentimentScores)

                    // REMOVED: Logic to update tvPosWords and tvNegWords (Text and Color)
                }
            }
        }
    }

    // --- 1. GARDEN SETUP (Horizontal Scroll List) ---
    private fun setupGardenList(flowers: Map<String, Int>) {
        val container = findViewById<LinearLayout>(R.id.gardenContainer)
        container.removeAllViews()
        findViewById<TextView>(R.id.tvFlowerCount).text = "${flowers.values.sum()}"

        if (flowers.isEmpty()) {
            val empty = TextView(this)
            empty.text = "No flowers yet."
            applyGamjaFont(empty)
            container.addView(empty)
            return
        }

        // Sort by most collected flowers and add cards
        flowers.entries.sortedByDescending { it.value }.forEach { (key, count) ->
            val card = createFlowerCard(key, count, flowers)
            container.addView(card)
        }

        // Clicking the main card opens the full Garden Dialog
        findViewById<View>(R.id.cardGarden).setOnClickListener {
            showGardenDialog(flowers)
        }
    }

    /** Creates the popup dialog showing all flowers collected */
    private fun showGardenDialog(flowers: Map<String, Int>) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_garden)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val width = (resources.displayMetrics.widthPixels * 0.9).toInt()
        dialog.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)

        val container = dialog.findViewById<LinearLayout>(R.id.gardenGrid)
        container.removeAllViews()

        dialog.findViewById<View>(R.id.btnCloseGarden)?.setOnClickListener { dialog.dismiss() }

        // List View inside Dialog
        flowers.entries.sortedByDescending { it.value }.forEach { (key, count) ->
            val row = LinearLayout(this)
            row.orientation = LinearLayout.HORIZONTAL
            row.gravity = Gravity.CENTER_VERTICAL
            row.setPadding(0, 0, 0, 32)

            val img = ImageView(this)
            val info = FlowerData.flowers[key]
            img.setImageResource(info?.drawable ?: R.drawable.white_daisy)
            img.layoutParams = LinearLayout.LayoutParams(180, 180)

            val textLayout = LinearLayout(this)
            textLayout.orientation = LinearLayout.VERTICAL
            textLayout.setPadding(32, 0, 0, 0)

            val name = TextView(this)
            name.text = info?.name ?: "Flower"
            name.textSize = 24f
            try { name.typeface = ResourcesCompat.getFont(this, R.font.gamja_flower) } catch (e: Exception){}
            name.setTextColor(Color.DKGRAY)

            val countTxt = TextView(this)
            countTxt.text = "Collected: $count"
            countTxt.textSize = 16f
            countTxt.setTextColor(Color.GRAY)

            textLayout.addView(name)
            textLayout.addView(countTxt)

            row.addView(img)
            row.addView(textLayout)
            container.addView(row)
        }
        dialog.show()
    }

    /** Helper to create a single Flower Card for the dashboard */
    private fun createFlowerCard(key: String, count: Int, allFlowers: Map<String, Int>): View {
        val card = CardView(this)
        val params = LinearLayout.LayoutParams(280, 400)
        params.marginEnd = 24
        card.layoutParams = params
        card.radius = 20f
        card.cardElevation = 0f
        card.setCardBackgroundColor(Color.parseColor("#F5F5F5"))

        val innerLayout = LinearLayout(this)
        innerLayout.orientation = LinearLayout.VERTICAL
        innerLayout.gravity = Gravity.CENTER
        innerLayout.setPadding(0, 20, 0, 20)

        val img = ImageView(this)
        val info = FlowerData.flowers[key]
        img.setImageResource(info?.drawable ?: R.drawable.white_daisy)
        img.layoutParams = LinearLayout.LayoutParams(140, 140)

        val name = TextView(this)
        name.text = info?.name ?: "Flower"
        name.gravity = Gravity.CENTER
        try { name.typeface = ResourcesCompat.getFont(this, R.font.gamja_flower) } catch (e: Exception){}
        name.textSize = 18f
        name.setTextColor(Color.DKGRAY)
        name.setPadding(0, 16, 0, 8)

        val badge = TextView(this)
        badge.text = "$count"
        badge.textSize = 12f
        badge.setTextColor(Color.WHITE)
        badge.gravity = Gravity.CENTER
        badge.background = ResourcesCompat.getDrawable(resources, R.drawable.rounded_red_badge, null)
        badge.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.GRAY)
        badge.layoutParams = LinearLayout.LayoutParams(60, 40)

        innerLayout.addView(img)
        innerLayout.addView(name)
        innerLayout.addView(badge)
        card.addView(innerLayout)

        card.setOnClickListener { showGardenDialog(allFlowers) }
        return card
    }

    // --- 2. EMOTION LIST SETUP ---
    private fun setupEmotionsList(counts: Map<String, Int>) {
        val container = findViewById<LinearLayout>(R.id.emotionsListContainer)
        val btnMore = findViewById<TextView>(R.id.btnShowMore)
        container.removeAllViews()

        val total = counts.values.sum()
        val sorted = counts.entries.sortedByDescending { it.value }

        // Inner function to render rows. Limit is 4 initially.
        fun render(limit: Int) {
            container.removeAllViews()
            sorted.take(limit).forEach { (emo, count) ->
                val percent = if(total>0) (count * 100 / total) else 0
                container.addView(createEmotionRow(emo, count, percent))
            }
        }
        render(4) // Initial render

        // "Show More" logic
        if (sorted.size > 4) {
            btnMore.visibility = View.VISIBLE
            var expanded = false
            btnMore.setOnClickListener {
                if (!expanded) {
                    render(sorted.size) // Show all
                    btnMore.text = "View Less ^"
                } else {
                    render(4) // Collapse
                    btnMore.text = "View More v"
                }
                expanded = !expanded
            }
        } else {
            btnMore.visibility = View.GONE
        }
    }

    /** Creates a row with Icon + Text + Progress Bar */
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
        val resId = FlowerData.getEmotionDrawable(this, emotion)
        if (resId != 0) icon.setImageResource(resId)
        icon.layoutParams = LinearLayout.LayoutParams(60, 60).apply { marginEnd = 16 }

        val text = TextView(this)
        text.text = "${emotion.replaceFirstChar { it.uppercase() }} $count ($percent%)"
        text.textSize = 22f
        text.setTextColor(Color.parseColor("#333333"))
        try { text.typeface = ResourcesCompat.getFont(this, R.font.gamja_flower) } catch (e: Exception){}

        headerRow.addView(icon)
        headerRow.addView(text)

        val pb = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal)
        pb.max = 100
        pb.progress = percent
        pb.progressTintList = android.content.res.ColorStateList.valueOf(getEmotionColor(emotion))
        val pbParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 20)
        pbParams.topMargin = 12
        pb.layoutParams = pbParams

        layout.addView(headerRow)
        layout.addView(pb)
        return layout
    }

    // --- 3. DAILY CHART SETUP (Stacked Bars) ---
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

        // Loop through all days of month to create bars
        for (d in 1..31) {
            val emotions = dailyData[d] ?: emptyList()
            if (emotions.isNotEmpty()) {
                container.addView(createStackedBar(d, emotions))
            }
        }
    }

    /** Creates a single vertical bar composed of multiple colors (emotions) */
    private fun createStackedBar(day: Int, emotions: List<String>): View {
        val barContainer = LinearLayout(this)
        barContainer.orientation = LinearLayout.VERTICAL
        barContainer.gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        val params = LinearLayout.LayoutParams(100, LinearLayout.LayoutParams.MATCH_PARENT)
        params.marginEnd = 12
        barContainer.layoutParams = params

        // Top number (Count)
        val countText = TextView(this)
        countText.text = "${emotions.size}"
        countText.gravity = Gravity.CENTER
        countText.textSize = 14f
        applyGamjaFont(countText)
        barContainer.addView(countText)

        // The colored stack
        val stack = LinearLayout(this)
        stack.orientation = LinearLayout.VERTICAL
        val height = (emotions.size * 40).coerceAtMost(500) // Cap height
        stack.layoutParams = LinearLayout.LayoutParams(40, height)

        emotions.forEach { emo ->
            val segment = View(this)
            segment.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f)
            segment.setBackgroundColor(getEmotionColor(emo.trim()))
            stack.addView(segment)
        }
        barContainer.addView(stack)

        // Bottom number (Day)
        val dayText = TextView(this)
        dayText.text = "$day"
        dayText.gravity = Gravity.CENTER
        dayText.textSize = 16f
        applyGamjaFont(dayText)
        dayText.setTextColor(Color.GRAY)
        barContainer.addView(dayText)

        return barContainer
    }

    // --- 4. POSITIVE INDEX SETUP (Green/Red Bars) ---
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
                // Green if >= 50%, Red if < 50%
                val color = if(score >= 50) "#66FF66" else "#FF6666"
                container.addView(createBar(d, score, color))
            }
        }
    }

    /** Creates a simple vertical bar */
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
        bar.layoutParams = LinearLayout.LayoutParams(30, h)
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

    // --- SWIPE GESTURE HELPER ---
    open class OnSwipeTouchListener(ctx: Context) : View.OnTouchListener {
        private val gestureDetector = GestureDetector(ctx, GestureListener())
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            if (gestureDetector.onTouchEvent(event)) return true
            return false
        }
        private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
            private val SWIPE_THRESHOLD = 100
            private val SWIPE_VELOCITY_THRESHOLD = 100
            override fun onDown(e: MotionEvent): Boolean { return false }
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