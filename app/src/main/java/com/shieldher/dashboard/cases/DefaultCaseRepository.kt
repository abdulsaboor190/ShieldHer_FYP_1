package com.shieldher.dashboard.cases

import com.shieldher.dashboard.data.Case
import com.shieldher.dashboard.data.CaseItem
import com.shieldher.dashboard.data.CaseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

/**
 * Production implementation of [CaseRepository] backed by [CaseStore].
 *
 * Rules:
 * - IDs are random UUID strings.
 * - createdAtMillis / addedAtMillis come from [System.currentTimeMillis].
 * - deleteCase removes its items and nothing else; it never touches the vault.
 * - removeItem never touches evidence; it only removes the CaseItem row.
 * - Every mutating call persists through the atomic write before returning.
 * - If the store is UNREADABLE every mutating call throws [CaseStoreException].
 *
 * No logging of content, paths, ids, titles or notes.
 */
class DefaultCaseRepository(private val store: CaseStore) : CaseRepository {

    override fun observeCases(): Flow<List<Case>> = store.cases

    override fun observeItems(caseId: String): Flow<List<CaseItem>> =
        store.items.map { all -> all.filter { it.caseId == caseId } }

    override suspend fun createCase(title: String, description: String): Case {
        val case = Case(
            id = UUID.randomUUID().toString(),
            title = title,
            description = description,
            createdAtMillis = System.currentTimeMillis()
        )
        val newCases = store.cases.value + case
        store.saveCases(newCases, store.items.value)
        return case
    }

    override suspend fun renameCase(caseId: String, title: String) {
        val newCases = store.cases.value.map { if (it.id == caseId) it.copy(title = title) else it }
        store.saveCases(newCases, store.items.value)
    }

    override suspend fun deleteCase(caseId: String) {
        val newCases = store.cases.value.filterNot { it.id == caseId }
        val newItems = store.items.value.filterNot { it.caseId == caseId }
        store.saveCases(newCases, newItems)
    }

    override suspend fun addEvidenceItem(
        caseId: String,
        evidenceId: String,
        note: String,
        incidentDateMillis: Long?
    ): CaseItem {
        val item = CaseItem(
            id = UUID.randomUUID().toString(),
            caseId = caseId,
            evidenceId = evidenceId,
            note = note,
            incidentDateMillis = incidentDateMillis,
            addedAtMillis = System.currentTimeMillis()
        )
        store.saveCases(store.cases.value, store.items.value + item)
        return item
    }

    override suspend fun addNoteItem(
        caseId: String,
        note: String,
        incidentDateMillis: Long?
    ): CaseItem {
        val item = CaseItem(
            id = UUID.randomUUID().toString(),
            caseId = caseId,
            evidenceId = null,
            note = note,
            incidentDateMillis = incidentDateMillis,
            addedAtMillis = System.currentTimeMillis()
        )
        store.saveCases(store.cases.value, store.items.value + item)
        return item
    }

    override suspend fun updateItem(itemId: String, note: String, incidentDateMillis: Long?) {
        val newItems = store.items.value.map { item ->
            if (item.id == itemId) item.copy(note = note, incidentDateMillis = incidentDateMillis) else item
        }
        store.saveCases(store.cases.value, newItems)
    }

    override suspend fun removeItem(itemId: String) {
        val newItems = store.items.value.filterNot { it.id == itemId }
        store.saveCases(store.cases.value, newItems)
    }

    override suspend fun casesForEvidence(evidenceId: String): List<Case> {
        val caseIds = store.items.value
            .filter { it.evidenceId == evidenceId }
            .map { it.caseId }
            .toSet()
        return store.cases.value.filter { it.id in caseIds }
    }

    override suspend fun deleteEverything() {
        store.deleteEverything()
    }
}
