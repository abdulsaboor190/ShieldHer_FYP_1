package com.shieldher.dashboard.common

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
 * Custom StatusBadge view displaying an icon and text label for a [BadgeStatus].
 *
 * Always shows both icon and text with the appropriate status tint background
 * and content color, never relying on color alone.
 */
class StatusBadge @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val iconView: ImageView
    private val labelView: TextView

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.view_status_badge, this, true)
        iconView = view.findViewById(R.id.badge_icon)
        labelView = view.findViewById(R.id.badge_label)
    }

    fun setStatus(status: BadgeStatus) {
        val tintColor = ContextCompat.getColor(context, status.tintRes)
        val contentColor = ContextCompat.getColor(context, status.contentColorRes)
        val labelText = context.getString(status.labelRes)

        // Tint the rounded background
        backgroundTintList = ColorStateList.valueOf(tintColor)

        // Set icon tinted with content colour
        iconView.setImageResource(status.iconRes)
        iconView.imageTintList = ColorStateList.valueOf(contentColor)

        // Set label text and text colour
        labelView.text = labelText
        labelView.setTextColor(contentColor)

        // Set contentDescription on the badge container
        contentDescription = labelText
    }
}
