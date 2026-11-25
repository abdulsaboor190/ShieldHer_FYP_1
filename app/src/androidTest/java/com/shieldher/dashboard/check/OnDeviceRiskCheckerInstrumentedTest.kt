package com.shieldher.dashboard.check

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import android.content.Context
import com.shieldher.dashboard.check.risk.OnDeviceRiskChecker
import com.shieldher.dashboard.data.RiskBand
import com.shieldher.dashboard.data.RiskInput
import com.shieldher.dashboard.data.RiskOutcome
import com.shieldher.dashboard.data.RiskPlatform
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OnDeviceRiskCheckerInstrumentedTest {

    private lateinit var checker: OnDeviceRiskChecker

    @Before
    fun setUp() {
        val context: Context = ApplicationProvider.getApplicationContext()
        checker = OnDeviceRiskChecker(context)
    }

    @Test
    fun testRealAssetScoresGoldenRowsAccurately() = runBlocking {
        // Row 1 (gold_0001): genuine, supported_prediction -> Result(LOWER)
        val row1 = RiskInput(
            platform = RiskPlatform.INSTAGRAM,
            followers = 261631L,
            following = 1179L,
            descriptionLength = 100,
            hasLink = false
        )
        val outcome1 = checker.check(row1)
        assertEquals(RiskOutcome.Result(RiskBand.LOWER), outcome1)

        // Row 2 (gold_0013): risk, supported_prediction -> Result(HIGHER)
        // gold_0013 has: followers=7, following=2136, desc=49, external_url=0
        val row2 = RiskInput(
            platform = RiskPlatform.INSTAGRAM,
            followers = 7L,
            following = 2136L,
            descriptionLength = 49,
            hasLink = false
        )
        val outcome2 = checker.check(row2)
        assertEquals(RiskOutcome.Result(RiskBand.HIGHER), outcome2)

        // Row 3 (gold_0006): risk, uncertain_prediction -> Uncertain
        val row3 = RiskInput(
            platform = RiskPlatform.INSTAGRAM,
            followers = 37L,
            following = 7621L,
            descriptionLength = 8,
            hasLink = true
        )
        val outcome3 = checker.check(row3)
        assertEquals(RiskOutcome.Uncertain, outcome3)

        // Row 4: Missing follower count -> UnsupportedInput
        val row4 = RiskInput(
            platform = RiskPlatform.INSTAGRAM,
            followers = null,
            following = 50L,
            descriptionLength = 10,
            hasLink = false
        )
        val outcome4 = checker.check(row4)
        assertEquals(RiskOutcome.UnsupportedInput, outcome4)

        // Row 5: Negative count -> UnsupportedInput
        val row5 = RiskInput(
            platform = RiskPlatform.INSTAGRAM,
            followers = -10L,
            following = 100L,
            descriptionLength = 5,
            hasLink = false
        )
        val outcome5 = checker.check(row5)
        assertEquals(RiskOutcome.UnsupportedInput, outcome5)
    }

    @Test
    fun testNonInstagramPlatformGivesNotValidated() = runBlocking {
        val xInput = RiskInput(
            platform = RiskPlatform.X_TWITTER,
            followers = 500L,
            following = 200L,
            descriptionLength = 30,
            hasLink = true
        )
        val xOutcome = checker.check(xInput)
        assertEquals(RiskOutcome.NotValidated, xOutcome)

        val otherInput = RiskInput(
            platform = RiskPlatform.OTHER,
            followers = 500L,
            following = 200L,
            descriptionLength = 30,
            hasLink = true
        )
        val otherOutcome = checker.check(otherInput)
        assertEquals(RiskOutcome.NotValidated, otherOutcome)
    }
}
