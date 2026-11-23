package com.shieldher.dashboard.home

import com.shieldher.dashboard.common.EvidenceCardModel

data class HomeEvidenceItem(
    val id: String,
    val cardModel: EvidenceCardModel
)

data class HomeContent(
    val evidenceCount: Int,
    val caseCount: Int?,
    val latestItems: List<HomeEvidenceItem>
)
