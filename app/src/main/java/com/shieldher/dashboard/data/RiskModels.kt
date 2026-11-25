package com.shieldher.dashboard.data

/**
 * Supported platforms for the Account Risk check.
 * Note: Model was trained and validated on Instagram only.
 */
enum class RiskPlatform {
    INSTAGRAM,
    X_TWITTER,
    OTHER
}

/**
 * Parsed inputs for the risk check.
 * No probability, percentage or score fields are stored here.
 */
data class RiskInput(
    val platform: RiskPlatform?,
    val followers: Long?,
    val following: Long?,
    val descriptionLength: Int,
    val hasLink: Boolean
)

/**
 * Qualitative risk band returned when an answered prediction is made.
 */
enum class RiskBand {
    HIGHER,
    LOWER
}

/**
 * Sealed outcome representing all possible results of the Account Risk check.
 * No probability, score or percentage field exists in these types.
 */
sealed interface RiskOutcome {
    data class Result(val band: RiskBand) : RiskOutcome
    object Uncertain : RiskOutcome
    object UnsupportedInput : RiskOutcome
    object NotValidated : RiskOutcome
    object PlatformNotChosen : RiskOutcome
    object Unavailable : RiskOutcome
}

/**
 * Interface for running the on-device or fake risk check.
 */
interface RiskChecker {
    suspend fun check(input: RiskInput): RiskOutcome
}
