package com.shieldher.dashboard.data.fake

import com.shieldher.dashboard.data.Case
import com.shieldher.dashboard.data.CaseItem
import com.shieldher.dashboard.data.CaseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.util.UUID

class FakeCaseRepository : CaseRepository {

    private val cases = MutableStateFlow(createInitialSyntheticCases())
    private val items = MutableStateFlow(createInitialSyntheticItems())

    private fun createInitialSyntheticCases(): List<Case> {
        return listOf(
            Case(
                id = "synth-case-1",
                title = "Example Harassment Incident 1",
                description = "Documentation for ongoing threatening messages",
                createdAtMillis = 1794500000000L
            ),
            Case(
                id = "synth-case-2",
                title = "Example Workplace Incident 2",
                description = "Documentation of inappropriate emails",
                createdAtMillis = 1794503600000L
            )
        )
    }

    private fun createInitialSyntheticItems(): List<CaseItem> {
        return listOf(
            // Case 1: contains an existing evidence item and a note-only item
            CaseItem(
                id = "synth-item-1",
                caseId = "synth-case-1",
                evidenceId = "synth-evidence-1",
                note = "Initial screenshot of message",
                incidentDateMillis = 1794490000000L,
                addedAtMillis = 1794500100000L
            ),
            CaseItem(
                id = "synth-item-2",
                caseId = "synth-case-1",
                evidenceId = null,
                note = "Phone call received from unknown number without recording",
                incidentDateMillis = 1794495000000L,
                addedAtMillis = 1794500200000L
            ),
            // Case 2: contains an item whose evidenceId is NOT in the gateway (exercises 'evidence deleted')
            CaseItem(
                id = "synth-item-3",
                caseId = "synth-case-2",
                evidenceId = "deleted-vault-evidence-999",
                note = "Exported log whose underlying file was removed",
                incidentDateMillis = 1794498000000L,
                addedAtMillis = 1794503700000L
            )
        )
    }

    override fun observeCases(): Flow<List<Case>> = cases.asStateFlow()

    override fun observeItems(caseId: String): Flow<List<CaseItem>> =
        items.map { list -> list.filter { it.caseId == caseId } }

    override suspend fun createCase(title: String, description: String): Case {
        val newCase = Case(
            id = "synth-case-" + UUID.randomUUID().toString(),
            title = title,
            description = description,
            createdAtMillis = System.currentTimeMillis()
        )
        cases.value = cases.value + newCase
        return newCase
    }

    override suspend fun renameCase(caseId: String, title: String) {
        cases.value = cases.value.map { c ->
            if (c.id == caseId) c.copy(title = title) else c
        }
    }

    override suspend fun deleteCase(caseId: String) {
        cases.value = cases.value.filterNot { it.id == caseId }
        // Removing a case removes its items, but never deletes underlying evidence
        items.value = items.value.filterNot { it.caseId == caseId }
    }

    override suspend fun addEvidenceItem(
        caseId: String,
        evidenceId: String,
        note: String,
        incidentDateMillis: Long?
    ): CaseItem {
        val newItem = CaseItem(
            id = "synth-item-" + UUID.randomUUID().toString(),
            caseId = caseId,
            evidenceId = evidenceId,
            note = note,
            incidentDateMillis = incidentDateMillis,
            addedAtMillis = System.currentTimeMillis()
        )
        items.value = items.value + newItem
        return newItem
    }

    override suspend fun addNoteItem(
        caseId: String,
        note: String,
        incidentDateMillis: Long?
    ): CaseItem {
        val newItem = CaseItem(
            id = "synth-item-" + UUID.randomUUID().toString(),
            caseId = caseId,
            evidenceId = null,
            note = note,
            incidentDateMillis = incidentDateMillis,
            addedAtMillis = System.currentTimeMillis()
        )
        items.value = items.value + newItem
        return newItem
    }

    override suspend fun updateItem(
        itemId: String,
        note: String,
        incidentDateMillis: Long?
    ) {
        items.value = items.value.map { item ->
            if (item.id == itemId) {
                item.copy(note = note, incidentDateMillis = incidentDateMillis)
            } else item
        }
    }

    override suspend fun removeItem(itemId: String) {
        // Removing an item never touches vault evidence
        items.value = items.value.filterNot { it.id == itemId }
    }

    override suspend fun casesForEvidence(evidenceId: String): List<Case> {
        val caseIds = items.value.filter { it.evidenceId == evidenceId }.map { it.caseId }.toSet()
        return cases.value.filter { it.id in caseIds }
    }

    override suspend fun deleteEverything() {
        cases.value = emptyList()
        items.value = emptyList()
    }
}
