package com.example.bloomix

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DayAdapter(
    private val days: List<DayModel>,
    private var flowersMap: Map<String, String>,
    private val listener: OnDayClickListener
) : RecyclerView.Adapter<DayAdapter.DayViewHolder>() {

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
        if (model.dayNumber == null) {
            // empty placeholder
            holder.dayNumberText.text = ""
            holder.flowerIv.visibility = View.GONE
            holder.itemView.setOnClickListener(null)
            holder.itemView.isClickable = false
        } else {
            holder.dayNumberText.text = model.dayNumber.toString()
            holder.itemView.isClickable = true
            holder.itemView.setOnClickListener {
                model.dateKey?.let { dk -> listener.onDayClicked(dk) }
            }

            // Show flower if saved
            val flowerName = model.dateKey?.let { flowersMap[it] }
            if (flowerName != null) {
                val drawableId = FlowerData.getDrawableForName(holder.itemView.context, flowerName)
                if (drawableId != 0) {
                    holder.flowerIv.setImageResource(drawableId)
                    holder.flowerIv.visibility = View.VISIBLE
                } else {
                    holder.flowerIv.visibility = View.GONE
                }
            } else {
                holder.flowerIv.visibility = View.GONE
            }
        }
    }

    override fun getItemCount(): Int = days.size

    fun updateFlowers(newMap: Map<String, String>) {
        this.flowersMap = newMap
    }
}
