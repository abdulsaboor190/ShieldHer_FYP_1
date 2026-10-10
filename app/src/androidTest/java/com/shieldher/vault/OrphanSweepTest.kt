package com.shieldher.vault

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.shieldher.vault.data.EvidenceDatabase
import com.shieldher.vault.data.EvidenceEntity
import com.shieldher.vault.data.EvidenceRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID

/**
 * Instrumented test for EvidenceRepository.sweepOrphans().
 *
 * The repository is constructed with evidenceDirName = "evidence_test".
 * EncryptedFileStore is therefore bound to filesDir/evidence_test, so
 * sweepOrphans() can only see and delete files in that directory.
 * The real "evidence" directory is never opened, listed, or touched.
 *
 * Three files are planted in evidence_test before each run:
 *
 *   A) orphanId   – raw file on disk, NO DB row, NOT in-flight.
 *                   sweepOrphans() MUST delete this file.
 *
 *   B) backedId   – raw file on disk WITH a corresponding DB row.
 *                   sweepOrphans() MUST leave this file alone.
 *
 *   C) inFlightId – raw file on disk, NO DB row, but its id is present
 *                   in inFlightIds at sweep time.
 *                   sweepOrphans() MUST leave this file alone.
 *
 * After the sweep: exactly 1 deletion is reported and only file A is gone.
 */
@RunWith(AndroidJUnit4::class)
class OrphanSweepTest {

    private lateinit var context: Context
    private lateinit var db: EvidenceDatabase
    private lateinit var repo: EvidenceRepository

    /** Isolated test directory — never the same path as the real "evidence" dir. */
    private lateinit var testDir: File

    // Unique IDs for this test run to avoid cross-test pollution.
    private val orphanId   = "orphan-"   + UUID.randomUUID().toString()
    private val backedId   = "backed-"   + UUID.randomUUID().toString()
    private val inFlightId = "inflight-" + UUID.randomUUID().toString()

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()

        // Build an in-memory Room DB so the test is hermetic (no leftover rows
        // from previous runs or from production data on the device).
        db = Room.inMemoryDatabaseBuilder(context, EvidenceDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        EvidenceDatabase.setInstanceForTesting(db)

        // Inject the isolated directory name. The real "evidence" directory is
        // never touched because EncryptedFileStore is constructed with this name.
        repo = EvidenceRepository(context, evidenceDirName = "evidence_test")

        // Mirror the path that EncryptedFileStore will use for file operations.
        testDir = File(context.filesDir, "evidence_test").also { it.mkdirs() }

        // Hard guarantee: testDir must not resolve to the real evidence directory.
        check(testDir.canonicalPath != File(context.filesDir, "evidence").canonicalPath) {
            "Test isolation broken: testDir resolved to the real evidence directory"
        }

        // --- Plant the three scenario files ---

        // A) Orphan: file on disk, no DB row, not in-flight
        File(testDir, orphanId).writeText("orphan-data")

        // B) Backed: file on disk + matching DB row
        File(testDir, backedId).writeText("backed-data")
        val backedEntity = EvidenceEntity(
            id                     = backedId,
            hash                   = "deadbeef",
            receiptTimeEpochMillis = System.currentTimeMillis(),
            timeZoneId             = "UTC",
            mimeType               = "image/jpeg",
            fileSizeBytes          = 11L,
            filePath               = File(testDir, backedId).absolutePath
        )
        runBlocking { db.evidenceDao().insert(backedEntity) }

        // C) In-flight: file on disk, no DB row, but id is in inFlightIds
        File(testDir, inFlightId).writeText("inflight-data")
        EvidenceRepository.addInFlightIdForTesting(inFlightId)
    }

    @After
    fun tearDown() {
        // Always clean up the in-flight marker so other tests are not affected.
        EvidenceRepository.removeInFlightIdForTesting(inFlightId)

        // Delete the entire test directory tree (not just individual files),
        // so no artifacts survive even if a test assertion fails midway.
        testDir.deleteRecursively()

        db.close()
        EvidenceDatabase.clearInstanceForTesting()
    }

    @Test
    fun sweepOrphans_deletesOnlyUnbackedNonInFlightFile() = runBlocking {
        // Act
        val deletedCount = repo.sweepOrphans()

        // Exactly one file should have been deleted and logged.
        assertEquals(
            "sweepOrphans() should report exactly 1 deletion",
            1, deletedCount
        )

        // A) Orphan file must be gone.
        assertFalse(
            "Orphan file (no row, not in-flight) must be deleted from disk",
            File(testDir, orphanId).exists()
        )

        // B) Backed file must remain.
        assertTrue(
            "Backed file (has a DB row) must NOT be deleted",
            File(testDir, backedId).exists()
        )

        // C) In-flight file must remain.
        assertTrue(
            "In-flight file (id in inFlightIds) must NOT be deleted",
            File(testDir, inFlightId).exists()
        )
    }
}
