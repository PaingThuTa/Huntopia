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
import kotlinx.coroutines.launch

class AchievementsUnlockFragment : Fragment() {

    private val repository = AchievementRepository()
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_achievements_unlock, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.rvUnlock)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.addItemDecoration(SpacingItemDecoration(dpToPx(18)))

        showEmptyUnlockItems()
        setupBottomNav(view)
    }

    override fun onResume() {
        super.onResume()
        loadUnlockItems()
    }

    private fun loadUnlockItems() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            showEmptyUnlockItems()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            val catalogItems = try {
                repository.getAllCatalogItems()
            } catch (_: Exception) {
                showEmptyUnlockItems()
                Toast.makeText(
                    requireContext(),
                    getString(R.string.error_load_achievements),
                    Toast.LENGTH_SHORT
                ).show()
                return@launch
            }

            val (collectedCodes, isCollectedReliable) = loadCollectedCodes(user.uid)

            val unlockItems = catalogItems
                .filter { it.code !in collectedCodes }
                .map {
                    UnlockAchievementItem(
                        code = it.code,
                        title = it.unfoundTitle.ifBlank { "Code ${it.code}" },
                        imageName = it.imageName
                    )
                }

            recyclerView.adapter = AchievementsUnlockAdapter(unlockItems) { item ->
                parentFragmentManager.beginTransaction()
                    .replace(
                        R.id.fragment_container,
                        AchievementDetailsFragment.newInstance(code = item.code, achieved = false)
                    )
                    .addToBackStack(null)
                    .commit()
            }

            if (!isCollectedReliable) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.warning_showing_all_locked_due_sync_issue),
                    Toast.LENGTH_SHORT
                ).show()
            } else if (unlockItems.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.all_achievements_unlocked),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private suspend fun loadCollectedCodes(uid: String): Pair<Set<String>, Boolean> {
        return try {
            repository.getCollectedAchievements(uid)
                .map { it.code }
                .toSet() to true
        } catch (_: Exception) {
            emptySet<String>() to false
        }
    }

    private fun showEmptyUnlockItems() {
        recyclerView.adapter = AchievementsUnlockAdapter(emptyList()) { _ -> }
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
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ScanFragment())
                .addToBackStack(null)
                .commit()
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
