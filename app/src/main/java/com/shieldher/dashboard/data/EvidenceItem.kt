package com.shieldher.dashboard.data

/**
 * UI-facing representation of an evidence item in ShieldHer.
 *
 * Derived directly from the Stage 1 Room EvidenceEntity schema.
 */
data class EvidenceItem(
    val id: String,
    val hash: String,
    val receiptTimeEpochMillis: Long,
    val timeZoneId: String,
    val sourceLabel: String?,
    val mimeType: String,
    val fileSizeBytes: Long,
    val filePath: String,
    val parentId: String? = null,
    val transformationType: String? = null,
    val deleted: Boolean = false
)
