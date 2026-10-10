package com.shieldher.vault.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.shieldher.vault.data.EvidenceEntity
import com.shieldher.vault.data.EvidenceRepository
import kotlinx.coroutines.flow.Flow

class EvidenceListViewModel @JvmOverloads constructor(
    application: Application,
    private val repository: EvidenceRepository = EvidenceRepository(application)
) : AndroidViewModel(application) {

    val evidenceList: Flow<List<EvidenceEntity>> = repository.getAllEvidence()
}
