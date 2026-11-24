package com.shieldher.dashboard.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shieldher.dashboard.cases.CaseStoreException
import com.shieldher.dashboard.data.CaseRepository
import com.shieldher.dashboard.data.EvidenceGateway
import com.shieldher.dashboard.data.ServiceLocator
import com.shieldher.dashboard.data.VerificationSession
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface DeleteAllResult {
    data object Success : DeleteAllResult
    data object GatewayFailed : DeleteAllResult
    data object CaseStoreFailed : DeleteAllResult
}

class SettingsViewModel(
    private val evidenceGateway: EvidenceGateway = ServiceLocator.evidenceGateway,
    private val caseRepository: CaseRepository = ServiceLocator.caseRepository,
    private val verificationSession: VerificationSession = ServiceLocator.verificationSession
) : ViewModel() {

    val evidenceCount: StateFlow<Int> = evidenceGateway.observeEvidence()
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _deleteAllResult = MutableSharedFlow<DeleteAllResult>()
    val deleteAllResult: SharedFlow<DeleteAllResult> = _deleteAllResult.asSharedFlow()

    fun deleteAll() {
        viewModelScope.launch {
            var gatewayOk = false
            var caseStoreOk = false

            try {
                evidenceGateway.deleteAll()
                gatewayOk = true
            } catch (_: Exception) {
                gatewayOk = false
            }

            try {
                caseRepository.deleteEverything()
                caseStoreOk = true
            } catch (_: CaseStoreException) {
                // If case repository throws CaseStoreException (unreadable store), still report outcome
                caseStoreOk = false
            } catch (_: Exception) {
                caseStoreOk = false
            }

            verificationSession.clear()

            if (gatewayOk && caseStoreOk) {
                _deleteAllResult.emit(DeleteAllResult.Success)
            } else if (!gatewayOk) {
                _deleteAllResult.emit(DeleteAllResult.GatewayFailed)
            } else {
                _deleteAllResult.emit(DeleteAllResult.CaseStoreFailed)
            }
        }
    }
}
