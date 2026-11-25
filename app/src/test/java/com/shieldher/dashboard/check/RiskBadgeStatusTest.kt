package com.shieldher.dashboard.check

import com.shieldher.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RiskBadgeStatusTest {

    @Test
    fun allFiveStatusesHaveDistinctIconsAndLabels() {
        val statuses = RiskBadgeStatus.values()
        assertEquals(5, statuses.size)

        val iconIds = statuses.map { it.iconResId }.toSet()
        assertEquals("Each status must have a distinct icon", 5, iconIds.size)

        val labelIds = statuses.map { it.labelResId }.toSet()
        assertEquals("Each status must have a distinct label", 5, labelIds.size)
    }

    @Test
    fun onlyHigherUsesWarningTokensAndRestUseNeutral() {
        assertEquals(R.color.sh_tint_warning, RiskBadgeStatus.HIGHER.tintBgColorResId)
        assertEquals(R.color.sh_status_warning_text, RiskBadgeStatus.HIGHER.textColorResId)

        val neutralStatuses = listOf(
            RiskBadgeStatus.LOWER,
            RiskBadgeStatus.UNCERTAIN,
            RiskBadgeStatus.UNSUPPORTED,
            RiskBadgeStatus.NOT_VALIDATED
        )

        for (status in neutralStatuses) {
            assertEquals("Status $status must use neutral tint", R.color.sh_tint_neutral, status.tintBgColorResId)
            assertEquals("Status $status must use neutral text", R.color.sh_status_neutral_text, status.textColorResId)
        }
    }

    @Test
    fun neverUsesSuccessOrErrorTokens() {
        val forbiddenColors = setOf(
            R.color.sh_tint_success,
            R.color.sh_status_success_text,
            R.color.sh_tint_error,
            R.color.sh_status_error_text,
            R.color.sh_error
        )

        for (status in RiskBadgeStatus.values()) {
            assertFalse(
                "Status $status must not use success or error tint",
                forbiddenColors.contains(status.tintBgColorResId)
            )
            assertFalse(
                "Status $status must not use success or error text color",
                forbiddenColors.contains(status.textColorResId)
            )
        }
    }
}
