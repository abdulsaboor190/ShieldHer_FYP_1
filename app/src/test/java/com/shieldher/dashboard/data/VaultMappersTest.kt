package com.shieldher.dashboard.data

import com.shieldher.vault.data.EvidenceDisplay
import com.shieldher.vault.data.EvidenceEntity
import com.shieldher.vault.data.ImportResult
import com.shieldher.vault.data.IntegrityResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VaultMappersTest {

    @Test
    fun mapEntityToItem_preservesAllFields() {
        val entity = EvidenceEntity(
            id = "test-id-123",
            hash = "deadbeef0123456789abcdef",
            receiptTimeEpochMillis = 1794500000000L,
            timeZoneId = "UTC",
            sourceLabel = "Signal app",
            mimeType = "image/png",
            fileSizeBytes = 2048L,
            filePath = "/fake/path/test-id-123"
        )

        val item = VaultMappers.mapEntityToItem(entity)
        assertEquals("test-id-123", item.id)
        assertEquals("deadbeef0123456789abcdef", item.hash)
        assertEquals(1794500000000L, item.receiptTimeEpochMillis)
        assertEquals("UTC", item.timeZoneId)
        assertEquals("Signal app", item.sourceLabel)
        assertEquals("image/png", item.mimeType)
        assertEquals(2048L, item.fileSizeBytes)
        assertEquals("/fake/path/test-id-123", item.filePath)
    }

    @Test
    fun mapIntegrityResult_mapsEveryEnum() {
        assertEquals(EvidenceIntegrity.UNCHANGED, VaultMappers.mapIntegrityResult(IntegrityResult.UNCHANGED))
        assertEquals(EvidenceIntegrity.CHANGED, VaultMappers.mapIntegrityResult(IntegrityResult.CHANGED))
        assertEquals(EvidenceIntegrity.MISSING, VaultMappers.mapIntegrityResult(IntegrityResult.MISSING))
    }

    @Test
    fun mapEvidenceDisplay_mapsAllVariants() {
        val textDisplay = VaultMappers.mapEvidenceDisplay(EvidenceDisplay.Text("Sample preview"))
        assertTrue(textDisplay is DisplayContent.Text)
        assertEquals("Sample preview", (textDisplay as DisplayContent.Text).text)

        val tooLarge = VaultMappers.mapEvidenceDisplay(EvidenceDisplay.TooLarge)
        assertEquals(DisplayContent.TooLarge, tooLarge)

        val unsupported = VaultMappers.mapEvidenceDisplay(EvidenceDisplay.Unsupported)
        assertEquals(DisplayContent.Unsupported, unsupported)

        val unavailable = VaultMappers.mapEvidenceDisplay(EvidenceDisplay.Unavailable)
        assertEquals(DisplayContent.Unavailable, unavailable)
    }

    @Test
    fun mapImportResult_mapsAllVariants() {
        val success = VaultMappers.mapImportResult(ImportResult.Success("imported-id"))
        assertTrue(success is ImportOutcome.Success)
        assertEquals("imported-id", (success as ImportOutcome.Success).id)

        assertEquals(ImportOutcome.TooLarge, VaultMappers.mapImportResult(ImportResult.TooLarge))
        assertEquals(ImportOutcome.Unsupported, VaultMappers.mapImportResult(ImportResult.Unsupported))
        assertEquals(ImportOutcome.Failed, VaultMappers.mapImportResult(ImportResult.Failed))
    }
}
