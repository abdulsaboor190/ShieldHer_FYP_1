package com.shieldher.dashboard.check.risk

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.security.MessageDigest

class RiskModelAssetTest {

    @Test
    fun binaryModelHeaderAndIntegrityMatchManifest() {
        // Asset file in app module
        val binFile = File("src/main/assets/account_risk_model_v1.bin").let {
            if (it.exists()) it else File("app/src/main/assets/account_risk_model_v1.bin")
        }
        assertTrue("Asset file exists: ${binFile.absolutePath}", binFile.exists())

        val bytes = binFile.readBytes()
        assertTrue("Binary has at least 16 bytes", bytes.size >= 16)

        // Verify magic and format version
        val magic = String(bytes.sliceArray(0 until 4), Charsets.US_ASCII)
        assertEquals("SHRM", magic)

        val version = (bytes[4].toInt() and 0xFF) or
                ((bytes[5].toInt() and 0xFF) shl 8) or
                ((bytes[6].toInt() and 0xFF) shl 16) or
                ((bytes[7].toInt() and 0xFF) shl 24)
        assertEquals(1, version)

        // Compute SHA-256
        val digest = MessageDigest.getInstance("SHA-256")
        val sha256Bytes = digest.digest(bytes)
        val computedSha256 = sha256Bytes.joinToString("") { "%02x".format(it) }

        // Read manifest recorded sha256
        val manifestFile = File("../docs/stage3/account_risk_model_v1.manifest.json").let {
            if (it.exists()) it else File("docs/stage3/account_risk_model_v1.manifest.json")
        }
        assertTrue("Manifest exists: ${manifestFile.absolutePath}", manifestFile.exists())

        val manifestText = manifestFile.readText()
        // Extract "sha256": "..."
        val regex = Regex("\"sha256\"\\s*:\\s*\"([a-fA-F0-9]{64})\"")
        val match = regex.find(manifestText)
        assertTrue("Found sha256 in manifest", match != null)

        val recordedSha256 = match!!.groupValues[1].lowercase()
        assertEquals("Asset SHA-256 must match manifest", recordedSha256, computedSha256)
    }
}
