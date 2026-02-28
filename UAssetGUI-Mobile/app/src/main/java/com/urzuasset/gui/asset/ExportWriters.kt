package com.urzuasset.gui.asset

import java.io.BufferedWriter
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets

object ExportWriters {

    fun writeTxt(entries: List<LocalizedTextEntry>, outputFile: File): File {
        outputFile.parentFile?.mkdirs()
        outputFile.bufferedWriter(StandardCharsets.UTF_8).use { writer ->
            writer.appendLine("# Namespace bazlı FText export")
            entries.groupBy { it.namespace }.toSortedMap().forEach { (namespace, nsEntries) ->
                writer.appendLine("[$namespace]")
                nsEntries.sortedBy { it.key }.forEach { entry ->
                    writer.appendLine("${entry.key}=${entry.value}")
                    writer.appendLine("; ascii_crc32=${entry.asciiCrc32} unicode_crc32=${entry.unicodeCrc32}")
                }
                writer.appendLine()
            }
        }
        return outputFile
    }

    fun writeLocresLike(entries: List<LocalizedTextEntry>, outputFile: File): File {
        outputFile.parentFile?.mkdirs()
        outputFile.outputStream().use { stream ->
            stream.write("LOCRES-LITE\u0000".toByteArray(StandardCharsets.US_ASCII))
            stream.write(intLe(entries.size))
            for (entry in entries) {
                writeString(stream, entry.namespace)
                writeString(stream, entry.key)
                writeString(stream, entry.value)
                stream.write(longLe(entry.asciiCrc32))
                stream.write(longLe(entry.unicodeCrc32))
            }
        }
        return outputFile
    }

    fun writeBinaryDump(input: File, output: File): File {
        output.parentFile?.mkdirs()
        val bytes = input.readBytes()
        BufferedWriter(output.writer(StandardCharsets.UTF_8)).use { writer ->
            writer.appendLine("# Dump: ${input.absolutePath}")
            writer.appendLine("# Size: ${bytes.size} bytes")
            var offset = 0
            while (offset < bytes.size) {
                val sliceLen = minOf(16, bytes.size - offset)
                val slice = bytes.copyOfRange(offset, offset + sliceLen)
                val hex = slice.joinToString(" ") { "%02X".format(it) }
                val ascii = slice.joinToString("") {
                    val c = it.toInt() and 0xFF
                    if (c in 32..126) c.toChar().toString() else "."
                }
                writer.appendLine("%08X  %-47s |%s|".format(offset, hex, ascii))
                offset += sliceLen
            }
        }
        return output
    }

    private fun writeString(stream: java.io.OutputStream, value: String) {
        val bytes = value.toByteArray(StandardCharsets.UTF_8)
        stream.write(intLe(bytes.size))
        stream.write(bytes)
    }

    private fun intLe(value: Int): ByteArray =
        ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array()

    private fun longLe(value: Long): ByteArray =
        ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putLong(value).array()
}
