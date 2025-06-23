package com.example.domain

import org.apache.commons.codec.binary.Base64
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import java.nio.charset.StandardCharsets

class EncryptionUtils(private val key: String = KEY) {

    /**
     * Функция шифрует строку с использованием AES-ECB-PKCS5Padding.
     */
    fun encrypt(text: String): String {
        val secretKey = SecretKeySpec(key.toByteArray(StandardCharsets.UTF_8), "AES")
        val cipher = Cipher.getInstance(INSTANCE_STRING)

        cipher.init(Cipher.ENCRYPT_MODE, secretKey)

        // Шифруем данные
        val encryptedData = cipher.doFinal(text.toByteArray(StandardCharsets.UTF_8))

        return Base64.encodeBase64String(encryptedData)
    }
    /**
     * Функция дешифрует строку.
     */
    fun decrypt(text: String): String {

        val decodedBytes = Base64.decodeBase64(text)

        val secretKey = SecretKeySpec(key.toByteArray(StandardCharsets.UTF_8), "AES")
        val cipher = Cipher.getInstance(INSTANCE_STRING)

        cipher.init(Cipher.DECRYPT_MODE, secretKey)

        // Дешифруем данные
        val decryptedData = cipher.doFinal(decodedBytes)
        return String(decryptedData, StandardCharsets.UTF_8)
    }

    companion object {
        private const val INSTANCE_STRING = "AES/ECB/PKCS5Padding"
        private const val KEY = "Ed49Kor65Fn!dM823Ytr93WaVcbn1azX"
    }

}