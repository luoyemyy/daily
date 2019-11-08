package com.github.luoyemyy.daily.util

import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object AesUtils {

    private const val AES_KEY = "daily0daily12345"
    private const val AES = "AES"
    private const val AES_ALG = "AES/CBC/PKCS5Padding"


    private fun getKey(): SecretKeySpec {
        return SecretKeySpec(AES_KEY.toByteArray(Charsets.UTF_8), AES)
    }

    private fun iv(): IvParameterSpec {
        return IvParameterSpec(AES_KEY.toByteArray(Charsets.UTF_8))
    }

    fun decrypt(content: String): String? {
        val key = getKey()
        val cipher = Cipher.getInstance(AES_ALG)
        cipher.init(Cipher.DECRYPT_MODE, key, iv())
        return try {
            cipher.doFinal(Base64.decode(content, Base64.DEFAULT)).toString(Charsets.UTF_8)
        } catch (e: Throwable) {
            null
        }
    }

    fun encrypt(content: String): String {
        val secretKeySpec = getKey()
        val cipher = Cipher.getInstance(AES_ALG)
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, iv())
        return cipher.doFinal(content.toByteArray(Charsets.UTF_8)).let {
            Base64.encode(it, Base64.DEFAULT).toString(Charsets.UTF_8)
        }
    }
}