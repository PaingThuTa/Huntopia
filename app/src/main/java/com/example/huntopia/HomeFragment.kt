package com.example.huntopia

import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

class HomeFragment : Fragment(R.layout.fragment_home) {

    private val repository = AchievementRepository()

    private lateinit var progressIndicator: CircularProgressIndicator
    private lateinit var progressText: TextView
    private lateinit var recyclerView: RecyclerView

    private val fallbackRecentItems = listOf(
        RecentAchievement("Sala Thai", "30/1/2026"),
        RecentAchievement("Albert Einstine Statue", "12/1/2026"),
        RecentAchievement("Angel", "29/12/2025")
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressIndicator = view.findViewById(R.id.progress_indicator)
        progressText = view.findViewById(R.id.progress_text)
        val viewAchievementsButton = view.findViewById<View>(R.id.view_achievements_button)
        recyclerView = view.findViewById(R.id.recently_list)
        val scanFab: View = view.findViewById(R.id.scan_fab)
        val profileImage: View = view.findViewById(R.id.profile_image)
        val ivProfile: android.widget.ImageView = view.findViewById(R.id.iv_profile)
        val ivHome: android.widget.ImageView = view.findViewById(R.id.iv_home)
        val ivStar: android.widget.ImageView = view.findViewById(R.id.iv_star)
        val ivHelp: android.widget.ImageView = view.findViewById(R.id.iv_help)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.setHasFixedSize(true)
        recyclerView.addItemDecoration(
            VerticalSpaceItemDecoration(resources.getDimensionPixelSize(R.dimen.recent_list_item_spacing))
        )

        showFallbackData()

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
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ScanFragment())
                .addToBackStack(null)
                .commit()
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

    override fun onResume() {
        super.onResume()
        loadHomeData()
    }

    private fun loadHomeData() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            showFallbackData()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val collected = repository.getCollectedAchievements(user.uid)
                val catalogCount = repository.getCatalogCount()

                val recent = collected.take(3).map {
                    RecentAchievement(
                        title = it.foundTitle.ifBlank { it.code },
                        dateLabel = formatDate(it.collectedAt?.toDate())
                    )
                }

                progressIndicator.max = if (catalogCount > 0) catalogCount else 1
                progressIndicator.setProgressCompat(collected.size.coerceAtMost(progressIndicator.max), true)
                progressText.text = "${collected.size}/$catalogCount"

                recyclerView.adapter = RecentAchievementAdapter(recent) { item ->
                    parentFragmentManager.beginTransaction()
                        .replace(
                            R.id.fragment_container,
                            AchievementDetailsFragment.newInstance(item.title, true)
                        )
                        .addToBackStack(null)
                        .commit()
                }
            } catch (_: Exception) {
                showFallbackData()
            }
        }
    }

    private fun showFallbackData() {
        val progressValue = 25
        val progressMax = 100
        progressIndicator.max = progressMax
        progressIndicator.setProgressCompat(progressValue, true)
        progressText.text = "$progressValue/$progressMax"
        recyclerView.adapter = RecentAchievementAdapter(fallbackRecentItems) { item ->
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AchievementDetailsFragment.newInstance(item.title, true))
                .addToBackStack(null)
                .commit()
        }
    }

    private fun formatDate(date: Date?): String {
        if (date == null) {
            return "N/A"
        }
        val formatter = SimpleDateFormat("d/M/yyyy", Locale.US)
        return formatter.format(date)
    }

    class VerticalSpaceItemDecoration(private val verticalSpace: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            outRect.bottom = verticalSpace
        }
    }
}
