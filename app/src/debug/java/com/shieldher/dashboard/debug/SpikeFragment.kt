package com.shieldher.dashboard.debug

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

/**
 * FE-0.2: Debug-only spike fragment to verify fragment-ktx and navigation.
 * Never shipped in release builds.
 */
class SpikeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return TextView(requireContext()).apply {
            text = "Spike Fragment (Debug Only)"
        }
    }
}
