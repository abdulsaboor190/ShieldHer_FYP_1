package com.shieldher.dashboard.common

import android.content.Context
import androidx.annotation.StringRes
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.shieldher.R

object ConfirmDialog {

    fun show(
        context: Context,
        @StringRes title: Int,
        @StringRes message: Int,
        @StringRes confirmText: Int,
        destructive: Boolean,
        onConfirm: () -> Unit
    ) {
        val builder = if (destructive) {
            MaterialAlertDialogBuilder(context, R.style.ThemeOverlay_ShieldHer_Dialog_Destructive)
        } else {
            MaterialAlertDialogBuilder(context)
        }

        builder
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(confirmText) { _, _ -> onConfirm() }
            .setNegativeButton(R.string.common_cancel, null)
            .show()
    }
}
