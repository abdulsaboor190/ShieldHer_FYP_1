package com.shieldher.dashboard.data

import android.net.Uri
import com.shieldher.vault.data.EvidenceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Production EvidenceGateway backed directly by the Stage 1 EvidenceRepository.
 */
class VaultEvidenceGateway(
    private val repository: EvidenceRepository
) : EvidenceGateway {

    override fun observeEvidence(): Flow<List<EvidenceItem>> {
        return repository.getAllEvidence().map { list ->
            list.map { VaultMappers.mapEntityToItem(it) }
        }
    }

    override suspend fun getEvidence(id: String): EvidenceItem? {
        val all = repository.getAllEvidence().first()
        val found = all.find { it.id == id } ?: return null
        return VaultMappers.mapEntityToItem(found)
    }

    override suspend fun verify(id: String): EvidenceIntegrity {
        val result = repository.verifyIntegrityDetailed(id)
        return VaultMappers.mapIntegrityResult(result)
    }

    override suspend fun readForDisplay(id: String, maxBytes: Int): DisplayContent {
        val display = repository.readForDisplay(id, maxBytes)
        return VaultMappers.mapEvidenceDisplay(display)
    }

    override suspend fun importFromUri(uri: Uri, mimeType: String?): ImportOutcome {
        val result = repository.importFromUri(uri, mimeType)
        return VaultMappers.mapImportResult(result)
    }

    override suspend fun delete(id: String): Boolean {
        return repository.delete(id)
    }

    override suspend fun deleteAll(): Int {
        val all = repository.getAllEvidence().first()
        var deletedCount = 0
        for (item in all) {
            if (repository.delete(item.id)) {
                deletedCount++
            }
        }
        return deletedCount
    }
}
