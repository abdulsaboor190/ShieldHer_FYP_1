package com.shieldher.dashboard.data.fake

import android.net.Uri
import com.shieldher.dashboard.data.DisplayContent
import com.shieldher.dashboard.data.EvidenceGateway
import com.shieldher.dashboard.data.EvidenceIntegrity
import com.shieldher.dashboard.data.EvidenceItem
import com.shieldher.dashboard.data.ImportOutcome
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

internal class FakeEvidenceGateway : EvidenceGateway {

    private val items = MutableStateFlow(createInitialSyntheticItems())

    private fun createInitialSyntheticItems(): List<EvidenceItem> {
        return listOf(
            EvidenceItem(
                id = "synth-evidence-1",
                hash = "a1b2c3d4e5f60102030405060708090a1b2c3d4e5f60102030405060708090a1",
                receiptTimeEpochMillis = 1794500000000L,
                timeZoneId = "UTC",
                sourceLabel = "Example app",
                mimeType = "image/png",
                fileSizeBytes = 204800L,
                filePath = "/fake/storage/synth-evidence-1"
            ),
            EvidenceItem(
                id = "synth-evidence-2",
                hash = "b2c3d4e5f6a10102030405060708090a1b2c3d4e5f60102030405060708090a2",
                receiptTimeEpochMillis = 1794503600000L,
                timeZoneId = "UTC",
                sourceLabel = "Example app",
                mimeType = "text/plain",
                fileSizeBytes = 1024L,
                filePath = "/fake/storage/synth-evidence-2"
            ),
            EvidenceItem(
                id = "synth-evidence-3",
                hash = "c3d4e5f6a1b20102030405060708090a1b2c3d4e5f60102030405060708090a3",
                receiptTimeEpochMillis = 1794507200000L,
                timeZoneId = "UTC",
                sourceLabel = "Example messenger",
                mimeType = "image/jpeg",
                fileSizeBytes = 512000L,
                filePath = "/fake/storage/synth-evidence-3"
            ),
            EvidenceItem(
                id = "synth-evidence-4",
                hash = "d4e5f6a1b2c30102030405060708090a1b2c3d4e5f60102030405060708090a4",
                receiptTimeEpochMillis = 1794510800000L,
                timeZoneId = "UTC",
                sourceLabel = "Example browser",
                mimeType = "text/plain",
                fileSizeBytes = 4096L,
                filePath = "/fake/storage/synth-evidence-4"
            ),
            EvidenceItem(
                id = "synth-evidence-5",
                hash = "e5f6a1b2c3d40102030405060708090a1b2c3d4e5f60102030405060708090a5",
                receiptTimeEpochMillis = 1794514400000L,
                timeZoneId = "UTC",
                sourceLabel = "Example app",
                mimeType = "image/png",
                fileSizeBytes = 1024000L,
                filePath = "/fake/storage/synth-evidence-5"
            ),
            EvidenceItem(
                id = "synth-evidence-6",
                hash = "f6a1b2c3d4e50102030405060708090a1b2c3d4e5f60102030405060708090a6",
                receiptTimeEpochMillis = 1794518000000L,
                timeZoneId = "UTC",
                sourceLabel = "Example email",
                mimeType = "text/plain",
                fileSizeBytes = 2048L,
                filePath = "/fake/storage/synth-evidence-6"
            )
        )
    }

    override fun observeEvidence(): Flow<List<EvidenceItem>> = items.asStateFlow()

    override suspend fun getEvidence(id: String): EvidenceItem? {
        return items.value.find { it.id == id }
    }

    override suspend fun verify(id: String): EvidenceIntegrity {
        return when (id) {
            "synth-evidence-2" -> EvidenceIntegrity.CHANGED
            "synth-evidence-5" -> EvidenceIntegrity.MISSING
            else -> EvidenceIntegrity.UNCHANGED
        }
    }

    override suspend fun readForDisplay(id: String, maxBytes: Int): DisplayContent {
        val item = getEvidence(id) ?: return DisplayContent.Unavailable
        return if (item.mimeType.startsWith("text/")) {
            DisplayContent.Text("Example preview text for synthetic evidence item")
        } else {
            DisplayContent.Unavailable
        }
    }

    override suspend fun importFromUri(uri: Uri, mimeType: String?): ImportOutcome {
        val newId = "synth-evidence-" + UUID.randomUUID().toString()
        val newItem = EvidenceItem(
            id = newId,
            hash = "synthetic_hash_" + UUID.randomUUID().toString().replace("-", ""),
            receiptTimeEpochMillis = System.currentTimeMillis(),
            timeZoneId = "UTC",
            sourceLabel = "In-app import",
            mimeType = mimeType ?: "text/plain",
            fileSizeBytes = 4096L,
            filePath = "/fake/storage/$newId"
        )
        items.value = items.value + newItem
        return ImportOutcome.Success(newId)
    }

    override suspend fun delete(id: String): Boolean {
        val current = items.value
        val filtered = current.filterNot { it.id == id }
        if (filtered.size != current.size) {
            items.value = filtered
            return true
        }
        return false
    }

    override suspend fun deleteAll(): Int {
        val count = items.value.size
        items.value = emptyList()
        return count
    }
}
