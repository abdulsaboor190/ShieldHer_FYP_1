package com.shieldher.dashboard.evidence

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.button.MaterialButton
import com.shieldher.R
import com.shieldher.dashboard.common.EmptyStateView
import com.shieldher.dashboard.common.ErrorStateView
import com.shieldher.dashboard.common.NavArgs
import com.shieldher.dashboard.common.viewModelFactory
import com.shieldher.dashboard.data.ServiceLocator
import kotlinx.coroutines.launch

class EvidenceListFragment : Fragment() {

    private val viewModel: DashboardEvidenceListViewModel by viewModels {
        viewModelFactory {
            DashboardEvidenceListViewModel(
                gateway = ServiceLocator.evidenceGateway,
                session = ServiceLocator.verificationSession
            )
        }
    }

    private var adapter: DashboardEvidenceListAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_evidence_list_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val loadingIndicator = view.findViewById<CircularProgressIndicator>(R.id.loading_indicator)
        val emptyView = view.findViewById<EmptyStateView>(R.id.empty_view)
        val errorView = view.findViewById<ErrorStateView>(R.id.error_view)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_evidence)
        val editSearch = view.findViewById<TextInputEditText>(R.id.edit_search)
        val buttonImport = view.findViewById<MaterialButton>(R.id.button_import)

        adapter = DashboardEvidenceListAdapter { evidenceId ->
            findNavController().navigate(
                R.id.evidenceDetailFragment,
                bundleOf(NavArgs.EVIDENCE_ID to evidenceId)
            )
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        buttonImport.setOnClickListener {
            findNavController().navigate(R.id.importFragment)
        }

        editSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.setSearchQuery(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is EvidenceListUiState.Loading -> {
                            loadingIndicator.visibility = View.VISIBLE
                            emptyView.visibility = View.GONE
                            errorView.visibility = View.GONE
                            recyclerView.visibility = View.GONE
                        }
                        is EvidenceListUiState.Empty -> {
                            loadingIndicator.visibility = View.GONE
                            emptyView.visibility = View.VISIBLE
                            errorView.visibility = View.GONE
                            recyclerView.visibility = View.GONE
                            emptyView.setContent(
                                icon = R.drawable.ic_nav_evidence,
                                message = R.string.evidence_list_empty_message,
                                actionText = R.string.evidence_list_action_import,
                                onAction = { findNavController().navigate(R.id.importFragment) }
                            )
                        }
                        is EvidenceListUiState.NoMatch -> {
                            loadingIndicator.visibility = View.GONE
                            emptyView.visibility = View.VISIBLE
                            errorView.visibility = View.GONE
                            recyclerView.visibility = View.GONE
                            emptyView.setContent(
                                icon = R.drawable.ic_search,
                                message = R.string.evidence_list_no_matches_message,
                                actionText = R.string.evidence_list_action_clear_search,
                                onAction = {
                                    editSearch.setText("")
                                    viewModel.clearSearch()
                                }
                            )
                        }
                        is EvidenceListUiState.Content -> {
                            loadingIndicator.visibility = View.GONE
                            emptyView.visibility = View.GONE
                            errorView.visibility = View.GONE
                            recyclerView.visibility = View.VISIBLE
                            adapter?.submitList(state.items)
                        }
                        is EvidenceListUiState.Error -> {
                            loadingIndicator.visibility = View.GONE
                            emptyView.visibility = View.GONE
                            errorView.visibility = View.VISIBLE
                            recyclerView.visibility = View.GONE
                            errorView.setContent(
                                message = state.messageRes,
                                canRetry = true,
                                onRetry = null
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        adapter = null
    }
}
