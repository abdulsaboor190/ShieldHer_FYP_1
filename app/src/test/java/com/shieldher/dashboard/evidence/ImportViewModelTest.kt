package com.shieldher.dashboard.evidence

import android.net.TestUriFactory
import android.net.Uri
import com.shieldher.dashboard.data.DisplayContent
import com.shieldher.dashboard.data.EvidenceGateway
import com.shieldher.dashboard.data.EvidenceIntegrity
import com.shieldher.dashboard.data.EvidenceItem
import com.shieldher.dashboard.data.ImportOutcome
import com.shieldher.dashboard.testing.MainDispatcherRule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class ImportViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // ---------------------------------------------------------------------------
    // Minimal test gateway that returns whatever outcome we tell it to.
    // ---------------------------------------------------------------------------
    private class ControlledGateway(private var outcome: ImportOutcome) : EvidenceGateway {
        private val _items = MutableStateFlow<List<EvidenceItem>>(emptyList())

        override fun observeEvidence(): Flow<List<EvidenceItem>> = _items.asStateFlow()
        override suspend fun getEvidence(id: String): EvidenceItem? = _items.value.find { it.id == id }
        override suspend fun verify(id: String): EvidenceIntegrity = EvidenceIntegrity.UNCHANGED
        override suspend fun readForDisplay(id: String, maxBytes: Int): DisplayContent = DisplayContent.Unavailable

        override suspend fun importFromUri(uri: Uri, mimeType: String?): ImportOutcome {
            if (outcome is ImportOutcome.Success) {
                val id = (outcome as ImportOutcome.Success).id
                val item = EvidenceItem(
                    id = id,
                    hash = "testhash_$id",
                    receiptTimeEpochMillis = System.currentTimeMillis(),
                    timeZoneId = "UTC",
                    sourceLabel = "In-app import",
                    mimeType = mimeType ?: "image/jpeg",
                    fileSizeBytes = 1024L,
                    filePath = "/fake/$id"
                )
                _items.value = _items.value + item
            }
            return outcome
        }

        override suspend fun delete(id: String): Boolean {
            val before = _items.value
            _items.value = before.filterNot { it.id == id }
            return _items.value.size != before.size
        }

        override suspend fun deleteAll(): Int {
            val count = _items.value.size
            _items.value = emptyList()
            return count
        }

        /** Expose items for assertions. */
        fun items(): List<EvidenceItem> = _items.value
    }

    private val dummyUri: Uri = TestUriFactory.create()

    // ---------------------------------------------------------------------------
    // Tests
    // ---------------------------------------------------------------------------

    @Test
    fun `initial state is Idle`() = runTest {
        val gateway = ControlledGateway(ImportOutcome.Success("new-1"))
        val vm = ImportViewModel(gateway)
        assertEquals(ImportScreenState.Idle, vm.state.first())
    }

    @Test
    fun `Success outcome gives Saved state with correct id`() = runTest {
        val gateway = ControlledGateway(ImportOutcome.Success("evidence-abc"))
        val vm = ImportViewModel(gateway)

        vm.onUriSelected(dummyUri, "image/jpeg")
        advanceUntilIdle()

        val state = vm.state.first()
        assertTrue("Expected Saved state, got $state", state is ImportScreenState.Saved)
        assertEquals("evidence-abc", (state as ImportScreenState.Saved).id)
    }

    @Test
    fun `Success outcome adds item to gateway`() = runTest {
        val gateway = ControlledGateway(ImportOutcome.Success("evidence-xyz"))
        val vm = ImportViewModel(gateway)

        vm.onUriSelected(dummyUri, "image/png")
        advanceUntilIdle()

        val items = gateway.items()
        assertTrue("Gateway should contain the new item", items.any { it.id == "evidence-xyz" })
    }

    @Test
    fun `TooLarge outcome gives TooLarge state`() = runTest {
        val gateway = ControlledGateway(ImportOutcome.TooLarge)
        val vm = ImportViewModel(gateway)

        vm.onUriSelected(dummyUri, "image/jpeg")
        advanceUntilIdle()

        assertEquals(ImportScreenState.TooLarge, vm.state.first())
    }

    @Test
    fun `Unsupported outcome gives UnsupportedType state`() = runTest {
        val gateway = ControlledGateway(ImportOutcome.Unsupported)
        val vm = ImportViewModel(gateway)

        vm.onUriSelected(dummyUri, "application/pdf")
        advanceUntilIdle()

        assertEquals(ImportScreenState.UnsupportedType, vm.state.first())
    }

    @Test
    fun `Failed outcome gives Failed state`() = runTest {
        val gateway = ControlledGateway(ImportOutcome.Failed)
        val vm = ImportViewModel(gateway)

        vm.onUriSelected(dummyUri, null)
        advanceUntilIdle()

        assertEquals(ImportScreenState.Failed, vm.state.first())
    }

    @Test
    fun `cancelled picker leaves Idle state unchanged`() = runTest {
        val gateway = ControlledGateway(ImportOutcome.Success("ignored"))
        val vm = ImportViewModel(gateway)

        // State is Idle initially; cancelled picker should keep it Idle.
        vm.onPickerCancelled()
        advanceUntilIdle()

        assertEquals(ImportScreenState.Idle, vm.state.first())
    }

    @Test
    fun `resetToIdle after Saved returns to Idle`() = runTest {
        val gateway = ControlledGateway(ImportOutcome.Success("evidence-reset"))
        val vm = ImportViewModel(gateway)

        vm.onUriSelected(dummyUri, "image/jpeg")
        advanceUntilIdle()

        // Confirm we are Saved
        assertTrue(vm.state.first() is ImportScreenState.Saved)

        vm.resetToIdle()
        advanceUntilIdle()

        assertEquals(ImportScreenState.Idle, vm.state.first())
    }

    @Test
    fun `resetToIdle after Failed returns to Idle`() = runTest {
        val gateway = ControlledGateway(ImportOutcome.Failed)
        val vm = ImportViewModel(gateway)

        vm.onUriSelected(dummyUri, null)
        advanceUntilIdle()

        assertEquals(ImportScreenState.Failed, vm.state.first())

        vm.resetToIdle()
        advanceUntilIdle()

        assertEquals(ImportScreenState.Idle, vm.state.first())
    }
}
