package com.ibit.chat.api.util

import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object AESUtils {
    private const val AES_CHARS = "ABCDEFGHJKMNPQRSTWXYZabcdefhijkmnprstwxyz2345678"

    private fun randomString(length: Int): String {
        val random = SecureRandom()
        return buildString(length) {
            repeat(length) {
                append(AES_CHARS[random.nextInt(AES_CHARS.length)])
            }
        }
    }

    private fun encryptAES(data: String, key: String, iv: String): String {
        val keyBytes = key.toByteArray(Charsets.UTF_8)
        val ivBytes = iv.toByteArray(Charsets.UTF_8)
        val dataBytes = data.toByteArray(Charsets.UTF_8)

        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val secretKey = SecretKeySpec(keyBytes, "AES")
        val ivSpec = IvParameterSpec(ivBytes)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec)
        val encrypted = cipher.doFinal(dataBytes)
        return Base64.getEncoder().encodeToString(encrypted)
    }

    fun encryptPassword(password: String, salt: String): String {
        return if (salt.isEmpty()) password
        else {
            val data = randomString(64) + password
            encryptAES(data, salt, randomString(16))
        }
    }
} 
