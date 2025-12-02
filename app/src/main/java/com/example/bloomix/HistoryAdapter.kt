package com.example.bloomix

import android.graphics.Typeface
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.min

class HistoryAdapter(
    private val entries: List<HistoryItem>,
    private val onItemClick: (HistoryItem) -> Unit
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

        // Font setup for Flower Name
        holder.flowerName.text = item.flowerName
        try {
            val typeface = ResourcesCompat.getFont(context, R.font.gamja_flower)
            holder.flowerName.typeface = typeface
        } catch (e: Exception) {
            holder.flowerName.typeface = Typeface.DEFAULT_BOLD
        }
        holder.flowerName.textSize = 24f

        holder.flowerImg.setImageResource(item.flowerResId)

        // --- POPULATE EMOTION ICONS (OPTIMIZED) ---
        holder.emotionContainer.removeAllViews()

        // 1. Get unique list (e.g., [happy, happy, sad] -> [happy, sad])
        val uniqueEmotions = item.emotions.map { it.lowercase().trim() }.distinct()

        // 2. Show up to 4 icons
        val displayCount = min(uniqueEmotions.size, 4)

        for (i in 0 until displayCount) {
            val emotion = uniqueEmotions[i]

            // --- OPTIMIZATION: Use the cached lookup instead of getIdentifier ---
            val resId = FlowerData.getEmotionDrawable(context, emotion)

            if (resId != 0) {
                val icon = ImageView(context)
                val size = 60
                val params = LinearLayout.LayoutParams(size, size)
                params.marginEnd = 12
                icon.layoutParams = params
                icon.setImageResource(resId)
                holder.emotionContainer.addView(icon)
            }
        }

        // 3. Add "+N" text if there are still more unique emotions hidden
        if (uniqueEmotions.size > displayCount) {
            val moreCount = TextView(context)
            moreCount.text = "+${uniqueEmotions.size - displayCount}"
            moreCount.textSize = 16f

            try {
                val typeface = ResourcesCompat.getFont(context, R.font.gamja_flower)
                moreCount.typeface = typeface
            } catch (e: Exception) {
                moreCount.typeface = Typeface.DEFAULT_BOLD
            }

            moreCount.setTextColor(android.graphics.Color.parseColor("#757575"))

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.gravity = Gravity.CENTER_VERTICAL
            moreCount.layoutParams = params

            holder.emotionContainer.addView(moreCount)
        }

        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount() = entries.size
}