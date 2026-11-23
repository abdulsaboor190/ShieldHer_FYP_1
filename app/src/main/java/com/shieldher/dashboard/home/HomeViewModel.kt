package com.shieldher.dashboard.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shieldher.R
import com.shieldher.dashboard.common.UiState
import com.shieldher.dashboard.common.toCardModel
import com.shieldher.dashboard.data.CaseRepository
import com.shieldher.dashboard.data.EvidenceGateway
import com.shieldher.dashboard.data.VerificationSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val evidenceGateway: EvidenceGateway,
    private val caseRepository: CaseRepository,
    private val verificationSession: VerificationSession
) : ViewModel() {

    private val refreshTrigger = MutableStateFlow(0)

    val uiState: StateFlow<UiState<HomeContent>> = combine(
        evidenceGateway.observeEvidence(),
        caseRepository.observeCases().map<List<Any>, Int?> { it.size }.catch { emit(null) },
        verificationSession.records,
        refreshTrigger
    ) { evidenceList, caseCount, records, _ ->
        if (evidenceList.isEmpty()) {
            UiState.Empty
        } else {
            val sorted = evidenceList.sortedByDescending { it.receiptTimeEpochMillis }
            val latest = sorted.take(3).map { item ->
                val record = records[item.id]
                HomeEvidenceItem(
                    id = item.id,
                    cardModel = item.toCardModel(record, null)
                )
            }
            UiState.Content(
                HomeContent(
                    evidenceCount = evidenceList.size,
                    caseCount = caseCount,
                    latestItems = latest
                )
            )
        }
    }
        .catch {
            emit(UiState.Error(R.string.error_generic))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UiState.Loading
        )

    fun retry() {
        viewModelScope.launch {
            refreshTrigger.value += 1
        }
    }
}
