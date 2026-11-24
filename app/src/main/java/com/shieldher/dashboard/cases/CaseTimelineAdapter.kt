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
import com.shieldher.dashboard.common.BadgeStatus
import com.shieldher.dashboard.common.EvidenceCardModel
import com.shieldher.dashboard.common.EvidenceCardView
import com.shieldher.dashboard.common.StatusBadge

class CaseTimelineAdapter(
    private val onEvidenceClick: (String) -> Unit,
    private val onEditClick: (TimelineEntry) -> Unit,
    private val onRemoveClick: (TimelineEntry) -> Unit
) : ListAdapter<TimelineEntry, RecyclerView.ViewHolder>(TimelineDiffCallback) {

    companion object {
        private const val TYPE_ACTIVE_EVIDENCE = 1
        private const val TYPE_DELETED_EVIDENCE = 2
        private const val TYPE_NOTE_ONLY = 3
    }

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is TimelineEntry.ActiveEvidence -> TYPE_ACTIVE_EVIDENCE
        is TimelineEntry.DeletedEvidence -> TYPE_DELETED_EVIDENCE
        is TimelineEntry.NoteOnly -> TYPE_NOTE_ONLY
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_ACTIVE_EVIDENCE -> {
                val view = inflater.inflate(R.layout.item_timeline_evidence, parent, false)
                ActiveEvidenceViewHolder(view)
            }
            TYPE_DELETED_EVIDENCE -> {
                val view = inflater.inflate(R.layout.item_timeline_deleted, parent, false)
                DeletedEvidenceViewHolder(view)
            }
            else -> {
                val view = inflater.inflate(R.layout.item_timeline_note, parent, false)
                NoteViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is TimelineEntry.ActiveEvidence -> (holder as ActiveEvidenceViewHolder).bind(item)
            is TimelineEntry.DeletedEvidence -> (holder as DeletedEvidenceViewHolder).bind(item)
            is TimelineEntry.NoteOnly -> (holder as NoteViewHolder).bind(item)
        }
    }

    inner class ActiveEvidenceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val textTimeLabel: TextView = view.findViewById(R.id.text_time_label)
        private val btnOptions: ImageButton = view.findViewById(R.id.btn_item_options)
        private val cardEvidence: EvidenceCardView = view.findViewById(R.id.card_evidence)
        private val textNote: TextView = view.findViewById(R.id.text_item_note)

        fun bind(entry: TimelineEntry.ActiveEvidence) {
            textTimeLabel.text = entry.timeLabelText

            val title = entry.evidence.sourceLabel ?: itemView.context.getString(R.string.type_file)
            val cardModel = EvidenceCardModel(
                title = title,
                receivedAtText = entry.timeLabelText,
                status = entry.status
            )
            cardEvidence.bind(cardModel)
            cardEvidence.setOnClickListener { onEvidenceClick(entry.evidence.id) }

            if (!entry.noteText.isNullOrBlank()) {
                textNote.visibility = View.VISIBLE
                textNote.text = entry.noteText
            } else {
                textNote.visibility = View.GONE
            }

            setupOptionsButton(btnOptions, entry)
        }
    }

    inner class DeletedEvidenceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val textTimeLabel: TextView = view.findViewById(R.id.text_deleted_time_label)
        private val btnOptions: ImageButton = view.findViewById(R.id.btn_deleted_options)
        private val badgeDeleted: StatusBadge = view.findViewById(R.id.badge_evidence_deleted)
        private val textNote: TextView = view.findViewById(R.id.text_deleted_note)

        fun bind(entry: TimelineEntry.DeletedEvidence) {
            textTimeLabel.text = entry.timeLabelText
            badgeDeleted.setStatus(BadgeStatus.DELETED)

            if (!entry.noteText.isNullOrBlank()) {
                textNote.visibility = View.VISIBLE
                textNote.text = entry.noteText
            } else {
                textNote.visibility = View.GONE
            }

            setupOptionsButton(btnOptions, entry)
        }
    }

    inner class NoteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val textTimeLabel: TextView = view.findViewById(R.id.text_note_time_label)
        private val btnOptions: ImageButton = view.findViewById(R.id.btn_note_options)
        private val textContent: TextView = view.findViewById(R.id.text_note_content)

        fun bind(entry: TimelineEntry.NoteOnly) {
            textTimeLabel.text = entry.timeLabelText
            textContent.text = entry.noteText
            setupOptionsButton(btnOptions, entry)
        }
    }

    private fun setupOptionsButton(button: ImageButton, entry: TimelineEntry) {
        button.setOnClickListener { v ->
            val popup = PopupMenu(v.context, v)
            popup.menu.add(0, 1, 0, R.string.timeline_action_edit)
            popup.menu.add(0, 2, 1, R.string.timeline_action_remove)
            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    1 -> {
                        onEditClick(entry)
                        true
                    }
                    2 -> {
                        onRemoveClick(entry)
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
    }

    private object TimelineDiffCallback : DiffUtil.ItemCallback<TimelineEntry>() {
        override fun areItemsTheSame(oldItem: TimelineEntry, newItem: TimelineEntry): Boolean =
            oldItem.caseItem.id == newItem.caseItem.id

        override fun areContentsTheSame(oldItem: TimelineEntry, newItem: TimelineEntry): Boolean =
            oldItem == newItem
    }
}
