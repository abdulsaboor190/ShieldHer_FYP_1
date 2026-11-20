package com.shieldher.dashboard.cases

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID
import com.shieldher.dashboard.data.Case
import com.shieldher.dashboard.data.CaseItem
import kotlinx.coroutines.runBlocking

@RunWith(AndroidJUnit4::class)
class CaseStoreTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    /** Unique directory per test run so tests never share state. */
    private val testDir: File by lazy {
        File(context.filesDir, "case_store_test_" + UUID.randomUUID().toString()).also {
            val canonical = it.canonicalPath
            check(canonical.startsWith(context.filesDir.canonicalPath)) {
                "Test dir escaped app files dir"
            }
        }
    }

    private fun freshStore(fileName: String = "cases.enc") = CaseStore(context, testDir, fileName)

    @Before fun setUp() {
        testDir.mkdirs()
    }

    @After fun tearDown() {
        testDir.deleteRecursively()
    }

    // ── Round-trip ────────────────────────────────────────────────────────────

    @Test
    fun roundTrip_allFieldsPreserved() = runBlocking {
        val store = freshStore()
        store.load()

        val case1 = Case(
            id = "c1",
            title = "Case One",
            description = "Some description",
            createdAtMillis = 1794500000000L,
            isDemo = true
        )
        val item1 = CaseItem(
            id = "i1",
            caseId = "c1",
            evidenceId = "ev-1",
            note = "Note text",
            incidentDateMillis = null,
            addedAtMillis = 1794500001000L,
            isDemo = false
        )
        store.saveCases(listOf(case1), listOf(item1))

        // Load fresh instance from same file.
        val store2 = freshStore()
        store2.load()

        val cases2 = store2.cases.value
        val items2 = store2.items.value
        assertEquals(1, cases2.size)
        assertEquals(case1, cases2[0])
        assertEquals(1, items2.size)
        assertEquals(item1, items2[0])
    }

    @Test
    fun roundTrip_nullIncidentDateAndNullEvidenceId_preserved() = runBlocking {
        val store = freshStore()
        store.load()

        val item = CaseItem(
            id = "i2", caseId = "c1", evidenceId = null, note = "plain note",
            incidentDateMillis = null, addedAtMillis = 100L
        )
        store.saveCases(
            listOf(Case("c1", "T", "D", 1L)),
            listOf(item)
        )

        val store2 = freshStore()
        store2.load()
        val loaded = store2.items.value[0]
        assertNull(loaded.evidenceId)
        assertNull(loaded.incidentDateMillis)
    }

    // ── Encryption proof ──────────────────────────────────────────────────────

    @Test
    fun storedFile_doesNotContainPlaintextMarker() = runBlocking {
        val store = freshStore()
        store.load()

        val marker = "PLAINTEXT-CHECK-MARKER"
        store.saveCases(
            listOf(Case("c1", marker, "desc", 1L)),
            emptyList()
        )

        val encFile = File(testDir, "cases.enc")
        assertTrue("Encrypted file must exist", encFile.exists())
        val rawBytes = encFile.readBytes()
        assertFalse(
            "Raw bytes must NOT contain the plaintext marker",
            rawBytes.toString(Charsets.ISO_8859_1).contains(marker)
        )
    }

    // ── Corrupt file → UNREADABLE ─────────────────────────────────────────────

    @Test
    fun corruptFile_entersUnreadableState_fileUnchanged() = runBlocking {
        // Write a valid store first.
        val store = freshStore()
        store.load()
        store.saveCases(listOf(Case("c1", "T", "D", 1L)), emptyList())

        val encFile = File(testDir, "cases.enc")
        val originalBytes = encFile.readBytes()

        // Overwrite with random bytes.
        encFile.writeBytes(ByteArray(256) { it.toByte() })
        val corruptBytes = encFile.readBytes()

        // Fresh load.
        val store2 = freshStore()
        store2.load()
        assertTrue("Store should be UNREADABLE", store2.isUnreadable)

        // File bytes must be unchanged after load.
        assertTrue(
            "Corrupt file must not be modified by load",
            corruptBytes.contentEquals(encFile.readBytes())
        )

        // Write attempt must throw and NOT reset the file.
        var threw = false
        try {
            store2.saveCases(emptyList(), emptyList())
        } catch (e: CaseStoreException) {
            threw = true
        }
        assertTrue("Write to UNREADABLE store must throw", threw)
        assertTrue(
            "File bytes must still be the corrupt bytes after failed write",
            corruptBytes.contentEquals(encFile.readBytes())
        )
    }

    // ── Atomic write / crash safety ───────────────────────────────────────────

    @Test
    fun midWriteCrash_previousContentStillReadable() = runBlocking {
        val store = freshStore()
        store.load()
        store.saveCases(listOf(Case("c1", "Before crash", "D", 1L)), emptyList())

        // Inject crash hook: throw after tmp is written.
        store.afterTmpWrittenHook = { throw RuntimeException("simulated crash") }

        var threw = false
        try {
            store.saveCases(listOf(Case("c2", "After crash", "D", 2L)), emptyList())
        } catch (_: RuntimeException) {
            threw = true
        }
        assertTrue("Hook should have thrown", threw)

        // Load again — previous content must be intact.
        val store2 = freshStore()
        store2.load()
        assertFalse("Store must not be UNREADABLE after recovery", store2.isUnreadable)
        val titles = store2.cases.value.map { it.title }
        assertTrue("Should still see 'Before crash' case", titles.contains("Before crash"))
    }

    @Test
    fun staleTmp_ignoredOnLoad_doesNotBreakLoading() = runBlocking {
        val store = freshStore()
        store.load()
        store.saveCases(listOf(Case("c1", "Real", "D", 1L)), emptyList())

        // Write garbage into the .tmp file to simulate a stale/corrupt tmp.
        val tmp = File(testDir, "cases.enc.tmp")
        tmp.writeBytes(ByteArray(32) { 0xFF.toByte() })

        val store2 = freshStore()
        store2.load()
        assertFalse("Store must not be UNREADABLE with stale tmp", store2.isUnreadable)
        assertEquals(1, store2.cases.value.size)
        assertFalse("Stale corrupt tmp must be deleted", tmp.exists())
    }

    @Test
    fun missingRealFile_validBakRestored() = runBlocking {
        val store = freshStore()
        store.load()
        store.saveCases(listOf(Case("c1", "Backup title", "D", 1L)), emptyList())

        // Rename real file to .bak manually to simulate missing real file.
        val real = File(testDir, "cases.enc")
        val bak = File(testDir, "cases.enc.bak")
        real.renameTo(bak)
        assertFalse(real.exists())
        assertTrue(bak.exists())

        val store2 = freshStore()
        store2.load()
        assertFalse("Store must not be UNREADABLE when .bak is restored", store2.isUnreadable)
        assertEquals("Backup title", store2.cases.value[0].title)
    }
}
