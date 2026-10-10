package com.shieldher.vault.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "evidence")
data class EvidenceEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val hash: String,
    val receiptTimeEpochMillis: Long,
    val timeZoneId: String,
    val sourceLabel: String? = null,
    val mimeType: String,
    val fileSizeBytes: Long,
    val filePath: String,
    val parentId: String? = null,
    val transformationType: String? = null,
    val deleted: Boolean = false
)
