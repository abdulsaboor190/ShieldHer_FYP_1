package com.shieldher.dashboard.data

import android.content.ContextWrapper
import com.shieldher.dashboard.data.fake.FakeCaseRepository
import com.shieldher.dashboard.data.fake.FakeEvidenceGateway
import org.junit.Assert.assertTrue
import org.junit.Test

class ServiceLocatorTest {

    @Test
    fun afterInstallFakes_initDoesNotReplaceFakes() {
        ServiceLocator.installFakes()
        assertTrue(ServiceLocator.evidenceGateway is FakeEvidenceGateway)
        assertTrue(ServiceLocator.caseRepository is FakeCaseRepository)

        // Use ContextWrapper(null) as a non-null Context instance
        val dummyContext = object : ContextWrapper(null) {}

        // Calling init should do nothing because fakes are installed
        ServiceLocator.init(dummyContext)

        assertTrue(ServiceLocator.evidenceGateway is FakeEvidenceGateway)
        assertTrue(ServiceLocator.caseRepository is FakeCaseRepository)
    }
}
