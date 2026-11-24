package com.shieldher.dashboard.check

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.ChipGroup
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.textfield.TextInputEditText
import com.shieldher.R
import com.shieldher.dashboard.common.BadgeStatus
import com.shieldher.dashboard.common.StatusBadge

class AccountCheckFragment : Fragment() {

    private var inputFollowers: TextInputEditText? = null
    private var inputFollowing: TextInputEditText? = null
    private var inputBio: TextInputEditText? = null
    private var chipGroupPlatform: ChipGroup? = null
    private var switchHasLink: MaterialSwitch? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_account_check, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val badge = view.findViewById<StatusBadge>(R.id.badge_account_check_preview)
        badge.setStatus(BadgeStatus.PREVIEW)

        inputFollowers = view.findViewById(R.id.input_followers)
        inputFollowing = view.findViewById(R.id.input_following)
        inputBio = view.findViewById(R.id.input_bio)
        chipGroupPlatform = view.findViewById(R.id.chip_group_platform)
        switchHasLink = view.findViewById(R.id.switch_has_link)

        val btnRunCheck = view.findViewById<MaterialButton>(R.id.btn_run_check)
        val textComingSoon = view.findViewById<TextView>(R.id.text_coming_soon)

        btnRunCheck.setOnClickListener {
            // Tapping shows ONLY the coming soon message inline
            textComingSoon.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clear all fields so no typed values are retained
        inputFollowers?.setText("")
        inputFollowing?.setText("")
        inputBio?.setText("")
        chipGroupPlatform?.clearCheck()
        switchHasLink?.isChecked = false

        inputFollowers = null
        inputFollowing = null
        inputBio = null
        chipGroupPlatform = null
        switchHasLink = null
    }
}
