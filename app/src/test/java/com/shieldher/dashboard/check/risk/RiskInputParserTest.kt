package com.shieldher.dashboard.check.risk

import com.shieldher.dashboard.data.RiskPlatform
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RiskInputParserTest {

    @Test
    fun parseCountValidNumbers() {
        assertEquals(0L, RiskInputParser.parseCount("0"))
        assertEquals(12L, RiskInputParser.parseCount("12"))
        assertEquals(5000L, RiskInputParser.parseCount("  5000  "))
        assertEquals(999999999999999L, RiskInputParser.parseCount("999999999999999")) // 15 digits
    }

    @Test
    fun parseCountInvalidOrRejected() {
        assertNull("Blank returns null", RiskInputParser.parseCount(""))
        assertNull("Whitespace returns null", RiskInputParser.parseCount("   "))
        assertNull("Null returns null", RiskInputParser.parseCount(null))
        assertNull("Commas are rejected", RiskInputParser.parseCount("1,000"))
        assertNull("Alphabetic rejected", RiskInputParser.parseCount("abc"))
        assertNull("Negative rejected", RiskInputParser.parseCount("-5"))
        assertNull("Decimals rejected", RiskInputParser.parseCount("12.5"))
        assertNull("More than 15 digits rejected", RiskInputParser.parseCount("1234567890123456")) // 16 digits
    }

    @Test
    fun parseBioLengthCountsCodePoints() {
        assertEquals(0, RiskInputParser.parseBioLength(""))
        assertEquals(0, RiskInputParser.parseBioLength(null))
        assertEquals(5, RiskInputParser.parseBioLength("hello"))

        // Multi-byte Unicode code point (e.g. emoji which is 2 UTF-16 chars but 1 code point)
        val emojiString = "Hi 🛡️"
        // 'H', 'i', ' ', emoji + variation selector
        val codePoints = emojiString.codePointCount(0, emojiString.length)
        assertEquals(codePoints, RiskInputParser.parseBioLength(emojiString))

        val singleEmoji = "😊" // surrogate pair: length=2, codePointCount=1
        assertEquals(2, singleEmoji.length)
        assertEquals(1, RiskInputParser.parseBioLength(singleEmoji))
    }

    @Test
    fun parseFullInput() {
        val input = RiskInputParser.parse(
            platform = RiskPlatform.INSTAGRAM,
            followersText = "250",
            followingText = "100",
            bioText = "Test bio",
            hasLink = true
        )
        assertEquals(RiskPlatform.INSTAGRAM, input.platform)
        assertEquals(250L, input.followers)
        assertEquals(100L, input.following)
        assertEquals(8, input.descriptionLength)
        assertTrue(input.hasLink)
    }
}
