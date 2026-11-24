package com.shieldher.dashboard.cases

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import com.shieldher.R
import com.shieldher.dashboard.common.ConfirmDialog
import com.shieldher.dashboard.common.EmptyStateView
import com.shieldher.dashboard.common.ErrorStateView
import com.shieldher.dashboard.common.NavArgs
import com.shieldher.dashboard.common.UiState
import com.shieldher.dashboard.common.viewModelFactory
import com.shieldher.dashboard.data.Case
import kotlinx.coroutines.launch

class CasesListFragment : Fragment() {

    private val viewModel: CasesListViewModel by viewModels {
        viewModelFactory { CasesListViewModel() }
    }

    private lateinit var adapter: CasesListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_cases_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnNewCase = view.findViewById<MaterialButton>(R.id.btn_new_case)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_cases)
        val progressLoading = view.findViewById<CircularProgressIndicator>(R.id.progress_loading)
        val emptyStateView = view.findViewById<EmptyStateView>(R.id.empty_state_view)
        val errorStateView = view.findViewById<ErrorStateView>(R.id.error_state_view)

        adapter = CasesListAdapter(
            onCaseClick = { case ->
                findNavController().navigate(
                    R.id.caseTimelineFragment,
                    bundleOf(NavArgs.CASE_ID to case.id)
                )
            },
            onRenameClick = { case -> showRenameCaseDialog(case) },
            onDeleteClick = { case -> showDeleteCaseDialog(case) }
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        btnNewCase.setOnClickListener { showNewCaseDialog() }

        emptyStateView.setContent(
            icon = R.drawable.ic_nav_cases,
            message = R.string.cases_list_empty_message,
            actionText = R.string.cases_list_action_new,
            onAction = { showNewCaseDialog() }
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
        state: UiState<List<Case>>,
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
            UiState.Loading -> {
                progressLoading.visibility = View.VISIBLE
            }

            UiState.Empty -> {
                emptyStateView.visibility = View.VISIBLE
            }

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
                        { viewModel.loadCases() }
                    } else null
                )
            }
        }
    }

    private fun showNewCaseDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_create_case, null)
        val inputTitle = dialogView.findViewById<TextInputEditText>(R.id.input_case_title)
        val inputDesc = dialogView.findViewById<TextInputEditText>(R.id.input_case_description)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.dialog_new_case_title)
            .setView(dialogView)
            .setPositiveButton(R.string.dialog_new_case_create) { _, _ ->
                val title = inputTitle.text?.toString() ?: ""
                val desc = inputDesc.text?.toString() ?: ""
                viewModel.createCase(title, desc)
            }
            .setNegativeButton(R.string.common_cancel, null)
            .show()
    }

    private fun showRenameCaseDialog(case: Case) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_rename_case, null)
        val inputTitle = dialogView.findViewById<TextInputEditText>(R.id.input_rename_title)
        inputTitle.setText(case.title)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.dialog_rename_case_title)
            .setView(dialogView)
            .setPositiveButton(R.string.dialog_rename_case_save) { _, _ ->
                val newTitle = inputTitle.text?.toString() ?: ""
                viewModel.renameCase(case.id, newTitle)
            }
            .setNegativeButton(R.string.common_cancel, null)
            .show()
    }

    private fun showDeleteCaseDialog(case: Case) {
        ConfirmDialog.show(
            context = requireContext(),
            title = R.string.dialog_delete_case_title,
            message = R.string.dialog_delete_case_message,
            confirmText = R.string.common_delete,
            destructive = true,
            onConfirm = {
                viewModel.deleteCase(case.id)
            }
        )
    }
}
