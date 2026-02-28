package com.urzuasset.gui.asset

import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.zip.CRC32

object Crc32Hasher {
    private val utf16le: Charset = StandardCharsets.UTF_16LE

    fun computeAscii(input: String): Long = compute(input.toByteArray(StandardCharsets.US_ASCII))

    fun computeUnicode(input: String): Long = compute(input.toByteArray(utf16le))

    fun computeUtf8(input: String): Long = compute(input.toByteArray(StandardCharsets.UTF_8))

    private fun compute(bytes: ByteArray): Long {
        val crc = CRC32()
        crc.update(bytes)
        return crc.value
    }
}
