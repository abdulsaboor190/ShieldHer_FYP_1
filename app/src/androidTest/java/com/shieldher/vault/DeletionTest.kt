package com.shieldher.vault

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.shieldher.vault.data.EvidenceDao
import com.shieldher.vault.data.EvidenceDatabase
import com.shieldher.vault.data.EvidenceEntity
import com.shieldher.vault.data.EvidenceRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayInputStream
import java.io.File
import java.util.UUID

/**
 * Instrumented tests for EvidenceRepository deletion operations.
 *
 * Uses an isolated directory ("evidence_test_deletion") and an in-memory
 * Room database. Never touches the production "evidence" directory.
 */
@RunWith(AndroidJUnit4::class)
class DeletionTest {

    private lateinit var context: Context
    private lateinit var db: EvidenceDatabase
    private lateinit var dao: EvidenceDao
    private lateinit var repo: EvidenceRepository
    private lateinit var testDir: File

    private val createdCacheFiles = mutableListOf<File>()

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()

        // Build hermetic in-memory database
        db = Room.inMemoryDatabaseBuilder(context, EvidenceDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        EvidenceDatabase.setInstanceForTesting(db)
        dao = db.evidenceDao()

        // Inject isolated directory name for tests
        repo = EvidenceRepository(context, evidenceDirName = "evidence_test_deletion")

        testDir = File(context.filesDir, "evidence_test_deletion").also { it.mkdirs() }

        // Hard guarantee: test directory must not resolve to the real evidence directory
        check(testDir.canonicalPath != File(context.filesDir, "evidence").canonicalPath) {
            "Test isolation broken: testDir resolved to the real evidence directory"
        }
    }

    @After
    fun tearDown() {
        // Clean up created cache files
        for (cacheFile in createdCacheFiles) {
            if (cacheFile.exists()) {
                cacheFile.delete()
            }
        }
        createdCacheFiles.clear()

        // Clean up isolated test files and reset database singleton
        testDir.deleteRecursively()
        db.close()
        EvidenceDatabase.clearInstanceForTesting()
    }

    @Test
    fun delete_removesFileAndRow() = runBlocking {
        val payload = "Single item deletion test".toByteArray(Charsets.UTF_8)
        val entity = repo.intake(ByteArrayInputStream(payload), "text/plain", "source")

        val storedFile = File(testDir, entity.id)
        assertTrue("Stored file must exist before deletion", storedFile.exists())
        assertNotNull("Database row must exist before deletion", dao.getById(entity.id))

        val deleted = repo.delete(entity.id)
        assertTrue("delete() must return true for existing evidence", deleted)
        assertFalse("Stored file must be removed from disk", storedFile.exists())
        assertNull("Database row must be removed from database", dao.getById(entity.id))
    }

    @Test
    fun delete_cascadesToDerivedArtifacts() = runBlocking {
        // 1. Intake original item
        val original = repo.intake(
            ByteArrayInputStream("Original evidence".toByteArray(Charsets.UTF_8)),
            "image/jpeg",
            "camera"
        )
        val originalFile = File(testDir, original.id)
        assertTrue(originalFile.exists())

        // 2. Insert two derived items directly with parentId = original.id
        val derived1Id = UUID.randomUUID().toString()
        val derived1File = File(testDir, derived1Id).also { it.writeText("derived1-content") }
        val derived1Entity = EvidenceEntity(
            id = derived1Id,
            hash = "hash-derived1",
            receiptTimeEpochMillis = System.currentTimeMillis(),
            timeZoneId = "UTC",
            mimeType = "image/jpeg",
            fileSizeBytes = derived1File.length(),
            filePath = derived1File.absolutePath,
            parentId = original.id,
            transformationType = "crop",
            deleted = false
        )
        dao.insert(derived1Entity)

        val derived2Id = UUID.randomUUID().toString()
        val derived2File = File(testDir, derived2Id).also { it.writeText("derived2-content") }
        val derived2Entity = EvidenceEntity(
            id = derived2Id,
            hash = "hash-derived2",
            receiptTimeEpochMillis = System.currentTimeMillis(),
            timeZoneId = "UTC",
            mimeType = "application/pdf",
            fileSizeBytes = derived2File.length(),
            filePath = derived2File.absolutePath,
            parentId = original.id,
            transformationType = "report",
            deleted = false
        )
        dao.insert(derived2Entity)

        // 3. Insert one grandchild whose parentId = derived1.id
        val grandchildId = UUID.randomUUID().toString()
        val grandchildFile = File(testDir, grandchildId).also { it.writeText("grandchild-content") }
        val grandchildEntity = EvidenceEntity(
            id = grandchildId,
            hash = "hash-grandchild",
            receiptTimeEpochMillis = System.currentTimeMillis(),
            timeZoneId = "UTC",
            mimeType = "image/png",
            fileSizeBytes = grandchildFile.length(),
            filePath = grandchildFile.absolutePath,
            parentId = derived1Id,
            transformationType = "watermark",
            deleted = false
        )
        dao.insert(grandchildEntity)

        // Verify initial state
        assertNotNull(dao.getById(original.id))
        assertNotNull(dao.getById(derived1Id))
        assertNotNull(dao.getById(derived2Id))
        assertNotNull(dao.getById(grandchildId))

        // Act: Delete original item
        val deleted = repo.delete(original.id)
        assertTrue("delete(original.id) must return true", deleted)

        // Assert: Database rows are all removed
        assertNull("Original row must be deleted", dao.getById(original.id))
        assertNull("Derived 1 row must be cascaded and deleted", dao.getById(derived1Id))
        assertNull("Derived 2 row must be cascaded and deleted", dao.getById(derived2Id))
        assertNull("Grandchild row must be cascaded and deleted", dao.getById(grandchildId))

        // Assert: All files are removed from disk
        assertFalse("Original file must be removed", originalFile.exists())
        assertFalse("Derived 1 file must be removed", derived1File.exists())
        assertFalse("Derived 2 file must be removed", derived2File.exists())
        assertFalse("Grandchild file must be removed", grandchildFile.exists())
    }

    @Test
    fun delete_unknownIdReturnsFalse() = runBlocking {
        // Intake a baseline file to ensure DB has data
        val baseline = repo.intake(
            ByteArrayInputStream("Baseline item".toByteArray(Charsets.UTF_8)),
            "text/plain",
            "source"
        )
        val baselineFile = File(testDir, baseline.id)

        val unknownId = UUID.randomUUID().toString()
        val deleted = repo.delete(unknownId)

        assertFalse("delete() on unknown ID must return false", deleted)
        assertNotNull("Baseline item must remain in DB", dao.getById(baseline.id))
        assertTrue("Baseline file must remain on disk", baselineFile.exists())
    }

    @Test
    fun delete_whenFileAlreadyMissing_stillRemovesRow() = runBlocking {
        val payload = "Missing file deletion test".toByteArray(Charsets.UTF_8)
        val entity = repo.intake(ByteArrayInputStream(payload), "text/plain", "source")

        val storedFile = File(testDir, entity.id)
        assertTrue(storedFile.exists())
        storedFile.delete()
        assertFalse("File deleted from disk manually", storedFile.exists())

        val deleted = repo.delete(entity.id)
        assertTrue("delete() must return true even if file was already missing", deleted)
        assertNull("Database row must be removed", dao.getById(entity.id))
    }

    @Test
    fun delete_doesNotAffectOtherEvidence() = runBlocking {
        val itemA = repo.intake(
            ByteArrayInputStream("Item A content".toByteArray(Charsets.UTF_8)),
            "text/plain",
            "sourceA"
        )
        val itemB = repo.intake(
            ByteArrayInputStream("Item B content".toByteArray(Charsets.UTF_8)),
            "text/plain",
            "sourceB"
        )

        val fileA = File(testDir, itemA.id)
        val fileB = File(testDir, itemB.id)
        assertTrue(fileA.exists())
        assertTrue(fileB.exists())

        val deletedA = repo.delete(itemA.id)
        assertTrue("delete(itemA) must succeed", deletedA)

        // Item A gone
        assertFalse(fileA.exists())
        assertNull(dao.getById(itemA.id))

        // Item B untouched
        assertTrue("Item B file must remain on disk", fileB.exists())
        assertNotNull("Item B row must remain in database", dao.getById(itemB.id))
        assertTrue("Item B integrity must still verify as true", repo.verifyIntegrity(itemB.id))
    }

    @Test
    fun clearCacheFor_removesMatchingCacheFiles() {
        val itemId = UUID.randomUUID().toString()
        val cacheDir = context.cacheDir

        val thumbFile = File(cacheDir, "${itemId}.thumb").also {
            it.writeText("thumbnail-data")
            createdCacheFiles.add(it)
        }
        val exportFile = File(cacheDir, "${itemId}_export.tmp").also {
            it.writeText("export-data")
            createdCacheFiles.add(it)
        }
        val unrelatedFile = File(cacheDir, "unrelated_${UUID.randomUUID()}.tmp").also {
            it.writeText("unrelated-data")
            createdCacheFiles.add(it)
        }

        assertTrue(thumbFile.exists())
        assertTrue(exportFile.exists())
        assertTrue(unrelatedFile.exists())

        // Act: Invoke clearCacheFor
        repo.clearCacheFor(itemId)

        // Assert: Matching cache files are deleted, unrelated is preserved
        assertFalse("Matching .thumb file must be deleted", thumbFile.exists())
        assertFalse("Matching _export.tmp file must be deleted", exportFile.exists())
        assertTrue("Unrelated cache file must remain untouched", unrelatedFile.exists())
    }
}
