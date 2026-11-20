package com.shieldher.vault.data

sealed interface ImportResult {
    data class Success(val id: String) : ImportResult
    object TooLarge : ImportResult
    object Unsupported : ImportResult
    object Failed : ImportResult
}
