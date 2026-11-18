package com.shieldher.dashboard.data

import com.shieldher.dashboard.data.fake.FakeCaseRepository
import com.shieldher.dashboard.testing.MainDispatcherRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class FakeCaseRepositoryTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repo = FakeCaseRepository()

    @Test
    fun observeCases_returnsSyntheticCases() = runTest {
        val list = repo.observeCases().first()
        assertEquals(2, list.size)
    }

    @Test
    fun createCase_addsCaseToList() = runTest {
        val created = repo.createCase("New Case", "New Description")
        assertNotNull(created.id)
        val cases = repo.observeCases().first()
        assertTrue(cases.any { it.id == created.id && it.title == "New Case" })
    }

    @Test
    fun renameCase_updatesTitle() = runTest {
        repo.renameCase("synth-case-1", "Renamed Incident")
        val cases = repo.observeCases().first()
        val c = cases.find { it.id == "synth-case-1" }
        assertEquals("Renamed Incident", c?.title)
    }

    @Test
    fun deleteCase_removesCaseAndItsItems() = runTest {
        repo.deleteCase("synth-case-1")
        val cases = repo.observeCases().first()
        assertTrue(cases.none { it.id == "synth-case-1" })

        val items = repo.observeItems("synth-case-1").first()
        assertTrue(items.isEmpty())
    }

    @Test
    fun addEvidenceItem_and_addNoteItem_workCorrectly() = runTest {
        val evItem = repo.addEvidenceItem("synth-case-2", "ev-100", "A note", 1000L)
        assertEquals("ev-100", evItem.evidenceId)

        val noteItem = repo.addNoteItem("synth-case-2", "A pure note", 2000L)
        assertEquals(null, noteItem.evidenceId)

        val items = repo.observeItems("synth-case-2").first()
        assertTrue(items.any { it.id == evItem.id })
        assertTrue(items.any { it.id == noteItem.id })
    }

    @Test
    fun updateItem_and_removeItem_workCorrectly() = runTest {
        repo.updateItem("synth-item-1", "Updated note text", 5555L)
        val items = repo.observeItems("synth-case-1").first()
        val updated = items.find { it.id == "synth-item-1" }
        assertEquals("Updated note text", updated?.note)
        assertEquals(5555L, updated?.incidentDateMillis)

        repo.removeItem("synth-item-1")
        val afterRemove = repo.observeItems("synth-case-1").first()
        assertTrue(afterRemove.none { it.id == "synth-item-1" })
    }

    @Test
    fun casesForEvidence_findsCasesContainingEvidenceId() = runTest {
        val matching = repo.casesForEvidence("synth-evidence-1")
        assertEquals(1, matching.size)
        assertEquals("synth-case-1", matching.first().id)
    }

    @Test
    fun deleteEverything_clearsAllCasesAndItems() = runTest {
        repo.deleteEverything()
        assertEquals(0, repo.observeCases().first().size)
        assertEquals(0, repo.observeItems("synth-case-1").first().size)
    }
}
