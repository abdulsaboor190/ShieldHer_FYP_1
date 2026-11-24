package com.shieldher.dashboard.cases

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shieldher.R
import com.shieldher.dashboard.common.BadgeStatus
import com.shieldher.dashboard.common.TimeFormat
import com.shieldher.dashboard.common.UiState
import com.shieldher.dashboard.data.CaseItem
import com.shieldher.dashboard.data.CaseRepository
import com.shieldher.dashboard.data.EvidenceGateway
import com.shieldher.dashboard.data.EvidenceItem
import com.shieldher.dashboard.data.ServiceLocator
import com.shieldher.dashboard.data.VerificationSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class CaseTimelineViewModel(
    private val caseId: String,
    private val caseRepository: CaseRepository = ServiceLocator.caseRepository,
    private val evidenceGateway: EvidenceGateway = ServiceLocator.evidenceGateway,
    private val verificationSession: VerificationSession = ServiceLocator.verificationSession
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<TimelineEntry>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<TimelineEntry>>> = _uiState.asStateFlow()

    init {
        loadTimeline()
    }

    fun loadTimeline() {
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            combine(
                caseRepository.observeItems(caseId),
                evidenceGateway.observeEvidence(),
                verificationSession.records
            ) { items, evidenceList, verificationRecords ->
                Triple(items, evidenceList, verificationRecords)
            }
                .catch { ex ->
                    if (ex is CaseStoreException) {
                        _uiState.value = UiState.Error(
                            messageRes = R.string.timeline_unreadable_store,
                            canRetry = false
                        )
                    } else {
                        _uiState.value = UiState.Error(
                            messageRes = R.string.timeline_error_message,
                            canRetry = true
                        )
                    }
                }
                .collect { (items, evidenceList, verificationRecords) ->
                    if (items.isEmpty()) {
                        _uiState.value = UiState.Empty
                    } else {
                        val evidenceMap = evidenceList.associateBy { it.id }
                        val entries = items.map { item ->
                            buildTimelineEntry(item, evidenceMap, verificationRecords)
                        }
                        // Order: oldest first
                        val sorted = entries.sortedBy { it.effectiveSortTimeMillis }
                        _uiState.value = UiState.Content(sorted)
                    }
                }
        }
    }

    private fun buildTimelineEntry(
        item: CaseItem,
        evidenceMap: Map<String, EvidenceItem>,
        verificationRecords: Map<String, com.shieldher.dashboard.data.VerificationRecord>
    ): TimelineEntry {
        val evidenceId = item.evidenceId
        val evidence = if (evidenceId != null) evidenceMap[evidenceId] else null

        // 3-level ordering rule:
        // 1) incidentDateMillis if present
        // 2) evidence receipt time if evidence present
        // 3) item's addedAtMillis
        val effectiveSortTime: Long = when {
            item.incidentDateMillis != null -> item.incidentDateMillis
            evidence != null -> evidence.receiptTimeEpochMillis
            else -> item.addedAtMillis
        }

        // Label string rule:
        // "You entered: <formatDateOnlyUtc>" when user entered a date
        // otherwise "Received by ShieldHer: <formatDateTime>" for evidence
        // otherwise "Added: <time>" for note with no date
        val timeLabelText = when {
            item.incidentDateMillis != null -> {
                "You entered: " + TimeFormat.formatDateOnlyUtc(item.incidentDateMillis)
            }
            evidence != null -> {
                "Received by ShieldHer: " + TimeFormat.formatDateTime(evidence.receiptTimeEpochMillis, evidence.timeZoneId)
            }
            else -> {
                "Added: " + TimeFormat.formatDateTime(item.addedAtMillis, null)
            }
        }

        val noteText = item.note.takeIf { it.isNotBlank() }

        return when {
            evidenceId == null -> {
                TimelineEntry.NoteOnly(
                    caseItem = item,
                    effectiveSortTimeMillis = effectiveSortTime,
                    timeLabelText = timeLabelText,
                    noteText = item.note
                )
            }
            evidence != null -> {
                val record = verificationRecords[evidenceId]
                val status = when (record?.result) {
                    com.shieldher.dashboard.data.EvidenceIntegrity.UNCHANGED -> BadgeStatus.UNCHANGED
                    com.shieldher.dashboard.data.EvidenceIntegrity.CHANGED -> BadgeStatus.CHANGED
                    com.shieldher.dashboard.data.EvidenceIntegrity.MISSING -> BadgeStatus.MISSING
                    null -> BadgeStatus.NOT_CHECKED
                }
                TimelineEntry.ActiveEvidence(
                    caseItem = item,
                    evidence = evidence,
                    status = status,
                    effectiveSortTimeMillis = effectiveSortTime,
                    timeLabelText = timeLabelText,
                    noteText = noteText
                )
            }
            else -> {
                // evidenceId exists but evidence is null (evidence was deleted)
                TimelineEntry.DeletedEvidence(
                    caseItem = item,
                    effectiveSortTimeMillis = effectiveSortTime,
                    timeLabelText = timeLabelText,
                    noteText = noteText
                )
            }
        }
    }

    suspend fun getAvailableEvidence(): List<EvidenceItem> {
        return try {
            evidenceGateway.observeEvidence().first()
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun addEvidence(evidenceId: String, note: String, incidentDateMillis: Long?) {
        viewModelScope.launch {
            try {
                caseRepository.addEvidenceItem(caseId, evidenceId, note.trim(), incidentDateMillis)
            } catch (ex: CaseStoreException) {
                _uiState.value = UiState.Error(
                    messageRes = R.string.timeline_unreadable_store,
                    canRetry = false
                )
            } catch (_: Exception) {
            }
        }
    }

    fun addNote(note: String, incidentDateMillis: Long?) {
        val trimmed = note.trim()
        if (trimmed.isBlank()) return
        viewModelScope.launch {
            try {
                caseRepository.addNoteItem(caseId, trimmed, incidentDateMillis)
            } catch (ex: CaseStoreException) {
                _uiState.value = UiState.Error(
                    messageRes = R.string.timeline_unreadable_store,
                    canRetry = false
                )
            } catch (_: Exception) {
            }
        }
    }

    fun updateItem(itemId: String, note: String, incidentDateMillis: Long?) {
        viewModelScope.launch {
            try {
                caseRepository.updateItem(itemId, note.trim(), incidentDateMillis)
            } catch (ex: CaseStoreException) {
                _uiState.value = UiState.Error(
                    messageRes = R.string.timeline_unreadable_store,
                    canRetry = false
                )
            } catch (_: Exception) {
            }
        }
    }

    fun removeItem(itemId: String) {
        viewModelScope.launch {
            try {
                caseRepository.removeItem(itemId)
            } catch (ex: CaseStoreException) {
                _uiState.value = UiState.Error(
                    messageRes = R.string.timeline_unreadable_store,
                    canRetry = false
                )
            } catch (_: Exception) {
            }
        }
    }
}
