package com.example.huntopia

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.rvRecent)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = RecentAchievementAdapter(buildDummyItems()) { item ->
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AchievementDetailsFragment.newInstance(item.title, true))
                .addToBackStack(null)
                .commit()
        }
        recyclerView.setHasFixedSize(true)
        recyclerView.addItemDecoration(
            HomeFragment.VerticalSpaceItemDecoration(
                resources.getDimensionPixelSize(R.dimen.recent_list_item_spacing)
            )
        )

        setupBottomNav(view)
    }

    private fun buildDummyItems(): List<RecentAchievement> {
        return listOf(
            RecentAchievement("Sala Thai", "30/1/2026"),
            RecentAchievement("Albert Einstine Statue", "12/1/2026"),
            RecentAchievement("Angel", "29/12/2025")
        )
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
        ivStar.alpha = 0.6f
        ivHelp.alpha = 0.6f
        ivProfile.alpha = 1f
        ivProfile.background = null
        ivStar.background = null
        ivHelp.background = null
        ivProfile.setBackgroundResource(R.drawable.nav_item_bg_circle)

        ivProfile.setOnClickListener {
            // Already here
        }
        ivHome.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment())
                .commit()
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
            // TODO: Handle scan
        }
    }

}
