package com.shieldher.dashboard.check.risk

import com.shieldher.dashboard.data.RiskInput
import com.shieldher.dashboard.data.RiskPlatform

/**
 * Pure functions turning raw screen inputs into [RiskInput].
 * Ensures privacy: bio text is converted to code point count and immediately discarded.
 */
object RiskInputParser {

    private const val MAX_DIGITS = 15

    /**
     * Parse follower or following text into a valid positive Long.
     * Blank, non-numeric, negative, commas, or numbers exceeding 15 digits return null.
     */
    fun parseCount(text: String?): Long? {
        if (text.isNullOrBlank()) return null
        val trimmed = text.trim()

        // Reject commas or other separators
        if (trimmed.contains(",") || trimmed.contains(" ") || trimmed.contains(".")) {
            return null
        }

        // Only pure digits allowed
        if (!trimmed.all { it.isDigit() }) {
            return null
        }

        // Reject if more than 15 digits
        if (trimmed.length > MAX_DIGITS) {
            return null
        }

        return trimmed.toLongOrNull()?.takeIf { it >= 0 }
    }

    /**
     * Compute Unicode code points count matching Python's len(str).
     * The input string is never stored.
     */
    fun parseBioLength(bioText: String?): Int {
        if (bioText.isNullOrEmpty()) return 0
        return bioText.codePointCount(0, bioText.length)
    }

    /**
     * Parse the complete form inputs into a [RiskInput].
     */
    fun parse(
        platform: RiskPlatform?,
        followersText: String?,
        followingText: String?,
        bioText: String?,
        hasLink: Boolean
    ): RiskInput {
        return RiskInput(
            platform = platform,
            followers = parseCount(followersText),
            following = parseCount(followingText),
            descriptionLength = parseBioLength(bioText),
            hasLink = hasLink
        )
    }
}
