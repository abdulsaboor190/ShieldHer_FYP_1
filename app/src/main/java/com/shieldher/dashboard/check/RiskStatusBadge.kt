package com.shieldher.dashboard.check

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.shieldher.R

/**
 * Custom view for displaying risk assessment status badge.
 * Reuses view_status_badge.xml layout with accessible colors and contentDescription.
 */
class RiskStatusBadge @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val iconView: ImageView
    private val textView: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.view_status_badge, this, true)
        iconView = findViewById(R.id.badge_icon)
        textView = findViewById(R.id.badge_label)
    }

    fun setStatus(status: RiskBadgeStatus) {
        val label = context.getString(status.labelResId)
        val tintColor = ContextCompat.getColor(context, status.textColorResId)
        val bgColor = ContextCompat.getColor(context, status.tintBgColorResId)

        textView.text = label
        textView.setTextColor(tintColor)

        iconView.setImageResource(status.iconResId)
        iconView.imageTintList = ColorStateList.valueOf(tintColor)

        backgroundTintList = ColorStateList.valueOf(bgColor)
        contentDescription = label
    }
}
