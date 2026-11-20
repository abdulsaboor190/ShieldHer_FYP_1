package com.shieldher.vault

import android.content.Context
import android.graphics.Bitmap
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.shieldher.vault.data.EvidenceDatabase
import com.shieldher.vault.data.EvidenceDisplay
import com.shieldher.vault.data.EvidenceRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class ReadForDisplayTest {

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

        repo = EvidenceRepository(context, evidenceDirName = "evidence_test_read_display")
        testDir = File(context.filesDir, "evidence_test_read_display").also { it.mkdirs() }

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
    fun readForDisplay_returnsText_forTextPlain() = runBlocking {
        val textPayload = "Hello preview world!"
        val entity = repo.intake(
            ByteArrayInputStream(textPayload.toByteArray(Charsets.UTF_8)),
            "text/plain",
            "source"
        )

        val result = repo.readForDisplay(entity.id)
        assertTrue(result is EvidenceDisplay.Text)
        assertEquals(textPayload, (result as EvidenceDisplay.Text).text)
    }

    @Test
    fun readForDisplay_returnsImage_forPngImage() = runBlocking {
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val imageBytes = baos.toByteArray()

        val entity = repo.intake(
            ByteArrayInputStream(imageBytes),
            "image/png",
            "source"
        )

        val result = repo.readForDisplay(entity.id, maxImageSidePx = 50)
        assertTrue(result is EvidenceDisplay.Image)
        val previewBitmap = (result as EvidenceDisplay.Image).bitmap
        assertTrue(maxOf(previewBitmap.width, previewBitmap.height) <= 50)
    }

    @Test
    fun readForDisplay_returnsTooLarge_whenExceedsMaxBytes() = runBlocking {
        val payload = "Some data slightly longer than 5 bytes".toByteArray(Charsets.UTF_8)
        val entity = repo.intake(ByteArrayInputStream(payload), "text/plain", "source")

        val result = repo.readForDisplay(entity.id, maxBytes = 5)
        assertEquals(EvidenceDisplay.TooLarge, result)
    }

    @Test
    fun readForDisplay_returnsUnavailable_forUnknownId() = runBlocking {
        val result = repo.readForDisplay(UUID.randomUUID().toString())
        assertEquals(EvidenceDisplay.Unavailable, result)
    }

    @Test
    fun readForDisplay_returnsUnsupported_forPdf() = runBlocking {
        val payload = "dummy pdf data".toByteArray(Charsets.UTF_8)
        val entity = repo.intake(ByteArrayInputStream(payload), "application/pdf", "source")

        val result = repo.readForDisplay(entity.id)
        assertEquals(EvidenceDisplay.Unsupported, result)
    }

    @Test
    fun readForDisplay_leavesNoNewFilesOnDisk() = runBlocking {
        val payload = "Check memory-only decryption".toByteArray(Charsets.UTF_8)
        val entity = repo.intake(ByteArrayInputStream(payload), "text/plain", "source")

        val filesBefore = (context.filesDir.listFiles()?.map { it.name } ?: emptyList()) +
                (context.cacheDir.listFiles()?.map { it.name } ?: emptyList())

        repo.readForDisplay(entity.id)

        val filesAfter = (context.filesDir.listFiles()?.map { it.name } ?: emptyList()) +
                (context.cacheDir.listFiles()?.map { it.name } ?: emptyList())

        assertEquals(filesBefore.sorted(), filesAfter.sorted())
    }
}
