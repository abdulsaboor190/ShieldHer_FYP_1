package com.shieldher.dashboard.evidence

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shieldher.R
import com.shieldher.dashboard.common.EvidenceMappers
import com.shieldher.dashboard.common.TimeFormat
import com.shieldher.dashboard.common.UiState
import com.shieldher.dashboard.common.toCardModel
import com.shieldher.dashboard.data.EvidenceGateway
import com.shieldher.dashboard.data.EvidenceItem
import com.shieldher.dashboard.data.VerificationSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

sealed interface EvidenceListUiState {
    data object Loading : EvidenceListUiState
    data object Empty : EvidenceListUiState
    data class NoMatch(val query: String) : EvidenceListUiState
    data class Content(val items: List<DashboardEvidenceListItem>) : EvidenceListUiState
    data class Error(val messageRes: Int) : EvidenceListUiState
}

class DashboardEvidenceListViewModel(
    private val gateway: EvidenceGateway,
    private val session: VerificationSession
) : ViewModel() {

    private val _uiState = MutableStateFlow<EvidenceListUiState>(EvidenceListUiState.Loading)
    val uiState: StateFlow<EvidenceListUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                gateway.observeEvidence(),
                session.records,
                _searchQuery
            ) { allItems, records, query ->
                Triple(allItems, records, query)
            }.collectLatest { (allItems, records, query) ->
                if (allItems.isEmpty()) {
                    _uiState.value = EvidenceListUiState.Empty
                    return@collectLatest
                }

                // Sort newest first
                val sorted = allItems.sortedByDescending { it.receiptTimeEpochMillis }

                val filtered = if (query.isBlank()) {
                    sorted
                } else {
                    val lowerQuery = query.lowercase()
                    sorted.filter { item ->
                        val sourceMatch = item.sourceLabel?.lowercase()?.contains(lowerQuery) == true
                        val dateText = TimeFormat.formatDateTime(item.receiptTimeEpochMillis, item.timeZoneId).lowercase()
                        val dateMatch = dateText.contains(lowerQuery)
                        sourceMatch || dateMatch
                    }
                }

                if (filtered.isEmpty()) {
                    _uiState.value = EvidenceListUiState.NoMatch(query)
                } else {
                    val listItems = filtered.map { item ->
                        DashboardEvidenceListItem(
                            id = item.id,
                            cardModel = item.toCardModel(records[item.id], null)
                        )
                    }
                    _uiState.value = EvidenceListUiState.Content(listItems)
                }
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearSearch() {
        _searchQuery.value = ""
    }
}
