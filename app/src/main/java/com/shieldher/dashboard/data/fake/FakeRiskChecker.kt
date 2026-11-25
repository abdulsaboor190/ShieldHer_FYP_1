package com.shieldher.dashboard.data.fake

import com.shieldher.dashboard.data.RiskBand
import com.shieldher.dashboard.data.RiskChecker
import com.shieldher.dashboard.data.RiskInput
import com.shieldher.dashboard.data.RiskOutcome

/**
 * Fake implementation of [RiskChecker] for unit testing.
 * Configurable outcome and call counter.
 */
internal class FakeRiskChecker(
    var outcomeToReturn: RiskOutcome = RiskOutcome.Result(RiskBand.LOWER)
) : RiskChecker {

    var callCount: Int = 0
        private set

    var lastInput: RiskInput? = null
        private set

    override suspend fun check(input: RiskInput): RiskOutcome {
        callCount++
        lastInput = input
        return outcomeToReturn
    }

    fun reset() {
        callCount = 0
        lastInput = null
    }
}
