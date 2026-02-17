package com.example.huntopia

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
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

    private val achievementRepository = AchievementRepository()
    private val userRepository = UserRepository()

    private lateinit var recyclerView: RecyclerView
    private lateinit var nameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var recentTitleView: TextView

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
        nameTextView = view.findViewById(R.id.tvName)
        emailTextView = view.findViewById(R.id.tvEmail)
        recentTitleView = view.findViewById(R.id.tvRecent)
        val logoutButton: MaterialButton = view.findViewById(R.id.btnLogout)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.setHasFixedSize(true)
        recyclerView.addItemDecoration(
            HomeFragment.VerticalSpaceItemDecoration(
                resources.getDimensionPixelSize(R.dimen.recent_list_item_spacing)
            )
        )

        showEmptyProfileState()
        setupLogoutButton(logoutButton)
        setupBottomNav(view)
    }

    override fun onResume() {
        super.onResume()
        loadProfileData()
    }

    private fun loadProfileData() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            showEmptyProfileState()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val profile = userRepository.getOrProvisionProfile(user.uid, user.email.orEmpty())
                nameTextView.text = profile.username
                emailTextView.text = profile.email.ifBlank { getString(R.string.profile_email_placeholder) }

                val recent = achievementRepository.getCollectedAchievements(user.uid)
                    .take(3)
                    .map {
                        RecentAchievement(
                            code = it.code,
                            title = it.foundTitle.ifBlank { it.code },
                            dateLabel = formatDate(it.collectedAt?.toDate())
                        )
                    }

                recentTitleView.text = if (recent.isEmpty()) {
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
                showProfileLoadError()
            }
        }
    }

    private fun showEmptyProfileState() {
        nameTextView.text = getString(R.string.profile_username_placeholder)
        emailTextView.text = getString(R.string.profile_email_placeholder)
        recentTitleView.text = getString(R.string.no_achievements_yet)
        recyclerView.adapter = RecentAchievementAdapter(emptyList()) { _ -> }
    }

    private fun showProfileLoadError() {
        showEmptyProfileState()
        Toast.makeText(requireContext(), getString(R.string.error_load_profile), Toast.LENGTH_SHORT).show()
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
            return getString(R.string.unknown_date)
        }
        return SimpleDateFormat("d/M/yyyy", Locale.US).format(date)
    }
}
