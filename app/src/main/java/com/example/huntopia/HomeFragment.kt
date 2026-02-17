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

    private val achievementRepository = AchievementRepository()
    private val userRepository = UserRepository()

    private lateinit var progressIndicator: CircularProgressIndicator
    private lateinit var progressText: TextView
    private lateinit var nameText: TextView
    private lateinit var recentlyTitle: TextView
    private lateinit var recyclerView: RecyclerView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressIndicator = view.findViewById(R.id.progress_indicator)
        progressText = view.findViewById(R.id.progress_text)
        nameText = view.findViewById(R.id.name_text)
        recentlyTitle = view.findViewById(R.id.recently_title)

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

        showEmptyHomeState()

        ivProfile.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ProfileFragment())
                .commit()
        }
        ivHome.setOnClickListener {
            Toast.makeText(requireContext(), getString(R.string.home_tab), Toast.LENGTH_SHORT).show()
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
            showEmptyHomeState()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val profile = userRepository.getOrProvisionProfile(user.uid, user.email.orEmpty())
                nameText.text = profile.username

                val collected = achievementRepository.getCollectedAchievements(user.uid)
                val catalogCount = achievementRepository.getCatalogCount()

                val recent = collected.take(3).map {
                    RecentAchievement(
                        code = it.code,
                        title = it.foundTitle.ifBlank { it.code },
                        dateLabel = formatDate(it.collectedAt?.toDate())
                    )
                }

                progressIndicator.max = if (catalogCount > 0) catalogCount else 1
                progressIndicator.setProgressCompat(collected.size.coerceAtMost(progressIndicator.max), true)
                progressText.text = "${collected.size}/$catalogCount"

                recentlyTitle.text = if (recent.isEmpty()) {
                    getString(R.string.no_achievements_yet)
                } else {
                    getString(R.string.recently_obtained)
                }

                recyclerView.adapter = RecentAchievementAdapter(recent) { item ->
                    parentFragmentManager.beginTransaction()
                        .replace(
                            R.id.fragment_container,
                            AchievementDetailsFragment.newInstance(code = item.code, achieved = true)
                        )
                        .addToBackStack(null)
                        .commit()
                }
            } catch (_: Exception) {
                showHomeLoadError()
            }
        }
    }

    private fun showEmptyHomeState() {
        nameText.text = getString(R.string.profile_username_placeholder)
        progressIndicator.max = 1
        progressIndicator.setProgressCompat(0, false)
        progressText.text = "0/0"
        recentlyTitle.text = getString(R.string.no_achievements_yet)
        recyclerView.adapter = RecentAchievementAdapter(emptyList()) { _ -> }
    }

    private fun showHomeLoadError() {
        showEmptyHomeState()
        Toast.makeText(requireContext(), getString(R.string.error_load_achievements), Toast.LENGTH_SHORT).show()
    }

    private fun formatDate(date: Date?): String {
        if (date == null) {
            return getString(R.string.unknown_date)
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
