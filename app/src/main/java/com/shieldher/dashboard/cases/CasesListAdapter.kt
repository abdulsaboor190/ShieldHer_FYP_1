package com.shieldher.dashboard.cases

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.shieldher.R
import com.shieldher.dashboard.common.TimeFormat
import com.shieldher.dashboard.data.Case

class CasesListAdapter(
    private val onCaseClick: (Case) -> Unit,
    private val onRenameClick: (Case) -> Unit,
    private val onDeleteClick: (Case) -> Unit
) : ListAdapter<Case, CasesListAdapter.CaseViewHolder>(CaseDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CaseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_dashboard_case, parent, false)
        return CaseViewHolder(view)
    }

    override fun onBindViewHolder(holder: CaseViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textTitle: TextView = itemView.findViewById(R.id.text_case_title)
        private val textDescription: TextView = itemView.findViewById(R.id.text_case_description)
        private val textDate: TextView = itemView.findViewById(R.id.text_case_date)
        private val btnOptions: ImageButton = itemView.findViewById(R.id.btn_case_options)

        fun bind(case: Case) {
            textTitle.text = case.title

            if (case.description.isNotBlank()) {
                textDescription.visibility = View.VISIBLE
                textDescription.text = case.description
            } else {
                textDescription.visibility = View.GONE
            }

            textDate.text = TimeFormat.formatDateTime(case.createdAtMillis, null)

            itemView.setOnClickListener { onCaseClick(case) }

            btnOptions.setOnClickListener { v ->
                val popup = PopupMenu(v.context, v)
                popup.menu.add(0, 1, 0, R.string.case_action_rename)
                popup.menu.add(0, 2, 1, R.string.case_action_delete)
                popup.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        1 -> {
                            onRenameClick(case)
                            true
                        }
                        2 -> {
                            onDeleteClick(case)
                            true
                        }
                        else -> false
                    }
                }
                popup.show()
            }
        }
    }

    private object CaseDiffCallback : DiffUtil.ItemCallback<Case>() {
        override fun areItemsTheSame(oldItem: Case, newItem: Case): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Case, newItem: Case): Boolean =
            oldItem == newItem
    }
}
