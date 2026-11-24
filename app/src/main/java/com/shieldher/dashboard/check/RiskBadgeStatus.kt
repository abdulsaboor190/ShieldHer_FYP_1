package com.shieldher.dashboard.check

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.shieldher.R

/**
 * Visual presentation states for RiskStatusBadge.
 * HIGHER uses warning (amber) tokens.
 * LOWER, UNCERTAIN, UNSUPPORTED, NOT_VALIDATED use neutral (grey) tokens.
 * Green (success) and Red (error) tokens are NEVER used.
 */
enum class RiskBadgeStatus(
    @StringRes val labelResId: Int,
    @DrawableRes val iconResId: Int,
    @ColorRes val tintBgColorResId: Int,
    @ColorRes val textColorResId: Int
) {
    HIGHER(
        labelResId = R.string.risk_title_higher,
        iconResId = R.drawable.ic_risk_higher,
        tintBgColorResId = R.color.sh_tint_warning,
        textColorResId = R.color.sh_status_warning_text
    ),
    LOWER(
        labelResId = R.string.risk_title_lower,
        iconResId = R.drawable.ic_risk_lower,
        tintBgColorResId = R.color.sh_tint_neutral,
        textColorResId = R.color.sh_status_neutral_text
    ),
    UNCERTAIN(
        labelResId = R.string.risk_title_uncertain,
        iconResId = R.drawable.ic_risk_uncertain,
        tintBgColorResId = R.color.sh_tint_neutral,
        textColorResId = R.color.sh_status_neutral_text
    ),
    UNSUPPORTED(
        labelResId = R.string.risk_title_unsupported,
        iconResId = R.drawable.ic_risk_unsupported,
        tintBgColorResId = R.color.sh_tint_neutral,
        textColorResId = R.color.sh_status_neutral_text
    ),
    NOT_VALIDATED(
        labelResId = R.string.risk_title_not_validated,
        iconResId = R.drawable.ic_risk_not_validated,
        tintBgColorResId = R.color.sh_tint_neutral,
        textColorResId = R.color.sh_status_neutral_text
    )
}
