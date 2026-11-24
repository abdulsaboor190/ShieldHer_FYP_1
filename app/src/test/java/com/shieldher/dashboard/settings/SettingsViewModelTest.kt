package com.shieldher.dashboard.settings

import com.shieldher.dashboard.cases.CaseStoreException
import com.shieldher.dashboard.common.shouldSecure
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // ---------------------------------------------------------------------------
    // Test Evidence Gateway
    // ---------------------------------------------------------------------------
    private class TestGateway(
        initialItems: List<EvidenceItem> = emptyList(),
        var failDeleteAll: Boolean = false
    ) : EvidenceGateway {
        val callOrder = mutableListOf<String>()
        val itemsFlow = MutableStateFlow(initialItems)

        override fun observeEvidence(): Flow<List<EvidenceItem>> = itemsFlow.asStateFlow()
        override suspend fun getEvidence(id: String): EvidenceItem? = itemsFlow.value.find { it.id == id }
        override suspend fun verify(id: String): EvidenceIntegrity = EvidenceIntegrity.UNCHANGED
        override suspend fun readForDisplay(id: String, maxBytes: Int): DisplayContent = DisplayContent.Unavailable
        override suspend fun importFromUri(uri: android.net.Uri, mimeType: String?): ImportOutcome = ImportOutcome.Failed
        override suspend fun delete(id: String): Boolean = true

        override suspend fun deleteAll(): Int {
            callOrder.add("gateway.deleteAll")
            if (failDeleteAll) throw RuntimeException("Gateway delete failed")
            val count = itemsFlow.value.size
            itemsFlow.value = emptyList()
            return count
        }
    }

    // ---------------------------------------------------------------------------
    // Test Case Repository
    // ---------------------------------------------------------------------------
    private class TestCaseRepository(
        var failDeleteEverything: Boolean = false,
        var throwCaseStoreException: Boolean = false
    ) : CaseRepository {
        val callOrder = mutableListOf<String>()
        val casesFlow = MutableStateFlow<List<Case>>(listOf(Case("case-1", "Title", "Desc", 100L)))

        override fun observeCases(): Flow<List<Case>> = casesFlow.asStateFlow()
        override fun observeItems(caseId: String): Flow<List<CaseItem>> = MutableStateFlow(emptyList())
        override suspend fun createCase(title: String, description: String): Case = throw UnsupportedOperationException()
        override suspend fun renameCase(caseId: String, title: String) {}
        override suspend fun deleteCase(caseId: String) {}
        override suspend fun addEvidenceItem(caseId: String, evidenceId: String, note: String, incidentDateMillis: Long?): CaseItem = throw UnsupportedOperationException()
        override suspend fun addNoteItem(caseId: String, note: String, incidentDateMillis: Long?): CaseItem = throw UnsupportedOperationException()
        override suspend fun updateItem(itemId: String, note: String, incidentDateMillis: Long?) {}
        override suspend fun removeItem(itemId: String) {}
        override suspend fun casesForEvidence(evidenceId: String): List<Case> = emptyList()

        override suspend fun deleteEverything() {
            callOrder.add("caseRepo.deleteEverything")
            if (throwCaseStoreException) throw CaseStoreException(CaseStoreException.Reason.UNREADABLE)
            if (failDeleteEverything) throw RuntimeException("Case store delete failed")
            casesFlow.value = emptyList()
        }
    }

    private fun createDummyEvidenceItem(id: String): EvidenceItem = EvidenceItem(
        id = id,
        hash = "hash_$id",
        receiptTimeEpochMillis = 1000L,
        timeZoneId = "UTC",
        sourceLabel = "Source",
        mimeType = "image/png",
        fileSizeBytes = 2048L,
        filePath = "/path/$id"
    )

    // ---------------------------------------------------------------------------
    // Tests
    // ---------------------------------------------------------------------------

    @Test
    fun `evidence count reflects gateway item count`() = runTest {
        val gateway = TestGateway(listOf(createDummyEvidenceItem("1"), createDummyEvidenceItem("2")))
        val caseRepo = TestCaseRepository()
        val session = VerificationSession()
        val vm = SettingsViewModel(gateway, caseRepo, session)

        val counts = mutableListOf<Int>()
        val job = launch(mainDispatcherRule.testDispatcher) {
            vm.evidenceCount.toList(counts)
        }
        advanceUntilIdle()

        assertEquals(2, counts.last())

        gateway.itemsFlow.value = emptyList()
        advanceUntilIdle()

        assertEquals(0, counts.last())
        job.cancel()
    }

    @Test
    fun `deleteAll empties fake gateway, fake case repository and session in order`() = runTest {
        val gateway = TestGateway(listOf(createDummyEvidenceItem("1")))
        val caseRepo = TestCaseRepository()
        val session = VerificationSession()
        session.record("1", EvidenceIntegrity.UNCHANGED, 100L)

        val vm = SettingsViewModel(gateway, caseRepo, session)

        val results = mutableListOf<DeleteAllResult>()
        val job = launch(mainDispatcherRule.testDispatcher) {
            vm.deleteAllResult.toList(results)
        }

        vm.deleteAll()
        advanceUntilIdle()

        // Verify order: gateway first, then caseRepo, then session
        assertTrue("Gateway deleteAll should be called", gateway.callOrder.contains("gateway.deleteAll"))
        assertTrue("CaseRepo deleteEverything should be called", caseRepo.callOrder.contains("caseRepo.deleteEverything"))

        assertEquals(0, gateway.itemsFlow.value.size)
        assertEquals(0, caseRepo.casesFlow.value.size)
        assertTrue("Session should be empty", session.records.value.isEmpty())
        assertEquals(listOf(DeleteAllResult.Success), results)

        job.cancel()
    }

    @Test
    fun `deleteAll reports GatewayFailed when gateway throws, still attempts case repository delete`() = runTest {
        val gateway = TestGateway(listOf(createDummyEvidenceItem("1")), failDeleteAll = true)
        val caseRepo = TestCaseRepository()
        val session = VerificationSession()
        val vm = SettingsViewModel(gateway, caseRepo, session)

        val results = mutableListOf<DeleteAllResult>()
        val job = launch(mainDispatcherRule.testDispatcher) {
            vm.deleteAllResult.toList(results)
        }

        vm.deleteAll()
        advanceUntilIdle()

        assertEquals(listOf(DeleteAllResult.GatewayFailed), results)
        assertTrue("CaseRepo deleteEverything should still be attempted", caseRepo.callOrder.contains("caseRepo.deleteEverything"))

        job.cancel()
    }

    @Test
    fun `deleteAll reports CaseStoreFailed when case repository throws CaseStoreException`() = runTest {
        val gateway = TestGateway(listOf(createDummyEvidenceItem("1")))
        val caseRepo = TestCaseRepository(throwCaseStoreException = true)
        val session = VerificationSession()
        val vm = SettingsViewModel(gateway, caseRepo, session)

        val results = mutableListOf<DeleteAllResult>()
        val job = launch(mainDispatcherRule.testDispatcher) {
            vm.deleteAllResult.toList(results)
        }

        vm.deleteAll()
        advanceUntilIdle()

        assertEquals(listOf(DeleteAllResult.CaseStoreFailed), results)
        assertEquals(0, gateway.itemsFlow.value.size)
        assertTrue("Session should still be cleared", session.records.value.isEmpty())

        job.cancel()
    }

    @Test
    fun `shouldSecure pure logic correctly reflects setting and demo mode`() {
        assertTrue("Enabled without demo should secure", shouldSecure(settingEnabled = true, demoModeActive = false))
        assertFalse("Disabled without demo should not secure", shouldSecure(settingEnabled = false, demoModeActive = false))
        assertFalse("Enabled with demo active should not secure", shouldSecure(settingEnabled = true, demoModeActive = true))
        assertFalse("Disabled with demo active should not secure", shouldSecure(settingEnabled = false, demoModeActive = true))
    }
}
