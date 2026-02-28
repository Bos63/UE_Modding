package com.urzuasset.gui.ui.editor

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.urzuasset.gui.databinding.ActivityEditorBinding
import com.urzuasset.gui.storage.ProjectRepository

class EditorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditorBinding
    private var projectName: String = ""

    private val treeAdapter = EditorTreeAdapter()
    private val propertyAdapter = EditorPropertyAdapter()

    private enum class EditorViewMode { TREE, PROPERTIES }

    private var currentMode = EditorViewMode.TREE


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        projectName = intent.getStringExtra(EXTRA_PROJECT_NAME).orEmpty()
        val project = ProjectRepository.find(projectName)

        if (project == null) {
            Snackbar.make(binding.root, "Proje bulunamadı", Snackbar.LENGTH_LONG).show()
            finish()
            return
        }

        supportActionBar?.title = "Editor • ${project.projectName}"
        binding.filePairLabel.text = "Bağlı dosyalar:\n${project.uassetPath}\n${project.uexpPath}"

        setupLists()
        setupViewMode()

        val propertyRows = if (project.content.isEmpty()) {
            defaultPropertyRows()
        } else {
            decodeRows(project.content)
        }

        treeAdapter.submit(defaultTreeNodes())
        propertyAdapter.submit(propertyRows)

        binding.saveButton.setOnClickListener {
            val snapshotBefore = encodeRows(propertyAdapter.snapshot())
            project.history.add(project.content.toList())
            project.content.clear()
            project.content.addAll(snapshotBefore)
            Snackbar.make(binding.root, "Değişiklik kaydedildi", Snackbar.LENGTH_SHORT).show()
        }

        binding.undoButton.setOnClickListener {
            val previous = project.history.removeLastOrNull()
            if (previous == null) {
                Snackbar.make(binding.root, "Geri alınacak kayıt yok", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            project.content.clear()
            project.content.addAll(previous)
            propertyAdapter.submit(decodeRows(previous))
            Snackbar.make(binding.root, "Önceki sürüm yüklendi", Snackbar.LENGTH_SHORT).show()
        }

        binding.previewButton.setOnClickListener {
            val top5 = propertyAdapter.snapshot().take(5)
                .joinToString("\n") { "${it.name} = ${it.value}" }
            binding.previewOutput.text = "Canlı Önizleme:\n$top5"
        }
    }



    private fun setupViewMode() {
        binding.treeTabButton.setOnClickListener {
            setMode(EditorViewMode.TREE)
        }

        binding.propertyTabButton.setOnClickListener {
            setMode(EditorViewMode.PROPERTIES)
        }

        setMode(EditorViewMode.TREE)
    }

    private fun setMode(mode: EditorViewMode) {
        currentMode = mode

        val treeVisible = mode == EditorViewMode.TREE
        binding.treePanel.visibility = if (treeVisible) View.VISIBLE else View.GONE
        binding.propertyPanel.visibility = if (treeVisible) View.GONE else View.VISIBLE

        binding.treeTabButton.alpha = if (treeVisible) 1f else 0.65f
        binding.propertyTabButton.alpha = if (treeVisible) 0.65f else 1f
    }

    private fun setupLists() {
        binding.treeRecycler.layoutManager = LinearLayoutManager(this)
        binding.treeRecycler.adapter = treeAdapter

        binding.propertyRecycler.layoutManager = LinearLayoutManager(this)
        binding.propertyRecycler.adapter = propertyAdapter
    }

    private fun defaultTreeNodes(): List<EditorTreeNode> = listOf(
        EditorTreeNode("General Information"),
        EditorTreeNode("Name Map"),
        EditorTreeNode("Import Data"),
        EditorTreeNode("Export Information"),
        EditorTreeNode("Depends Map"),
        EditorTreeNode("Export Data", highlighted = true),
        EditorTreeNode("Export 1 (ExecuteUbergraph_BP_SMG)", indentLevel = 1),
        EditorTreeNode("Export 6 (BP_SMG_C)", indentLevel = 1),
        EditorTreeNode("Export 7 (Default_BP_SMG_C)", indentLevel = 1, highlighted = true),
        EditorTreeNode("6 (114)", indentLevel = 2, highlighted = true),
        EditorTreeNode("Extra Data (0 B)", indentLevel = 3)
    )

    private fun defaultPropertyRows(): List<EditorPropertyRow> = listOf(
        EditorPropertyRow(52, "ProjectileClass", "ObjectProperty", "-57", "RifleProjectile_C"),
        EditorPropertyRow(53, "MeleeProjectileClass", "ObjectProperty", "-56", "MeleeProjectile_C"),
        EditorPropertyRow(54, "MeleeDamage", "FloatProperty", "", "48"),
        EditorPropertyRow(55, "MeleeHeadshotDamageMultiplier", "FloatProperty", "", "2"),
        EditorPropertyRow(56, "MeleeDamageType", "ObjectProperty", "-54", "DT_Melee_C"),
        EditorPropertyRow(57, "MeleeDelay", "FloatProperty", "", "0.15"),
        EditorPropertyRow(58, "MeleeCooldown", "FloatProperty", "", "0.4"),
        EditorPropertyRow(59, "MaxAmmo", "IntProperty", "", "200"),
        EditorPropertyRow(60, "AmmoType", "EnumProperty", "", "EAmmoType"),
        EditorPropertyRow(61, "WeaponCustomizationCategories", "ArrayProperty", "StructProperty", ""),
        EditorPropertyRow(64, "AmmoPerClip", "IntProperty", "", "50"),
        EditorPropertyRow(65, "Damage", "FloatProperty", "", "10")
    )

    private fun encodeRows(rows: List<EditorPropertyRow>): List<String> {
        return rows.map { "${it.index}|${it.name}|${it.type}|${it.variant}|${it.value}" }
    }

    private fun decodeRows(lines: List<String>): List<EditorPropertyRow> {
        val parsed = lines.mapNotNull { line ->
            val chunks = line.split("|")
            if (chunks.size < 5) return@mapNotNull null
            val index = chunks[0].toIntOrNull() ?: return@mapNotNull null
            EditorPropertyRow(
                index = index,
                name = chunks[1],
                type = chunks[2],
                variant = chunks[3],
                value = chunks.subList(4, chunks.size).joinToString("|")
            )
        }
        return if (parsed.isEmpty()) defaultPropertyRows() else parsed
    }

    companion object {
        const val EXTRA_PROJECT_NAME = "extra_project_name"
    }
}
