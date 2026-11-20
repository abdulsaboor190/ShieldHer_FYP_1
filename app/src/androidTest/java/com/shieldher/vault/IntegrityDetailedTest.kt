package com.shieldher.vault

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.shieldher.vault.data.EvidenceDatabase
import com.shieldher.vault.data.EvidenceRepository
import com.shieldher.vault.data.IntegrityResult
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayInputStream
import java.io.File
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class IntegrityDetailedTest {

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

        repo = EvidenceRepository(context, evidenceDirName = "evidence_test_integrity_detailed")
        testDir = File(context.filesDir, "evidence_test_integrity_detailed").also { it.mkdirs() }

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
    fun verifyIntegrityDetailed_returnsUnchanged_forUntouchedFile() = runBlocking {
        val payload = "Untouched file payload 12345".toByteArray(Charsets.UTF_8)
        val entity = repo.intake(ByteArrayInputStream(payload), "text/plain", "source")

        val result = repo.verifyIntegrityDetailed(entity.id)
        assertEquals(IntegrityResult.UNCHANGED, result)
    }

    @Test
    fun verifyIntegrityDetailed_returnsChanged_whenOneByteFlipped() = runBlocking {
        val payload = "Byte flip test payload 12345".toByteArray(Charsets.UTF_8)
        val entity = repo.intake(ByteArrayInputStream(payload), "text/plain", "source")

        val storedFile = File(testDir, entity.id)
        val rawBytes = storedFile.readBytes()
        val mid = rawBytes.size / 2
        rawBytes[mid] = (rawBytes[mid].toInt() xor 0xFF).toByte()
        storedFile.writeBytes(rawBytes)

        val result = repo.verifyIntegrityDetailed(entity.id)
        assertEquals(IntegrityResult.CHANGED, result)
    }

    @Test
    fun verifyIntegrityDetailed_returnsChanged_forTruncatedFile() = runBlocking {
        val payload = "Truncated file payload for detailed integrity test".toByteArray(Charsets.UTF_8)
        val entity = repo.intake(ByteArrayInputStream(payload), "text/plain", "source")

        val storedFile = File(testDir, entity.id)
        val rawBytes = storedFile.readBytes()
        val truncated = rawBytes.copyOf(rawBytes.size / 2)
        storedFile.writeBytes(truncated)

        val result = repo.verifyIntegrityDetailed(entity.id)
        assertEquals(IntegrityResult.CHANGED, result)
    }

    @Test
    fun verifyIntegrityDetailed_returnsMissing_whenFileDeleted() = runBlocking {
        val payload = "File to be deleted".toByteArray(Charsets.UTF_8)
        val entity = repo.intake(ByteArrayInputStream(payload), "text/plain", "source")

        val storedFile = File(testDir, entity.id)
        storedFile.delete()

        val result = repo.verifyIntegrityDetailed(entity.id)
        assertEquals(IntegrityResult.MISSING, result)
    }

    @Test
    fun verifyIntegrityDetailed_returnsMissing_forUnknownId() = runBlocking {
        val unknownId = UUID.randomUUID().toString()
        val result = repo.verifyIntegrityDetailed(unknownId)
        assertEquals(IntegrityResult.MISSING, result)
    }
}
