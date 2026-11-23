package com.shieldher.dashboard.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
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
import com.shieldher.dashboard.common.EmptyStateView
import com.shieldher.dashboard.common.ErrorStateView
import com.shieldher.dashboard.common.EvidenceCardView
import com.shieldher.dashboard.common.NavArgs
import com.shieldher.dashboard.common.UiState
import com.shieldher.dashboard.common.viewModelFactory
import com.shieldher.dashboard.data.ServiceLocator
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private val viewModel: HomeViewModel by viewModels {
        viewModelFactory {
            HomeViewModel(
                evidenceGateway = ServiceLocator.evidenceGateway,
                caseRepository = ServiceLocator.caseRepository,
                verificationSession = ServiceLocator.verificationSession
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val loadingIndicator = view.findViewById<CircularProgressIndicator>(R.id.loading_indicator)
        val errorView = view.findViewById<ErrorStateView>(R.id.error_view)
        val emptyView = view.findViewById<EmptyStateView>(R.id.empty_view)
        val contentScrollView = view.findViewById<View>(R.id.content_scroll_view)

        val evidenceCountText = view.findViewById<TextView>(R.id.text_evidence_count)
        val caseCountText = view.findViewById<TextView>(R.id.text_case_count)
        val cardsContainer = view.findViewById<LinearLayout>(R.id.latest_cards_container)
        val seeAllButton = view.findViewById<View>(R.id.action_see_all)
        val importButton = view.findViewById<MaterialButton>(R.id.button_import)

        seeAllButton.setOnClickListener {
            findNavController().navigate(R.id.evidence)
        }

        importButton.setOnClickListener {
            findNavController().navigate(R.id.importFragment)
        }

        emptyView.setContent(
            icon = R.drawable.ic_nav_evidence,
            message = R.string.home_empty_message,
            actionText = R.string.home_action_import,
            onAction = { findNavController().navigate(R.id.importFragment) }
        )

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is UiState.Loading -> {
                            loadingIndicator.visibility = View.VISIBLE
                            errorView.visibility = View.GONE
                            emptyView.visibility = View.GONE
                            contentScrollView.visibility = View.GONE
                        }
                        is UiState.Error -> {
                            loadingIndicator.visibility = View.GONE
                            errorView.visibility = View.VISIBLE
                            emptyView.visibility = View.GONE
                            contentScrollView.visibility = View.GONE
                            errorView.setContent(
                                message = state.messageRes,
                                canRetry = state.canRetry,
                                onRetry = { viewModel.retry() }
                            )
                        }
                        is UiState.Empty -> {
                            loadingIndicator.visibility = View.GONE
                            errorView.visibility = View.GONE
                            emptyView.visibility = View.VISIBLE
                            contentScrollView.visibility = View.GONE
                        }
                        is UiState.Content -> {
                            loadingIndicator.visibility = View.GONE
                            errorView.visibility = View.GONE
                            emptyView.visibility = View.GONE
                            contentScrollView.visibility = View.VISIBLE

                            val data = state.data
                            evidenceCountText.text = data.evidenceCount.toString()
                            caseCountText.text = if (data.caseCount != null) {
                                data.caseCount.toString()
                            } else {
                                getString(R.string.home_stat_cases_unavailable)
                            }

                            cardsContainer.removeAllViews()
                            val spacing = resources.getDimensionPixelSize(R.dimen.sh_space_8)
                            for (item in data.latestItems) {
                                val cardView = EvidenceCardView(requireContext())
                                cardView.bind(item.cardModel)
                                cardView.setOnClickListener {
                                    val args = bundleOf(NavArgs.EVIDENCE_ID to item.id)
                                    findNavController().navigate(R.id.evidenceDetailFragment, args)
                                }
                                val lp = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                                ).apply {
                                    bottomMargin = spacing
                                }
                                cardsContainer.addView(cardView, lp)
                            }
                        }
                    }
                }
            }
        }
    }
}
