package com.shieldher.dashboard.common

import com.shieldher.R
import com.shieldher.dashboard.data.EvidenceIntegrity
import com.shieldher.dashboard.data.EvidenceItem
import com.shieldher.dashboard.data.VerificationRecord
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EvidenceMappersTest {

    @Test
    fun friendlyType_returnsCorrectResourceForEachMime() {
        assertEquals(R.string.type_image, EvidenceMappers.friendlyType("image/png"))
        assertEquals(R.string.type_image, EvidenceMappers.friendlyType("IMAGE/JPEG"))
        assertEquals(R.string.type_text, EvidenceMappers.friendlyType("text/plain"))
        assertEquals(R.string.type_document, EvidenceMappers.friendlyType("application/pdf"))
        assertEquals(R.string.type_file, EvidenceMappers.friendlyType("application/octet-stream"))
        assertEquals(R.string.type_file, EvidenceMappers.friendlyType("unknown/custom"))
    }

    @Test
    fun toCardModel_withNullRecord_givesNotChecked() {
        val item = EvidenceItem(
            id = "test-1",
            hash = "dummy-hash",
            receiptTimeEpochMillis = 1700000000000L,
            timeZoneId = "UTC",
            sourceLabel = "Example App",
            mimeType = "image/png",
            fileSizeBytes = 1024L,
            filePath = "/fake/path"
        )

        val cardModel = item.toCardModel(null, null)
        assertEquals(BadgeStatus.NOT_CHECKED, cardModel.status)
        assertTrue(cardModel.title.contains("Example App"))
        assertTrue(cardModel.title.contains("Image"))
    }

    @Test
    fun toCardModel_withUnchangedRecord_givesUnchanged() {
        val item = EvidenceItem(
            id = "test-2",
            hash = "dummy-hash",
            receiptTimeEpochMillis = 1700000000000L,
            timeZoneId = "UTC",
            sourceLabel = "Example App",
            mimeType = "text/plain",
            fileSizeBytes = 512L,
            filePath = "/fake/path"
        )
        val record = VerificationRecord(
            result = EvidenceIntegrity.UNCHANGED,
            checkedAtMillis = 1700000010000L
        )

        val cardModel = item.toCardModel(record, null)
        assertEquals(BadgeStatus.UNCHANGED, cardModel.status)
        assertTrue(cardModel.title.contains("Text"))
    }
}
