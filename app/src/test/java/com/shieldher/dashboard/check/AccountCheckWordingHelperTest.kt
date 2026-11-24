package com.shieldher.dashboard.check

import com.shieldher.R
import com.shieldher.dashboard.common.BadgeStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class AccountCheckWordingHelperTest {

    @Test
    fun `preview status is PREVIEW with correct resource string`() {
        assertEquals(BadgeStatus.PREVIEW, AccountCheckWordingHelper.previewBadgeStatus)
        assertEquals(R.string.status_preview, AccountCheckWordingHelper.previewBadgeStatus.labelRes)
    }

    @Test
    fun `preview label and coming soon string resource ids match approved constants`() {
        assertEquals(R.string.account_check_preview_label, AccountCheckWordingHelper.previewLabelRes)
        assertEquals(R.string.account_check_coming_soon, AccountCheckWordingHelper.comingSoonRes)
    }
}
