package com.shieldher.dashboard.check

import androidx.annotation.StringRes
import com.shieldher.R
import com.shieldher.dashboard.common.BadgeStatus

object AccountCheckWordingHelper {
    @StringRes
    val previewBadgeStatus: BadgeStatus = BadgeStatus.PREVIEW

    @StringRes
    val previewLabelRes: Int = R.string.account_check_preview_label

    @StringRes
    val comingSoonRes: Int = R.string.account_check_coming_soon
}
