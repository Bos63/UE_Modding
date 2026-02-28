package com.urzuasset.gui.asset

import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets

object UnrealLocalizationProcessor {

    private val textPattern = Regex("NS=([^;\\n\\r]+);KEY=([^;\\n\\r]+);VAL=([^\\n\\r]+)")

    fun processFiles(uassetPath: String, uexpPath: String): LocalizationValidationResult {
        val uassetEntries = parseFile(File(uassetPath))
        val uexpEntries = parseFile(File(uexpPath))
        return validateEntries(uassetEntries + uexpEntries)
    }

    private fun parseFile(file: File): List<LocalizedTextEntry> {
        if (!file.exists() || !file.isFile) return emptyList()

        val bytes = file.readBytes()
        val utf8Strings = extractUtf8Strings(bytes)
        val utf16Strings = extractUtf16LeStrings(bytes)
        val combined = (utf8Strings + utf16Strings)
            .groupBy { it.first }
            .mapValues { group -> group.value.minBy { it.second }.second }

        val entries = mutableListOf<LocalizedTextEntry>()
        combined.forEach { (raw, offset) ->
            val match = textPattern.find(raw) ?: return@forEach
            val namespace = match.groupValues[1].trim()
            val key = match.groupValues[2].trim()
            val value = match.groupValues[3].trim()
            if (namespace.isEmpty() || key.isEmpty() || value.isEmpty()) return@forEach

            val keyId = "$namespace::$key"
            entries += LocalizedTextEntry(
                namespace = namespace,
                key = key,
                value = value,
                sourceFile = file.name,
                byteOffset = offset.toLong(),
                asciiCrc32 = Crc32Hasher.computeAscii(keyId),
                unicodeCrc32 = Crc32Hasher.computeUnicode(keyId)
            )
        }

        return entries.sortedBy { it.byteOffset }
    }

    private fun validateEntries(entries: List<LocalizedTextEntry>): LocalizationValidationResult {
        val issues = mutableListOf<ValidationIssue>()
        val seenByAscii = mutableMapOf<Long, LocalizedTextEntry>()
        val seenByUnicode = mutableMapOf<Long, LocalizedTextEntry>()

        for (entry in entries) {
            val keyId = "${entry.namespace}::${entry.key}"
            val asciiExpected = Crc32Hasher.computeAscii(keyId)
            val unicodeExpected = Crc32Hasher.computeUnicode(keyId)

            if (entry.asciiCrc32 != asciiExpected || entry.unicodeCrc32 != unicodeExpected) {
                issues += ValidationIssue(entry.namespace, entry.key, "CRC hesaplaması uyuşmuyor")
            }

            val duplicateAscii = seenByAscii[entry.asciiCrc32]
            if (duplicateAscii != null && duplicateAscii.key != entry.key) {
                issues += ValidationIssue(
                    entry.namespace,
                    entry.key,
                    "ASCII CRC32 çakışması: ${duplicateAscii.namespace}::${duplicateAscii.key}"
                )
            } else {
                seenByAscii[entry.asciiCrc32] = entry
            }

            val duplicateUnicode = seenByUnicode[entry.unicodeCrc32]
            if (duplicateUnicode != null && duplicateUnicode.key != entry.key) {
                issues += ValidationIssue(
                    entry.namespace,
                    entry.key,
                    "Unicode CRC32 çakışması: ${duplicateUnicode.namespace}::${duplicateUnicode.key}"
                )
            } else {
                seenByUnicode[entry.unicodeCrc32] = entry
            }
        }

        return LocalizationValidationResult(entries = entries, issues = issues)
    }

    private fun extractUtf8Strings(bytes: ByteArray, minLen: Int = 8): List<Pair<String, Int>> {
        val out = mutableListOf<Pair<String, Int>>()
        var start = -1
        for (i in bytes.indices) {
            val b = bytes[i].toInt() and 0xFF
            val printable = b in 0x20..0x7E || b == 0x09
            if (printable) {
                if (start < 0) start = i
            } else if (start >= 0) {
                if (i - start >= minLen) {
                    val s = String(bytes, start, i - start, StandardCharsets.UTF_8)
                    out += s to start
                }
                start = -1
            }
        }
        if (start >= 0 && bytes.size - start >= minLen) {
            out += String(bytes, start, bytes.size - start, StandardCharsets.UTF_8) to start
        }
        return out
    }

    private fun extractUtf16LeStrings(bytes: ByteArray, minChars: Int = 4): List<Pair<String, Int>> {
        val out = mutableListOf<Pair<String, Int>>()
        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
        var i = 0
        while (i + 1 < buffer.limit()) {
            val start = i
            val chars = StringBuilder()
            var j = i
            while (j + 1 < buffer.limit()) {
                val code = buffer.getShort(j).toInt() and 0xFFFF
                if (code == 0) break
                if (code in 0x20..0x7E || code in 0x80..0xD7FF || code in 0xE000..0xFFFD) {
                    chars.append(code.toChar())
                    j += 2
                } else {
                    chars.clear()
                    break
                }
            }

            if (chars.length >= minChars) {
                out += chars.toString() to start
                i = j + 2
            } else {
                i += 2
            }
        }
        return out
    }
}
