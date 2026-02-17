package com.example.huntopia

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

class AchievementsFragment : Fragment() {

    private val repository = AchievementRepository()
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_achievements, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.rvAchievements)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.addItemDecoration(SpacingItemDecoration(dpToPx(18)))

        showEmptyAchievements()
        setupBottomNav(view)
    }

    override fun onResume() {
        super.onResume()
        loadAchievements()
    }

    private fun loadAchievements() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            showEmptyAchievements()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val collected = repository.getCollectedAchievements(user.uid)
                val items = collected.map {
                    AchievementItem(
                        code = it.code,
                        title = it.foundTitle.ifBlank { it.code },
                        achievedDate = formatDate(it.collectedAt?.toDate()),
                        achievedTime = formatTime(it.collectedAt?.toDate()),
                        imageName = it.imageName
                    )
                }

                recyclerView.adapter = AchievementsAdapter(items) { item ->
                    parentFragmentManager.beginTransaction()
                        .replace(
                            R.id.fragment_container,
                            AchievementDetailsFragment.newInstance(code = item.code, achieved = true)
                        )
                        .addToBackStack(null)
                        .commit()
                }

                if (items.isEmpty()) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.no_achievements_yet),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (_: Exception) {
                showEmptyAchievements()
                Toast.makeText(
                    requireContext(),
                    getString(R.string.error_load_achievements),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun showEmptyAchievements() {
        recyclerView.adapter = AchievementsAdapter(emptyList()) { _ -> }
    }

    private fun setupBottomNav(root: View) {
        val ivProfile: ImageView = root.findViewById(R.id.iv_profile)
        val ivHome: ImageView = root.findViewById(R.id.iv_home)
        val ivStar: ImageView = root.findViewById(R.id.iv_star)
        val ivHelp: ImageView = root.findViewById(R.id.iv_help)
        val scanFab: View = root.findViewById(R.id.scan_fab)
        val homeActiveContainer: View = root.findViewById(R.id.home_active_container)

        homeActiveContainer.background = null
        ivHome.alpha = 0.6f
        ivStar.alpha = 1f
        ivProfile.background = null
        ivStar.background = null
        ivHelp.background = null
        ivStar.setBackgroundResource(R.drawable.nav_item_bg_circle)

        ivProfile.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ProfileFragment())
                .commit()
        }
        ivHome.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment())
                .commit()
        }
        ivStar.setOnClickListener {
            // Already on Achievements
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
    }

    private fun formatDate(date: Date?): String {
        if (date == null) {
            return getString(R.string.unknown_date)
        }
        return SimpleDateFormat("d/M/yyyy", Locale.US).format(date)
    }

    private fun formatTime(date: Date?): String {
        if (date == null) {
            return getString(R.string.unknown_time)
        }
        return SimpleDateFormat("h:mm a", Locale.US).format(date)
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    private class SpacingItemDecoration(private val spacePx: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            val position = parent.getChildAdapterPosition(view)
            if (position == 0) {
                outRect.top = spacePx
            }
            outRect.bottom = spacePx
        }
    }
}
