package com.shieldher.dashboard.data

import com.shieldher.dashboard.common.BadgeStatus

/**
 * Maps an optional [EvidenceIntegrity] result to the corresponding [BadgeStatus].
 *
 * A null integrity check indicates the item has not been checked yet during this session.
 */
fun EvidenceIntegrity?.toBadgeStatus(): BadgeStatus = when (this) {
    null -> BadgeStatus.NOT_CHECKED
    EvidenceIntegrity.UNCHANGED -> BadgeStatus.UNCHANGED
    EvidenceIntegrity.CHANGED -> BadgeStatus.CHANGED
    EvidenceIntegrity.MISSING -> BadgeStatus.MISSING
}
