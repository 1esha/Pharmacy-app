package com.example.domain

import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

class EncryptionUtils(private val key: String = KEY) {

    // Функция шифровки строки
    fun encrypt(text: String): String {
        val secretKey = SecretKeySpec(key.toByteArray(), "AES")
        val cipher = Cipher.getInstance(INSTANCE_STRING)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)

        val encryptedData = cipher.doFinal(text.toByteArray(Charsets.UTF_8))
        return Base64.getEncoder().encodeToString(encryptedData)
    }

    // Функция дешифровки строки
    fun decrypt(text: String): String {
        val decodedBytes = Base64.getDecoder().decode(text)
        val secretKey = SecretKeySpec(key.toByteArray(), "AES")
        val cipher = Cipher.getInstance(INSTANCE_STRING)
        cipher.init(Cipher.DECRYPT_MODE, secretKey)

        val decryptedData = cipher.doFinal(decodedBytes)
        return String(decryptedData, Charsets.UTF_8)
    }

    companion object {
        private const val INSTANCE_STRING = "AES/ECB/PKCS5Padding"
        private const val KEY = "Ed49Kor65Fn!dM823Ytr93WaVcbn1azX"
    }
}