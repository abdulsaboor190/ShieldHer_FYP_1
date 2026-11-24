package com.shieldher.dashboard.cases

import com.shieldher.R
import com.shieldher.dashboard.common.BadgeStatus
import com.shieldher.dashboard.common.UiState
import com.shieldher.dashboard.data.Case
import com.shieldher.dashboard.data.CaseItem
import com.shieldher.dashboard.data.CaseRepository
import com.shieldher.dashboard.data.DisplayContent
import com.shieldher.dashboard.data.EvidenceGateway
import com.shieldher.dashboard.data.EvidenceIntegrity
import com.shieldher.dashboard.data.EvidenceItem
import com.shieldher.dashboard.data.ImportOutcome
import com.shieldher.dashboard.data.VerificationSession
import com.shieldher.dashboard.testing.MainDispatcherRule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.util.UUID

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class CaseTimelineViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // ---------------------------------------------------------------------------
    // Test Case Repository
    // ---------------------------------------------------------------------------
    private class TestTimelineCaseRepository(
        initialItems: List<CaseItem> = emptyList(),
        var throwCaseStoreException: Boolean = false
    ) : CaseRepository {
        val itemsFlow = MutableStateFlow(initialItems)

        override fun observeCases(): Flow<List<Case>> = MutableStateFlow(emptyList())

        override fun observeItems(caseId: String): Flow<List<CaseItem>> = if (throwCaseStoreException) {
            flow { throw CaseStoreException(CaseStoreException.Reason.UNREADABLE) }
        } else {
            itemsFlow.asStateFlow()
        }

        override suspend fun createCase(title: String, description: String): Case = throw UnsupportedOperationException()
        override suspend fun renameCase(caseId: String, title: String) {}
        override suspend fun deleteCase(caseId: String) {}

        override suspend fun addEvidenceItem(caseId: String, evidenceId: String, note: String, incidentDateMillis: Long?): CaseItem {
            val item = CaseItem(
                id = "item-" + UUID.randomUUID().toString(),
                caseId = caseId,
                evidenceId = evidenceId,
                note = note,
                incidentDateMillis = incidentDateMillis,
                addedAtMillis = System.currentTimeMillis()
            )
            itemsFlow.value = itemsFlow.value + item
            return item
        }

        override suspend fun addNoteItem(caseId: String, note: String, incidentDateMillis: Long?): CaseItem {
            val item = CaseItem(
                id = "note-" + UUID.randomUUID().toString(),
                caseId = caseId,
                evidenceId = null,
                note = note,
                incidentDateMillis = incidentDateMillis,
                addedAtMillis = System.currentTimeMillis()
            )
            itemsFlow.value = itemsFlow.value + item
            return item
        }

        override suspend fun updateItem(itemId: String, note: String, incidentDateMillis: Long?) {
            itemsFlow.value = itemsFlow.value.map {
                if (it.id == itemId) it.copy(note = note, incidentDateMillis = incidentDateMillis) else it
            }
        }

        override suspend fun removeItem(itemId: String) {
            itemsFlow.value = itemsFlow.value.filterNot { it.id == itemId }
        }

        override suspend fun casesForEvidence(evidenceId: String): List<Case> = emptyList()
        override suspend fun deleteEverything() {}
    }

    // ---------------------------------------------------------------------------
    // Test Evidence Gateway
    // ---------------------------------------------------------------------------
    private class TestTimelineEvidenceGateway(
        initialEvidence: List<EvidenceItem> = emptyList()
    ) : EvidenceGateway {
        val evidenceFlow = MutableStateFlow(initialEvidence)

        override fun observeEvidence(): Flow<List<EvidenceItem>> = evidenceFlow.asStateFlow()
        override suspend fun getEvidence(id: String): EvidenceItem? = evidenceFlow.value.find { it.id == id }
        override suspend fun verify(id: String): EvidenceIntegrity = EvidenceIntegrity.UNCHANGED
        override suspend fun readForDisplay(id: String, maxBytes: Int): DisplayContent = DisplayContent.Unavailable
        override suspend fun importFromUri(uri: android.net.Uri, mimeType: String?): ImportOutcome = ImportOutcome.Failed
        override suspend fun delete(id: String): Boolean = true
        override suspend fun deleteAll(): Int = 0
    }

    private fun createEvidence(id: String, receiptTime: Long) = EvidenceItem(
        id = id,
        hash = "hash_$id",
        receiptTimeEpochMillis = receiptTime,
        timeZoneId = "UTC",
        sourceLabel = "Source $id",
        mimeType = "image/png",
        fileSizeBytes = 2048L,
        filePath = "/path/$id"
    )

    // ---------------------------------------------------------------------------
    // Tests
    // ---------------------------------------------------------------------------

    @Test
    fun `empty case shows empty state`() = runTest {
        val caseRepo = TestTimelineCaseRepository(emptyList())
        val gateway = TestTimelineEvidenceGateway(emptyList())
        val session = VerificationSession()

        val vm = CaseTimelineViewModel("case-1", caseRepo, gateway, session)
        val states = mutableListOf<UiState<List<TimelineEntry>>>()
        val job = launch(mainDispatcherRule.testDispatcher) {
            vm.uiState.toList(states)
        }
        advanceUntilIdle()

        assertEquals(UiState.Empty, states.last())
        job.cancel()
    }

    @Test
    fun `ordering follows three-level rule oldest first`() = runTest {
        // Item A: has user-entered date = 1000L
        val itemA = CaseItem("a", "case-1", "ev-1", "Note A", incidentDateMillis = 1000L, addedAtMillis = 5000L)
        // Item B: has evidence receipt time = 2000L, incidentDateMillis = null
        val itemB = CaseItem("b", "case-1", "ev-2", "Note B", incidentDateMillis = null, addedAtMillis = 6000L)
        // Item C: note only, incidentDateMillis = null, addedAtMillis = 3000L
        val itemC = CaseItem("c", "case-1", null, "Note C", incidentDateMillis = null, addedAtMillis = 3000L)

        val ev1 = createEvidence("ev-1", 4000L)
        val ev2 = createEvidence("ev-2", 2000L)

        val caseRepo = TestTimelineCaseRepository(listOf(itemC, itemA, itemB))
        val gateway = TestTimelineEvidenceGateway(listOf(ev1, ev2))
        val session = VerificationSession()

        val vm = CaseTimelineViewModel("case-1", caseRepo, gateway, session)
        val states = mutableListOf<UiState<List<TimelineEntry>>>()
        val job = launch(mainDispatcherRule.testDispatcher) {
            vm.uiState.toList(states)
        }
        advanceUntilIdle()

        val content = states.last() as UiState.Content
        val entries = content.data

        assertEquals(3, entries.size)
        // Sorted oldest first:
        // itemA (1000L) -> itemB (2000L) -> itemC (3000L)
        assertEquals("a", entries[0].caseItem.id)
        assertEquals("b", entries[1].caseItem.id)
        assertEquals("c", entries[2].caseItem.id)

        // Labels check
        assertTrue("Item A should have 'You entered' label", entries[0].timeLabelText.startsWith("You entered:"))
        assertTrue("Item B should have 'Received by ShieldHer' label", entries[1].timeLabelText.startsWith("Received by ShieldHer:"))
        assertTrue("Item C should have 'Added:' label", entries[2].timeLabelText.startsWith("Added:"))

        job.cancel()
    }

    @Test
    fun `item with deleted evidence shows DeletedEvidence entry and keeps note`() = runTest {
        // item points to ev-missing which is not in gateway
        val item = CaseItem("item-del", "case-1", "ev-missing", "Important retained note", null, 1000L)
        val caseRepo = TestTimelineCaseRepository(listOf(item))
        val gateway = TestTimelineEvidenceGateway(emptyList()) // No evidence in gateway
        val session = VerificationSession()

        val vm = CaseTimelineViewModel("case-1", caseRepo, gateway, session)
        val states = mutableListOf<UiState<List<TimelineEntry>>>()
        val job = launch(mainDispatcherRule.testDispatcher) {
            vm.uiState.toList(states)
        }
        advanceUntilIdle()

        val content = states.last() as UiState.Content
        val entry = content.data[0]

        assertTrue("Should be DeletedEvidence", entry is TimelineEntry.DeletedEvidence)
        assertEquals("Important retained note", entry.noteText)

        job.cancel()
    }

    @Test
    fun `addEvidence, addNote, updateItem, and removeItem modify repository correctly`() = runTest {
        val caseRepo = TestTimelineCaseRepository(emptyList())
        val ev1 = createEvidence("ev-1", 1000L)
        val gateway = TestTimelineEvidenceGateway(listOf(ev1))
        val session = VerificationSession()

        val vm = CaseTimelineViewModel("case-1", caseRepo, gateway, session)
        val states = mutableListOf<UiState<List<TimelineEntry>>>()
        val job = launch(mainDispatcherRule.testDispatcher) {
            vm.uiState.toList(states)
        }
        advanceUntilIdle()
        assertEquals(UiState.Empty, states.last())

        // 1. Add evidence
        vm.addEvidence("ev-1", "Evidence note", 1500L)
        advanceUntilIdle()
        var current = (states.last() as UiState.Content).data
        assertEquals(1, current.size)
        val addedEvidenceItem = current[0].caseItem

        // 2. Add note
        vm.addNote("Standalone incident note", 1600L)
        advanceUntilIdle()
        current = (states.last() as UiState.Content).data
        assertEquals(2, current.size)
        val noteItem = current.find { it.caseItem.evidenceId == null }!!.caseItem

        // 3. Update note
        vm.updateItem(noteItem.id, "Edited note text", 1700L)
        advanceUntilIdle()
        current = (states.last() as UiState.Content).data
        val updatedNoteEntry = current.find { it.caseItem.id == noteItem.id }!!
        assertEquals("Edited note text", updatedNoteEntry.noteText)

        // 4. Remove evidence item
        vm.removeItem(addedEvidenceItem.id)
        advanceUntilIdle()
        current = (states.last() as UiState.Content).data
        assertEquals(1, current.size)
        assertEquals(noteItem.id, current[0].caseItem.id)

        // Gateway evidence should be completely untouched
        assertEquals(1, gateway.evidenceFlow.value.size)

        job.cancel()
    }

    @Test
    fun `CaseStoreException shows error state with no retry`() = runTest {
        val caseRepo = TestTimelineCaseRepository(throwCaseStoreException = true)
        val gateway = TestTimelineEvidenceGateway(emptyList())
        val session = VerificationSession()

        val vm = CaseTimelineViewModel("case-1", caseRepo, gateway, session)
        val states = mutableListOf<UiState<List<TimelineEntry>>>()
        val job = launch(mainDispatcherRule.testDispatcher) {
            vm.uiState.toList(states)
        }
        advanceUntilIdle()

        val last = states.last()
        assertTrue(last is UiState.Error)
        val error = last as UiState.Error
        assertEquals(R.string.timeline_unreadable_store, error.messageRes)
        assertEquals(false, error.canRetry)

        job.cancel()
    }
}
