package com.shieldher.dashboard.data

import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing case folders, case notes, and case-evidence associations.
 *
 * Removing an item or deleting a case NEVER deletes evidence from the vault; an item whose
 * evidenceId no longer exists in the vault is displayed by the UI as "evidence deleted".
 */
interface CaseRepository {
    fun observeCases(): Flow<List<Case>>
    fun observeItems(caseId: String): Flow<List<CaseItem>>
    suspend fun createCase(title: String, description: String): Case
    suspend fun renameCase(caseId: String, title: String)
    suspend fun deleteCase(caseId: String)
    suspend fun addEvidenceItem(caseId: String, evidenceId: String, note: String, incidentDateMillis: Long?): CaseItem
    suspend fun addNoteItem(caseId: String, note: String, incidentDateMillis: Long?): CaseItem
    suspend fun updateItem(itemId: String, note: String, incidentDateMillis: Long?)
    suspend fun removeItem(itemId: String)
    suspend fun casesForEvidence(evidenceId: String): List<Case>
    suspend fun deleteEverything()
}
