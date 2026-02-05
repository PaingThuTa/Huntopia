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
        val profileImage: View = view.findViewById(R.id.profile_image)
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
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AchievementDetailsFragment.newInstance(item.title, true))
                .addToBackStack(null)
                .commit()
        }
        recyclerView.setHasFixedSize(true)
        recyclerView.addItemDecoration(VerticalSpaceItemDecoration(dpToPx(16)))

        ivProfile.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ProfileFragment())
                .commit()
        }
        ivHome.setOnClickListener {
            Toast.makeText(requireContext(), "Home", Toast.LENGTH_SHORT).show()
        }
        ivStar.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AchievementsFragment())
                .commit()
        }
        ivHelp.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AchievementsUnlockFragment())
                .commit()
        }
        scanFab.setOnClickListener {
            Toast.makeText(requireContext(), "Scan", Toast.LENGTH_SHORT).show()
        }

        profileImage.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ProfileFragment())
                .commit()
        }

        viewAchievementsButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AchievementsFragment())
                .commit()
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
