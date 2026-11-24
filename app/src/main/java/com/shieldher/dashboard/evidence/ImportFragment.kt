package com.shieldher.dashboard.evidence

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.shieldher.R
import com.shieldher.dashboard.common.NavArgs
import com.shieldher.dashboard.common.viewModelFactory
import kotlinx.coroutines.launch

class ImportFragment : Fragment() {

    private val viewModel: ImportViewModel by viewModels {
        viewModelFactory { ImportViewModel() }
    }

    // Document picker: image/* and text/plain; no permanent URI access.
    private val pickDocument = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) {
            viewModel.onPickerCancelled()
            return@registerForActivityResult
        }
        // Resolve mimeType via ContentResolver before passing to ViewModel.
        // The URI itself is never stored or logged beyond this point.
        val mimeType = try {
            requireContext().contentResolver.getType(uri)
        } catch (_: Exception) {
            null
        }
        viewModel.onUriSelected(uri, mimeType)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_import, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnChooseFile = view.findViewById<MaterialButton>(R.id.btn_choose_file)
        val layoutSaving = view.findViewById<View>(R.id.layout_saving)
        val layoutSaved = view.findViewById<View>(R.id.layout_saved)
        val textTooLarge = view.findViewById<View>(R.id.text_too_large)
        val textUnsupported = view.findViewById<View>(R.id.text_unsupported)
        val layoutFailed = view.findViewById<View>(R.id.layout_failed)
        val btnViewEvidence = view.findViewById<MaterialButton>(R.id.btn_view_evidence)
        val btnImportAnother = view.findViewById<MaterialButton>(R.id.btn_import_another)
        val btnRetry = view.findViewById<MaterialButton>(R.id.btn_retry)

        btnChooseFile.setOnClickListener {
            pickDocument.launch(arrayOf("image/*", "text/plain"))
        }

        btnRetry.setOnClickListener {
            viewModel.resetToIdle()
            pickDocument.launch(arrayOf("image/*", "text/plain"))
        }

        btnImportAnother.setOnClickListener {
            viewModel.resetToIdle()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    renderState(
                        state,
                        btnChooseFile,
                        layoutSaving,
                        layoutSaved,
                        textTooLarge,
                        textUnsupported,
                        layoutFailed,
                        btnViewEvidence
                    )
                }
            }
        }
    }

    private fun renderState(
        state: ImportScreenState,
        btnChooseFile: MaterialButton,
        layoutSaving: View,
        layoutSaved: View,
        textTooLarge: View,
        textUnsupported: View,
        layoutFailed: View,
        btnViewEvidence: MaterialButton
    ) {
        // Hide everything first, then show what belongs to this state.
        layoutSaving.visibility = View.GONE
        layoutSaved.visibility = View.GONE
        textTooLarge.visibility = View.GONE
        textUnsupported.visibility = View.GONE
        layoutFailed.visibility = View.GONE
        btnChooseFile.isEnabled = true

        when (state) {
            ImportScreenState.Idle -> {
                // All hidden; choose button enabled — initial view.
            }

            ImportScreenState.Saving -> {
                btnChooseFile.isEnabled = false
                layoutSaving.visibility = View.VISIBLE
            }

            is ImportScreenState.Saved -> {
                layoutSaved.visibility = View.VISIBLE
                val savedId = state.id
                btnViewEvidence.setOnClickListener {
                    findNavController().navigate(
                        R.id.evidenceDetailFragment,
                        bundleOf(NavArgs.EVIDENCE_ID to savedId)
                    )
                }
            }

            ImportScreenState.TooLarge -> {
                textTooLarge.visibility = View.VISIBLE
            }

            ImportScreenState.UnsupportedType -> {
                textUnsupported.visibility = View.VISIBLE
            }

            ImportScreenState.Failed -> {
                layoutFailed.visibility = View.VISIBLE
            }
        }
    }
}
