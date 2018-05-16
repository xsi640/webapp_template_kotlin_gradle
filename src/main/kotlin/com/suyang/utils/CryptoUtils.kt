package com.suyang.utils

import org.apache.commons.codec.binary.Base64


/**
 * 密码工具类
 * @author Yang
 */
object CryptoUtils {
    private val saltSize = 32
    private val iterations = 1000
    private val subKeySize = 32

    /**
     * 获取 Salt
     * @return
     */
    val salt: String
        get() = Rfc2898DeriveBytes.generateSalt(saltSize)

    /**
     * 获取hash后的密码
     * @param password
     * @param salt
     * @return
     */
    fun getHash(password: String, salt: String): String {
        lateinit var keyGenerator: Rfc2898DeriveBytes
        try {
            keyGenerator = Rfc2898DeriveBytes(password + salt, saltSize, iterations)
        } catch (e1: Exception) {
            e1.printStackTrace()
        }

        val subKey = keyGenerator.getBytes(subKeySize)
        val bSalt = keyGenerator.salt
        val hashPassword = ByteArray(1 + saltSize + subKeySize)
        System.arraycopy(bSalt, 0, hashPassword, 1, saltSize)
        System.arraycopy(subKey, 0, hashPassword, saltSize + 1, subKeySize)
        return Base64.encodeBase64String(hashPassword)
    }

    /**
     * 验证密码
     * @param hashedPassword
     * @param password
     * @param salt
     * @return
     */
    fun verify(hashedPassword: String, password: String, salt: String): Boolean {
        val hashedPasswordBytes = Base64.decodeBase64(hashedPassword)
        if (hashedPasswordBytes.size != 1 + saltSize + subKeySize || hashedPasswordBytes[0].toInt() != 0x00) {
            return false
        }

        val bSalt = ByteArray(saltSize)
        System.arraycopy(hashedPasswordBytes, 1, bSalt, 0, saltSize)
        val storedSubkey = ByteArray(subKeySize)
        System.arraycopy(hashedPasswordBytes, 1 + saltSize, storedSubkey, 0, subKeySize)
        var deriveBytes: Rfc2898DeriveBytes? = null
        try {
            deriveBytes = Rfc2898DeriveBytes(password + salt, bSalt, iterations)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val generatedSubkey = deriveBytes!!.getBytes(subKeySize)
        return byteArraysEqual(storedSubkey, generatedSubkey)
    }

    private fun byteArraysEqual(storedSubkey: ByteArray, generatedSubkey: ByteArray): Boolean {
        val size = storedSubkey.size
        if (size != generatedSubkey.size) {
            return false
        }

        for (i in 0 until size) {
            if (storedSubkey[i] != generatedSubkey[i]) {
                return false
            }
        }
        return true
    }

}
