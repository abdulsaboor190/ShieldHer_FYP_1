package com.shieldher.dashboard.home

import com.shieldher.dashboard.common.BadgeStatus
import com.shieldher.dashboard.common.UiState
import com.shieldher.dashboard.data.Case
import com.shieldher.dashboard.data.EvidenceIntegrity
import com.shieldher.dashboard.data.EvidenceItem
import com.shieldher.dashboard.data.VerificationRecord
import com.shieldher.dashboard.data.VerificationSession
import com.shieldher.dashboard.data.fake.FakeCaseRepository
import com.shieldher.dashboard.data.fake.FakeEvidenceGateway
import com.shieldher.dashboard.testing.MainDispatcherRule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun uiState_startsInLoading_thenReachesContent() = runTest {
        val gateway = FakeEvidenceGateway()
        val cases = FakeCaseRepository()
        val session = VerificationSession()

        val viewModel = HomeViewModel(gateway, cases, session)

        val states = mutableListOf<UiState<HomeContent>>()
        val job = kotlinx.coroutines.launch(mainDispatcherRule.testDispatcher) {
            viewModel.uiState.toList(states)
        }

        testScheduler.advanceUntilIdle()

        assertTrue(states.any { it is UiState.Content })
        val content = states.filterIsInstance<UiState.Content<HomeContent>>().last().data
        assertEquals(6, content.evidenceCount)
        assertEquals(2, content.caseCount)
        assertEquals(3, content.latestItems.size)

        job.cancel()
    }

    @Test
    fun uiState_emitsEmpty_whenEvidenceGatewayHasNoItems() = runTest {
        val gateway = FakeEvidenceGateway()
        gateway.deleteAll()
        val cases = FakeCaseRepository()
        val session = VerificationSession()

        val viewModel = HomeViewModel(gateway, cases, session)

        val states = mutableListOf<UiState<HomeContent>>()
        val job = kotlinx.coroutines.launch(mainDispatcherRule.testDispatcher) {
            viewModel.uiState.toList(states)
        }

        testScheduler.advanceUntilIdle()

        assertTrue(states.any { it is UiState.Empty })

        job.cancel()
    }

    @Test
    fun uiState_returnsOnlyThreeLatestItemsSortedNewestFirst() = runTest {
        val gateway = FakeEvidenceGateway()
        val cases = FakeCaseRepository()
        val session = VerificationSession()

        val viewModel = HomeViewModel(gateway, cases, session)

        val states = mutableListOf<UiState<HomeContent>>()
        val job = kotlinx.coroutines.launch(mainDispatcherRule.testDispatcher) {
            viewModel.uiState.toList(states)
        }

        testScheduler.advanceUntilIdle()

        val content = states.filterIsInstance<UiState.Content<HomeContent>>().last().data
        assertEquals(3, content.latestItems.size)
        // synth-evidence-6 (1794518000000L), 5 (1794514400000L), 4 (1794510800000L)
        assertEquals("synth-evidence-6", content.latestItems[0].id)
        assertEquals("synth-evidence-5", content.latestItems[1].id)
        assertEquals("synth-evidence-4", content.latestItems[2].id)

        job.cancel()
    }

    @Test
    fun uiState_badgesComeFromVerificationSessionRecords() = runTest {
        val gateway = FakeEvidenceGateway()
        val cases = FakeCaseRepository()
        val session = VerificationSession()

        session.record("synth-evidence-6", EvidenceIntegrity.CHANGED, 1794519000000L)

        val viewModel = HomeViewModel(gateway, cases, session)

        val states = mutableListOf<UiState<HomeContent>>()
        val job = kotlinx.coroutines.launch(mainDispatcherRule.testDispatcher) {
            viewModel.uiState.toList(states)
        }

        testScheduler.advanceUntilIdle()

        val content = states.filterIsInstance<UiState.Content<HomeContent>>().last().data
        assertEquals(BadgeStatus.CHANGED, content.latestItems[0].cardModel.status)
        assertEquals(BadgeStatus.NOT_CHECKED, content.latestItems[1].cardModel.status)

        job.cancel()
    }

    @Test
    fun uiState_handlesUnreadableCaseRepositoryGracefully() = runTest {
        val gateway = FakeEvidenceGateway()
        val session = VerificationSession()
        val failingCases = object : FakeCaseRepository() {
            override fun observeCases(): Flow<List<Case>> = flow {
                throw RuntimeException("Store unreadable")
            }
        }

        val viewModel = HomeViewModel(gateway, failingCases, session)

        val states = mutableListOf<UiState<HomeContent>>()
        val job = kotlinx.coroutines.launch(mainDispatcherRule.testDispatcher) {
            viewModel.uiState.toList(states)
        }

        testScheduler.advanceUntilIdle()

        val content = states.filterIsInstance<UiState.Content<HomeContent>>().last().data
        assertEquals(6, content.evidenceCount)
        assertNull(content.caseCount)

        job.cancel()
    }
}
