package com.shieldher.dashboard.cases

import com.shieldher.dashboard.common.BadgeStatus
import com.shieldher.dashboard.data.CaseItem
import com.shieldher.dashboard.data.EvidenceItem

sealed interface TimelineEntry {
    val caseItem: CaseItem
    val effectiveSortTimeMillis: Long
    val timeLabelText: String
    val noteText: String?

    data class ActiveEvidence(
        override val caseItem: CaseItem,
        val evidence: EvidenceItem,
        val status: BadgeStatus,
        override val effectiveSortTimeMillis: Long,
        override val timeLabelText: String,
        override val noteText: String?
    ) : TimelineEntry

    data class DeletedEvidence(
        override val caseItem: CaseItem,
        override val effectiveSortTimeMillis: Long,
        override val timeLabelText: String,
        override val noteText: String?
    ) : TimelineEntry

    data class NoteOnly(
        override val caseItem: CaseItem,
        override val effectiveSortTimeMillis: Long,
        override val timeLabelText: String,
        override val noteText: String
    ) : TimelineEntry
}
