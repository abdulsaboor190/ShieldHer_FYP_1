package com.shieldher.vault

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.shieldher.vault.data.EvidenceDatabase
import com.shieldher.vault.data.EvidenceRepository
import com.shieldher.vault.data.EvidenceTooLargeException
import com.shieldher.vault.files.HashUtil
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayInputStream
import java.io.File
import java.util.UUID

/**
 * Instrumented tests for EvidenceRepository integrity operations.
 *
 * Uses an isolated directory ("evidence_test_integrity") and an in-memory
 * Room database. Never touches or mutates the production "evidence" directory.
 */
@RunWith(AndroidJUnit4::class)
class IntegrityTest {

    private lateinit var context: Context
    private lateinit var db: EvidenceDatabase
    private lateinit var repo: EvidenceRepository
    private lateinit var testDir: File

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()

        // Build hermetic in-memory database
        db = Room.inMemoryDatabaseBuilder(context, EvidenceDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        EvidenceDatabase.setInstanceForTesting(db)

        // Inject isolated directory name for tests
        repo = EvidenceRepository(context, evidenceDirName = "evidence_test_integrity")

        testDir = File(context.filesDir, "evidence_test_integrity").also { it.mkdirs() }

        // Hard guarantee: test directory must not resolve to the real evidence directory
        check(testDir.canonicalPath != File(context.filesDir, "evidence").canonicalPath) {
            "Test isolation broken: testDir resolved to the real evidence directory"
        }
    }

    @After
    fun tearDown() {
        // Clean up isolated test files and reset database singleton
        testDir.deleteRecursively()
        db.close()
        EvidenceDatabase.clearInstanceForTesting()
    }

    @Test
    fun intake_storesCorrectHashAndMetadata() = runBlocking {
        val payload = "Evidence Vault Sample Data".toByteArray(Charsets.UTF_8)
        val expectedHash = HashUtil.sha256(payload)
        val mimeType = "text/plain"
        val sourceLabel = "Test Source"

        val entity = repo.intake(ByteArrayInputStream(payload), mimeType, sourceLabel)

        assertEquals(expectedHash, entity.hash)
        assertEquals(mimeType, entity.mimeType)
        assertEquals(sourceLabel, entity.sourceLabel)
        assertNull(entity.parentId)
        assertNull(entity.transformationType)
        val storedFile = File(testDir, entity.id)
        assertTrue("Stored file must exist in test directory", storedFile.exists())
    }

    @Test
    fun verifyIntegrity_returnsTrueForUntouchedFile() = runBlocking {
        val payload = "Untouched file payload".toByteArray(Charsets.UTF_8)
        val entity = repo.intake(ByteArrayInputStream(payload), "text/plain", "source")

        val result = repo.verifyIntegrity(entity.id)
        assertTrue("verifyIntegrity must return true for untouched file", result)
    }

    @Test
    fun verifyIntegrity_returnsFalseAfterTampering() = runBlocking {
        val payload = "Tamper test payload 1234567890".toByteArray(Charsets.UTF_8)
        val entity = repo.intake(ByteArrayInputStream(payload), "text/plain", "source")

        val storedFile = File(testDir, entity.id)
        assertTrue("Stored file must exist prior to tampering", storedFile.exists())

        // Mutate raw bytes of ciphertext directly on disk
        val rawBytes = storedFile.readBytes()
        assertTrue("Stored ciphertext must not be empty", rawBytes.isNotEmpty())
        val mid = rawBytes.size / 2
        rawBytes[mid] = (rawBytes[mid].toInt() xor 0xFF).toByte()
        storedFile.writeBytes(rawBytes)

        val result = try {
            repo.verifyIntegrity(entity.id)
        } catch (e: Throwable) {
            fail("verifyIntegrity must not throw on tampered file, but threw: $e")
            false
        }
        assertFalse("verifyIntegrity must return false for tampered file", result)
    }

    @Test
    fun verifyIntegrity_returnsFalseForTruncatedFile() = runBlocking {
        val payload = "Truncated file payload with enough length to halve".toByteArray(Charsets.UTF_8)
        val entity = repo.intake(ByteArrayInputStream(payload), "text/plain", "source")

        val storedFile = File(testDir, entity.id)
        assertTrue("Stored file must exist prior to truncation", storedFile.exists())

        val rawBytes = storedFile.readBytes()
        val truncatedBytes = rawBytes.copyOf(rawBytes.size / 2)
        storedFile.writeBytes(truncatedBytes)

        val result = try {
            repo.verifyIntegrity(entity.id)
        } catch (e: Throwable) {
            fail("verifyIntegrity must not throw on truncated file, but threw: $e")
            false
        }
        assertFalse("verifyIntegrity must return false for truncated file", result)
    }

    @Test
    fun verifyIntegrity_returnsFalseForMissingFile() = runBlocking {
        val payload = "Missing file payload".toByteArray(Charsets.UTF_8)
        val entity = repo.intake(ByteArrayInputStream(payload), "text/plain", "source")

        val storedFile = File(testDir, entity.id)
        assertTrue("Stored file must exist prior to deletion", storedFile.exists())
        storedFile.delete()
        assertFalse("Stored file must be deleted", storedFile.exists())

        val result = try {
            repo.verifyIntegrity(entity.id)
        } catch (e: Throwable) {
            fail("verifyIntegrity must not throw on missing file, but threw: $e")
            false
        }
        assertFalse("verifyIntegrity must return false for missing file", result)
    }

    @Test
    fun verifyIntegrity_returnsFalseForUnknownId() = runBlocking {
        val unknownId = UUID.randomUUID().toString()

        val result = try {
            repo.verifyIntegrity(unknownId)
        } catch (e: Throwable) {
            fail("verifyIntegrity must not throw on unknown ID, but threw: $e")
            false
        }
        assertFalse("verifyIntegrity must return false for unknown ID", result)
    }

    @Test
    fun intake_rejectsInputOverLimit() = runBlocking {
        val repoWithLimit = EvidenceRepository(
            context,
            evidenceDirName = "evidence_test_integrity",
            maxFileSizeBytes = 1024L
        )
        val fiveKbPayload = ByteArray(5 * 1024) { 0x55.toByte() }

        var exceptionThrown = false
        try {
            repoWithLimit.intake(
                ByteArrayInputStream(fiveKbPayload),
                "application/octet-stream",
                "oversized-source"
            )
        } catch (e: EvidenceTooLargeException) {
            exceptionThrown = true
        } catch (e: Throwable) {
            fail("Expected EvidenceTooLargeException, but got: $e")
        }

        assertTrue("EvidenceTooLargeException must be thrown when input exceeds limit", exceptionThrown)

        // Assert no row exists in the database
        val rows = db.evidenceDao().getAll().first()
        assertEquals("Database must contain zero rows after rejected intake", 0, rows.size)

        // Assert test evidence directory contains zero files
        val files = testDir.listFiles() ?: emptyArray()
        assertEquals("Test directory must contain zero files after rejected intake", 0, files.size)
    }

    @Test
    fun intake_acceptsInputAtLimit() = runBlocking {
        val repoWithLimit = EvidenceRepository(
            context,
            evidenceDirName = "evidence_test_integrity",
            maxFileSizeBytes = 1024L
        )
        val exactLimitPayload = ByteArray(1024) { 0x55.toByte() }

        val entity = repoWithLimit.intake(
            ByteArrayInputStream(exactLimitPayload),
            "application/octet-stream",
            "at-limit-source"
        )

        assertNotNull("Returned entity must not be null", entity)
        assertEquals(1024L, entity.fileSizeBytes)
        val verified = repoWithLimit.verifyIntegrity(entity.id)
        assertTrue("verifyIntegrity must return true for item at exact limit", verified)
    }
}
