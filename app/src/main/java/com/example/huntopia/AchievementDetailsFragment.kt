package com.example.huntopia

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

class AchievementDetailsFragment : Fragment() {

    private val repository = AchievementRepository()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_achievement_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val code = requireArguments().getString(ARG_CODE).orEmpty()
        val isAchieved = requireArguments().getBoolean(ARG_ACHIEVED, true)

        val ivAchievement = view.findViewById<ImageView>(R.id.ivAchievement)
        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
        val tvDescription = view.findViewById<TextView>(R.id.tvDescription)
        val tvDate = view.findViewById<TextView>(R.id.tvDate)
        val btnBack = view.findViewById<View>(R.id.btnBack)

        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        if (!AchievementRepository.isValidCode(code)) {
            tvTitle.text = getString(R.string.achievement_not_found)
            tvDescription.text = getString(R.string.error_load_achievements)
            tvDate.text = getString(R.string.unknown_date)
            ivAchievement.setImageResource(if (isAchieved) R.drawable.ic_nav_star else R.drawable.ic_nav_help)
            return
        }

        tvTitle.text = getString(R.string.loading)
        tvDescription.text = getString(R.string.loading)
        tvDate.text = getString(R.string.loading)

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val catalog = repository.getCatalogByCode(code)
                if (catalog == null) {
                    tvTitle.text = getString(R.string.achievement_not_found)
                    tvDescription.text = getString(R.string.error_load_achievements)
                    tvDate.text = if (isAchieved) {
                        getString(R.string.achievement_date_unknown)
                    } else {
                        getString(R.string.not_achieved_yet)
                    }
                    ivAchievement.setImageResource(
                        if (isAchieved) R.drawable.ic_nav_star else R.drawable.ic_nav_help
                    )
                    return@launch
                }

                val resolvedTitle = if (isAchieved) {
                    catalog.foundTitle.ifBlank {
                        catalog.unfoundTitle.ifBlank { "Code ${catalog.code}" }
                    }
                } else {
                    catalog.unfoundTitle.ifBlank {
                        catalog.foundTitle.ifBlank { "Code ${catalog.code}" }
                    }
                }

                val resolvedDescription = if (isAchieved) {
                    catalog.foundDescription.ifBlank { getString(R.string.achievement_desc_unavailable) }
                } else {
                    catalog.unfoundDescription.ifBlank { getString(R.string.achievement_locked_hint_default) }
                }

                val imageResId = resources.getIdentifier(
                    catalog.imageName,
                    "drawable",
                    requireContext().packageName
                )
                ivAchievement.setImageResource(
                    if (imageResId != 0) imageResId else if (isAchieved) R.drawable.ic_nav_star else R.drawable.ic_nav_help
                )

                tvTitle.text = resolvedTitle
                tvDescription.text = resolvedDescription

                tvDate.text = if (isAchieved) {
                    val uid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
                    val collected = if (uid.isBlank()) {
                        null
                    } else {
                        repository.getCollectedAchievementByCode(uid, code)
                    }
                    collected?.collectedAt?.toDate()?.let { formatDateTime(it) }
                        ?: getString(R.string.achievement_date_unknown)
                } else {
                    getString(R.string.not_achieved_yet)
                }
            } catch (_: Exception) {
                tvTitle.text = getString(R.string.achievement_not_found)
                tvDescription.text = getString(R.string.error_load_achievements)
                tvDate.text = if (isAchieved) {
                    getString(R.string.achievement_date_unknown)
                } else {
                    getString(R.string.not_achieved_yet)
                }
                ivAchievement.setImageResource(if (isAchieved) R.drawable.ic_nav_star else R.drawable.ic_nav_help)
            }
        }
    }

    private fun formatDateTime(date: Date): String {
        return SimpleDateFormat("d MMM yyyy, h:mm a", Locale.US).format(date)
    }

    companion object {
        private const val ARG_CODE = "arg_code"
        private const val ARG_ACHIEVED = "arg_achieved"

        fun newInstance(code: String, achieved: Boolean): AchievementDetailsFragment {
            val fragment = AchievementDetailsFragment()
            fragment.arguments = Bundle().apply {
                putString(ARG_CODE, code)
                putBoolean(ARG_ACHIEVED, achieved)
            }
            return fragment
        }
    }
}
