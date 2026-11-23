package com.shieldher.dashboard.evidence

import com.shieldher.dashboard.data.VerificationSession

fun VerificationSession.remove(id: String) {
    // VerificationSession does not expose a mutable map directly or a remove method,
    // so we can clear and re-record or if there's no way without altering class,
    // let's check what VerificationSession has:
    // It has clear() and record() and records StateFlow<Map<String, VerificationRecord>>
    val current = records.value
    if (id in current) {
        val remaining = current - id
        clear()
        remaining.forEach { (key, value) ->
            record(key, value.result, value.checkedAtMillis)
        }
    }
}
