package com.suyang.utils

import org.apache.commons.codec.binary.Base64
import java.io.UnsupportedEncodingException
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.experimental.xor


/**
 * This implementation follows RFC 2898 recommendations. See
 * http://www.ietf.org/rfc/Rfc2898.txt
 */
class Rfc2898DeriveBytes {
    private var hmacsha1: Mac? = null
    var salt: ByteArray? = null
        private set
    private var iterations: Int = 0
    private var buffer = ByteArray(BLOCK_SIZE)
    private var startIndex = 0
    private var endIndex = 0
    private var block = 1

    val saltAsString: String
        get() = Base64.encodeBase64String(this.salt)

    /**
     * Creates new instance.
     *
     * @param password
     * The password used to derive the key.
     * @param salt
     * The key salt used to derive the key.
     * @param iterations
     * The number of iterations for the operation.
     * @throws NoSuchAlgorithmException
     * HmacSHA1 algorithm cannot be found.
     * @throws InvalidKeyException
     * Salt must be 8 bytes or more. -or- Password cannot be null.
     */
    @Throws(NoSuchAlgorithmException::class, InvalidKeyException::class)
    constructor(password: ByteArray, salt: ByteArray, iterations: Int) {
        this.salt = salt
        this.iterations = iterations
        this.hmacsha1 = Mac.getInstance("HmacSHA1")
        this.hmacsha1!!.init(SecretKeySpec(password, "HmacSHA1"))
    }

    /**
     * Creates new instance.
     *
     * @param password
     * The password used to derive the key.
     * @param salt
     * The key salt used to derive the key.
     * @param iterations
     * The number of iterations for the operation.
     * @throws NoSuchAlgorithmException
     * HmacSHA1 algorithm cannot be found.
     * @throws InvalidKeyException
     * Salt must be 8 bytes or more. -or- Password cannot be null.
     * @throws UnsupportedEncodingException
     */
    @Throws(NoSuchAlgorithmException::class, InvalidKeyException::class, UnsupportedEncodingException::class)
    @JvmOverloads constructor(password: String, saltSize: Int, iterations: Int = 1000) {
        this.salt = randomSalt(saltSize)
        this.iterations = iterations
        this.hmacsha1 = Mac.getInstance("HmacSHA1")
        this.hmacsha1!!.init(SecretKeySpec(password.toByteArray(charset("UTF-8")), "HmacSHA1"))
        this.buffer = ByteArray(BLOCK_SIZE)
        this.block = 1
        this.endIndex = 1
        this.startIndex = this.endIndex
    }

    /**
     * Creates new instance.
     *
     * @param password
     * The password used to derive the key.
     * @param salt
     * The key salt used to derive the key.
     * @param iterations
     * The number of iterations for the operation.
     * @throws NoSuchAlgorithmException
     * HmacSHA1 algorithm cannot be found.
     * @throws InvalidKeyException
     * Salt must be 8 bytes or more. -or- Password cannot be null.
     * @throws UnsupportedEncodingException
     * UTF-8 encoding is not supported.
     */
    @Throws(InvalidKeyException::class, NoSuchAlgorithmException::class, UnsupportedEncodingException::class)
    constructor(password: String, salt: ByteArray, iterations: Int) : this(password.toByteArray(charset("UTF8")), salt, iterations) {
    }

    /**
     * Returns a pseudo-random key from a data, salt and iteration count.
     *
     * @param cb
     * Number of bytes to return.
     * @return Byte array.
     */
    fun getBytes(cb: Int): ByteArray {
        val result = ByteArray(cb)
        var offset = 0
        val size = this.endIndex - this.startIndex
        if (size > 0) { // if there is some data in buffer
            if (cb >= size) { // if there is enough data in buffer
                System.arraycopy(this.buffer, this.startIndex, result, 0, size)
                this.endIndex = 0
                this.startIndex = this.endIndex
                offset += size
            } else {
                System.arraycopy(this.buffer, this.startIndex, result, 0, cb)
                startIndex += cb
                return result
            }
        }

        while (offset < cb) {
            val block = this.func()
            val remainder = cb - offset
            if (remainder > BLOCK_SIZE) {
                System.arraycopy(block, 0, result, offset, BLOCK_SIZE)
                offset += BLOCK_SIZE
            } else {
                System.arraycopy(block, 0, result, offset, remainder)
                offset += remainder
                System.arraycopy(block, remainder, this.buffer, startIndex, BLOCK_SIZE - remainder)
                endIndex += BLOCK_SIZE - remainder
                return result
            }
        }
        return result
    }

    private fun func(): ByteArray {
        this.hmacsha1!!.update(this.salt, 0, this.salt!!.size)
        var tempHash = this.hmacsha1!!.doFinal(getBytesFromInt(this.block))

        this.hmacsha1!!.reset()
        val finalHash = tempHash
        for (i in 2..this.iterations) {
            tempHash = this.hmacsha1!!.doFinal(tempHash)
            for (j in 0..19) {
                finalHash[j] = (finalHash[j] xor tempHash[j]).toByte()
            }
        }
        if (this.block == 2147483647) {
            this.block = -2147483648
        } else {
            this.block += 1
        }
        return finalHash
    }

    companion object {
        private val BLOCK_SIZE = 20
        private val random = Random()

        fun randomSalt(size: Int): ByteArray {
            val salt = ByteArray(size)
            random.nextBytes(salt)
            return salt
        }

        /**
         * Generate random Salt
         *
         * @param size
         * @return
         */
        fun generateSalt(size: Int): String {
            val salt = randomSalt(size)
            return Base64.encodeBase64String(salt)
        }

        private fun getBytesFromInt(i: Int): ByteArray {
            return byteArrayOf(i.ushr(24).toByte(), i.ushr(16).toByte(), i.ushr(8).toByte(), i.toByte())
        }
    }
}