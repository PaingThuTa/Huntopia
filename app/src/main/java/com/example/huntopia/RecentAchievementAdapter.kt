package com.example.huntopia

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RecentAchievementAdapter(
    private val items: List<RecentAchievement>,
    private val onItemClick: (RecentAchievement) -> Unit
) : RecyclerView.Adapter<RecentAchievementAdapter.AchievementViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AchievementViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recent_achievement, parent, false)
        return AchievementViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: AchievementViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class AchievementViewHolder(
        itemView: View,
        private val onItemClick: (RecentAchievement) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val titleText: TextView = itemView.findViewById(R.id.title_text)
        private val dateChip: TextView = itemView.findViewById(R.id.date_chip)

        fun bind(item: RecentAchievement) {
            titleText.text = item.title
            dateChip.text = item.dateLabel
            itemView.setOnClickListener { onItemClick(item) }
        }
    }
}
