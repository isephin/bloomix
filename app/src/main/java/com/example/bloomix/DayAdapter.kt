package com.example.bloomix

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DayAdapter(
    private val days: List<DayModel>,
    private var flowersMap: Map<String, String>, // Maps DateKey -> FlowerName
    private val listener: OnDayClickListener     // Callback for clicks
) : RecyclerView.Adapter<DayAdapter.DayViewHolder>() {

    // Interface to let the CalendarActivity know when a day is tapped
    interface OnDayClickListener {
        fun onDayClicked(dateKey: String)
    }

    inner class DayViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dayNumberText: TextView = view.findViewById(R.id.tvDayNumber)
        val flowerIv: ImageView = view.findViewById(R.id.ivFlower)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_day, parent, false)
        return DayViewHolder(view)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val model = days[position]

        // If dayNumber is null, this is an empty placeholder slot (padding)
        if (model.dayNumber == null) {
            holder.dayNumberText.text = ""
            holder.flowerIv.visibility = View.INVISIBLE
            holder.itemView.setOnClickListener(null)
            holder.itemView.isClickable = false
        } else {
            // Actual Date
            holder.dayNumberText.text = model.dayNumber.toString()
            holder.itemView.isClickable = true

            // Handle Click
            holder.itemView.setOnClickListener {
                model.dateKey?.let { dk -> listener.onDayClicked(dk) }
            }

            // Check if there is a flower saved for this date
            val flowerName = model.dateKey?.let { flowersMap[it] }

            if (flowerName != null) {
                // Entry Exists -> Show the saved Flower
                val drawableId = FlowerData.getDrawableForName(holder.itemView.context, flowerName)
                if (drawableId != 0) {
                    holder.flowerIv.setImageResource(drawableId)
                    holder.flowerIv.visibility = View.VISIBLE
                } else {
                    holder.flowerIv.visibility = View.INVISIBLE
                }
            } else {
                // No Entry -> Hide Flower (Show blank space)
                holder.flowerIv.setImageDrawable(null)
                holder.flowerIv.visibility = View.INVISIBLE
            }
        }
    }

    override fun getItemCount(): Int = days.size

    // Helper to update the flower map when database data loads
    fun updateFlowers(newMap: Map<String, String>) {
        this.flowersMap = newMap
    }
}