package com.shieldher.dashboard.data

import com.shieldher.dashboard.data.fake.FakeCaseRepository
import com.shieldher.dashboard.data.fake.FakeEvidenceGateway

/**
 * ServiceLocator providing dependencies to dashboard ViewModels and screens.
 *
 * TEMPORARY: P2-4 switches these defaults to the real implementations; fakes stay only
 * for tests and the debug gallery.
 */
object ServiceLocator {

    var evidenceGateway: EvidenceGateway = FakeEvidenceGateway()
    var caseRepository: CaseRepository = FakeCaseRepository()
    val verificationSession: VerificationSession = VerificationSession()

    fun installFakes() {
        evidenceGateway = FakeEvidenceGateway()
        caseRepository = FakeCaseRepository()
        verificationSession.clear()
    }
}
