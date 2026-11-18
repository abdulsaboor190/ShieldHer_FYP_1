package com.shieldher.dashboard.data

/**
 * Outcome of an in-app file/URI import operation.
 *
 * Failed carries no message text to avoid leaking any metadata or sensitive error details.
 */
sealed interface ImportOutcome {
    data class Success(val id: String) : ImportOutcome
    data object TooLarge : ImportOutcome
    data object Unsupported : ImportOutcome
    data object Failed : ImportOutcome
}
