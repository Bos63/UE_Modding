package com.urzuasset.gui.ui.editor

import android.app.AlertDialog
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.urzuasset.gui.databinding.ActivityEditorBinding
import com.urzuasset.gui.databinding.DialogEditNamemapBinding
import com.urzuasset.gui.databinding.DialogEditValueBinding
import com.urzuasset.gui.databinding.DialogExtraFunctionsBinding
import com.urzuasset.gui.databinding.ItemNamemapEntryBinding
import com.urzuasset.gui.storage.ProjectRepository

class EditorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditorBinding
    private var projectName: String = ""

    private val propertyAdapter = EditorPropertyAdapter { row ->
        showEditValueDialog(row)
    }

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

        supportActionBar?.title = "UAssetGUI v2.0"
        binding.filePairLabel.text = "Đang mở: ${project.uassetPath.substringAfterLast('/')}"

        setupList()

        val propertyRows = if (project.content.isEmpty()) {
            defaultPropertyRows()
        } else {
            decodeRows(project.content)
        }
        propertyAdapter.submit(propertyRows)

        binding.searchInput.addTextChangedListener(SimpleTextWatcher {
            propertyAdapter.filter(binding.searchInput.text?.toString().orEmpty())
        })

        binding.openFileButton.setOnClickListener {
            Snackbar.make(binding.root, "OPEN FILE demo: ${project.uassetPath}", Snackbar.LENGTH_SHORT).show()
        }

        binding.dumpTxtButton.setOnClickListener {
            Snackbar.make(binding.root, "Đã load ${propertyAdapter.snapshot().size} Properties", Snackbar.LENGTH_SHORT).show()
        }

        binding.extraFunctionButton.setOnClickListener {
            showExtraFunctionsDialog()
        }

        binding.saveButton.setOnClickListener {
            val snapshotBefore = encodeRows(propertyAdapter.snapshot())
            project.history.add(project.content.toList())
            project.content.clear()
            project.content.addAll(snapshotBefore)
            Snackbar.make(binding.root, "Değişiklik kaydedildi", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun setupList() {
        binding.propertyRecycler.layoutManager = LinearLayoutManager(this)
        binding.propertyRecycler.adapter = propertyAdapter
    }

    private fun showExtraFunctionsDialog() {
        val dialogBinding = DialogExtraFunctionsBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this).setView(dialogBinding.root).create()

        dialogBinding.compareUassetButton.setOnClickListener {
            dialog.dismiss()
            Snackbar.make(binding.root, "Compare UAsset hazırlandı", Snackbar.LENGTH_SHORT).show()
        }
        dialogBinding.compareUexpButton.setOnClickListener {
            dialog.dismiss()
            Snackbar.make(binding.root, "Compare UEXP hazırlandı", Snackbar.LENGTH_SHORT).show()
        }
        dialogBinding.editAimbotButton.setOnClickListener {
            dialog.dismiss()
            Snackbar.make(binding.root, "Edit Aimbot demo", Snackbar.LENGTH_SHORT).show()
        }
        dialogBinding.editNamemapButton.setOnClickListener {
            dialog.dismiss()
            showEditNamemapDialog()
        }
        dialogBinding.hexEditorButton.setOnClickListener {
            dialog.dismiss()
            Snackbar.make(binding.root, "Hex editor modülü eklenecek", Snackbar.LENGTH_SHORT).show()
        }

        dialog.show()
    }

    private fun showEditValueDialog(row: EditorPropertyRow) {
        val dialogBinding = DialogEditValueBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this).setView(dialogBinding.root).create()

        dialogBinding.editValueTitle.text = "SỬA VALUE: ${row.name}"
        dialogBinding.editValueInput.setText(row.value)

        dialogBinding.cancelEditButton.setOnClickListener { dialog.dismiss() }
        dialogBinding.saveEditButton.setOnClickListener {
            row.value = dialogBinding.editValueInput.text?.toString().orEmpty()
            propertyAdapter.submit(propertyAdapter.snapshot())
            dialog.dismiss()
            Snackbar.make(binding.root, "${row.name} güncellendi", Snackbar.LENGTH_SHORT).show()
        }

        dialog.show()
    }

    private fun showEditNamemapDialog() {
        val dialogBinding = DialogEditNamemapBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this).setView(dialogBinding.root).create()

        val allEntries = defaultNamemapEntries()
        val adapter = NameMapAdapter(allEntries.toMutableList())
        dialogBinding.namemapRecycler.layoutManager = LinearLayoutManager(this)
        dialogBinding.namemapRecycler.adapter = adapter

        dialogBinding.namemapSearch.addTextChangedListener(SimpleTextWatcher {
            val q = dialogBinding.namemapSearch.text?.toString().orEmpty().trim().lowercase()
            val filtered = if (q.isEmpty()) allEntries else allEntries.filter { it.lowercase().contains(q) }
            adapter.submit(filtered)
            dialogBinding.namemapTitle.text = "EDIT NAMEMAP (${filtered.size})"
        })

        dialogBinding.closeNamemapButton.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    private fun defaultPropertyRows(): List<EditorPropertyRow> = listOf(
        EditorPropertyRow(0, "0x9AF", "ReloadingCDMax", "Float", "2.2"),
        EditorPropertyRow(1, "0x9FD", "TargetArmLength_30_200...", "Float", "210"),
        EditorPropertyRow(2, "0xA1A", "LagSpeed_34_CBFA2960...", "Float", "100"),
        EditorPropertyRow(3, "0xAB9", "DefaultCapsuleRadius", "Float", "30"),
        EditorPropertyRow(4, "0xC23", "Fov", "Float", "0"),
        EditorPropertyRow(5, "0xC40", "Scale", "Float", "0"),
        EditorPropertyRow(6, "0xC5D", "Offset", "Float", "0"),
        EditorPropertyRow(7, "0xCB3", "Fov", "Float", "0"),
        EditorPropertyRow(8, "0xCD0", "Scale", "Float", "0"),
        EditorPropertyRow(9, "0xCED", "Offset", "Float", "0"),
        EditorPropertyRow(10, "0xDB2", "HaveToOpenHeightToGro...", "Float", "40000"),
        EditorPropertyRow(11, "0xEE8", "ProbeSize", "Float", "15")
    )

    private fun defaultNamemapEntries(): List<String> = listOf(
        "[0] /Game/Arts/PhysicalMaterial/PhysicalMaterial_Flesh",
        "[1] /Game/Arts/UI/NoAtlas/AQ_Icon_xuanzhong_Curve",
        "[2] /Game/Arts/UI/NoAtlas/AQ_Icon_xuanzhong_Mat",
        "[3] /Game/Arts_Effect/Materials/mesh/M_PostProcess_hurt_blue_01_Inst",
        "[4] /Game/Arts_Effect/Materials/mesh/M_PostProcess_hurt_blue_01_Inst.M_PostProcess_hurt_blue_01_Inst",
        "[5] /Game/Arts_Effect/Materials/mesh/M_PvePoison_Hurt01_Inst",
        "[6] /Game/Arts_Effect/Materials/mesh/M_PvePoison_Hurt02_Inst",
        "[7] /Game/Arts_Effect/Materials/mesh/M_PvePoison_Hurt03_Inst"
    )

    private fun encodeRows(rows: List<EditorPropertyRow>): List<String> {
        return rows.map { "${it.index}|${it.offsetHex}|${it.name}|${it.type}|${it.value}" }
    }

    private fun decodeRows(lines: List<String>): List<EditorPropertyRow> {
        val parsed = lines.mapNotNull { line ->
            val chunks = line.split("|")
            if (chunks.size < 5) return@mapNotNull null
            val index = chunks[0].toIntOrNull() ?: return@mapNotNull null
            EditorPropertyRow(
                index = index,
                offsetHex = chunks[1],
                name = chunks[2],
                type = chunks[3],
                value = chunks.subList(4, chunks.size).joinToString("|")
            )
        }
        return if (parsed.isEmpty()) defaultPropertyRows() else parsed
    }

    companion object {
        const val EXTRA_PROJECT_NAME = "extra_project_name"
    }
}

private class NameMapAdapter(
    initial: MutableList<String>
) : RecyclerView.Adapter<NameMapAdapter.NameMapViewHolder>() {

    private val items = initial

    fun submit(newItems: List<String>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): NameMapViewHolder {
        val binding = ItemNamemapEntryBinding.inflate(android.view.LayoutInflater.from(parent.context), parent, false)
        return NameMapViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NameMapViewHolder, position: Int) = holder.bind(items[position])

    override fun getItemCount(): Int = items.size

    class NameMapViewHolder(private val binding: ItemNamemapEntryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(value: String) {
            binding.entryPath.text = value
            binding.entryMeta.text = "Unused / Class / Stru..."
        }
    }
}

private class SimpleTextWatcher(
    private val onTextChanged: () -> Unit
) : android.text.TextWatcher {
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
    override fun afterTextChanged(s: android.text.Editable?) = onTextChanged()
}
