package com.shieldher.vault.data

import android.graphics.Bitmap

sealed interface EvidenceDisplay {
    data class Text(val text: String) : EvidenceDisplay
    data class Image(val bitmap: Bitmap) : EvidenceDisplay
    object TooLarge : EvidenceDisplay
    object Unsupported : EvidenceDisplay
    object Unavailable : EvidenceDisplay
}
