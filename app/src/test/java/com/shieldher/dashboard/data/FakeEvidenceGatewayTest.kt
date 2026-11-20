package com.shieldher.dashboard.data

import android.net.TestUri
import android.net.Uri
import com.shieldher.dashboard.data.fake.FakeEvidenceGateway
import com.shieldher.dashboard.testing.MainDispatcherRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class FakeEvidenceGatewayTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val gateway = FakeEvidenceGateway()

    companion object {
        /**
         * Concrete Uri subclass for JVM unit tests.
         * Lives in src/test/java/android/net/TestUri.java (same package as Uri),
         * which allows calling the package-private super() constructor.
         * Uri.<init>() initialises 'this' via Object.<init>() then throws
         * RuntimeException("Stub!"); TestUri's constructor catches that throw so
         * the instance is returned cleanly.  FakeEvidenceGateway.importFromUri
         * never calls any method on the uri parameter.
         */
        private val testUri: Uri = TestUri.create()
    }

    @Test
    fun observeEvidence_initiallyEmitsSyntheticItems() = runTest {
        val list = gateway.observeEvidence().first()
        assertEquals(6, list.size)
        assertTrue(list.any { it.id == "synth-evidence-1" })
    }

    @Test
    fun verify_returnsAllThreeOutcomesForDifferentIds() = runTest {
        assertEquals(EvidenceIntegrity.UNCHANGED, gateway.verify("synth-evidence-1"))
        assertEquals(EvidenceIntegrity.CHANGED, gateway.verify("synth-evidence-2"))
        assertEquals(EvidenceIntegrity.MISSING, gateway.verify("synth-evidence-5"))
    }

    @Test
    fun readForDisplay_returnsTextForTextMimeAndUnavailableForOthers() = runTest {
        val textResult = gateway.readForDisplay("synth-evidence-2", 1024)
        assertTrue(textResult is DisplayContent.Text)

        val imageResult = gateway.readForDisplay("synth-evidence-1", 1024)
        assertEquals(DisplayContent.Unavailable, imageResult)
    }

    @Test
    fun importFromUri_addsItemAndReturnsSuccess() = runTest {
        val outcome = gateway.importFromUri(testUri, "image/png")
        assertTrue(outcome is ImportOutcome.Success)
        val newId = (outcome as ImportOutcome.Success).id

        val item = gateway.getEvidence(newId)
        assertNotNull(item)
        assertEquals("In-app import", item?.sourceLabel)
    }

    @Test
    fun delete_removesItemAndReturnsTrue() = runTest {
        assertTrue(gateway.delete("synth-evidence-1"))
        assertFalse(gateway.delete("synth-evidence-1"))

        val items = gateway.observeEvidence().first()
        assertEquals(5, items.size)
    }

    @Test
    fun deleteAll_clearsAllItems() = runTest {
        val deletedCount = gateway.deleteAll()
        assertEquals(6, deletedCount)
        assertEquals(0, gateway.observeEvidence().first().size)
    }
}
