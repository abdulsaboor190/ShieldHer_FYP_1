package com.shieldher.dashboard.common

import com.shieldher.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BadgeStatusTest {

    @Test
    fun allStatusesHaveDistinctLabelsAndIcons() {
        val statuses = BadgeStatus.entries
        val labelIds = statuses.map { it.labelRes }
        val iconIds = statuses.map { it.iconRes }

        // All 6 statuses must have distinct label resource ids
        assertEquals(statuses.size, labelIds.toSet().size)

        // All 6 statuses must have distinct icon resource ids
        assertEquals(statuses.size, iconIds.toSet().size)

        // No resource id should be 0
        statuses.forEach { status ->
            assertNotEquals(0, status.labelRes)
            assertNotEquals(0, status.iconRes)
            assertNotEquals(0, status.tintRes)
            assertNotEquals(0, status.contentColorRes)
        }
    }

    @Test
    fun changedAndMissingUseDifferentIcons() {
        // Decision DS-1: CHANGED and MISSING must not share an icon
        assertNotEquals(BadgeStatus.CHANGED.iconRes, BadgeStatus.MISSING.iconRes)
    }

    @Test
    fun statusColorsMatchDesignBasis() {
        // UNCHANGED uses success colours
        assertEquals(R.color.sh_tint_success, BadgeStatus.UNCHANGED.tintRes)
        assertEquals(R.color.sh_status_success_text, BadgeStatus.UNCHANGED.contentColorRes)

        // CHANGED and MISSING use error colours
        val errorStatuses = listOf(BadgeStatus.CHANGED, BadgeStatus.MISSING)
        errorStatuses.forEach { status ->
            assertEquals(R.color.sh_tint_error, status.tintRes)
            assertEquals(R.color.sh_status_error_text, status.contentColorRes)
        }

        // NOT_CHECKED, DELETED, PREVIEW use neutral colours
        val neutralStatuses = listOf(BadgeStatus.NOT_CHECKED, BadgeStatus.DELETED, BadgeStatus.PREVIEW)
        neutralStatuses.forEach { status ->
            assertEquals(R.color.sh_tint_neutral, status.tintRes)
            assertEquals(R.color.sh_status_neutral_text, status.contentColorRes)
        }
    }
}
