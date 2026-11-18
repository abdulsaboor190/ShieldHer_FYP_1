package com.shieldher.dashboard.common

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.shieldher.R

class ErrorStateView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val iconView: ImageView
    private val messageView: TextView
    private val retryButton: MaterialButton

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.view_error_state, this, true)
        iconView = view.findViewById(R.id.error_icon)
        messageView = view.findViewById(R.id.error_message)
        retryButton = view.findViewById(R.id.error_retry_button)

        iconView.setImageResource(R.drawable.ic_status_changed)
        val errorTextColor = ContextCompat.getColor(context, R.color.sh_status_error_text)
        iconView.imageTintList = ColorStateList.valueOf(errorTextColor)
    }

    fun setContent(
        @StringRes message: Int? = null,
        canRetry: Boolean,
        onRetry: (() -> Unit)? = null
    ) {
        messageView.setText(message ?: R.string.error_generic)

        if (canRetry && onRetry != null) {
            retryButton.visibility = View.VISIBLE
            retryButton.setOnClickListener { onRetry() }
        } else {
            retryButton.visibility = View.GONE
            retryButton.setOnClickListener(null)
        }
    }
}
