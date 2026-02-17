package com.example.huntopia

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private val repository = AchievementRepository()
    private lateinit var recyclerView: RecyclerView
    private lateinit var emailTextView: TextView

    private val fallbackItems = listOf(
        RecentAchievement("Sala Thai", "30/1/2026"),
        RecentAchievement("Albert Einstine Statue", "12/1/2026"),
        RecentAchievement("Angel", "29/12/2025")
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.rvRecent)
        emailTextView = view.findViewById(R.id.tvEmail)
        val logoutButton: MaterialButton = view.findViewById(R.id.btnLogout)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.setHasFixedSize(true)
        recyclerView.addItemDecoration(
            HomeFragment.VerticalSpaceItemDecoration(
                resources.getDimensionPixelSize(R.dimen.recent_list_item_spacing)
            )
        )

        showFallbackData()
        updateProfileIdentity()
        setupLogoutButton(logoutButton)
        setupBottomNav(view)
    }

    override fun onResume() {
        super.onResume()
        updateProfileIdentity()
        loadRecentAchievements()
    }

    private fun loadRecentAchievements() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            showFallbackData()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val recent = repository.getCollectedAchievements(user.uid)
                    .take(3)
                    .map {
                        RecentAchievement(
                            title = it.foundTitle.ifBlank { it.code },
                            dateLabel = formatDate(it.collectedAt?.toDate())
                        )
                    }

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
        recyclerView.adapter = RecentAchievementAdapter(fallbackItems) { item ->
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AchievementDetailsFragment.newInstance(item.title, true))
                .addToBackStack(null)
                .commit()
        }
    }

    private fun updateProfileIdentity() {
        val email = FirebaseAuth.getInstance().currentUser?.email
        emailTextView.text = email ?: getString(R.string.profile_email_placeholder)
    }

    private fun setupLogoutButton(logoutButton: MaterialButton) {
        logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(requireContext(), LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            requireActivity().finish()
        }
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
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ScanFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun formatDate(date: Date?): String {
        if (date == null) {
            return "N/A"
        }
        return SimpleDateFormat("d/M/yyyy", Locale.US).format(date)
    }
}
