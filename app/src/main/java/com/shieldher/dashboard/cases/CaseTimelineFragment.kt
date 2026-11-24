package com.shieldher.dashboard.cases

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import com.shieldher.R
import com.shieldher.dashboard.common.ConfirmDialog
import com.shieldher.dashboard.common.EmptyStateView
import com.shieldher.dashboard.common.ErrorStateView
import com.shieldher.dashboard.common.NavArgs
import com.shieldher.dashboard.common.TimeFormat
import com.shieldher.dashboard.common.UiState
import com.shieldher.dashboard.common.viewModelFactory
import com.shieldher.dashboard.data.EvidenceItem
import kotlinx.coroutines.launch

class CaseTimelineFragment : Fragment() {

    private val caseId: String by lazy {
        arguments?.getString(NavArgs.CASE_ID) ?: ""
    }

    private val viewModel: CaseTimelineViewModel by viewModels {
        viewModelFactory { CaseTimelineViewModel(caseId) }
    }

    private lateinit var adapter: CaseTimelineAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_case_timeline, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnAddEvidence = view.findViewById<MaterialButton>(R.id.btn_add_evidence)
        val btnAddNote = view.findViewById<MaterialButton>(R.id.btn_add_note)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_timeline)
        val progressLoading = view.findViewById<CircularProgressIndicator>(R.id.progress_loading)
        val emptyStateView = view.findViewById<EmptyStateView>(R.id.empty_state_view)
        val errorStateView = view.findViewById<ErrorStateView>(R.id.error_state_view)

        adapter = CaseTimelineAdapter(
            onEvidenceClick = { evidenceId ->
                findNavController().navigate(
                    R.id.evidenceDetailFragment,
                    bundleOf(NavArgs.EVIDENCE_ID to evidenceId)
                )
            },
            onEditClick = { entry -> showEditDialog(entry) },
            onRemoveClick = { entry -> showRemoveDialog(entry) }
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        btnAddEvidence.setOnClickListener { showAddEvidenceDialog() }
        btnAddNote.setOnClickListener { showAddNoteDialog() }

        emptyStateView.setContent(
            icon = R.drawable.ic_nav_cases,
            message = R.string.timeline_empty_message,
            actionText = R.string.timeline_action_add_evidence,
            onAction = { showAddEvidenceDialog() }
        )

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    renderState(state, recyclerView, progressLoading, emptyStateView, errorStateView)
                }
            }
        }
    }

    private fun renderState(
        state: UiState<List<TimelineEntry>>,
        recyclerView: RecyclerView,
        progressLoading: CircularProgressIndicator,
        emptyStateView: EmptyStateView,
        errorStateView: ErrorStateView
    ) {
        recyclerView.visibility = View.GONE
        progressLoading.visibility = View.GONE
        emptyStateView.visibility = View.GONE
        errorStateView.visibility = View.GONE

        when (state) {
            UiState.Loading -> progressLoading.visibility = View.VISIBLE
            UiState.Empty -> emptyStateView.visibility = View.VISIBLE
            is UiState.Content -> {
                recyclerView.visibility = View.VISIBLE
                adapter.submitList(state.data)
            }
            is UiState.Error -> {
                errorStateView.visibility = View.VISIBLE
                errorStateView.setContent(
                    message = state.messageRes,
                    canRetry = state.canRetry,
                    onRetry = if (state.canRetry) {
                        { viewModel.loadTimeline() }
                    } else null
                )
            }
        }
    }

    private fun showAddEvidenceDialog() {
        viewLifecycleOwner.lifecycleScope.launch {
            val allEvidence = viewModel.getAvailableEvidence()
            if (allEvidence.isEmpty()) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.dialog_add_evidence_title)
                    .setMessage(R.string.dialog_add_evidence_empty)
                    .setPositiveButton(R.string.common_ok, null)
                    .show()
                return@launch
            }

            val currentItems = (viewModel.uiState.value as? UiState.Content)?.data ?: emptyList()
            val linkedEvidenceIds = currentItems.mapNotNull { it.caseItem.evidenceId }.toSet()

            val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_timeline_add_evidence, null)
            val dropdownEvidence = dialogView.findViewById<AutoCompleteTextView>(R.id.dropdown_evidence)
            val inputNote = dialogView.findViewById<TextInputEditText>(R.id.input_evidence_note)
            val textSelectedDate = dialogView.findViewById<TextView>(R.id.text_selected_date)
            val btnPickDate = dialogView.findViewById<MaterialButton>(R.id.btn_pick_date)

            val displayLabels = allEvidence.map { ev ->
                val label = ev.sourceLabel ?: getString(R.string.type_file)
                val base = label + " (" + TimeFormat.formatDateTime(ev.receiptTimeEpochMillis, ev.timeZoneId) + ")"
                if (linkedEvidenceIds.contains(ev.id)) {
                    getString(R.string.dialog_add_evidence_already_in_case, base)
                } else {
                    base
                }
            }

            val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, displayLabels)
            dropdownEvidence.setAdapter(spinnerAdapter)

            var selectedEvidenceIndex: Int = -1
            dropdownEvidence.setOnItemClickListener { _, _, position, _ ->
                selectedEvidenceIndex = position
            }

            var chosenDateMillis: Long? = null
            btnPickDate.setOnClickListener {
                val picker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText(R.string.dialog_action_pick_date)
                    .build()
                picker.addOnPositiveButtonClickListener { selection ->
                    chosenDateMillis = selection
                    textSelectedDate.text = getString(R.string.timeline_label_you_entered, TimeFormat.formatDateOnlyUtc(selection))
                }
                picker.show(childFragmentManager, "timeline_date_picker")
            }

            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.dialog_add_evidence_title)
                .setView(dialogView)
                .setPositiveButton(R.string.dialog_action_add) { _, _ ->
                    if (selectedEvidenceIndex in allEvidence.indices) {
                        val evidenceId = allEvidence[selectedEvidenceIndex].id
                        val note = inputNote.text?.toString() ?: ""
                        viewModel.addEvidence(evidenceId, note, chosenDateMillis)
                    }
                }
                .setNegativeButton(R.string.common_cancel, null)
                .show()
        }
    }

    private fun showAddNoteDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_timeline_note_date, null)
        val inputNote = dialogView.findViewById<TextInputEditText>(R.id.input_note)
        val textSelectedDate = dialogView.findViewById<TextView>(R.id.text_selected_date)
        val btnPickDate = dialogView.findViewById<MaterialButton>(R.id.btn_pick_date)

        var chosenDateMillis: Long? = null
        btnPickDate.setOnClickListener {
            val picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(R.string.dialog_action_pick_date)
                .build()
            picker.addOnPositiveButtonClickListener { selection ->
                chosenDateMillis = selection
                textSelectedDate.text = getString(R.string.timeline_label_you_entered, TimeFormat.formatDateOnlyUtc(selection))
            }
            picker.show(childFragmentManager, "timeline_note_picker")
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.dialog_add_note_title)
            .setView(dialogView)
            .setPositiveButton(R.string.dialog_action_add) { _, _ ->
                val note = inputNote.text?.toString() ?: ""
                viewModel.addNote(note, chosenDateMillis)
            }
            .setNegativeButton(R.string.common_cancel, null)
            .show()
    }

    private fun showEditDialog(entry: TimelineEntry) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_timeline_note_date, null)
        val inputNote = dialogView.findViewById<TextInputEditText>(R.id.input_note)
        val textSelectedDate = dialogView.findViewById<TextView>(R.id.text_selected_date)
        val btnPickDate = dialogView.findViewById<MaterialButton>(R.id.btn_pick_date)

        inputNote.setText(entry.caseItem.note)
        var chosenDateMillis: Long? = entry.caseItem.incidentDateMillis

        if (chosenDateMillis != null) {
            textSelectedDate.text = getString(R.string.timeline_label_you_entered, TimeFormat.formatDateOnlyUtc(chosenDateMillis!!))
        }

        btnPickDate.setOnClickListener {
            val picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(R.string.dialog_action_pick_date)
                .setSelection(chosenDateMillis ?: MaterialDatePicker.todayInUtcMilliseconds())
                .build()
            picker.addOnPositiveButtonClickListener { selection ->
                chosenDateMillis = selection
                textSelectedDate.text = getString(R.string.timeline_label_you_entered, TimeFormat.formatDateOnlyUtc(selection))
            }
            picker.show(childFragmentManager, "timeline_edit_date_picker")
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.dialog_edit_item_title)
            .setView(dialogView)
            .setPositiveButton(R.string.dialog_action_save) { _, _ ->
                val updatedNote = inputNote.text?.toString() ?: ""
                viewModel.updateItem(entry.caseItem.id, updatedNote, chosenDateMillis)
            }
            .setNegativeButton(R.string.common_cancel, null)
            .show()
    }

    private fun showRemoveDialog(entry: TimelineEntry) {
        ConfirmDialog.show(
            context = requireContext(),
            title = R.string.dialog_remove_item_title,
            message = R.string.dialog_remove_item_message,
            confirmText = R.string.dialog_remove_item_confirm,
            destructive = false,
            onConfirm = {
                viewModel.removeItem(entry.caseItem.id)
            }
        )
    }
}
