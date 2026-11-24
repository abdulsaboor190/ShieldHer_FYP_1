package com.shieldher.dashboard.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.materialswitch.MaterialSwitch
import com.shieldher.R
import com.shieldher.dashboard.common.AppSettings
import com.shieldher.dashboard.common.ConfirmDialog
import com.shieldher.dashboard.common.applyTo
import com.shieldher.dashboard.common.viewModelFactory
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {

    private val viewModel: SettingsViewModel by viewModels {
        viewModelFactory { SettingsViewModel() }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_settings, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val textEvidenceCount = view.findViewById<TextView>(R.id.text_evidence_count)
        val switchScreenshot = view.findViewById<MaterialSwitch>(R.id.switch_screenshot_protection)
        val rowLimits = view.findViewById<View>(R.id.row_limits)
        val btnDeleteAll = view.findViewById<MaterialButton>(R.id.btn_delete_all)

        // Screenshot protection setup
        val settings = AppSettings(requireContext())
        switchScreenshot.isChecked = settings.screenshotProtectionEnabled
        switchScreenshot.setOnCheckedChangeListener { _, isChecked ->
            settings.screenshotProtectionEnabled = isChecked
            activity?.window?.let { window ->
                applyTo(window, settings)
            }
        }

        // Navigate to limits fragment
        rowLimits.setOnClickListener {
            findNavController().navigate(R.id.limitsFragment)
        }

        // Delete all button
        btnDeleteAll.setOnClickListener {
            ConfirmDialog.show(
                context = requireContext(),
                title = R.string.dialog_delete_all_title,
                message = R.string.dialog_delete_all_message,
                confirmText = R.string.dialog_delete_all_confirm,
                destructive = true,
                onConfirm = {
                    viewModel.deleteAll()
                }
            )
        }

        // Observe ViewModel states
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.evidenceCount.collect { count ->
                        textEvidenceCount.text = getString(R.string.settings_evidence_count, count)
                    }
                }
                launch {
                    viewModel.deleteAllResult.collect { result ->
                        when (result) {
                            DeleteAllResult.Success -> {
                                Toast.makeText(
                                    requireContext(),
                                    R.string.settings_delete_all_success,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            DeleteAllResult.GatewayFailed -> {
                                Toast.makeText(
                                    requireContext(),
                                    R.string.settings_delete_all_gateway_error,
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            DeleteAllResult.CaseStoreFailed -> {
                                Toast.makeText(
                                    requireContext(),
                                    R.string.settings_delete_all_case_error,
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                }
            }
        }
    }
}
