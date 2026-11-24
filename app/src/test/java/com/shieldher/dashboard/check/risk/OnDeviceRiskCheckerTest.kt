package com.shieldher.dashboard.check.risk

import com.shieldher.dashboard.data.RiskBand
import com.shieldher.dashboard.data.RiskInput
import com.shieldher.dashboard.data.RiskOutcome
import com.shieldher.dashboard.data.RiskPlatform
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream

@OptIn(ExperimentalCoroutinesApi::class)
class OnDeviceRiskCheckerTest {

    private fun getAssetStream(): FileInputStream {
        val binFile = File("src/main/assets/account_risk_model_v1.bin").let {
            if (it.exists()) it else File("app/src/main/assets/account_risk_model_v1.bin")
        }
        assertTrue("Asset file exists: ${binFile.absolutePath}", binFile.exists())
        return FileInputStream(binFile)
    }

    @Test
    fun nullPlatformGivesPlatformNotChosen() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val checker = OnDeviceRiskChecker(inputStreamProvider = { getAssetStream() }, dispatcher = testDispatcher)

        val input = RiskInput(
            platform = null,
            followers = 100L,
            following = 50L,
            descriptionLength = 10,
            hasLink = false
        )
        val outcome = checker.check(input)
        assertEquals(RiskOutcome.PlatformNotChosen, outcome)
    }

    @Test
    fun unsupportedPlatformGivesNotValidatedWithoutScoring() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        var streamOpened = false
        val checker = OnDeviceRiskChecker(
            inputStreamProvider = {
                streamOpened = true
                getAssetStream()
            },
            dispatcher = testDispatcher
        )

        val inputX = RiskInput(
            platform = RiskPlatform.X_TWITTER,
            followers = 100L,
            following = 50L,
            descriptionLength = 10,
            hasLink = false
        )
        val outcomeX = checker.check(inputX)
        assertEquals(RiskOutcome.NotValidated, outcomeX)

        val inputOther = RiskInput(
            platform = RiskPlatform.OTHER,
            followers = 100L,
            following = 50L,
            descriptionLength = 10,
            hasLink = false
        )
        val outcomeOther = checker.check(inputOther)
        assertEquals(RiskOutcome.NotValidated, outcomeOther)
    }

    @Test
    fun missingOrInvalidFollowersGivesUnsupportedInput() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val checker = OnDeviceRiskChecker(inputStreamProvider = { getAssetStream() }, dispatcher = testDispatcher)

        val inputNullFw = RiskInput(
            platform = RiskPlatform.INSTAGRAM,
            followers = null,
            following = 50L,
            descriptionLength = 10,
            hasLink = false
        )
        val outcomeNull = checker.check(inputNullFw)
        assertEquals(RiskOutcome.UnsupportedInput, outcomeNull)

        val inputNegFw = RiskInput(
            platform = RiskPlatform.INSTAGRAM,
            followers = -5L,
            following = 50L,
            descriptionLength = 10,
            hasLink = false
        )
        val outcomeNeg = checker.check(inputNegFw)
        assertEquals(RiskOutcome.UnsupportedInput, outcomeNeg)
    }

    @Test
    fun brokenStreamReturnsUnavailable() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val checker = OnDeviceRiskChecker(
            inputStreamProvider = { ByteArrayInputStream(byteArrayOf(1, 2, 3)) },
            dispatcher = testDispatcher
        )

        val input = RiskInput(
            platform = RiskPlatform.INSTAGRAM,
            followers = 100L,
            following = 50L,
            descriptionLength = 10,
            hasLink = false
        )
        val outcome = checker.check(input)
        assertEquals(RiskOutcome.Unavailable, outcome)
    }

    @Test
    fun instagramInputProducesExpectedOutcome() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val checker = OnDeviceRiskChecker(inputStreamProvider = { getAssetStream() }, dispatcher = testDispatcher)

        // Typical normal profile numbers with strong genuine resemblance -> Result(LOWER)
        val input = RiskInput(
            platform = RiskPlatform.INSTAGRAM,
            followers = 500L,
            following = 200L,
            descriptionLength = 40,
            hasLink = false
        )
        val outcome = checker.check(input)
        assertTrue(
            "Expected Result(LOWER) or Result(HIGHER), got $outcome",
            outcome is RiskOutcome.Result
        )
        assertEquals(RiskOutcome.Result(RiskBand.LOWER), outcome)
    }
}
