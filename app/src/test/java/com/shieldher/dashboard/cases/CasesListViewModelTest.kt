package com.shieldher.dashboard.cases

import com.shieldher.R
import com.shieldher.dashboard.common.UiState
import com.shieldher.dashboard.data.Case
import com.shieldher.dashboard.data.CaseItem
import com.shieldher.dashboard.data.CaseRepository
import com.shieldher.dashboard.testing.MainDispatcherRule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.util.UUID

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class CasesListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // ---------------------------------------------------------------------------
    // Fake CaseRepository for test verification
    // ---------------------------------------------------------------------------
    private class TestableCaseRepository(
        initialCases: List<Case> = emptyList(),
        var throwCaseStoreException: Boolean = false
    ) : CaseRepository {
        val cases = MutableStateFlow(initialCases)

        override fun observeCases(): Flow<List<Case>> = if (throwCaseStoreException) {
            flow { throw CaseStoreException(CaseStoreException.Reason.UNREADABLE) }
        } else {
            cases.asStateFlow()
        }

        override fun observeItems(caseId: String): Flow<List<CaseItem>> = MutableStateFlow(emptyList())

        override suspend fun createCase(title: String, description: String): Case {
            if (throwCaseStoreException) throw CaseStoreException(CaseStoreException.Reason.UNREADABLE)
            val newCase = Case(
                id = "case-" + UUID.randomUUID().toString(),
                title = title,
                description = description,
                createdAtMillis = System.currentTimeMillis()
            )
            cases.value = cases.value + newCase
            return newCase
        }

        override suspend fun renameCase(caseId: String, title: String) {
            if (throwCaseStoreException) throw CaseStoreException(CaseStoreException.Reason.UNREADABLE)
            cases.value = cases.value.map {
                if (it.id == caseId) it.copy(title = title) else it
            }
        }

        override suspend fun deleteCase(caseId: String) {
            if (throwCaseStoreException) throw CaseStoreException(CaseStoreException.Reason.UNREADABLE)
            cases.value = cases.value.filterNot { it.id == caseId }
        }

        override suspend fun addEvidenceItem(caseId: String, evidenceId: String, note: String, incidentDateMillis: Long?): CaseItem = throw UnsupportedOperationException()
        override suspend fun addNoteItem(caseId: String, note: String, incidentDateMillis: Long?): CaseItem = throw UnsupportedOperationException()
        override suspend fun updateItem(itemId: String, note: String, incidentDateMillis: Long?) {}
        override suspend fun removeItem(itemId: String) {}
        override suspend fun casesForEvidence(evidenceId: String): List<Case> = emptyList()
        override suspend fun deleteEverything() {}
    }

    private fun createCase(id: String, title: String, time: Long) = Case(
        id = id,
        title = title,
        description = "Description for $title",
        createdAtMillis = time
    )

    // ---------------------------------------------------------------------------
    // Tests
    // ---------------------------------------------------------------------------

    @Test
    fun `empty state appears with no cases`() = runTest {
        val repo = TestableCaseRepository(emptyList())
        val vm = CasesListViewModel(repo)

        val states = mutableListOf<UiState<List<Case>>>()
        val job = launch(mainDispatcherRule.testDispatcher) {
            vm.uiState.toList(states)
        }
        advanceUntilIdle()

        assertEquals(UiState.Empty, states.last())
        job.cancel()
    }

    @Test
    fun `cases are sorted newest first`() = runTest {
        val caseOlder = createCase("1", "Older case", 1000L)
        val caseNewer = createCase("2", "Newer case", 2000L)
        val repo = TestableCaseRepository(listOf(caseOlder, caseNewer))
        val vm = CasesListViewModel(repo)

        val states = mutableListOf<UiState<List<Case>>>()
        val job = launch(mainDispatcherRule.testDispatcher) {
            vm.uiState.toList(states)
        }
        advanceUntilIdle()

        val last = states.last()
        assertTrue(last is UiState.Content)
        val list = (last as UiState.Content).data
        assertEquals("Newer case should be first", "2", list[0].id)
        assertEquals("Older case should be second", "1", list[1].id)

        job.cancel()
    }

    @Test
    fun `createCase adds a case and list updates`() = runTest {
        val repo = TestableCaseRepository(emptyList())
        val vm = CasesListViewModel(repo)

        val states = mutableListOf<UiState<List<Case>>>()
        val job = launch(mainDispatcherRule.testDispatcher) {
            vm.uiState.toList(states)
        }
        advanceUntilIdle()
        assertEquals(UiState.Empty, states.last())

        vm.createCase("Incident Alpha", "Detailed report")
        advanceUntilIdle()

        val last = states.last()
        assertTrue(last is UiState.Content)
        val list = (last as UiState.Content).data
        assertEquals(1, list.size)
        assertEquals("Incident Alpha", list[0].title)

        job.cancel()
    }

    @Test
    fun `blank title is rejected during create`() = runTest {
        val repo = TestableCaseRepository(emptyList())
        val vm = CasesListViewModel(repo)

        val states = mutableListOf<UiState<List<Case>>>()
        val job = launch(mainDispatcherRule.testDispatcher) {
            vm.uiState.toList(states)
        }
        advanceUntilIdle()

        vm.createCase("   ", "Some description")
        advanceUntilIdle()

        assertEquals(UiState.Empty, states.last())
        job.cancel()
    }

    @Test
    fun `renameCase changes the case title`() = runTest {
        val case1 = createCase("1", "Original Title", 1000L)
        val repo = TestableCaseRepository(listOf(case1))
        val vm = CasesListViewModel(repo)

        val states = mutableListOf<UiState<List<Case>>>()
        val job = launch(mainDispatcherRule.testDispatcher) {
            vm.uiState.toList(states)
        }
        advanceUntilIdle()

        vm.renameCase("1", "Updated Title")
        advanceUntilIdle()

        val last = states.last()
        assertTrue(last is UiState.Content)
        assertEquals("Updated Title", (last as UiState.Content).data[0].title)

        job.cancel()
    }

    @Test
    fun `deleteCase removes the case`() = runTest {
        val case1 = createCase("1", "Case 1", 1000L)
        val repo = TestableCaseRepository(listOf(case1))
        val vm = CasesListViewModel(repo)

        val states = mutableListOf<UiState<List<Case>>>()
        val job = launch(mainDispatcherRule.testDispatcher) {
            vm.uiState.toList(states)
        }
        advanceUntilIdle()

        vm.deleteCase("1")
        advanceUntilIdle()

        assertEquals(UiState.Empty, states.last())
        job.cancel()
    }

    @Test
    fun `CaseStoreException gives error state with unreadable message and no retry`() = runTest {
        val repo = TestableCaseRepository(throwCaseStoreException = true)
        val vm = CasesListViewModel(repo)

        val states = mutableListOf<UiState<List<Case>>>()
        val job = launch(mainDispatcherRule.testDispatcher) {
            vm.uiState.toList(states)
        }
        advanceUntilIdle()

        val last = states.last()
        assertTrue(last is UiState.Error)
        val error = last as UiState.Error
        assertEquals(R.string.cases_list_unreadable_store, error.messageRes)
        assertEquals(false, error.canRetry)

        job.cancel()
    }
}
