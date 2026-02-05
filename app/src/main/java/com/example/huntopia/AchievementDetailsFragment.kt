package com.example.huntopia

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

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

        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
        val tvDescription = view.findViewById<TextView>(R.id.tvDescription)
        val tvDate = view.findViewById<TextView>(R.id.tvDate)
        val btnBack = view.findViewById<View>(R.id.btnBack)

        tvTitle.text = title
        tvDescription.text = if (isAchieved) {
            "This place has been achieved. More details can be added later."
        } else {
            "This achievement is still locked. Find the hint to unlock it."
        }
        tvDate.text = if (isAchieved) {
            "Achieved on 30 Jan, 2026"
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

        fun newInstance(title: String, achieved: Boolean): AchievementDetailsFragment {
            val fragment = AchievementDetailsFragment()
            fragment.arguments = Bundle().apply {
                putString(ARG_TITLE, title)
                putBoolean(ARG_ACHIEVED, achieved)
            }
            return fragment
        }
    }
}
