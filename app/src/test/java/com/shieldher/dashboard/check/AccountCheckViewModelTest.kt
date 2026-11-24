package com.shieldher.dashboard.check

import com.shieldher.dashboard.data.RiskBand
import com.shieldher.dashboard.data.RiskOutcome
import com.shieldher.dashboard.data.RiskPlatform
import com.shieldher.dashboard.data.ServiceLocator
import com.shieldher.dashboard.data.fake.FakeRiskChecker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AccountCheckViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val fakeChecker = FakeRiskChecker()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        ServiceLocator.riskChecker = fakeChecker
        fakeChecker.reset()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        fakeChecker.reset()
    }

    @Test
    fun initialStateIsIdle() {
        val viewModel = AccountCheckViewModel()
        assertEquals(AccountCheckState.Idle, viewModel.state.value)
    }

    @Test
    fun runTransitionsToComputingThenDone() = runTest(testDispatcher) {
        fakeChecker.outcomeToReturn = RiskOutcome.Result(RiskBand.HIGHER)
        val viewModel = AccountCheckViewModel()

        viewModel.run(
            platform = RiskPlatform.INSTAGRAM,
            followersText = "5000",
            followingText = "150",
            bioText = "Test bio",
            hasLink = false
        )

        advanceUntilIdle()

        assertEquals(1, fakeChecker.callCount)
        val doneState = viewModel.state.value
        assertTrue("State should be Done", doneState is AccountCheckState.Done)
        assertEquals(RiskOutcome.Result(RiskBand.HIGHER), (doneState as AccountCheckState.Done).outcome)
    }

    @Test
    fun clearResetsStateToIdle() = runTest(testDispatcher) {
        val viewModel = AccountCheckViewModel()
        viewModel.run(
            platform = RiskPlatform.INSTAGRAM,
            followersText = "5000",
            followingText = "150",
            bioText = "Test",
            hasLink = false
        )
        advanceUntilIdle()

        assertTrue(viewModel.state.value is AccountCheckState.Done)

        viewModel.clear()
        assertEquals(AccountCheckState.Idle, viewModel.state.value)
    }

    @Test
    fun noFieldOfViewModelIsAString() {
        val fields = AccountCheckViewModel::class.java.declaredFields
        for (field in fields) {
            val type = field.type
            assertTrue(
                "Field '${field.name}' in AccountCheckViewModel must not be String or CharSequence",
                type != String::class.java && !CharSequence::class.java.isAssignableFrom(type)
            )
        }
    }
}
