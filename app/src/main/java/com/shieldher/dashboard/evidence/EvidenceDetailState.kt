package com.shieldher.dashboard.evidence

import android.graphics.Bitmap
import com.shieldher.dashboard.common.BadgeStatus
import com.shieldher.dashboard.data.EvidenceItem

sealed interface PreviewState {
    data object Loading : PreviewState
    data class Text(val content: String) : PreviewState
    data class Image(val bitmap: Bitmap) : PreviewState
    data object TooLarge : PreviewState
    data object Unsupported : PreviewState
    data object Unavailable : PreviewState
}

data class VerificationUiState(
    val status: BadgeStatus,
    val checkedAtText: String? = null,
    val isVerifying: Boolean = false
)

data class EvidenceDetailContent(
    val item: EvidenceItem,
    val formattedReceivedTime: String,
    val previewState: PreviewState,
    val verificationState: VerificationUiState
)
