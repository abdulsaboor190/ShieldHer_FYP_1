package com.shieldher.dashboard.data

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VerificationSessionTest {

    @Test
    fun recordedResult_isReturned() {
        val session = VerificationSession()
        session.record("item-1", EvidenceIntegrity.UNCHANGED, 1000L)

        val record = session.get("item-1")
        assertNotNull(record)
        assertEquals(EvidenceIntegrity.UNCHANGED, record?.result)
        assertEquals(1000L, record?.checkedAtMillis)
    }

    @Test
    fun recordForAnotherId_isSeparate() {
        val session = VerificationSession()
        session.record("item-1", EvidenceIntegrity.UNCHANGED, 1000L)
        session.record("item-2", EvidenceIntegrity.CHANGED, 2000L)

        assertEquals(EvidenceIntegrity.UNCHANGED, session.get("item-1")?.result)
        assertEquals(EvidenceIntegrity.CHANGED, session.get("item-2")?.result)
    }

    @Test
    fun clear_emptiesSession() {
        val session = VerificationSession()
        session.record("item-1", EvidenceIntegrity.UNCHANGED, 1000L)
        session.clear()

        assertNull(session.get("item-1"))
        assertTrue(session.records.value.isEmpty())
    }

    @Test
    fun newInstance_startsEmpty() = runTest {
        val session = VerificationSession()
        assertNull(session.get("item-1"))
        assertTrue(session.records.first().isEmpty())
    }
}
