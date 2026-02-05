package com.example.huntopia

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.CircularProgressIndicator

class HomeFragment : Fragment(R.layout.fragment_home) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val progressIndicator: CircularProgressIndicator = view.findViewById(R.id.progress_indicator)
        val progressText = view.findViewById<android.widget.TextView>(R.id.progress_text)
        val viewAchievementsButton = view.findViewById<View>(R.id.view_achievements_button)
        val recyclerView: RecyclerView = view.findViewById(R.id.recently_list)
        val scanFab: View = view.findViewById(R.id.scan_fab)
        val ivProfile: android.widget.ImageView = view.findViewById(R.id.iv_profile)
        val ivHome: android.widget.ImageView = view.findViewById(R.id.iv_home)
        val ivStar: android.widget.ImageView = view.findViewById(R.id.iv_star)
        val ivHelp: android.widget.ImageView = view.findViewById(R.id.iv_help)

        val progressValue = 25
        val progressMax = 100
        progressIndicator.max = progressMax
        progressIndicator.setProgressCompat(progressValue, true)
        progressText.text = "$progressValue/$progressMax"

        val items = listOf(
            RecentAchievement("Sala Thai", "30/1/2026"),
            RecentAchievement("Albert Einstine Statue", "12/1/2026"),
            RecentAchievement("Angel", "29/12/2025")
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = RecentAchievementAdapter(items) { item ->
            Toast.makeText(requireContext(), item.title, Toast.LENGTH_SHORT).show()
        }
        recyclerView.setHasFixedSize(true)
        recyclerView.addItemDecoration(VerticalSpaceItemDecoration(dpToPx(16)))

        ivProfile.setOnClickListener {
            Toast.makeText(requireContext(), "Profile", Toast.LENGTH_SHORT).show()
        }
        ivHome.setOnClickListener {
            Toast.makeText(requireContext(), "Home", Toast.LENGTH_SHORT).show()
        }
        ivStar.setOnClickListener {
            Toast.makeText(requireContext(), "Achievements", Toast.LENGTH_SHORT).show()
        }
        ivHelp.setOnClickListener {
            Toast.makeText(requireContext(), "Help", Toast.LENGTH_SHORT).show()
        }
        scanFab.setOnClickListener {
            Toast.makeText(requireContext(), "Scan", Toast.LENGTH_SHORT).show()
        }

        viewAchievementsButton.setOnClickListener {
            Toast.makeText(requireContext(), "View Achievements", Toast.LENGTH_SHORT).show()
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    class VerticalSpaceItemDecoration(private val verticalSpace: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: android.graphics.Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            outRect.bottom = verticalSpace
        }
    }
}
