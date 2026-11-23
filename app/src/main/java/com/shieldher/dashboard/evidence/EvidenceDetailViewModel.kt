package com.shieldher.dashboard.evidence

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shieldher.R
import com.shieldher.dashboard.common.BadgeStatus
import com.shieldher.dashboard.common.TimeFormat
import com.shieldher.dashboard.common.UiState
import com.shieldher.dashboard.data.DisplayContent
import com.shieldher.dashboard.data.EvidenceGateway
import com.shieldher.dashboard.data.EvidenceItem
import com.shieldher.dashboard.data.VerificationRecord
import com.shieldher.dashboard.data.VerificationSession
import com.shieldher.dashboard.data.toBadgeStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EvidenceDetailViewModel(
    private val evidenceId: String,
    private val gateway: EvidenceGateway,
    private val session: VerificationSession
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<EvidenceDetailContent>>(UiState.Loading)
    val uiState: StateFlow<UiState<EvidenceDetailContent>> = _uiState.asStateFlow()

    private var loadedItem: EvidenceItem? = null
    private var currentPreview: PreviewState = PreviewState.Loading
    private var isVerifying = false
    private var heldBitmap: Bitmap? = null

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

            emitContent(item, currentPreview, verificationState)

            // Load preview
            loadPreview(item.id)
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
            emitContent(item, currentPreview, vState)
        }
    }

    fun verify() {
        val item = loadedItem ?: return
        if (isVerifying) return

        isVerifying = true
        val record = session.get(item.id)
        emitContent(item, currentPreview, toVerificationUiState(record, true))

        viewModelScope.launch {
            val result = gateway.verify(item.id)
            val now = System.currentTimeMillis()
            session.record(item.id, result, now)

            isVerifying = false
            val updatedRecord = session.get(item.id)
            emitContent(item, currentPreview, toVerificationUiState(updatedRecord, false))
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
        verification: VerificationUiState
    ) {
        val formattedTime = TimeFormat.formatDateTime(item.receiptTimeEpochMillis, item.timeZoneId)
        _uiState.value = UiState.Content(
            EvidenceDetailContent(
                item = item,
                formattedReceivedTime = formattedTime,
                previewState = preview,
                verificationState = verification
            )
        )
    }

    override fun onCleared() {
        super.onCleared()
        heldBitmap?.recycle()
        heldBitmap = null
    }
}
