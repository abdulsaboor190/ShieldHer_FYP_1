package com.shieldher.dashboard.common

import androidx.annotation.StringRes

/**
 * Standard UI state model for ShieldHer dashboard screens.
 *
 * Every screen exposes exactly one `StateFlow<UiState<T>>` and renders
 * exactly these four states:
 * - [Loading]: Initial or in-progress async work.
 * - [Empty]: Async work completed successfully with zero items.
 * - [Content]: Async work completed successfully with data [T].
 * - [Error]: Async work failed with a user-facing string resource and retry flag.
 */
sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data object Empty : UiState<Nothing>
    data class Content<out T>(val data: T) : UiState<T>
    data class Error(
        @StringRes val messageRes: Int,
        val canRetry: Boolean = true
    ) : UiState<Nothing>
}
