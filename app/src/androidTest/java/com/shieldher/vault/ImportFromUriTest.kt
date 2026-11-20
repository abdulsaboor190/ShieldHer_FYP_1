package com.shieldher.vault

import android.content.Context
import android.net.Uri
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.shieldher.vault.data.EvidenceDatabase
import com.shieldher.vault.data.EvidenceRepository
import com.shieldher.vault.data.ImportResult
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class ImportFromUriTest {

    private lateinit var context: Context
    private lateinit var db: EvidenceDatabase
    private lateinit var repo: EvidenceRepository
    private lateinit var testDir: File

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, EvidenceDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        EvidenceDatabase.setInstanceForTesting(db)

        repo = EvidenceRepository(context, evidenceDirName = "evidence_test_import_uri")
        testDir = File(context.filesDir, "evidence_test_import_uri").also { it.mkdirs() }

        check(testDir.canonicalPath != File(context.filesDir, "evidence").canonicalPath) {
            "Test isolation broken"
        }
    }

    @After
    fun tearDown() {
        testDir.deleteRecursively()
        db.close()
        EvidenceDatabase.clearInstanceForTesting()
    }

    @Test
    fun importFromUri_importsSmallFileSuccessfully() = runBlocking {
        val sampleFile = File(context.cacheDir, "sample_import.txt").also {
            it.writeText("Import me into the vault!")
        }
        val fileUri = Uri.fromFile(sampleFile)

        try {
            val result = repo.importFromUri(fileUri, "text/plain")
            assertTrue(result is ImportResult.Success)
            val newId = (result as ImportResult.Success).id

            val entity = db.evidenceDao().getById(newId)
            assertEquals("In-app import", entity?.sourceLabel)
            assertEquals("text/plain", entity?.mimeType)
        } finally {
            sampleFile.delete()
        }
    }

    @Test
    fun importFromUri_returnsFailed_whenUriCannotBeOpened() = runBlocking {
        val nonExistentFile = File(context.cacheDir, "non_existent_file.txt")
        val fileUri = Uri.fromFile(nonExistentFile)

        val countBefore = db.evidenceDao().getAll().first().size
        val dirFilesBefore = testDir.listFiles()?.size ?: 0

        val result = repo.importFromUri(fileUri, "text/plain")
        assertEquals(ImportResult.Failed, result)

        val countAfter = db.evidenceDao().getAll().first().size
        val dirFilesAfter = testDir.listFiles()?.size ?: 0

        assertEquals(countBefore, countAfter)
        assertEquals(dirFilesBefore, dirFilesAfter)
    }
}
