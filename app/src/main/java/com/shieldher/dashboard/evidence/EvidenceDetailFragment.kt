package com.shieldher.dashboard.evidence

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import com.shieldher.R
import com.shieldher.dashboard.common.BadgeStatus
import com.shieldher.dashboard.common.ConfirmDialog
import com.shieldher.dashboard.common.EvidenceMappers
import com.shieldher.dashboard.common.ErrorStateView
import com.shieldher.dashboard.common.NavArgs
import com.shieldher.dashboard.common.StatusBadge
import com.shieldher.dashboard.common.TimeFormat
import com.shieldher.dashboard.common.UiState
import com.shieldher.dashboard.common.viewModelFactory
import com.shieldher.dashboard.data.Case
import com.shieldher.dashboard.data.ServiceLocator
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class EvidenceDetailFragment : Fragment() {

    private val evidenceId: String by lazy {
        arguments?.getString(NavArgs.EVIDENCE_ID) ?: ""
    }

    private val viewModel: EvidenceDetailViewModel by viewModels {
        viewModelFactory {
            EvidenceDetailViewModel(
                evidenceId = evidenceId,
                gateway = ServiceLocator.evidenceGateway,
                caseRepository = ServiceLocator.caseRepository,
                session = ServiceLocator.verificationSession
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_evidence_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val loadingIndicator = view.findViewById<CircularProgressIndicator>(R.id.loading_indicator)
        val errorView = view.findViewById<ErrorStateView>(R.id.error_view)
        val contentScrollView = view.findViewById<View>(R.id.content_scroll_view)

        // Metadata views
        val hashValueText = view.findViewById<TextView>(R.id.text_hash_value)
        val copyHashButton = view.findViewById<MaterialButton>(R.id.button_copy_hash)
        val rowHash = view.findViewById<View>(R.id.row_hash)
        val receivedTimeText = view.findViewById<TextView>(R.id.text_received_time_value)
        val rowReceivedTime = view.findViewById<View>(R.id.row_received_time)
        val sourceValueText = view.findViewById<TextView>(R.id.text_source_value)
        val rowSource = view.findViewById<View>(R.id.row_source)
        val typeValueText = view.findViewById<TextView>(R.id.text_type_value)
        val rowType = view.findViewById<View>(R.id.row_type)
        val sizeValueText = view.findViewById<TextView>(R.id.text_size_value)
        val rowSize = view.findViewById<View>(R.id.row_size)

        // Preview views
        val previewLoading = view.findViewById<CircularProgressIndicator>(R.id.preview_loading)
        val previewImage = view.findViewById<ImageView>(R.id.preview_image)
        val previewText = view.findViewById<TextView>(R.id.preview_text)
        val previewFallback = view.findViewById<TextView>(R.id.preview_fallback)

        // Verification views
        val verificationBadge = view.findViewById<StatusBadge>(R.id.verification_badge)
        val textCheckedTime = view.findViewById<TextView>(R.id.text_checked_time)
        val textVerificationNote = view.findViewById<TextView>(R.id.text_verification_note)
        val layoutMeaning = view.findViewById<LinearLayout>(R.id.layout_meaning)
        val buttonToggleMeaning = view.findViewById<TextView>(R.id.button_toggle_meaning)
        val textMeaningBody = view.findViewById<TextView>(R.id.text_meaning_body)
        val buttonVerify = view.findViewById<MaterialButton>(R.id.button_verify)
        val verifyProgress = view.findViewById<CircularProgressIndicator>(R.id.verify_progress)

        // Linked Cases views
        val containerCasesList = view.findViewById<LinearLayout>(R.id.container_cases_list)
        val textNoLinkedCases = view.findViewById<TextView>(R.id.text_no_linked_cases)
        val textCasesUnreadable = view.findViewById<TextView>(R.id.text_cases_unreadable)
        val buttonAddToCase = view.findViewById<MaterialButton>(R.id.button_add_to_case)

        // Delete action button
        val buttonDeleteEvidence = view.findViewById<MaterialButton>(R.id.button_delete_evidence)

        buttonToggleMeaning.setOnClickListener {
            if (textMeaningBody.visibility == View.VISIBLE) {
                textMeaningBody.visibility = View.GONE
            } else {
                textMeaningBody.visibility = View.VISIBLE
            }
        }

        buttonVerify.setOnClickListener {
            viewModel.verify()
        }

        buttonAddToCase.setOnClickListener {
            showAddToCaseDialog()
        }

        buttonDeleteEvidence.setOnClickListener {
            ConfirmDialog.show(
                context = requireContext(),
                title = R.string.dialog_delete_title,
                message = R.string.dialog_delete_message,
                confirmText = R.string.common_delete,
                destructive = true,
                onConfirm = {
                    viewModel.deleteEvidence()
                }
            )
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.deleteEvent.collect { success ->
                        if (success) {
                            findNavController().popBackStack()
                        } else {
                            Toast.makeText(requireContext(), R.string.detail_delete_failed, Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                launch {
                    viewModel.uiState.collect { state ->
                        when (state) {
                            is UiState.Loading -> {
                                loadingIndicator.visibility = View.VISIBLE
                                errorView.visibility = View.GONE
                                contentScrollView.visibility = View.GONE
                            }
                            is UiState.Error -> {
                                loadingIndicator.visibility = View.GONE
                                errorView.visibility = View.VISIBLE
                                contentScrollView.visibility = View.GONE
                                errorView.setContent(
                                    message = state.messageRes,
                                    canRetry = state.canRetry,
                                    onRetry = null
                                )
                            }
                            is UiState.Empty -> {
                                loadingIndicator.visibility = View.GONE
                                errorView.visibility = View.VISIBLE
                                contentScrollView.visibility = View.GONE
                                errorView.setContent(
                                    message = R.string.detail_not_found,
                                    canRetry = false,
                                    onRetry = null
                                )
                            }
                            is UiState.Content -> {
                                loadingIndicator.visibility = View.GONE
                                errorView.visibility = View.GONE
                                contentScrollView.visibility = View.VISIBLE

                                val content = state.data
                                val item = content.item

                                // 1. Metadata bindings
                                hashValueText.text = item.hash
                                copyHashButton.setOnClickListener {
                                    val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Evidence SHA-256", item.hash)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(requireContext(), R.string.detail_hash_copied, Toast.LENGTH_SHORT).show()
                                }
                                rowHash.contentDescription = "${getString(R.string.detail_hash_label)}: ${item.hash}"

                                receivedTimeText.text = content.formattedReceivedTime
                                rowReceivedTime.contentDescription = "${getString(R.string.label_received)}: ${content.formattedReceivedTime}"

                                val sourceText = item.sourceLabel ?: getString(R.string.type_file)
                                sourceValueText.text = sourceText
                                rowSource.contentDescription = "${getString(R.string.detail_source_label)}: $sourceText"

                                val typeName = getString(EvidenceMappers.friendlyType(item.mimeType))
                                val fullType = "$typeName (${item.mimeType})"
                                typeValueText.text = fullType
                                rowType.contentDescription = "${getString(R.string.detail_type_label)}: $fullType"

                                val sizeText = "${item.fileSizeBytes} bytes"
                                sizeValueText.text = sizeText
                                rowSize.contentDescription = "${getString(R.string.detail_stored_size_label)}: $sizeText"

                                // 2. Preview bindings
                                when (val preview = content.previewState) {
                                    is PreviewState.Loading -> {
                                        previewLoading.visibility = View.VISIBLE
                                        previewImage.visibility = View.GONE
                                        previewText.visibility = View.GONE
                                        previewFallback.visibility = View.GONE
                                    }
                                    is PreviewState.Image -> {
                                        previewLoading.visibility = View.GONE
                                        previewImage.visibility = View.VISIBLE
                                        previewText.visibility = View.GONE
                                        previewFallback.visibility = View.GONE
                                        previewImage.setImageBitmap(preview.bitmap)
                                    }
                                    is PreviewState.Text -> {
                                        previewLoading.visibility = View.GONE
                                        previewImage.visibility = View.GONE
                                        previewText.visibility = View.VISIBLE
                                        previewFallback.visibility = View.GONE
                                        previewText.text = preview.content
                                    }
                                    PreviewState.TooLarge -> {
                                        previewLoading.visibility = View.GONE
                                        previewImage.visibility = View.GONE
                                        previewText.visibility = View.GONE
                                        previewFallback.visibility = View.VISIBLE
                                        previewFallback.setText(R.string.detail_preview_too_large)
                                    }
                                    PreviewState.Unsupported -> {
                                        previewLoading.visibility = View.GONE
                                        previewImage.visibility = View.GONE
                                        previewText.visibility = View.GONE
                                        previewFallback.visibility = View.VISIBLE
                                        previewFallback.setText(R.string.detail_preview_unsupported)
                                    }
                                    PreviewState.Unavailable -> {
                                        previewLoading.visibility = View.GONE
                                        previewImage.visibility = View.GONE
                                        previewText.visibility = View.GONE
                                        previewFallback.visibility = View.VISIBLE
                                        previewFallback.setText(R.string.detail_preview_unavailable)
                                    }
                                }

                                // 3. Verification bindings
                                val vState = content.verificationState
                                verificationBadge.setStatus(vState.status)

                                if (vState.checkedAtText != null) {
                                    textCheckedTime.visibility = View.VISIBLE
                                    textCheckedTime.text = getString(R.string.detail_checked_time_prefix, vState.checkedAtText)
                                } else {
                                    textCheckedTime.visibility = View.GONE
                                }

                                when (vState.status) {
                                    BadgeStatus.UNCHANGED -> {
                                        textVerificationNote.visibility = View.GONE
                                        layoutMeaning.visibility = View.VISIBLE
                                    }
                                    BadgeStatus.CHANGED -> {
                                        textVerificationNote.visibility = View.VISIBLE
                                        textVerificationNote.setText(R.string.detail_changed_explanation)
                                        layoutMeaning.visibility = View.GONE
                                        textMeaningBody.visibility = View.GONE
                                    }
                                    BadgeStatus.MISSING -> {
                                        textVerificationNote.visibility = View.VISIBLE
                                        textVerificationNote.setText(R.string.detail_missing_explanation)
                                        layoutMeaning.visibility = View.GONE
                                        textMeaningBody.visibility = View.GONE
                                    }
                                    else -> {
                                        textVerificationNote.visibility = View.GONE
                                        layoutMeaning.visibility = View.GONE
                                        textMeaningBody.visibility = View.GONE
                                    }
                                }

                                if (vState.isVerifying) {
                                    buttonVerify.isEnabled = false
                                    buttonVerify.text = ""
                                    verifyProgress.visibility = View.VISIBLE
                                } else {
                                    buttonVerify.isEnabled = true
                                    buttonVerify.setText(R.string.detail_action_verify)
                                    verifyProgress.visibility = View.GONE
                                }

                                // 4. Linked Cases bindings
                                bindLinkedCases(
                                    state = content.linkedCasesState,
                                    container = containerCasesList,
                                    emptyText = textNoLinkedCases,
                                    unreadableText = textCasesUnreadable
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun bindLinkedCases(
        state: LinkedCasesState,
        container: LinearLayout,
        emptyText: TextView,
        unreadableText: TextView
    ) {
        container.removeAllViews()
        when (state) {
            is LinkedCasesState.Loading -> {
                emptyText.visibility = View.GONE
                unreadableText.visibility = View.GONE
            }
            is LinkedCasesState.Unreadable -> {
                emptyText.visibility = View.GONE
                unreadableText.visibility = View.VISIBLE
            }
            is LinkedCasesState.Content -> {
                unreadableText.visibility = View.GONE
                if (state.cases.isEmpty()) {
                    emptyText.visibility = View.VISIBLE
                } else {
                    emptyText.visibility = View.GONE
                    val inflater = LayoutInflater.from(requireContext())
                    for (c in state.cases) {
                        val row = inflater.inflate(R.layout.item_linked_case, container, false)
                        val titleView = row.findViewById<TextView>(R.id.text_case_title)
                        titleView.text = c.title
                        row.setOnClickListener {
                            findNavController().navigate(
                                R.id.caseTimelineFragment,
                                bundleOf(NavArgs.CASE_ID to c.id)
                            )
                        }
                        container.addView(row)
                    }
                }
            }
        }
    }

    private fun showAddToCaseDialog() {
        lifecycleScope.launch {
            val cases = viewModel.allCases.first()
            if (cases.isEmpty()) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.detail_add_to_case_title)
                    .setMessage(R.string.detail_no_cases_found)
                    .setPositiveButton(R.string.detail_action_create_case_first) { _, _ ->
                        findNavController().navigate(R.id.cases)
                    }
                    .setNegativeButton(R.string.common_cancel, null)
                    .show()
                return@launch
            }

            val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_to_case, null)
            val caseSelectAutoComplete = dialogView.findViewById<AutoCompleteTextView>(R.id.text_case_select)
            val editNote = dialogView.findViewById<TextInputEditText>(R.id.edit_note)
            val textSelectedDate = dialogView.findViewById<TextView>(R.id.text_selected_date)
            val buttonPickDate = dialogView.findViewById<MaterialButton>(R.id.button_pick_date)

            val caseTitles = cases.map { it.title }
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, caseTitles)
            caseSelectAutoComplete.setAdapter(adapter)

            var selectedCaseIndex = 0
            caseSelectAutoComplete.setText(cases[0].title, false)
            caseSelectAutoComplete.setOnItemClickListener { _, _, position, _ ->
                selectedCaseIndex = position
            }

            var chosenDateMillis: Long? = null
            buttonPickDate.setOnClickListener {
                val picker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText(R.string.detail_date_label)
                    .build()
                picker.addOnPositiveButtonClickListener { selection ->
                    chosenDateMillis = selection
                    textSelectedDate.text = getString(R.string.detail_date_label) + ": " + TimeFormat.formatDateOnlyUtc(selection)
                }
                picker.show(childFragmentManager, "ADD_TO_CASE_DATE_PICKER")
            }

            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.detail_add_to_case_title)
                .setView(dialogView)
                .setPositiveButton(R.string.detail_action_confirm_add) { _, _ ->
                    val chosenCase = cases.getOrNull(selectedCaseIndex) ?: cases.first()
                    val noteText = editNote.text?.toString()?.trim() ?: ""
                    viewModel.addToCase(
                        caseId = chosenCase.id,
                        note = noteText,
                        incidentDateMillis = chosenDateMillis
                    )
                }
                .setNegativeButton(R.string.common_cancel, null)
                .show()
        }
    }
}
