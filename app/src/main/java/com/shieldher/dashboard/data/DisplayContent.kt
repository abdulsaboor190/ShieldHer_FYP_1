package com.shieldher.dashboard.data

import android.graphics.Bitmap

/**
 * Displayable in-memory preview of an evidence item.
 */
sealed interface DisplayContent {
    data class Text(val text: String) : DisplayContent
    data class Image(val bitmap: Bitmap) : DisplayContent
    data object TooLarge : DisplayContent
    data object Unsupported : DisplayContent
    data object Unavailable : DisplayContent
}
