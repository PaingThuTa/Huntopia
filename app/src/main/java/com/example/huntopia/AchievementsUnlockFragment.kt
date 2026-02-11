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
                .replace(
                    R.id.fragment_container,
                    AchievementDetailsFragment.newInstance(item.title, false, item.imageName)
                )
                .addToBackStack(null)
                .commit()
        }
        recyclerView.addItemDecoration(SpacingItemDecoration(dpToPx(18)))

        setupBottomNav(view)
    }

    private fun buildDummyItems(): List<UnlockAchievementItem> {
        return listOf(
            UnlockAchievementItem("1001", "Where time stands tall", "clocktower"),
            UnlockAchievementItem("1002", "Shopping or snacking?", "aumall"),
            UnlockAchievementItem("1003", "Where innovation begins", "vmes"),
            UnlockAchievementItem("1004", "Legends in motion", "fivehorses"),
            UnlockAchievementItem("1005", "Fit for the journey", "jp2sport"),
            UnlockAchievementItem("1006", "A taste of elegance", "crystal"),
            UnlockAchievementItem("1007", "Where tech meets innovation", "itbuilding"),
            UnlockAchievementItem("1008", "A touch of tradition", "salathai"),
            UnlockAchievementItem("1009", "Take a dive", "indoorswim"),
            UnlockAchievementItem("1010", "Peace and grace", "church"),
            UnlockAchievementItem("1011", "Lights, camera, creativity!", "cabuilding"),
            UnlockAchievementItem("1012", "Start your journey", "coachterminal"),
            UnlockAchievementItem("1013", "Design your discovery", "arbuilding"),
            UnlockAchievementItem("1014", "Where big ideas meet", "conferencecenter"),
            UnlockAchievementItem("1015", "Dive into excellence", "aquaticcenter"),
            UnlockAchievementItem("1016", "The business hub", "msm"),
            UnlockAchievementItem("1017", "The power of analysis", "mse"),
            UnlockAchievementItem("1018", "A new chapter in health", "medschool"),
            UnlockAchievementItem("1019", "Game. Set. Match.", "tennis"),
            UnlockAchievementItem("1020", "Catch it if you can!", "randomtram"),
            UnlockAchievementItem("1021", "Reach new heights", "clbuilding"),
            UnlockAchievementItem("1022", "A secret spot awaits", "hiddenhaven"),
            UnlockAchievementItem("1023", "Where angels stand", "angelstatue"),
            UnlockAchievementItem("1024", "Our Grand Opening", "spespecial")
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
