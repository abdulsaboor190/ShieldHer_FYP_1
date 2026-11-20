package com.shieldher.dashboard.cases

/** Thrown when the case store is in UNREADABLE state and a write is attempted. */
class CaseStoreException(
    val reason: Reason,
    cause: Throwable? = null
) : Exception("Case store is $reason", cause) {
    enum class Reason { UNREADABLE }
}
