package com.shieldher.vault.files

import java.io.InputStream
import java.security.DigestInputStream
import java.security.MessageDigest

object HashUtil {

    fun sha256(bytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(bytes).toHex()
    }

    fun sha256Stream(input: InputStream): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val buffer = ByteArray(8192)
        DigestInputStream(input, digest).use { dis ->
            while (dis.read(buffer) != -1) {
                // reading to feed the digest; bytes discarded
            }
        }
        return digest.digest().toHex()
    }

    private fun ByteArray.toHex(): String =
        joinToString(separator = "") { byte -> "%02x".format(byte) }
}
