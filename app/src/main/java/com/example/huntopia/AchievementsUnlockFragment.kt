package com.example.huntopia

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AchievementsUnlockFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_achievements_unlock, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.rvUnlock)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = AchievementsUnlockAdapter(buildDummyItems()) { item ->
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AchievementDetailsFragment.newInstance(item.title, false))
                .addToBackStack(null)
                .commit()
        }
        recyclerView.addItemDecoration(SpacingItemDecoration(dpToPx(18)))

        setupBottomNav(view)
    }

    private fun buildDummyItems(): List<UnlockAchievementItem> {
        return listOf(
            UnlockAchievementItem("LuLuLu"),
            UnlockAchievementItem("NaNaNa"),
            UnlockAchievementItem("HaHaHa"),
            UnlockAchievementItem("WHATTTTTT")
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
        ivHelp.alpha = 1f
        ivProfile.background = null
        ivStar.background = null
        ivHelp.background = null
        ivHelp.setBackgroundResource(R.drawable.nav_item_bg_circle)

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
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AchievementsFragment())
                .commit()
        }
        ivHelp.setOnClickListener {
            // Already here
        }
        scanFab.setOnClickListener {
            // TODO: Handle scan
        }
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
