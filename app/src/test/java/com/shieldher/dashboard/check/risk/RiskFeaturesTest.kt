package com.shieldher.dashboard.check.risk

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import kotlin.math.abs
import kotlin.math.ln1p

class RiskFeaturesTest {

    @Test
    fun emptyBioLengthZeroIsAllowed() {
        val feats = RiskFeatures.transform(
            followers = 100L,
            following = 50L,
            descriptionLength = 0,
            hasLink = false
        )
        assertNotNull(feats)
        assertEquals(5, feats!!.size)
        assertEquals(0.0, feats[2], 1e-12)
        assertEquals(0.0, feats[3], 1e-12)
    }

    @Test
    fun negativeOrMissingCountsAreUnsupported() {
        assertNull("Negative followers must return null", RiskFeatures.transform(-1L, 100L, 0, false))
        assertNull("Negative following must return null", RiskFeatures.transform(100L, -1L, 0, false))
        assertNull("Negative bio length must return null", RiskFeatures.transform(100L, 100L, -1, false))
        assertNull("Null followers must return null", RiskFeatures.transform(null, 100L, 0, false))
        assertNull("Null following must return null", RiskFeatures.transform(100L, null, 0, false))
        assertNull("Both null must return null", RiskFeatures.transform(null, null, 0, false))
    }

    @Test
    fun zeroFollowersAndZeroFollowingGiveFiniteRatio() {
        val feats = RiskFeatures.transform(
            followers = 0L,
            following = 0L,
            descriptionLength = 10,
            hasLink = true
        )
        assertNotNull(feats)
        assertEquals(ln1p(0.0), feats!![0], 1e-12)
        assertEquals(ln1p(0.0), feats[1], 1e-12)
        assertEquals(10.0, feats[2], 1e-12)
        assertEquals(1.0, feats[3], 1e-12)
        // ratio = 0.0 / (0.0 + 1.0) = 0.0, ln1p(0.0) = 0.0
        assertEquals(ln1p(0.0), feats[4], 1e-12)
    }

    @Test
    fun knownInputGivesExactExpectedFeatureVector() {
        val followers = 350L
        val following = 420L
        val descLen = 65
        val hasLink = false

        val feats = RiskFeatures.transform(followers, following, descLen, hasLink)
        assertNotNull(feats)

        val expectedFw = ln1p(350.0)
        val expectedFg = ln1p(420.0)
        val expectedDesc = 65.0
        val expectedLink = 0.0
        val expectedRatio = ln1p(350.0 / (420.0 + 1.0))

        assertEquals(expectedFw, feats!![0], 1e-12)
        assertEquals(expectedFg, feats[1], 1e-12)
        assertEquals(expectedDesc, feats[2], 1e-12)
        assertEquals(expectedLink, feats[3], 1e-12)
        assertEquals(expectedRatio, feats[4], 1e-12)
    }
}
