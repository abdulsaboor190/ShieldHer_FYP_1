package com.shieldher.dashboard.common

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView
import com.shieldher.R

/**
 * Custom EvidenceCard view presenting evidence title, formatted received time,
 * and a status badge.
 */
class EvidenceCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val titleView: TextView
    private val receivedTimeView: TextView
    private val statusBadge: StatusBadge

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.view_evidence_card, this, true)
        titleView = view.findViewById(R.id.card_title)
        receivedTimeView = view.findViewById(R.id.card_received_time)
        statusBadge = view.findViewById(R.id.card_status_badge)
    }

    fun bind(model: EvidenceCardModel) {
        titleView.text = model.title
        receivedTimeView.text = model.receivedAtText
        statusBadge.setStatus(model.status)
    }
}
