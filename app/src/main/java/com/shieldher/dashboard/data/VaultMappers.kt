package com.shieldher.dashboard.data

import com.shieldher.vault.data.EvidenceDisplay
import com.shieldher.vault.data.EvidenceEntity
import com.shieldher.vault.data.ImportResult
import com.shieldher.vault.data.IntegrityResult

/**
 * Pure mapping functions between vault domain models and dashboard data models.
 * Kept separate for unit testing on JVM without Android dependencies.
 */
object VaultMappers {

    fun mapEntityToItem(entity: EvidenceEntity): EvidenceItem {
        return EvidenceItem(
            id = entity.id,
            hash = entity.hash,
            receiptTimeEpochMillis = entity.receiptTimeEpochMillis,
            timeZoneId = entity.timeZoneId,
            sourceLabel = entity.sourceLabel,
            mimeType = entity.mimeType,
            fileSizeBytes = entity.fileSizeBytes,
            filePath = entity.filePath
        )
    }

    fun mapIntegrityResult(result: IntegrityResult): EvidenceIntegrity {
        return when (result) {
            IntegrityResult.UNCHANGED -> EvidenceIntegrity.UNCHANGED
            IntegrityResult.CHANGED -> EvidenceIntegrity.CHANGED
            IntegrityResult.MISSING -> EvidenceIntegrity.MISSING
        }
    }

    fun mapEvidenceDisplay(display: EvidenceDisplay): DisplayContent {
        return when (display) {
            is EvidenceDisplay.Text -> DisplayContent.Text(display.text)
            is EvidenceDisplay.Image -> DisplayContent.Image(display.bitmap)
            EvidenceDisplay.TooLarge -> DisplayContent.TooLarge
            EvidenceDisplay.Unsupported -> DisplayContent.Unsupported
            EvidenceDisplay.Unavailable -> DisplayContent.Unavailable
        }
    }

    fun mapImportResult(result: ImportResult): ImportOutcome {
        return when (result) {
            is ImportResult.Success -> ImportOutcome.Success(result.id)
            ImportResult.TooLarge -> ImportOutcome.TooLarge
            ImportResult.Unsupported -> ImportOutcome.Unsupported
            ImportResult.Failed -> ImportOutcome.Failed
        }
    }
}
