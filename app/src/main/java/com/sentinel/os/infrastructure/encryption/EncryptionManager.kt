package com.sentinel.os.infrastructure.encryption

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import timber.log.Timber

/**
 * Manages AES-256 GCM encryption using Android Keystore.
 * All encryption keys are stored securely in the device keystore.
 */
class EncryptionManager {
    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
    private val keyAlias = "SentinelOS_Key"
    private val cipher = Cipher.getInstance("AES/GCM/NoPadding")
    private val gcmTagLength = 128 // 128 bits = 16 bytes

    init {
        ensureKeyExists()
    }

    private fun ensureKeyExists() {
        if (!keyStore.containsAlias(keyAlias)) {
            generateKey()
        }
    }

    private fun generateKey() {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            "AndroidKeyStore"
        )

        val keySpec = KeyGenParameterSpec.Builder(
            keyAlias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setKeySize(256)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .build()

        keyGenerator.init(keySpec)
        keyGenerator.generateKey()
        Timber.d("Encryption key generated")
    }

    private fun getKey(): SecretKey {
        return (keyStore.getKey(keyAlias, null) as? SecretKey)
            ?: throw IllegalStateException("Failed to retrieve encryption key")
    }

    fun encrypt(plaintext: ByteArray): ByteArray {
        return try {
            val key = getKey()
            cipher.init(Cipher.ENCRYPT_MODE, key)

            val ciphertext = cipher.doFinal(plaintext)
            val iv = cipher.iv

            // Prepend IV to ciphertext for decryption
            iv + ciphertext
        } catch (e: Exception) {
            Timber.e(e, "Encryption failed")
            throw e
        }
    }

    fun decrypt(encryptedData: ByteArray): ByteArray {
        return try {
            val key = getKey()
            val iv = encryptedData.sliceArray(0 until 12) // GCM IV is 12 bytes
            val ciphertext = encryptedData.sliceArray(12 until encryptedData.size)

            val gcmSpec = GCMParameterSpec(gcmTagLength, iv)
            cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec)

            cipher.doFinal(ciphertext)
        } catch (e: Exception) {
            Timber.e(e, "Decryption failed")
            throw e
        }
    }

    fun encryptString(plaintext: String): String {
        val encrypted = encrypt(plaintext.toByteArray(Charsets.UTF_8))
        return android.util.Base64.encodeToString(encrypted, android.util.Base64.DEFAULT)
    }

    fun decryptString(encryptedString: String): String {
        val encrypted = android.util.Base64.decode(encryptedString, android.util.Base64.DEFAULT)
        val decrypted = decrypt(encrypted)
        return String(decrypted, Charsets.UTF_8)
    }
}
