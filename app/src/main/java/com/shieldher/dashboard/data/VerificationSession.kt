package com.shieldher.dashboard.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class VerificationRecord(
    val result: EvidenceIntegrity,
    val checkedAtMillis: Long
)

/**
 * In-memory verification session holding verification outcomes.
 *
 * It must never write to disk or log sensitive verification details.
 */
class VerificationSession {

    private val _records = MutableStateFlow<Map<String, VerificationRecord>>(emptyMap())
    val records: StateFlow<Map<String, VerificationRecord>> = _records.asStateFlow()

    fun record(id: String, result: EvidenceIntegrity, checkedAtMillis: Long) {
        _records.value = _records.value + (id to VerificationRecord(result, checkedAtMillis))
    }

    fun get(id: String): VerificationRecord? {
        return _records.value[id]
    }

    fun clear() {
        _records.value = emptyMap()
    }
}
