package com.shieldher.dashboard.evidence

import com.shieldher.dashboard.common.BadgeStatus
import com.shieldher.dashboard.common.UiState
import com.shieldher.dashboard.data.EvidenceIntegrity
import com.shieldher.dashboard.data.VerificationSession
import com.shieldher.dashboard.data.fake.FakeEvidenceGateway
import com.shieldher.dashboard.testing.MainDispatcherRule
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class DashboardEvidenceListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun list_isSortedNewestFirst() = runTest {
        val gateway = FakeEvidenceGateway()
        val session = VerificationSession()
        val viewModel = DashboardEvidenceListViewModel(gateway, session)

        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is EvidenceListUiState.Content)
        val items = (state as EvidenceListUiState.Content).items
        for (i in 0 until items.size - 1) {
            val a = gateway.observeEvidence()
            // Items should be newest first (highest id index = latest receiptTime in fake data)
        }
        // synth-evidence-6 has the highest timestamp
        assertEquals("synth-evidence-6", items.first().id)
        assertEquals("synth-evidence-1", items.last().id)
    }

    @Test
    fun filter_matchesSourceLabel_caseInsensitive() = runTest {
        val gateway = FakeEvidenceGateway()
        val session = VerificationSession()
        val viewModel = DashboardEvidenceListViewModel(gateway, session)
        testScheduler.advanceUntilIdle()

        // All fake items have sourceLabel "Example app"
        viewModel.setSearchQuery("example")
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is EvidenceListUiState.Content)
        val items = (state as EvidenceListUiState.Content).items
        assertTrue(items.isNotEmpty())
    }

    @Test
    fun filter_matchesDateText_caseInsensitive() = runTest {
        val gateway = FakeEvidenceGateway()
        val session = VerificationSession()
        val viewModel = DashboardEvidenceListViewModel(gateway, session)
        testScheduler.advanceUntilIdle()

        // The fake timestamps are in Nov 2026
        viewModel.setSearchQuery("nov")
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("Expected Content with matches", state is EvidenceListUiState.Content)
    }

    @Test
    fun filter_noMatch_producesNoMatchState_andClearRestoresList() = runTest {
        val gateway = FakeEvidenceGateway()
        val session = VerificationSession()
        val viewModel = DashboardEvidenceListViewModel(gateway, session)
        testScheduler.advanceUntilIdle()

        viewModel.setSearchQuery("ZZZZZNOWAYMATCH12345")
        testScheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value is EvidenceListUiState.NoMatch)

        viewModel.clearSearch()
        testScheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value is EvidenceListUiState.Content)
    }

    @Test
    fun statusBadge_reflectsVerificationSessionRecord() = runTest {
        val gateway = FakeEvidenceGateway()
        val session = VerificationSession()
        session.record("synth-evidence-6", EvidenceIntegrity.CHANGED, System.currentTimeMillis())

        val viewModel = DashboardEvidenceListViewModel(gateway, session)
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value as EvidenceListUiState.Content
        val item6 = state.items.find { it.id == "synth-evidence-6" }!!
        val item1 = state.items.find { it.id == "synth-evidence-1" }!!
        assertEquals(BadgeStatus.CHANGED, item6.cardModel.status)
        assertEquals(BadgeStatus.NOT_CHECKED, item1.cardModel.status)
    }

    @Test
    fun emptyGateway_producesEmptyState() = runTest {
        val gateway = FakeEvidenceGateway()
        gateway.deleteAll()
        val session = VerificationSession()

        val viewModel = DashboardEvidenceListViewModel(gateway, session)
        testScheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value is EvidenceListUiState.Empty)
    }
}
