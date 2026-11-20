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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

@RunWith(AndroidJUnit4::class)
class DefaultCaseRepositoryTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    private val testDir: File by lazy {
        File(context.filesDir, "case_repo_test_" + UUID.randomUUID().toString()).also {
            val canonical = it.canonicalPath
            check(canonical.startsWith(context.filesDir.canonicalPath)) {
                "Test dir escaped app files dir"
            }
        }
    }

    private lateinit var store: CaseStore
    private lateinit var repo: DefaultCaseRepository

    @Before fun setUp() = runBlocking {
        testDir.mkdirs()
        store = CaseStore(context, testDir)
        store.load()
        repo = DefaultCaseRepository(store)
    }

    @After fun tearDown() {
        testDir.deleteRecursively()
    }

    // ── Case CRUD ─────────────────────────────────────────────────────────────

    @Test
    fun createCase_appearsInObserveCases() = runBlocking {
        val case = repo.createCase("My Case", "Description here")
        assertNotNull(case.id)
        assertTrue(case.createdAtMillis > 0)

        val list = repo.observeCases().first()
        assertTrue(list.any { it.id == case.id })
    }

    @Test
    fun renameCase_updatesTitle() = runBlocking {
        val case = repo.createCase("Old title", "")
        repo.renameCase(case.id, "New title")

        val updated = repo.observeCases().first().find { it.id == case.id }
        assertEquals("New title", updated?.title)
    }

    @Test
    fun deleteCase_removesCaseAndItsItems() = runBlocking {
        val case = repo.createCase("To delete", "")
        val item = repo.addNoteItem(case.id, "note", null)

        repo.deleteCase(case.id)

        val cases = repo.observeCases().first()
        assertFalse(cases.any { it.id == case.id })

        val items = repo.observeItems(case.id).first()
        assertFalse(items.any { it.id == item.id })
    }

    @Test
    fun deleteCase_doesNotRemoveOtherCases() = runBlocking {
        val c1 = repo.createCase("Keep", "")
        val c2 = repo.createCase("Delete", "")
        repo.addNoteItem(c1.id, "keep note", null)

        repo.deleteCase(c2.id)

        val cases = repo.observeCases().first()
        assertTrue(cases.any { it.id == c1.id })
        val items = repo.observeItems(c1.id).first()
        assertEquals(1, items.size)
    }

    // ── Item CRUD ─────────────────────────────────────────────────────────────

    @Test
    fun addEvidenceItem_andNoteItem_appear() = runBlocking {
        val case = repo.createCase("C", "")
        val evItem = repo.addEvidenceItem(case.id, "ev-123", "see this", 1000L)
        val noteItem = repo.addNoteItem(case.id, "just a note", null)

        val items = repo.observeItems(case.id).first()
        assertEquals(2, items.size)

        val ev = items.find { it.id == evItem.id }
        assertNotNull(ev)
        assertEquals("ev-123", ev?.evidenceId)
        assertEquals(1000L, ev?.incidentDateMillis)

        val note = items.find { it.id == noteItem.id }
        assertNotNull(note)
        assertNull(note?.evidenceId)
        assertNull(note?.incidentDateMillis)
    }

    @Test
    fun updateItem_changesNoteAndDate() = runBlocking {
        val case = repo.createCase("C", "")
        val item = repo.addNoteItem(case.id, "original", null)

        repo.updateItem(item.id, "updated", 9999L)

        val updated = repo.observeItems(case.id).first().find { it.id == item.id }
        assertEquals("updated", updated?.note)
        assertEquals(9999L, updated?.incidentDateMillis)
    }

    @Test
    fun removeItem_removesOnlyThatItem() = runBlocking {
        val case = repo.createCase("C", "")
        val a = repo.addNoteItem(case.id, "a", null)
        val b = repo.addNoteItem(case.id, "b", null)

        repo.removeItem(a.id)

        val items = repo.observeItems(case.id).first()
        assertFalse(items.any { it.id == a.id })
        assertTrue(items.any { it.id == b.id })
    }

    @Test
    fun removeItem_doesNotTouchEvidence() = runBlocking {
        // DefaultCaseRepository has no dependency on the vault; this test
        // verifies that by confirming removeItem only changes the items list.
        val case = repo.createCase("C", "")
        val item = repo.addEvidenceItem(case.id, "ev-999", "note", null)
        repo.removeItem(item.id)

        // The item is gone but the cases list is unchanged.
        val cases = repo.observeCases().first()
        assertTrue(cases.any { it.id == case.id })
        val items = repo.observeItems(case.id).first()
        assertFalse(items.any { it.id == item.id })
    }

    // ── casesForEvidence ──────────────────────────────────────────────────────

    @Test
    fun casesForEvidence_findsCasesContainingEvidenceId() = runBlocking {
        val c1 = repo.createCase("Has evidence", "")
        val c2 = repo.createCase("No evidence", "")
        repo.addEvidenceItem(c1.id, "ev-abc", "note", null)
        repo.addNoteItem(c2.id, "plain note", null)

        val found = repo.casesForEvidence("ev-abc")
        assertEquals(1, found.size)
        assertEquals(c1.id, found[0].id)
    }

    @Test
    fun casesForEvidence_returnsEmpty_whenNoneMatch() = runBlocking {
        repo.createCase("C", "")
        assertTrue(repo.casesForEvidence("nonexistent-ev").isEmpty())
    }

    // ── deleteEverything ──────────────────────────────────────────────────────

    @Test
    fun deleteEverything_removesAllAndFile() = runBlocking {
        repo.createCase("C", "")
        repo.deleteEverything()

        assertTrue(repo.observeCases().first().isEmpty())
        // All three file variants must be gone.
        assertFalse(File(testDir, "cases.enc").exists())
        assertFalse(File(testDir, "cases.enc.tmp").exists())
        assertFalse(File(testDir, "cases.enc.bak").exists())
    }
}
