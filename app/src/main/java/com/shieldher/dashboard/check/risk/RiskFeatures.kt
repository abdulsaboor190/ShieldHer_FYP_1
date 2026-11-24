package com.shieldher.dashboard.check.risk

import kotlin.math.ln1p

/**
 * Feature transformation utilities for Account Risk model (Mask A).
 * Order of 5 features:
 * 0: followers (ln1p)
 * 1: following (ln1p)
 * 2: description_length (Double)
 * 3: external_url (Double, 0.0 or 1.0)
 * 4: follower_following_ratio = ln1p(followers / (following + 1))
 */
internal object RiskFeatures {

    const val STATE_UNSUPPORTED = "unsupported_input"

    /**
     * Transforms raw inputs into model features array of size 5.
     * Returns null if inputs are missing or invalid (triggering unsupported_input state).
     */
    fun transform(
        followers: Long?,
        following: Long?,
        descriptionLength: Int,
        hasLink: Boolean
    ): DoubleArray? {
        // Missing (null) counts
        if (followers == null || following == null) {
            return null
        }

        // Negative counts or negative bio length are invalid
        if (followers < 0 || following < 0 || descriptionLength < 0) {
            return null
        }

        val fw = followers.toDouble()
        val fg = following.toDouble()
        val ratio = fw / (fg + 1.0)

        return doubleArrayOf(
            ln1p(fw),
            ln1p(fg),
            descriptionLength.toDouble(),
            if (hasLink) 1.0 else 0.0,
            ln1p(ratio)
        )
    }
}
