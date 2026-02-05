package com.example.huntopia

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AchievementsUnlockAdapter(
    private val items: List<UnlockAchievementItem>,
    private val onItemClick: (UnlockAchievementItem) -> Unit
) : RecyclerView.Adapter<AchievementsUnlockAdapter.UnlockViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UnlockViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_achievement_unlock, parent, false)
        return UnlockViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: UnlockViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class UnlockViewHolder(
        itemView: View,
        private val onItemClick: (UnlockAchievementItem) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val tvName: TextView = itemView.findViewById(R.id.tvName)

        fun bind(item: UnlockAchievementItem) {
            tvName.text = item.title
            itemView.setOnClickListener { onItemClick(item) }
        }
    }
}
