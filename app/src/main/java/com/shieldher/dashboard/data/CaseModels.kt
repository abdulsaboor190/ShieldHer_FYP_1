package com.shieldher.dashboard.data

data class Case(
    val id: String,
    val title: String,
    val description: String,
    val createdAtMillis: Long,
    val isDemo: Boolean = false
)

data class CaseItem(
    val id: String,
    val caseId: String,
    val evidenceId: String?,
    val note: String,
    val incidentDateMillis: Long?,
    val addedAtMillis: Long,
    val isDemo: Boolean = false
)
