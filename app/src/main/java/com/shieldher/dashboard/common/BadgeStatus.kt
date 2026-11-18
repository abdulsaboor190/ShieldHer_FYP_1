package com.shieldher.dashboard.common

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.shieldher.R

enum class BadgeStatus(
    @StringRes val labelRes: Int,
    @DrawableRes val iconRes: Int,
    @ColorRes val tintRes: Int,
    @ColorRes val contentColorRes: Int
) {
    UNCHANGED(
        labelRes = R.string.status_unchanged,
        iconRes = R.drawable.ic_status_unchanged,
        tintRes = R.color.sh_tint_success,
        contentColorRes = R.color.sh_status_success_text
    ),
    CHANGED(
        labelRes = R.string.status_changed,
        iconRes = R.drawable.ic_status_changed,
        tintRes = R.color.sh_tint_error,
        contentColorRes = R.color.sh_status_error_text
    ),
    MISSING(
        labelRes = R.string.status_missing,
        iconRes = R.drawable.ic_status_missing,
        tintRes = R.color.sh_tint_error,
        contentColorRes = R.color.sh_status_error_text
    ),
    NOT_CHECKED(
        labelRes = R.string.status_not_checked,
        iconRes = R.drawable.ic_status_not_checked,
        tintRes = R.color.sh_tint_neutral,
        contentColorRes = R.color.sh_status_neutral_text
    ),
    DELETED(
        labelRes = R.string.status_deleted,
        iconRes = R.drawable.ic_status_deleted,
        tintRes = R.color.sh_tint_neutral,
        contentColorRes = R.color.sh_status_neutral_text
    ),
    PREVIEW(
        labelRes = R.string.status_preview,
        iconRes = R.drawable.ic_status_preview,
        tintRes = R.color.sh_tint_neutral,
        contentColorRes = R.color.sh_status_neutral_text
    )
}
