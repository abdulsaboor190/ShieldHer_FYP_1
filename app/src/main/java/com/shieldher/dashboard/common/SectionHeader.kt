package com.shieldher.dashboard.common

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.shieldher.R

class SectionHeader @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    init {
        setTextAppearance(R.style.ShieldHer_TextAppearance_Heading)
        val padStart = resources.getDimensionPixelSize(R.dimen.sh_space_16)
        val padTop = resources.getDimensionPixelSize(R.dimen.sh_space_16)
        val padEnd = resources.getDimensionPixelSize(R.dimen.sh_space_16)
        val padBottom = resources.getDimensionPixelSize(R.dimen.sh_space_8)
        setPadding(padStart, padTop, padEnd, padBottom)
    }
}
