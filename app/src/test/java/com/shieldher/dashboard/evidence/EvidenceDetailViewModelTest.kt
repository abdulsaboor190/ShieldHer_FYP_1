package com.shieldher.dashboard.evidence

import android.graphics.Bitmap
import com.shieldher.dashboard.common.BadgeStatus
import com.shieldher.dashboard.common.UiState
import com.shieldher.dashboard.data.DisplayContent
import com.shieldher.dashboard.data.EvidenceGateway
import com.shieldher.dashboard.data.EvidenceIntegrity
import com.shieldher.dashboard.data.EvidenceItem
import com.shieldher.dashboard.data.ImportOutcome
import com.shieldher.dashboard.data.VerificationSession
import com.shieldher.dashboard.data.fake.FakeEvidenceGateway
import com.shieldher.dashboard.testing.MainDispatcherRule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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
        val session = VerificationSession()

        val viewModel = EvidenceDetailViewModel("non-existent-id", gateway, session)

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
        val session = VerificationSession()
        session.record("synth-evidence-1", EvidenceIntegrity.CHANGED, 1794500000000L)

        val viewModel = EvidenceDetailViewModel("synth-evidence-1", gateway, session)

        val states = mutableListOf<UiState<EvidenceDetailContent>>()
        val job = launch(mainDispatcherRule.testDispatcher) {
            viewModel.uiState.toList(states)
        }
        testScheduler.advanceUntilIdle()

        val content = states.filterIsInstance<UiState<EvidenceDetailContent>>().last().data
        assertEquals(BadgeStatus.CHANGED, content.verificationState.status)
        assertNotNull(content.verificationState.checkedAtText)

        job.cancel()
    }

    @Test
    fun verify_recordsRightResultInSession_forAllThreeOutcomes() = runTest {
        val gateway = FakeEvidenceGateway()

        // 1. Unchanged (synth-evidence-1)
        val session1 = VerificationSession()
        val vm1 = EvidenceDetailViewModel("synth-evidence-1", gateway, session1)
        testScheduler.advanceUntilIdle()
        vm1.verify()
        testScheduler.advanceUntilIdle()
        assertEquals(EvidenceIntegrity.UNCHANGED, session1.get("synth-evidence-1")?.result)

        // 2. Changed (synth-evidence-2)
        val session2 = VerificationSession()
        val vm2 = EvidenceDetailViewModel("synth-evidence-2", gateway, session2)
        testScheduler.advanceUntilIdle()
        vm2.verify()
        testScheduler.advanceUntilIdle()
        assertEquals(EvidenceIntegrity.CHANGED, session2.get("synth-evidence-2")?.result)

        // 3. Missing (synth-evidence-5)
        val session3 = VerificationSession()
        val vm3 = EvidenceDetailViewModel("synth-evidence-5", gateway, session3)
        testScheduler.advanceUntilIdle()
        vm3.verify()
        testScheduler.advanceUntilIdle()
        assertEquals(EvidenceIntegrity.MISSING, session3.get("synth-evidence-5")?.result)
    }

    @Test
    fun readForDisplay_mapsToEachPreviewState() = runTest {
        val session = VerificationSession()

        // Text item: synth-evidence-2 has text/plain in FakeEvidenceGateway
        val gateway = FakeEvidenceGateway()
        val vmText = EvidenceDetailViewModel("synth-evidence-2", gateway, session)

        val textStates = mutableListOf<UiState<EvidenceDetailContent>>()
        val jobText = launch(mainDispatcherRule.testDispatcher) {
            vmText.uiState.toList(textStates)
        }
        testScheduler.advanceUntilIdle()
        val contentText = textStates.filterIsInstance<UiState<EvidenceDetailContent>>().last().data
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
        val vmTooLarge = EvidenceDetailViewModel("any-id", customGateway, session)
        val s1 = mutableListOf<UiState<EvidenceDetailContent>>()
        val j1 = launch(mainDispatcherRule.testDispatcher) { vmTooLarge.uiState.toList(s1) }
        testScheduler.advanceUntilIdle()
        assertTrue(s1.filterIsInstance<UiState<EvidenceDetailContent>>().last().data.previewState is PreviewState.TooLarge)
        j1.cancel()

        customGateway.displayResponse = DisplayContent.Unsupported
        val vmUnsupported = EvidenceDetailViewModel("any-id", customGateway, session)
        val s2 = mutableListOf<UiState<EvidenceDetailContent>>()
        val j2 = launch(mainDispatcherRule.testDispatcher) { vmUnsupported.uiState.toList(s2) }
        testScheduler.advanceUntilIdle()
        assertTrue(s2.filterIsInstance<UiState<EvidenceDetailContent>>().last().data.previewState is PreviewState.Unsupported)
        j2.cancel()

        customGateway.displayResponse = DisplayContent.Unavailable
        val vmUnavailable = EvidenceDetailViewModel("any-id", customGateway, session)
        val s3 = mutableListOf<UiState<EvidenceDetailContent>>()
        val j3 = launch(mainDispatcherRule.testDispatcher) { vmUnavailable.uiState.toList(s3) }
        testScheduler.advanceUntilIdle()
        assertTrue(s3.filterIsInstance<UiState<EvidenceDetailContent>>().last().data.previewState is PreviewState.Unavailable)
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

        val session = VerificationSession()
        val viewModel = EvidenceDetailViewModel("any-id", customGateway, session)
        testScheduler.advanceUntilIdle()

        assertFalse("Verify should not be called automatically on load", verifyCalled)
        assertEquals(BadgeStatus.NOT_CHECKED, (viewModel.uiState.value as UiState.Content).data.verificationState.status)
    }
}
