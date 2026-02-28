package com.urzuasset.gui.storage

import com.urzuasset.gui.model.PairedAssetFiles

object ProjectRepository {
    private val projects = mutableListOf<PairedAssetFiles>()

    fun all(): List<PairedAssetFiles> = projects.toList()

    fun add(project: PairedAssetFiles) {
        projects.removeAll { it.projectName == project.projectName }
        projects.add(project)
    }

    fun find(projectName: String): PairedAssetFiles? = projects.find { it.projectName == projectName }
}
