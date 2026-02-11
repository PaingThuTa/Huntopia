package com.example.huntopia

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AchievementsAdapter(
    private val items: List<AchievementItem>,
    private val onItemClick: (AchievementItem) -> Unit
) : RecyclerView.Adapter<AchievementsAdapter.AchievementViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AchievementViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_achievement, parent, false)
        return AchievementViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: AchievementViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class AchievementViewHolder(
        itemView: View,
        private val onItemClick: (AchievementItem) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val ivIcon: ImageView = itemView.findViewById(R.id.ivIcon)
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)

        fun bind(item: AchievementItem) {
            val context = itemView.context
            val resId = context.resources.getIdentifier(
                item.imageName,
                "drawable",
                context.packageName
            )
            ivIcon.setImageResource(if (resId != 0) resId else R.drawable.ic_nav_star)
            tvName.text = item.title
            tvStatus.text = "Achieved on ${item.achievedDate}"
            itemView.setOnClickListener { onItemClick(item) }
        }
    }
}
