package com.example.huntopia

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
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

        private val ivIcon: ImageView = itemView.findViewById(R.id.ivHelp)
        private val tvName: TextView = itemView.findViewById(R.id.tvName)

        fun bind(item: UnlockAchievementItem) {
            val context = itemView.context
            val resId = context.resources.getIdentifier(
                item.imageName,
                "drawable",
                context.packageName
            )
            ivIcon.setImageResource(if (resId != 0) resId else R.drawable.ic_nav_help)
            tvName.text = item.title
            itemView.setOnClickListener { onItemClick(item) }
        }
    }
}
