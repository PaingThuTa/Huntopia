package com.example.huntopia

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import java.util.Locale

class AchievementDetailsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_achievement_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val title = requireArguments().getString(ARG_TITLE, "Achievement")
        val isAchieved = requireArguments().getBoolean(ARG_ACHIEVED, true)
        val imageName = requireArguments().getString(ARG_IMAGE_NAME)

        val ivAchievement = view.findViewById<ImageView>(R.id.ivAchievement)
        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
        val tvDescription = view.findViewById<TextView>(R.id.tvDescription)
        val tvDate = view.findViewById<TextView>(R.id.tvDate)
        val btnBack = view.findViewById<View>(R.id.btnBack)

        val resolvedImageName = if (!imageName.isNullOrBlank()) {
            imageName
        } else {
            resolveImageNameFromTitle(title)
        }
        val resId = if (resolvedImageName.isBlank()) {
            0
        } else {
            resources.getIdentifier(resolvedImageName, "drawable", requireContext().packageName)
        }
        ivAchievement.setImageResource(if (resId != 0) resId else R.drawable.salathai)

        tvTitle.text = title
        tvDescription.text = if (isAchieved) {
            "This place has been achieved. More details can be added later."
        } else {
            "This achievement is still locked. Find the hint to unlock it."
        }
        tvDate.text = if (isAchieved) {
            "30 Jan, 2026"
        } else {
            "Not achieved yet"
        }

        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    companion object {
        private const val ARG_TITLE = "arg_title"
        private const val ARG_ACHIEVED = "arg_achieved"
        private const val ARG_IMAGE_NAME = "arg_image_name"

        fun newInstance(
            title: String,
            achieved: Boolean,
            imageName: String? = null
        ): AchievementDetailsFragment {
            val fragment = AchievementDetailsFragment()
            fragment.arguments = Bundle().apply {
                putString(ARG_TITLE, title)
                putBoolean(ARG_ACHIEVED, achieved)
                putString(ARG_IMAGE_NAME, imageName)
            }
            return fragment
        }
    }

    private fun resolveImageNameFromTitle(title: String): String {
        val normalized = title.lowercase(Locale.US)
        return when {
            "sala thai" in normalized -> "salathai"
            "clock tower" in normalized -> "clocktower"
            "angel" in normalized -> "angelstatue"
            "mall" in normalized || "cafeteria" in normalized -> "aumall"
            "vmes" in normalized -> "vmes"
            "sports" in normalized || "john paul" in normalized -> "jp2sport"
            "crystal" in normalized -> "crystal"
            "it" in normalized -> "itbuilding"
            "swim" in normalized || "pool" in normalized -> "indoorswim"
            "church" in normalized || "chapel" in normalized -> "church"
            "communication arts" in normalized || normalized == "ca" -> "cabuilding"
            "coach" in normalized || "terminal" in normalized -> "coachterminal"
            "architecture" in normalized || normalized == "ar" -> "arbuilding"
            "conference" in normalized -> "conferencecenter"
            "aquatic" in normalized -> "aquaticcenter"
            "msm" in normalized -> "msm"
            "mse" in normalized || "economics" in normalized -> "mse"
            "medicine" in normalized || "aumd" in normalized -> "medschool"
            "tennis" in normalized -> "tennis"
            "tram" in normalized -> "randomtram"
            normalized == "cl" || "cathedral of learning" in normalized -> "clbuilding"
            "hidden haven" in normalized || "secret spot" in normalized || "hidden" in normalized -> "hiddenhaven"
            "five horses" in normalized || "horses" in normalized -> "fivehorses"
            else -> ""
        }
    }
}
