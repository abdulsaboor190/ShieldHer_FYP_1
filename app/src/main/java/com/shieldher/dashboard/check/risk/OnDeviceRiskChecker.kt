package com.shieldher.dashboard.check.risk

import android.content.Context
import com.shieldher.dashboard.data.RiskBand
import com.shieldher.dashboard.data.RiskChecker
import com.shieldher.dashboard.data.RiskInput
import com.shieldher.dashboard.data.RiskOutcome
import com.shieldher.dashboard.data.RiskPlatform
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream

/**
 * On-device evaluator implementing [RiskChecker].
 * Loads the frozen model once lazily and evaluates completely offline.
 */
class OnDeviceRiskChecker(
    private val inputStreamProvider: () -> InputStream,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : RiskChecker {

    constructor(context: Context, dispatcher: CoroutineDispatcher = Dispatchers.Default) : this(
        inputStreamProvider = { context.assets.open(MODEL_ASSET_NAME) },
        dispatcher = dispatcher
    )

    companion object {
        const val MODEL_ASSET_NAME = "account_risk_model_v1.bin"
        val SUPPORTED_PLATFORMS = setOf(RiskPlatform.INSTAGRAM)

        @Volatile
        private var cachedEvaluator: RiskEvaluator? = null
        private val lock = Any()
    }

    private fun getOrLoadEvaluator(): RiskEvaluator? {
        cachedEvaluator?.let { return it }
        return synchronized(lock) {
            cachedEvaluator?.let { return it }
            try {
                inputStreamProvider().use { stream ->
                    val model = RiskModel.load(stream)
                    val evaluator = RiskEvaluator(model)
                    cachedEvaluator = evaluator
                    evaluator
                }
            } catch (_: Throwable) {
                // Never log errors per privacy guidelines
                null
            }
        }
    }

    override suspend fun check(input: RiskInput): RiskOutcome = withContext(dispatcher) {
        // 1. Platform null check
        if (input.platform == null) {
            return@withContext RiskOutcome.PlatformNotChosen
        }

        // 2. Platform supported check (without scoring)
        if (input.platform !in SUPPORTED_PLATFORMS) {
            return@withContext RiskOutcome.NotValidated
        }

        // 3. Null or invalid follower/following check
        if (input.followers == null || input.following == null) {
            return@withContext RiskOutcome.UnsupportedInput
        }

        val features = RiskFeatures.transform(
            followers = input.followers,
            following = input.following,
            descriptionLength = input.descriptionLength,
            hasLink = input.hasLink
        ) ?: return@withContext RiskOutcome.UnsupportedInput

        val evaluator = getOrLoadEvaluator() ?: return@withContext RiskOutcome.Unavailable

        val score = evaluator.evaluate(features)

        when (score.state) {
            RiskFeatures.STATE_UNSUPPORTED -> RiskOutcome.UnsupportedInput
            "uncertain_prediction" -> RiskOutcome.Uncertain
            "supported_prediction" -> {
                if (score.label == "risk") {
                    RiskOutcome.Result(RiskBand.HIGHER)
                } else {
                    RiskOutcome.Result(RiskBand.LOWER)
                }
            }
            else -> RiskOutcome.Unavailable
        }
    }
}
