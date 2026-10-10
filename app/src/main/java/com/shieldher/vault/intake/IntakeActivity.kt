package com.shieldher.vault.intake

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.IntentCompat
import androidx.lifecycle.lifecycleScope
import com.shieldher.vault.data.EvidenceRepository
import com.shieldher.vault.data.EvidenceTooLargeException
import com.shieldher.vault.data.MAX_EVIDENCE_FILE_SIZE_BYTES
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.InputStream

class IntakeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent.action != Intent.ACTION_SEND) {
            showErrorAndFinish()
            return
        }

        val type = intent.type
        if (type == null) {
            showErrorAndFinish()
            return
        }

        if (type.startsWith("image/")) {
            handleImageShare(type)
        } else if (type == "text/plain") {
            handleTextShare()
        } else {
            showUnsupportedTypeAndFinish()
        }
    }

    private fun handleImageShare(mimeType: String) {
        val uri = try {
            IntentCompat.getParcelableExtra(intent, Intent.EXTRA_STREAM, Uri::class.java)
        } catch (_: Exception) {
            null
        }

        if (uri == null) {
            showErrorAndFinish()
            return
        }

        val reportedSize = try {
            contentResolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (sizeIndex != -1 && !cursor.isNull(sizeIndex)) {
                        cursor.getLong(sizeIndex)
                    } else null
                } else null
            }
        } catch (_: Exception) {
            null
        }

        if (reportedSize != null && reportedSize > MAX_EVIDENCE_FILE_SIZE_BYTES) {
            Log.w("ShieldHer", "intake rejected: size limit exceeded")
            Toast.makeText(
                applicationContext,
                "File too large — ShieldHer supports files up to 500MB",
                Toast.LENGTH_LONG
            ).show()
            finish()
            return
        }

        val inputStream = try {
            contentResolver.openInputStream(uri)
        } catch (_: Exception) {
            null
        }

        if (inputStream == null) {
            showErrorAndFinish()
            return
        }

        saveEvidence(inputStream, mimeType)
    }

    private fun handleTextShare() {
        val text = intent.getStringExtra(Intent.EXTRA_TEXT)
        if (text.isNullOrBlank()) {
            showErrorAndFinish()
            return
        }

        val inputStream = ByteArrayInputStream(text.toByteArray(Charsets.UTF_8))
        saveEvidence(inputStream, "text/plain")
    }

    private fun saveEvidence(inputStream: InputStream, mimeType: String) {
        val sourceLabel = inferSourceFromCaller()
        lifecycleScope.launch {
            try {
                val repo = EvidenceRepository(this@IntakeActivity)
                withContext(Dispatchers.IO) {
                    repo.sweepOrphans()
                    inputStream.use { stream ->
                        repo.intake(stream, mimeType, sourceLabel = sourceLabel)
                    }
                }
                Toast.makeText(this@IntakeActivity, "Evidence saved", Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: EvidenceTooLargeException) {
                Log.w("ShieldHer", "intake rejected: ${e.javaClass.simpleName}")
                Toast.makeText(applicationContext, "File too large — ShieldHer supports files up to 500MB", Toast.LENGTH_LONG).show()
                finish()
            } catch (_: Exception) {
                showErrorAndFinish()
            }
        }
    }

    private fun inferSourceFromCaller(): String? {
        return try {
            val refUri = referrer ?: return null
            refUri.host ?: refUri.authority
        } catch (_: Exception) {
            null
        }
    }

    private fun showUnsupportedTypeAndFinish() {
        Toast.makeText(
            applicationContext,
            "ShieldHer doesn't support this file type yet",
            Toast.LENGTH_SHORT
        ).show()
        finish()
    }

    private fun showErrorAndFinish() {
        Toast.makeText(applicationContext, "Couldn't read shared content", Toast.LENGTH_SHORT).show()
        finish()
    }
}
