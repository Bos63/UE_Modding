package com.urzuasset.gui.asset

import java.io.File
import java.nio.charset.StandardCharsets

private const val CHUNK_SIZE = 16

data class AssetLineRecord(
    val index: Int,
    val displayOffset: String,
    val nameMapValue: String,
    val type: String,
    val value: String,
    val sourceFilePath: String,
    val absoluteOffset: Int,
    val reservedLength: Int
)

data class AssetPairData(
    val records: List<AssetLineRecord>,
    val nameMapEntries: List<String>,
    val uassetFile: File,
    val uexpFile: File
)

object AssetPairProcessor {

    fun loadPair(uassetFile: File, uexpFile: File): AssetPairData {
        val uassetBytes = uassetFile.readBytes()
        val uexpBytes = uexpFile.readBytes()

        val uassetStrings = extractStrings(uassetBytes)
        val uexpStrings = extractStrings(uexpBytes)
        val nameMap = (uassetStrings.map { it.text } + uexpStrings.map { it.text }).distinct().sorted()

        val records = mutableListOf<AssetLineRecord>()
        addRecords(records, uassetFile, uassetBytes, uassetStrings)
        addRecords(records, uexpFile, uexpBytes, uexpStrings)

        return AssetPairData(records = records, nameMapEntries = nameMap, uassetFile = uassetFile, uexpFile = uexpFile)
    }

    fun writeUpdatedValue(record: AssetLineRecord, newValue: String) {
        val file = File(record.sourceFilePath)
        val bytes = file.readBytes()
        val encoded = newValue.toByteArray(StandardCharsets.UTF_8)
        val fixed = ByteArray(record.reservedLength)
        val copyLength = minOf(record.reservedLength, encoded.size)
        System.arraycopy(encoded, 0, fixed, 0, copyLength)

        if (record.absoluteOffset < 0 || record.absoluteOffset + record.reservedLength > bytes.size) {
            throw IllegalStateException("Geçersiz dosya offset'i")
        }

        val backup = File(file.parentFile, "${file.name}.bak")
        if (!backup.exists()) {
            backup.writeBytes(bytes)
        }

        System.arraycopy(fixed, 0, bytes, record.absoluteOffset, record.reservedLength)
        file.writeBytes(bytes)
    }

    fun writeDumpTxt(pairData: AssetPairData, outputFile: File) {
        outputFile.parentFile?.mkdirs()
        outputFile.bufferedWriter(StandardCharsets.UTF_8).use { writer ->
            writer.appendLine("# Dump çıktısı")
            writer.appendLine("# UAsset: ${pairData.uassetFile.absolutePath}")
            writer.appendLine("# UEXP: ${pairData.uexpFile.absolutePath}")
            pairData.records.forEach { line ->
                writer.appendLine("${line.index}\t${line.displayOffset}\t${line.nameMapValue}\t${line.type}\t${line.value}")
            }
        }
    }

    private data class LocatedString(val text: String, val start: Int, val length: Int, val type: String)

    private fun addRecords(
        target: MutableList<AssetLineRecord>,
        sourceFile: File,
        bytes: ByteArray,
        locatedStrings: List<LocatedString>
    ) {
        val byOffset = locatedStrings.associateBy { it.start }
        var localIndex = 0
        var offset = 0
        while (offset < bytes.size) {
            val hit = byOffset[offset]
            if (hit != null) {
                target += AssetLineRecord(
                    index = target.size,
                    displayOffset = "${sourceFile.name}:0x${offset.toString(16)}",
                    nameMapValue = hit.text,
                    type = hit.type,
                    value = hit.text,
                    sourceFilePath = sourceFile.absolutePath,
                    absoluteOffset = hit.start,
                    reservedLength = hit.length
                )
                offset += hit.length
            } else {
                val len = minOf(CHUNK_SIZE, bytes.size - offset)
                val chunk = bytes.copyOfRange(offset, offset + len)
                target += AssetLineRecord(
                    index = target.size,
                    displayOffset = "${sourceFile.name}:0x${offset.toString(16)}",
                    nameMapValue = "(ham-kayıt-${localIndex++})",
                    type = "HEX",
                    value = chunk.joinToString(" ") { "%02X".format(it) },
                    sourceFilePath = sourceFile.absolutePath,
                    absoluteOffset = offset,
                    reservedLength = len
                )
                offset += len
            }
        }
    }

    private fun extractStrings(bytes: ByteArray): List<LocatedString> {
        val out = mutableListOf<LocatedString>()
        var i = 0
        while (i < bytes.size) {
            val b = bytes[i].toInt() and 0xFF
            if (b in 32..126) {
                val start = i
                while (i < bytes.size && ((bytes[i].toInt() and 0xFF) in 32..126)) i++
                val len = i - start
                if (len >= 4) {
                    val text = String(bytes, start, len, StandardCharsets.UTF_8)
                    out += LocatedString(text = text, start = start, length = len, type = "UTF-8")
                }
            } else {
                i++
            }
        }
        return out
    }
}
