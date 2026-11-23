package com.shieldher.dashboard.evidence

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.shieldher.R
import com.shieldher.dashboard.common.EvidenceCardModel
import com.shieldher.dashboard.common.EvidenceCardView

data class DashboardEvidenceListItem(
    val id: String,
    val cardModel: EvidenceCardModel
)

class DashboardEvidenceListAdapter(
    private val onItemClick: (String) -> Unit
) : ListAdapter<DashboardEvidenceListItem, DashboardEvidenceListAdapter.ViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_dashboard_evidence, parent, false)
        return ViewHolder(view.findViewById(R.id.evidence_card_view))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.cardView.bind(item.cardModel)
        holder.cardView.setOnClickListener { onItemClick(item.id) }
    }

    class ViewHolder(val cardView: EvidenceCardView) : RecyclerView.ViewHolder(cardView)

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<DashboardEvidenceListItem>() {
            override fun areItemsTheSame(
                oldItem: DashboardEvidenceListItem,
                newItem: DashboardEvidenceListItem
            ) = oldItem.id == newItem.id

            override fun areContentsTheSame(
                oldItem: DashboardEvidenceListItem,
                newItem: DashboardEvidenceListItem
            ) = oldItem == newItem
        }
    }
}
