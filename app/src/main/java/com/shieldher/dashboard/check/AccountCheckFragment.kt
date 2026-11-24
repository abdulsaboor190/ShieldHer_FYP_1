package com.shieldher.dashboard.check

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.ChipGroup
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.textfield.TextInputEditText
import com.shieldher.R
import com.shieldher.dashboard.data.RiskBand
import com.shieldher.dashboard.data.RiskOutcome
import com.shieldher.dashboard.data.RiskPlatform
import kotlinx.coroutines.launch

class AccountCheckFragment : Fragment() {

    private val viewModel: AccountCheckViewModel by viewModels()

    private var inputFollowers: TextInputEditText? = null
    private var inputFollowing: TextInputEditText? = null
    private var inputBio: TextInputEditText? = null
    private var chipGroupPlatform: ChipGroup? = null
    private var switchHasLink: MaterialSwitch? = null

    private var progressComputing: ProgressBar? = null
    private var containerResult: View? = null
    private var badgeRiskResult: RiskStatusBadge? = null
    private var textRiskTitle: TextView? = null
    private var textRiskExplanation: TextView? = null
    private var textPlatformAlert: TextView? = null
    private var textAboutToggle: TextView? = null
    private var textAboutBody: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_account_check, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        inputFollowers = view.findViewById(R.id.input_followers)
        inputFollowing = view.findViewById(R.id.input_following)
        inputBio = view.findViewById(R.id.input_bio)
        chipGroupPlatform = view.findViewById(R.id.chip_group_platform)
        switchHasLink = view.findViewById(R.id.switch_has_link)

        progressComputing = view.findViewById(R.id.progress_computing)
        containerResult = view.findViewById(R.id.container_result)
        badgeRiskResult = view.findViewById(R.id.badge_risk_result)
        textRiskTitle = view.findViewById(R.id.text_risk_title)
        textRiskExplanation = view.findViewById(R.id.text_risk_explanation)
        textPlatformAlert = view.findViewById(R.id.text_platform_alert)
        textAboutToggle = view.findViewById(R.id.text_about_toggle)
        textAboutBody = view.findViewById(R.id.text_about_body)

        val btnRunCheck = view.findViewById<MaterialButton>(R.id.btn_run_check)
        val btnClear = view.findViewById<MaterialButton>(R.id.btn_clear)

        // Expandable limits / how well does it work section
        textAboutToggle?.setOnClickListener {
            val body = textAboutBody ?: return@setOnClickListener
            body.visibility = if (body.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }

        btnRunCheck.setOnClickListener {
            val selectedPlatform = when (chipGroupPlatform?.checkedChipId) {
                R.id.chip_instagram -> RiskPlatform.INSTAGRAM
                R.id.chip_x -> RiskPlatform.X_TWITTER
                R.id.chip_other -> RiskPlatform.OTHER
                else -> null
            }

            textPlatformAlert?.visibility = View.GONE

            viewModel.run(
                platform = selectedPlatform,
                followersText = inputFollowers?.text?.toString(),
                followingText = inputFollowing?.text?.toString(),
                bioText = inputBio?.text?.toString(),
                hasLink = switchHasLink?.isChecked == true
            )
        }

        btnClear.setOnClickListener {
            clearInputs()
            viewModel.clear()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    renderState(state)
                }
            }
        }
    }

    private fun renderState(state: AccountCheckState) {
        when (state) {
            AccountCheckState.Idle -> {
                progressComputing?.visibility = View.GONE
                containerResult?.visibility = View.GONE
                textPlatformAlert?.visibility = View.GONE
            }
            AccountCheckState.Computing -> {
                progressComputing?.visibility = View.VISIBLE
                containerResult?.visibility = View.GONE
                textPlatformAlert?.visibility = View.GONE
            }
            is AccountCheckState.Done -> {
                progressComputing?.visibility = View.GONE
                renderOutcome(state.outcome)
            }
        }
    }

    private fun renderOutcome(outcome: RiskOutcome) {
        when (outcome) {
            RiskOutcome.PlatformNotChosen -> {
                containerResult?.visibility = View.GONE
                textPlatformAlert?.visibility = View.VISIBLE
            }
            is RiskOutcome.Result -> {
                textPlatformAlert?.visibility = View.GONE
                containerResult?.visibility = View.VISIBLE
                if (outcome.band == RiskBand.HIGHER) {
                    badgeRiskResult?.setStatus(RiskBadgeStatus.HIGHER)
                    textRiskTitle?.setText(R.string.risk_title_higher)
                    textRiskExplanation?.setText(R.string.risk_expl_higher)
                } else {
                    badgeRiskResult?.setStatus(RiskBadgeStatus.LOWER)
                    textRiskTitle?.setText(R.string.risk_title_lower)
                    textRiskExplanation?.setText(R.string.risk_expl_lower)
                }
            }
            RiskOutcome.Uncertain -> {
                textPlatformAlert?.visibility = View.GONE
                containerResult?.visibility = View.VISIBLE
                badgeRiskResult?.setStatus(RiskBadgeStatus.UNCERTAIN)
                textRiskTitle?.setText(R.string.risk_title_uncertain)
                textRiskExplanation?.setText(R.string.risk_expl_uncertain)
            }
            RiskOutcome.UnsupportedInput -> {
                textPlatformAlert?.visibility = View.GONE
                containerResult?.visibility = View.VISIBLE
                badgeRiskResult?.setStatus(RiskBadgeStatus.UNSUPPORTED)
                textRiskTitle?.setText(R.string.risk_title_unsupported)
                textRiskExplanation?.setText(R.string.risk_expl_unsupported)
            }
            RiskOutcome.NotValidated -> {
                textPlatformAlert?.visibility = View.GONE
                containerResult?.visibility = View.VISIBLE
                badgeRiskResult?.setStatus(RiskBadgeStatus.NOT_VALIDATED)
                textRiskTitle?.setText(R.string.risk_title_not_validated)
                textRiskExplanation?.setText(R.string.risk_expl_not_validated)
            }
            RiskOutcome.Unavailable -> {
                textPlatformAlert?.visibility = View.GONE
                containerResult?.visibility = View.VISIBLE
                badgeRiskResult?.setStatus(RiskBadgeStatus.UNSUPPORTED)
                textRiskTitle?.setText(R.string.risk_title_unavailable)
                textRiskExplanation?.setText(R.string.risk_expl_unavailable)
            }
        }
    }

    private fun clearInputs() {
        inputFollowers?.setText("")
        inputFollowing?.setText("")
        inputBio?.setText("")
        chipGroupPlatform?.clearCheck()
        switchHasLink?.isChecked = false
        textPlatformAlert?.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Privacy rule: clear all fields and viewModel on view destroy
        clearInputs()
        viewModel.clear()

        inputFollowers = null
        inputFollowing = null
        inputBio = null
        chipGroupPlatform = null
        switchHasLink = null
        progressComputing = null
        containerResult = null
        badgeRiskResult = null
        textRiskTitle = null
        textRiskExplanation = null
        textPlatformAlert = null
        textAboutToggle = null
        textAboutBody = null
    }
}
