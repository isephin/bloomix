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

    // View Holder: Caches references to the views in the layout file (item_history_card.xml)
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dayNum: TextView = view.findViewById(R.id.tvDayNumber)
        val dayName: TextView = view.findViewById(R.id.tvDayName)
        val flowerName: TextView = view.findViewById(R.id.tvFlowerName)
        val flowerImg: ImageView = view.findViewById(R.id.ivFlower)
        val emotionContainer: LinearLayout = view.findViewById(R.id.emotionsContainer)
    }

    // Inflates the XML layout for a single row
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history_card, parent, false)
        return ViewHolder(view)
    }

    // Binds the data (HistoryItem) to the Views
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = entries[position]
        val context = holder.itemView.context

        // Set simple text fields
        holder.dayNum.text = item.dayNumber
        holder.dayName.text = item.dayName

        // --- FONT SETUP ---
        // We apply the custom "Gamja Flower" font programmatically to be safe
        holder.flowerName.text = item.flowerName
        try {
            val typeface = ResourcesCompat.getFont(context, R.font.gamja_flower)
            holder.flowerName.typeface = typeface
        } catch (e: Exception) {
            holder.flowerName.typeface = Typeface.DEFAULT_BOLD
        }
        holder.flowerName.textSize = 24f

        // Set the flower image
        holder.flowerImg.setImageResource(item.flowerResId)

        // --- DYNAMIC EMOTION ICONS ---
        // Since the number of emotions varies, we build this view dynamically in code.
        holder.emotionContainer.removeAllViews()

        // 1. Get unique list (e.g., [happy, happy, sad] -> [happy, sad])
        val uniqueEmotions = item.emotions.map { it.lowercase().trim() }.distinct()

        // 2. Limit display to 4 icons to prevent overflow
        val displayCount = min(uniqueEmotions.size, 4)

        for (i in 0 until displayCount) {
            val emotion = uniqueEmotions[i]

            // OPTIMIZATION: Use the cached lookup from FlowerData we built earlier
            val resId = FlowerData.getEmotionDrawable(context, emotion)

            if (resId != 0) {
                val icon = ImageView(context)
                val size = 60 // Fixed size for icons
                val params = LinearLayout.LayoutParams(size, size)
                params.marginEnd = 12 // Spacing between icons
                icon.layoutParams = params
                icon.setImageResource(resId)
                holder.emotionContainer.addView(icon)
            }
        }

        // 3. Add "+N" text if there are more emotions hidden (e.g., "+2")
        if (uniqueEmotions.size > displayCount) {
            val moreCount = TextView(context)
            moreCount.text = "+${uniqueEmotions.size - displayCount}"
            moreCount.textSize = 16f

            // Apply font to the "+N" text too
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

        // Set click listener for the whole card
        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount() = entries.size
}