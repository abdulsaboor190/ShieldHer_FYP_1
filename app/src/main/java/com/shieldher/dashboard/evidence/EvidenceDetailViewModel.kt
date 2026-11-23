package com.shieldher.dashboard.evidence

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shieldher.R
import com.shieldher.dashboard.cases.CaseStoreException
import com.shieldher.dashboard.common.BadgeStatus
import com.shieldher.dashboard.common.TimeFormat
import com.shieldher.dashboard.common.UiState
import com.shieldher.dashboard.data.Case
import com.shieldher.dashboard.data.CaseRepository
import com.shieldher.dashboard.data.DisplayContent
import com.shieldher.dashboard.data.EvidenceGateway
import com.shieldher.dashboard.data.EvidenceItem
import com.shieldher.dashboard.data.VerificationRecord
import com.shieldher.dashboard.data.VerificationSession
import com.shieldher.dashboard.data.toBadgeStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EvidenceDetailViewModel(
    private val evidenceId: String,
    private val gateway: EvidenceGateway,
    private val caseRepository: CaseRepository,
    private val session: VerificationSession
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<EvidenceDetailContent>>(UiState.Loading)
    val uiState: StateFlow<UiState<EvidenceDetailContent>> = _uiState.asStateFlow()

    private val _deleteEvent = MutableSharedFlow<Boolean>()
    val deleteEvent: SharedFlow<Boolean> = _deleteEvent.asSharedFlow()

    private var loadedItem: EvidenceItem? = null
    private var currentPreview: PreviewState = PreviewState.Loading
    private var currentLinkedCases: LinkedCasesState = LinkedCasesState.Loading
    private var isVerifying = false
    private var heldBitmap: Bitmap? = null

    val allCases: Flow<List<Case>> = caseRepository.observeCases()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val item = gateway.getEvidence(evidenceId)
            if (item == null) {
                _uiState.value = UiState.Error(R.string.detail_not_found, canRetry = false)
                return@launch
            }
            loadedItem = item

            val existingRecord = session.get(evidenceId)
            val verificationState = toVerificationUiState(existingRecord, false)

            emitContent(item, currentPreview, verificationState, currentLinkedCases)

            // Load preview & linked cases in parallel
            launch { loadPreview(item.id) }
            launch { loadLinkedCases(item.id) }
        }
    }

    private suspend fun loadLinkedCases(id: String) {
        currentLinkedCases = try {
            val cases = caseRepository.casesForEvidence(id)
            LinkedCasesState.Content(cases)
        } catch (_: CaseStoreException) {
            LinkedCasesState.Unreadable
        } catch (_: Throwable) {
            LinkedCasesState.Unreadable
        }

        loadedItem?.let { item ->
            val record = session.get(item.id)
            emitContent(item, currentPreview, toVerificationUiState(record, isVerifying), currentLinkedCases)
        }
    }

    fun addToCase(caseId: String, note: String, incidentDateMillis: Long?) {
        val item = loadedItem ?: return
        viewModelScope.launch {
            try {
                caseRepository.addEvidenceItem(
                    caseId = caseId,
                    evidenceId = item.id,
                    note = note,
                    incidentDateMillis = incidentDateMillis
                )
                loadLinkedCases(item.id)
            } catch (_: CaseStoreException) {
                currentLinkedCases = LinkedCasesState.Unreadable
                val record = session.get(item.id)
                emitContent(item, currentPreview, toVerificationUiState(record, isVerifying), currentLinkedCases)
            }
        }
    }

    fun deleteEvidence() {
        val item = loadedItem ?: return
        viewModelScope.launch {
            val success = gateway.delete(item.id)
            _deleteEvent.emit(success)
        }
    }

    private suspend fun loadPreview(id: String) {
        val display = gateway.readForDisplay(id, 10_485_760)
        currentPreview = when (display) {
            is DisplayContent.Text -> PreviewState.Text(display.text)
            is DisplayContent.Image -> {
                heldBitmap = display.bitmap
                PreviewState.Image(display.bitmap)
            }
            DisplayContent.TooLarge -> PreviewState.TooLarge
            DisplayContent.Unsupported -> PreviewState.Unsupported
            DisplayContent.Unavailable -> PreviewState.Unavailable
        }

        loadedItem?.let { item ->
            val record = session.get(item.id)
            val vState = toVerificationUiState(record, isVerifying)
            emitContent(item, currentPreview, vState, currentLinkedCases)
        }
    }

    fun verify() {
        val item = loadedItem ?: return
        if (isVerifying) return

        isVerifying = true
        val record = session.get(item.id)
        emitContent(item, currentPreview, toVerificationUiState(record, true), currentLinkedCases)

        viewModelScope.launch {
            val result = gateway.verify(item.id)
            val now = System.currentTimeMillis()
            session.record(item.id, result, now)

            isVerifying = false
            val updatedRecord = session.get(item.id)
            emitContent(item, currentPreview, toVerificationUiState(updatedRecord, false), currentLinkedCases)
        }
    }

    private fun toVerificationUiState(record: VerificationRecord?, verifying: Boolean): VerificationUiState {
        val badge = record?.result.toBadgeStatus()
        val checkedText = record?.let {
            TimeFormat.formatDateTime(it.checkedAtMillis, null)
        }
        return VerificationUiState(
            status = badge,
            checkedAtText = checkedText,
            isVerifying = verifying
        )
    }

    private fun emitContent(
        item: EvidenceItem,
        preview: PreviewState,
        verification: VerificationUiState,
        linkedCases: LinkedCasesState
    ) {
        val formattedTime = TimeFormat.formatDateTime(item.receiptTimeEpochMillis, item.timeZoneId)
        _uiState.value = UiState.Content(
            EvidenceDetailContent(
                item = item,
                formattedReceivedTime = formattedTime,
                previewState = preview,
                verificationState = verification,
                linkedCasesState = linkedCases
            )
        )
    }

    override fun onCleared() {
        super.onCleared()
        heldBitmap?.recycle()
        heldBitmap = null
    }
}
