package com.shieldher.vault.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.shieldher.R
import com.shieldher.vault.data.EvidenceEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EvidenceListAdapter(
    private val onVerifyClick: ((EvidenceEntity) -> Unit)? = null,
    private val onDeleteClick: ((EvidenceEntity) -> Unit)? = null
) : ListAdapter<EvidenceEntity, EvidenceListAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val receiptTimeText: TextView = view.findViewById(R.id.receiptTimeText)
        val sourceLabelText: TextView = view.findViewById(R.id.sourceLabelText)
        val hashText: TextView = view.findViewById(R.id.hashText)
        val verifyButton: Button = view.findViewById(R.id.verifyButton)
        val deleteButton: Button = view.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_evidence, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val evidence = getItem(position)

        val dateFormat = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault())
        holder.receiptTimeText.text = dateFormat.format(Date(evidence.receiptTimeEpochMillis))

        holder.sourceLabelText.text = evidence.sourceLabel ?: "Unknown source"

        val truncatedHash = if (evidence.hash.length >= 12) {
            "${evidence.hash.take(12)}…"
        } else {
            evidence.hash
        }
        holder.hashText.text = truncatedHash

        holder.verifyButton.setOnClickListener {
            onVerifyClick?.invoke(evidence)
        }
        holder.deleteButton.setOnClickListener {
            onDeleteClick?.invoke(evidence)
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<EvidenceEntity>() {
        override fun areItemsTheSame(oldItem: EvidenceEntity, newItem: EvidenceEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: EvidenceEntity, newItem: EvidenceEntity): Boolean {
            return oldItem == newItem
        }
    }
}
