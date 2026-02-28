package com.urzuasset.gui.asset

data class LocalizedTextEntry(
    val namespace: String,
    val key: String,
    val value: String,
    val sourceFile: String,
    val byteOffset: Long,
    val asciiCrc32: Long,
    val unicodeCrc32: Long
)

data class ValidationIssue(
    val namespace: String,
    val key: String,
    val reason: String
)

data class LocalizationValidationResult(
    val entries: List<LocalizedTextEntry>,
    val issues: List<ValidationIssue>
)
