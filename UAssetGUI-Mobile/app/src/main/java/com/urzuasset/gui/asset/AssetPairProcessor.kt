package com.urzuasset.gui.asset

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

private val KNOWN_TYPES = listOf(
    "BoolProperty",
    "IntProperty",
    "FloatProperty",
    "StrProperty",
    "NameProperty",
    "ArrayProperty",
    "StructProperty"
)

data class AssetLineRecord(
    val index: Int,
    val offsetHex: String,
    val nameMapValue: String,
    val type: String,
    val value: String
)

data class AssetPairData(
    val baseName: String,
    val uassetName: String,
    val uexpName: String,
    val ubulkName: String?,
    val records: List<AssetLineRecord>,
    val nameMapEntries: List<String>,
    val warnings: List<String>,
    val summaryLines: List<String>
)

object AssetPairProcessor {

    fun loadPair(
        baseName: String,
        uassetName: String,
        uassetBytes: ByteArray,
        uexpName: String,
        uexpBytes: ByteArray,
        ubulkName: String? = null,
        ubulkBytes: ByteArray? = null
    ): AssetPairData {
        val summary = mutableListOf<String>()
        val warnings = mutableListOf<String>()

        val header = parseHeader(uassetBytes)
        summary += "Magic: 0x${header.magic.toString(16)}"
        summary += "Version: ${header.version}"
        summary += "NameCount: ${header.nameCount}"
        summary += "NameOffset: 0x${header.nameOffset.toString(16)}"

        val names = parseNameMap(uassetBytes, header.nameOffset, header.nameCount)
        if (names.isEmpty()) {
            warnings += "NameMap okunamadı, ham tarama kullanıldı."
        }

        val records = parseUexpProperties(uexpBytes, names.ifEmpty { fallbackNames(uexpBytes) })
        if (records.any { it.type == "Raw/Unknown" }) {
            warnings += "Bazı alanlar çözümlenemedi (Raw)."
        }

        ubulkBytes?.let {
            summary += "UBULK: ${ubulkName ?: "(adsız)"} (${it.size} bytes)"
        }

        return AssetPairData(
            baseName = baseName,
            uassetName = uassetName,
            uexpName = uexpName,
            ubulkName = ubulkName,
            records = records,
            nameMapEntries = names,
            warnings = warnings,
            summaryLines = summary
        )
    }

    private data class HeaderInfo(
        val magic: Int,
        val version: Int,
        val nameCount: Int,
        val nameOffset: Int
    )

    private fun parseHeader(bytes: ByteArray): HeaderInfo {
        val bb = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
        fun intAt(offset: Int): Int = if (offset + 4 <= bytes.size) bb.getInt(offset) else 0

        val magic = intAt(0)
        val version = intAt(8)

        val candidates = listOf(
            intAt(0x20) to intAt(0x24),
            intAt(0x28) to intAt(0x2C),
            intAt(0x38) to intAt(0x3C),
            intAt(0x40) to intAt(0x44)
        )
        val picked = candidates.firstOrNull { (count, offset) ->
            count in 1..1_000_000 && offset in 0 until bytes.size
        } ?: (0 to 0)

        return HeaderInfo(
            magic = magic,
            version = version,
            nameCount = picked.first,
            nameOffset = picked.second
        )
    }

    private fun parseNameMap(bytes: ByteArray, nameOffset: Int, nameCount: Int): List<String> {
        if (nameCount <= 0 || nameOffset !in bytes.indices) return emptyList()
        val out = ArrayList<String>(nameCount)
        val bb = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
        var cursor = nameOffset

        repeat(nameCount) {
            if (cursor + 4 > bytes.size) return@repeat
            val len = bb.getInt(cursor)
            cursor += 4
            if (len == 0) return@repeat

            val name = if (len > 0) {
                val byteLen = len
                if (cursor + byteLen > bytes.size) return@repeat
                val s = String(bytes, cursor, byteLen - 1, StandardCharsets.UTF_8)
                cursor += byteLen
                s
            } else {
                val charLen = -len
                val byteLen = charLen * 2
                if (cursor + byteLen > bytes.size) return@repeat
                val s = String(bytes, cursor, byteLen - 2, Charset.forName("UTF-16LE"))
                cursor += byteLen
                s
            }
            out += name

            if (cursor + 4 <= bytes.size) {
                cursor += 4
            }
        }

        return out.filter { it.isNotBlank() }.distinct()
    }

    private fun fallbackNames(bytes: ByteArray): List<String> {
        val strings = mutableListOf<String>()
        var i = 0
        while (i < bytes.size) {
            val b = bytes[i].toInt() and 0xFF
            if (b in 32..126) {
                val s = i
                while (i < bytes.size && ((bytes[i].toInt() and 0xFF) in 32..126)) i++
                val len = i - s
                if (len >= 4) {
                    strings += String(bytes, s, len, StandardCharsets.UTF_8)
                }
            } else {
                i++
            }
        }
        return strings.distinct().take(2000)
    }

    private fun parseUexpProperties(uexp: ByteArray, names: List<String>): List<AssetLineRecord> {
        val rows = mutableListOf<AssetLineRecord>()
        val nameSet = names.toSet()
        var idx = 0
        var offset = 0
        while (offset + 8 < uexp.size) {
            val preview = extractAscii(uexp, offset, 96)
            val type = KNOWN_TYPES.firstOrNull { preview.contains(it) }
            val pickedName = names.firstOrNull { it.length >= 3 && preview.contains(it) }

            if (type != null && pickedName != null) {
                val value = preview.substringAfter(type, "").trim().take(120)
                rows += AssetLineRecord(
                    index = idx++,
                    offsetHex = "0x${offset.toString(16)}",
                    nameMapValue = pickedName,
                    type = type,
                    value = if (value.isBlank()) "-" else value
                )
                offset += 32
                continue
            }

            val chunk = uexp.copyOfRange(offset, minOf(offset + 16, uexp.size))
            rows += AssetLineRecord(
                index = idx++,
                offsetHex = "0x${offset.toString(16)}",
                nameMapValue = names.getOrNull(idx % maxOf(nameSet.size, 1)) ?: "Unknown",
                type = "Raw/Unknown",
                value = "(${chunk.size} bytes) ${chunk.joinToString(" ") { "%02X".format(it) }}"
            )
            offset += 16
        }
        return rows
    }

    private fun extractAscii(bytes: ByteArray, offset: Int, length: Int): String {
        val end = minOf(bytes.size, offset + length)
        if (offset >= end) return ""
        val sb = StringBuilder(end - offset)
        for (i in offset until end) {
            val b = bytes[i].toInt() and 0xFF
            if (b in 32..126) sb.append(b.toChar()) else sb.append(' ')
        }
        return sb.toString()
    }
}
