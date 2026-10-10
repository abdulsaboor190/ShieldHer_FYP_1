package com.shieldher.vault.files

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import java.io.ByteArrayInputStream

class HashUtilTest {

    @Test
    fun sha256_emptyByteArray_returnsKnownHash() {
        val expected = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
        val actual = HashUtil.sha256(ByteArray(0))
        assertEquals(expected, actual)
    }

    @Test
    fun sha256_testString_returnsKnownHash() {
        val expected = "9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08"
        val actual = HashUtil.sha256("test".toByteArray(Charsets.UTF_8))
        assertEquals(expected, actual)
    }

    @Test
    fun sha256Stream_matchesSha256_forTestString() {
        val bytes = "test".toByteArray(Charsets.UTF_8)
        val streamHash = HashUtil.sha256Stream(ByteArrayInputStream(bytes))
        val directHash = HashUtil.sha256(bytes)
        assertEquals(directHash, streamHash)
    }

    @Test
    fun sha256Stream_matchesSha256_forMultiBuffer5MbPayload() {
        val fiveMb = 5 * 1024 * 1024
        val bytes = ByteArray(fiveMb) { 0x42.toByte() }
        val streamHash = HashUtil.sha256Stream(ByteArrayInputStream(bytes))
        val directHash = HashUtil.sha256(bytes)
        assertEquals(directHash, streamHash)
    }

    @Test
    fun sha256_isDeterministic_sameInputProducesSameHash() {
        val bytes = "deterministic-test-payload".toByteArray(Charsets.UTF_8)
        val firstHash = HashUtil.sha256(bytes)
        val secondHash = HashUtil.sha256(bytes)
        assertEquals(firstHash, secondHash)
    }

    @Test
    fun sha256_differingByOneByte_producesDifferentHash() {
        val original = "deterministic-test-payload".toByteArray(Charsets.UTF_8)
        val modified = original.clone()
        modified[0] = (modified[0].toInt() xor 1).toByte()

        val originalHash = HashUtil.sha256(original)
        val modifiedHash = HashUtil.sha256(modified)
        assertNotEquals(originalHash, modifiedHash)
    }
}
