package com.shieldher.dashboard.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.shieldher.R

class MoreFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_more, container, false)

        view.findViewById<View>(R.id.row_settings).setOnClickListener {
            findNavController().navigate(R.id.settingsFragment)
        }

        view.findViewById<View>(R.id.row_account_check).setOnClickListener {
            findNavController().navigate(R.id.accountCheckFragment)
        }

        view.findViewById<View>(R.id.row_welcome_guide).setOnClickListener {
            findNavController().navigate(R.id.onboardingFragment)
        }

        return view
    }
}
