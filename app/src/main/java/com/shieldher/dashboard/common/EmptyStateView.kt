package com.shieldher.dashboard.common

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.shieldher.R

class EmptyStateView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val iconView: ImageView
    private val messageView: TextView
    private val actionButton: MaterialButton

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.view_empty_state, this, true)
        iconView = view.findViewById(R.id.empty_icon)
        messageView = view.findViewById(R.id.empty_message)
        actionButton = view.findViewById(R.id.empty_action_button)

        val secondaryColor = ContextCompat.getColor(context, R.color.sh_text_secondary)
        iconView.imageTintList = ColorStateList.valueOf(secondaryColor)
    }

    fun setContent(
        @DrawableRes icon: Int,
        @StringRes message: Int,
        @StringRes actionText: Int? = null,
        onAction: (() -> Unit)? = null
    ) {
        iconView.setImageResource(icon)
        messageView.setText(message)

        if (actionText != null && onAction != null) {
            actionButton.visibility = View.VISIBLE
            actionButton.setText(actionText)
            actionButton.setOnClickListener { onAction() }
        } else {
            actionButton.visibility = View.GONE
            actionButton.setOnClickListener(null)
        }
    }
}
