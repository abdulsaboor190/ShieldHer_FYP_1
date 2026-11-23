package com.shieldher.dashboard.common

import android.content.Context
import androidx.annotation.StringRes
import com.shieldher.R
import com.shieldher.dashboard.data.EvidenceItem
import com.shieldher.dashboard.data.VerificationRecord
import com.shieldher.dashboard.data.toBadgeStatus

object EvidenceMappers {

    @StringRes
    fun friendlyType(mimeType: String): Int {
        val lower = mimeType.lowercase()
        return when {
            lower.startsWith("image/") -> R.string.type_image
            lower == "text/plain" || lower.startsWith("text/") -> R.string.type_text
            lower == "application/pdf" || lower.contains("document") || lower.contains("msword") || lower.contains("officedocument") -> R.string.type_document
            else -> R.string.type_file
        }
    }
}

fun EvidenceItem.displayTitleParts(): Pair<String, Int> {
    val source = sourceLabel ?: "File"
    val typeRes = EvidenceMappers.friendlyType(mimeType)
    return Pair(source, typeRes)
}

fun EvidenceItem.toCardModel(record: VerificationRecord?, context: Context?): EvidenceCardModel {
    val (source, typeRes) = displayTitleParts()
    val typeName = context?.getString(typeRes) ?: when (typeRes) {
        R.string.type_image -> "Image"
        R.string.type_text -> "Text"
        R.string.type_document -> "Document"
        else -> "File"
    }
    val title = "$source • $typeName"
    val formattedTime = TimeFormat.formatDateTime(receiptTimeEpochMillis, timeZoneId)
    val badgeStatus = record?.result.toBadgeStatus()
    return EvidenceCardModel(
        title = title,
        receivedAtText = formattedTime,
        status = badgeStatus
    )
}
