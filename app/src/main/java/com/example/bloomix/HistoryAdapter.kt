package com.example.bloomix

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.min

class HistoryAdapter(
    private val entries: List<HistoryItem>, // FIXED: Changed HistoryModel to HistoryItem
    private val onItemClick: (HistoryItem) -> Unit // FIXED: Changed HistoryModel to HistoryItem
) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dayNum: TextView = view.findViewById(R.id.tvDayNumber)
        val dayName: TextView = view.findViewById(R.id.tvDayName)
        val flowerName: TextView = view.findViewById(R.id.tvFlowerName)
        val flowerImg: ImageView = view.findViewById(R.id.ivFlower)
        val emotionContainer: LinearLayout = view.findViewById(R.id.emotionsContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = entries[position]
        val context = holder.itemView.context

        holder.dayNum.text = item.dayNumber
        holder.dayName.text = item.dayName
        holder.flowerName.text = item.flowerName
        holder.flowerImg.setImageResource(item.flowerResId)

        // --- POPULATE EMOTION ICONS ---
        holder.emotionContainer.removeAllViews()

        // Only show up to 3 icons to prevent overcrowding
        val displayCount = min(item.emotions.size, 3)

        for (i in 0 until displayCount) {
            val emotion = item.emotions[i].lowercase().trim()

            // Try to find the drawable. We check multiple naming conventions to be safe.
            // Priority: "happy" -> "em_happy" -> "happy_chip"
            var resId = context.resources.getIdentifier(emotion, "drawable", context.packageName)

            if (resId == 0) {
                resId = context.resources.getIdentifier("em_$emotion", "drawable", context.packageName)
            }
            if (resId == 0) {
                resId = context.resources.getIdentifier("${emotion}_chip", "drawable", context.packageName)
            }

            if (resId != 0) {
                val icon = ImageView(context)
                val size = 50 // roughly 20dp
                val params = LinearLayout.LayoutParams(size, size)
                params.marginEnd = 8
                icon.layoutParams = params
                icon.setImageResource(resId)
                holder.emotionContainer.addView(icon)
            }
        }

        // Add "+2" text if there are more emotions than we showed
        if (item.emotions.size > 3) {
            val moreCount = TextView(context)
            moreCount.text = "+${item.emotions.size - 3}"
            moreCount.textSize = 12f
            moreCount.setTextColor(android.graphics.Color.GRAY)
            // Center the text vertically with the icons
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.gravity = android.view.Gravity.CENTER_VERTICAL
            moreCount.layoutParams = params

            holder.emotionContainer.addView(moreCount)
        }

        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount() = entries.size
}