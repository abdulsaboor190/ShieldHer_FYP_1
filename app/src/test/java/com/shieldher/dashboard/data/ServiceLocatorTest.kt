package com.shieldher.dashboard.data

import android.content.Context
import com.shieldher.dashboard.data.fake.FakeCaseRepository
import com.shieldher.dashboard.data.fake.FakeEvidenceGateway
import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.reflect.Proxy

class ServiceLocatorTest {

    @Test
    fun afterInstallFakes_initDoesNotReplaceFakes() {
        ServiceLocator.installFakes()
        assertTrue(ServiceLocator.evidenceGateway is FakeEvidenceGateway)
        assertTrue(ServiceLocator.caseRepository is FakeCaseRepository)

        // Mock context using dynamic proxy since Context is an abstract Android class
        val dummyContext = Proxy.newProxyInstance(
            Context::class.java.classLoader,
            arrayOf(Context::class.java)
        ) { _, _, _ -> null } as Context

        // Calling init should do nothing because fakes are installed
        ServiceLocator.init(dummyContext)

        assertTrue(ServiceLocator.evidenceGateway is FakeEvidenceGateway)
        assertTrue(ServiceLocator.caseRepository is FakeCaseRepository)
    }
}
