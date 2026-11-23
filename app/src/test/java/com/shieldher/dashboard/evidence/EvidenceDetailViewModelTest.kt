package com.shieldher.dashboard.evidence

import android.graphics.Bitmap
import com.shieldher.dashboard.cases.CaseStoreException
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
import com.shieldher.dashboard.data.fake.FakeCaseRepository
import com.shieldher.dashboard.data.fake.FakeEvidenceGateway
import com.shieldher.dashboard.testing.MainDispatcherRule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class EvidenceDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun itemLoads_andMissingIdGivesErrorState() = runTest {
        val gateway = FakeEvidenceGateway()
        val caseRepo = FakeCaseRepository()
        val session = VerificationSession()

        val viewModel = EvidenceDetailViewModel("non-existent-id", gateway, caseRepo, session)

        val states = mutableListOf<UiState<EvidenceDetailContent>>()
        val job = launch(mainDispatcherRule.testDispatcher) {
            viewModel.uiState.toList(states)
        }
        testScheduler.advanceUntilIdle()

        assertTrue(states.any { it is UiState.Error })
        val error = states.filterIsInstance<UiState.Error>().last()
        assertFalse(error.canRetry)

        job.cancel()
    }

    @Test
    fun existingSessionRecord_isShownOnOpen() = runTest {
        val gateway = FakeEvidenceGateway()
        val caseRepo = FakeCaseRepository()
        val session = VerificationSession()
        session.record("synth-evidence-1", EvidenceIntegrity.CHANGED, 1794500000000L)

        val viewModel = EvidenceDetailViewModel("synth-evidence-1", gateway, caseRepo, session)

        val states = mutableListOf<UiState<EvidenceDetailContent>>()
        val job = launch(mainDispatcherRule.testDispatcher) {
            viewModel.uiState.toList(states)
        }
        testScheduler.advanceUntilIdle()

        val content = states.filterIsInstance<UiState.Content<EvidenceDetailContent>>().last().data
        assertEquals(BadgeStatus.CHANGED, content.verificationState.status)
        assertNotNull(content.verificationState.checkedAtText)

        job.cancel()
    }

    @Test
    fun verify_recordsRightResultInSession_forAllThreeOutcomes() = runTest {
        val gateway = FakeEvidenceGateway()
        val caseRepo = FakeCaseRepository()

        // 1. Unchanged (synth-evidence-1)
        val session1 = VerificationSession()
        val vm1 = EvidenceDetailViewModel("synth-evidence-1", gateway, caseRepo, session1)
        testScheduler.advanceUntilIdle()
        vm1.verify()
        testScheduler.advanceUntilIdle()
        assertEquals(EvidenceIntegrity.UNCHANGED, session1.get("synth-evidence-1")?.result)

        // 2. Changed (synth-evidence-2)
        val session2 = VerificationSession()
        val vm2 = EvidenceDetailViewModel("synth-evidence-2", gateway, caseRepo, session2)
        testScheduler.advanceUntilIdle()
        vm2.verify()
        testScheduler.advanceUntilIdle()
        assertEquals(EvidenceIntegrity.CHANGED, session2.get("synth-evidence-2")?.result)

        // 3. Missing (synth-evidence-5)
        val session3 = VerificationSession()
        val vm3 = EvidenceDetailViewModel("synth-evidence-5", gateway, caseRepo, session3)
        testScheduler.advanceUntilIdle()
        vm3.verify()
        testScheduler.advanceUntilIdle()
        assertEquals(EvidenceIntegrity.MISSING, session3.get("synth-evidence-5")?.result)
    }

    @Test
    fun readForDisplay_mapsToEachPreviewState() = runTest {
        val session = VerificationSession()
        val caseRepo = FakeCaseRepository()

        // Text item: synth-evidence-2 has text/plain in FakeEvidenceGateway
        val gateway = FakeEvidenceGateway()
        val vmText = EvidenceDetailViewModel("synth-evidence-2", gateway, caseRepo, session)

        val textStates = mutableListOf<UiState<EvidenceDetailContent>>()
        val jobText = launch(mainDispatcherRule.testDispatcher) {
            vmText.uiState.toList(textStates)
        }
        testScheduler.advanceUntilIdle()
        val contentText = textStates.filterIsInstance<UiState.Content<EvidenceDetailContent>>().last().data
        assertTrue(contentText.previewState is PreviewState.Text)
        jobText.cancel()

        // Custom gateway testing TooLarge, Unsupported, Unavailable, Image
        val customGateway = object : EvidenceGateway {
            var displayResponse: DisplayContent = DisplayContent.TooLarge

            override fun observeEvidence(): Flow<List<EvidenceItem>> = MutableStateFlow(emptyList())
            override suspend fun getEvidence(id: String): EvidenceItem = EvidenceItem(
                id = id,
                hash = "hash",
                receiptTimeEpochMillis = 1000L,
                timeZoneId = "UTC",
                sourceLabel = "Source",
                mimeType = "custom/mime",
                fileSizeBytes = 100L,
                filePath = "/path"
            )
            override suspend fun verify(id: String): EvidenceIntegrity = EvidenceIntegrity.UNCHANGED
            override suspend fun readForDisplay(id: String, maxBytes: Int): DisplayContent = displayResponse
            override suspend fun importFromUri(uri: android.net.Uri, mimeType: String?): ImportOutcome = ImportOutcome.Failed
            override suspend fun delete(id: String): Boolean = true
            override suspend fun deleteAll(): Int = 0
        }

        customGateway.displayResponse = DisplayContent.TooLarge
        val vmTooLarge = EvidenceDetailViewModel("any-id", customGateway, caseRepo, session)
        val s1 = mutableListOf<UiState<EvidenceDetailContent>>()
        val j1 = launch(mainDispatcherRule.testDispatcher) { vmTooLarge.uiState.toList(s1) }
        testScheduler.advanceUntilIdle()
        assertTrue(s1.filterIsInstance<UiState.Content<EvidenceDetailContent>>().last().data.previewState is PreviewState.TooLarge)
        j1.cancel()

        customGateway.displayResponse = DisplayContent.Unsupported
        val vmUnsupported = EvidenceDetailViewModel("any-id", customGateway, caseRepo, session)
        val s2 = mutableListOf<UiState<EvidenceDetailContent>>()
        val j2 = launch(mainDispatcherRule.testDispatcher) { vmUnsupported.uiState.toList(s2) }
        testScheduler.advanceUntilIdle()
        assertTrue(s2.filterIsInstance<UiState.Content<EvidenceDetailContent>>().last().data.previewState is PreviewState.Unsupported)
        j2.cancel()

        customGateway.displayResponse = DisplayContent.Unavailable
        val vmUnavailable = EvidenceDetailViewModel("any-id", customGateway, caseRepo, session)
        val s3 = mutableListOf<UiState<EvidenceDetailContent>>()
        val j3 = launch(mainDispatcherRule.testDispatcher) { vmUnavailable.uiState.toList(s3) }
        testScheduler.advanceUntilIdle()
        assertTrue(s3.filterIsInstance<UiState.Content<EvidenceDetailContent>>().last().data.previewState is PreviewState.Unavailable)
        j3.cancel()
    }

    @Test
    fun viewModel_doesNotCallVerifyUntilAsked() = runTest {
        var verifyCalled = false
        val customGateway = object : EvidenceGateway {
            override fun observeEvidence(): Flow<List<EvidenceItem>> = MutableStateFlow(emptyList())
            override suspend fun getEvidence(id: String): EvidenceItem = EvidenceItem(
                id = id,
                hash = "hash",
                receiptTimeEpochMillis = 1000L,
                timeZoneId = "UTC",
                sourceLabel = "Source",
                mimeType = "text/plain",
                fileSizeBytes = 100L,
                filePath = "/path"
            )
            override suspend fun verify(id: String): EvidenceIntegrity {
                verifyCalled = true
                return EvidenceIntegrity.UNCHANGED
            }
            override suspend fun readForDisplay(id: String, maxBytes: Int): DisplayContent = DisplayContent.Unavailable
            override suspend fun importFromUri(uri: android.net.Uri, mimeType: String?): ImportOutcome = ImportOutcome.Failed
            override suspend fun delete(id: String): Boolean = true
            override suspend fun deleteAll(): Int = 0
        }

        val caseRepo = FakeCaseRepository()
        val session = VerificationSession()
        val viewModel = EvidenceDetailViewModel("any-id", customGateway, caseRepo, session)
        testScheduler.advanceUntilIdle()

        assertFalse("Verify should not be called automatically on load", verifyCalled)
        assertEquals(BadgeStatus.NOT_CHECKED, (viewModel.uiState.value as UiState.Content).data.verificationState.status)
    }

    @Test
    fun linkedCases_listedForRightEvidenceId() = runTest {
        val gateway = FakeEvidenceGateway()
        val caseRepo = FakeCaseRepository()
        val session = VerificationSession()

        // synth-evidence-1 belongs to synth-case-1 in FakeCaseRepository
        val viewModel = EvidenceDetailViewModel("synth-evidence-1", gateway, caseRepo, session)
        testScheduler.advanceUntilIdle()

        val content = (viewModel.uiState.value as UiState.Content).data
        val linked = content.linkedCasesState as LinkedCasesState.Content
        assertEquals(1, linked.cases.size)
        assertEquals("synth-case-1", linked.cases[0].id)

        // synth-evidence-2 has no linked cases initially
        val vm2 = EvidenceDetailViewModel("synth-evidence-2", gateway, caseRepo, session)
        testScheduler.advanceUntilIdle()
        val content2 = (vm2.uiState.value as UiState.Content).data
        val linked2 = content2.linkedCasesState as LinkedCasesState.Content
        assertTrue(linked2.cases.isEmpty())
    }

    @Test
    fun addToCase_createsItemInCaseRepository_andRefreshesLinkedCases() = runTest {
        val gateway = FakeEvidenceGateway()
        val caseRepo = FakeCaseRepository()
        val session = VerificationSession()

        val viewModel = EvidenceDetailViewModel("synth-evidence-2", gateway, caseRepo, session)
        testScheduler.advanceUntilIdle()

        // Initially 0 linked cases
        var content = (viewModel.uiState.value as UiState.Content).data
        assertEquals(0, (content.linkedCasesState as LinkedCasesState.Content).cases.size)

        // Add to synth-case-2
        val dateMillis = 1794500000000L
        viewModel.addToCase("synth-case-2", "Test note", dateMillis)
        testScheduler.advanceUntilIdle()

        // Verify caseRepo has the item
        val items = caseRepo.observeItems("synth-case-2").first()
        val addedItem = items.find { it.evidenceId == "synth-evidence-2" }
        assertNotNull(addedItem)
        assertEquals("Test note", addedItem?.note)
        assertEquals(dateMillis, addedItem?.incidentDateMillis)

        // Verify linkedCasesState refreshed
        content = (viewModel.uiState.value as UiState.Content).data
        val linked = content.linkedCasesState as LinkedCasesState.Content
        assertEquals(1, linked.cases.size)
        assertEquals("synth-case-2", linked.cases[0].id)
    }

    @Test
    fun deleteEvidence_removesFromGateway_andDoesNotTouchCaseRepository() = runTest {
        val gateway = FakeEvidenceGateway()
        val caseRepo = FakeCaseRepository()
        val session = VerificationSession()

        // synth-evidence-1 is in synth-case-1
        val viewModel = EvidenceDetailViewModel("synth-evidence-1", gateway, caseRepo, session)
        testScheduler.advanceUntilIdle()

        val deleteEvents = mutableListOf<Boolean>()
        val job = launch(mainDispatcherRule.testDispatcher) {
            viewModel.deleteEvent.toList(deleteEvents)
        }

        viewModel.deleteEvidence()
        testScheduler.advanceUntilIdle()

        assertTrue(deleteEvents.isNotEmpty() && deleteEvents.last())

        // Gateway should no longer have synth-evidence-1
        val itemInGateway = gateway.getEvidence("synth-evidence-1")
        assertEquals(null, itemInGateway)

        // Case repo items for synth-case-1 must still have the item referencing synth-evidence-1
        val caseItems = caseRepo.observeItems("synth-case-1").first()
        val stillExists = caseItems.any { it.evidenceId == "synth-evidence-1" }
        assertTrue("Case item must still exist after evidence deletion", stillExists)

        job.cancel()
    }

    @Test
    fun caseStoreException_leadsToUnreadableState() = runTest {
        val gateway = FakeEvidenceGateway()
        val brokenRepo = object : CaseRepository {
            override fun observeCases(): Flow<List<Case>> = MutableStateFlow(emptyList())
            override fun observeItems(caseId: String): Flow<List<CaseItem>> = MutableStateFlow(emptyList())
            override suspend fun createCase(title: String, description: String): Case = throw CaseStoreException(CaseStoreException.Reason.UNREADABLE)
            override suspend fun renameCase(caseId: String, title: String) = throw CaseStoreException(CaseStoreException.Reason.UNREADABLE)
            override suspend fun deleteCase(caseId: String) = throw CaseStoreException(CaseStoreException.Reason.UNREADABLE)
            override suspend fun addEvidenceItem(caseId: String, evidenceId: String, note: String, incidentDateMillis: Long?): CaseItem = throw CaseStoreException(CaseStoreException.Reason.UNREADABLE)
            override suspend fun addNoteItem(caseId: String, note: String, incidentDateMillis: Long?): CaseItem = throw CaseStoreException(CaseStoreException.Reason.UNREADABLE)
            override suspend fun updateItem(itemId: String, note: String, incidentDateMillis: Long?) = throw CaseStoreException(CaseStoreException.Reason.UNREADABLE)
            override suspend fun removeItem(itemId: String) = throw CaseStoreException(CaseStoreException.Reason.UNREADABLE)
            override suspend fun casesForEvidence(evidenceId: String): List<Case> = throw CaseStoreException(CaseStoreException.Reason.UNREADABLE)
            override suspend fun deleteEverything() = throw CaseStoreException(CaseStoreException.Reason.UNREADABLE)
        }
        val session = VerificationSession()

        val viewModel = EvidenceDetailViewModel("synth-evidence-1", gateway, brokenRepo, session)
        testScheduler.advanceUntilIdle()

        val content = (viewModel.uiState.value as UiState.Content).data
        assertEquals(LinkedCasesState.Unreadable, content.linkedCasesState)
    }
}
