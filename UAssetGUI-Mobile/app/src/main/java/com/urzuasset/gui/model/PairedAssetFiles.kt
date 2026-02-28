package com.urzuasset.gui.model

data class PairedAssetFiles(
    val projectName: String,
    val uassetPath: String,
    val uexpPath: String,
    val content: MutableList<String> = mutableListOf(),
    val history: MutableList<List<String>> = mutableListOf()
)
