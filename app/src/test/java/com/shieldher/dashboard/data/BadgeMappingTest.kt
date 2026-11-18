package com.shieldher.dashboard.data

import com.shieldher.dashboard.common.BadgeStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class BadgeMappingTest {

    @Test
    fun nullIntegrityMapsToNotChecked() {
        val integrity: EvidenceIntegrity? = null
        assertEquals(BadgeStatus.NOT_CHECKED, integrity.toBadgeStatus())
    }

    @Test
    fun unchangedMapsToUnchanged() {
        val integrity: EvidenceIntegrity? = EvidenceIntegrity.UNCHANGED
        assertEquals(BadgeStatus.UNCHANGED, integrity.toBadgeStatus())
    }

    @Test
    fun changedMapsToChanged() {
        val integrity: EvidenceIntegrity? = EvidenceIntegrity.CHANGED
        assertEquals(BadgeStatus.CHANGED, integrity.toBadgeStatus())
    }

    @Test
    fun missingMapsToMissing() {
        val integrity: EvidenceIntegrity? = EvidenceIntegrity.MISSING
        assertEquals(BadgeStatus.MISSING, integrity.toBadgeStatus())
    }
}
