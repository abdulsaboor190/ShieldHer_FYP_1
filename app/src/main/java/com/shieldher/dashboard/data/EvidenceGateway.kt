package com.shieldher.dashboard.data

import android.net.Uri
import kotlinx.coroutines.flow.Flow

/**
 * Gateway providing high-level evidence operations for dashboard screens.
 */
interface EvidenceGateway {
    fun observeEvidence(): Flow<List<EvidenceItem>>
    suspend fun getEvidence(id: String): EvidenceItem?
    suspend fun verify(id: String): EvidenceIntegrity
    suspend fun readForDisplay(id: String, maxBytes: Int): DisplayContent
    suspend fun importFromUri(uri: Uri, mimeType: String?): ImportOutcome
    suspend fun delete(id: String): Boolean
    suspend fun deleteAll(): Int
}
