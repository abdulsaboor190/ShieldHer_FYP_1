package com.shieldher.dashboard.data

import android.content.Context
import com.shieldher.dashboard.cases.CaseStore
import com.shieldher.dashboard.cases.DefaultCaseRepository
import com.shieldher.dashboard.data.fake.FakeCaseRepository
import com.shieldher.dashboard.data.fake.FakeEvidenceGateway
import com.shieldher.dashboard.data.fake.FakeRiskChecker
import com.shieldher.vault.data.EvidenceRepository
import java.io.File

/**
 * ServiceLocator providing dependencies to dashboard ViewModels and screens.
 * Defaults to production implementations initialized via [init].
 */
object ServiceLocator {

    private var _evidenceGateway: EvidenceGateway? = null
    var evidenceGateway: EvidenceGateway
        get() = _evidenceGateway ?: FakeEvidenceGateway().also { _evidenceGateway = it }
        set(value) {
            _evidenceGateway = value
        }

    private var _caseRepository: CaseRepository? = null
    var caseRepository: CaseRepository
        get() = _caseRepository ?: FakeCaseRepository().also { _caseRepository = it }
        set(value) {
            _caseRepository = value
        }

    private var _riskChecker: RiskChecker? = null
    var riskChecker: RiskChecker
        get() = _riskChecker ?: FakeRiskChecker().also { _riskChecker = it }
        set(value) {
            _riskChecker = value
        }

    val verificationSession: VerificationSession = VerificationSession()

    private var fakesInstalled = false

    fun init(context: Context) {
        if (fakesInstalled) return
        val appContext = context.applicationContext
        val vaultRepo = EvidenceRepository(appContext)
        _evidenceGateway = VaultEvidenceGateway(vaultRepo)

        val casesDir = File(appContext.filesDir, "cases")
        val caseStore = CaseStore(appContext, casesDir)
        _caseRepository = DefaultCaseRepository(caseStore)

        _riskChecker = com.shieldher.dashboard.check.risk.OnDeviceRiskChecker(appContext)
    }

    fun installFakes() {
        fakesInstalled = true
        _evidenceGateway = FakeEvidenceGateway()
        _caseRepository = FakeCaseRepository()
        _riskChecker = FakeRiskChecker()
        verificationSession.clear()
    }
}
