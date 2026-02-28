package com.urzuasset.gui.ui.editor

import android.app.AlertDialog
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.urzuasset.gui.asset.AssetPairData
import com.urzuasset.gui.asset.AssetPairProcessor
import com.urzuasset.gui.databinding.ActivityEditorBinding
import com.urzuasset.gui.databinding.DialogEditNamemapBinding
import com.urzuasset.gui.databinding.DialogEditValueBinding
import com.urzuasset.gui.databinding.DialogExtraFunctionsBinding
import com.urzuasset.gui.databinding.ItemNamemapEntryBinding
import java.io.File

class EditorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditorBinding
    private var activePair: AssetPairData? = null
    private val baseFolder = File("/storage/emulated/0/Download/URAZMOD_UASSETGUİ")

    private val lineAdapter = EditorLineAdapter { row ->
        showEditValueDialog(row)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "UAssetGUI Editör"
        setupList()
        setupUiTexts()

        binding.searchInput.addTextChangedListener(SimpleTextWatcher {
            lineAdapter.filter(binding.searchInput.text?.toString().orEmpty())
        })

        binding.openFileButton.setOnClickListener {
            openUassetSelection()
        }

        binding.dumpTxtButton.setOnClickListener {
            dumpTxt()
        }

        binding.extraFunctionButton.setOnClickListener {
            showExtraFunctionsDialog()
        }

        binding.saveButton.setOnClickListener {
            Snackbar.make(binding.root, "Kayıtlar dosyaya anlık işlenir", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun setupUiTexts() {
        binding.openFileButton.text = "DOSYA AÇ"
        binding.dumpTxtButton.text = "METNE DÖK (TXT)"
        binding.searchInput.hint = "🔍 Satır ara..."
        binding.filePairLabel.text = "Henüz dosya açılmadı"
    }

    private fun setupList() {
        binding.lineRecycler.layoutManager = LinearLayoutManager(this)
        binding.lineRecycler.adapter = lineAdapter
    }

    private fun openUassetSelection() {
        try {
            if (!baseFolder.exists()) {
                baseFolder.mkdirs()
            }
            val files = baseFolder.listFiles()?.filter { it.isFile && it.extension.equals("uasset", true) }
                ?.sortedBy { it.name.lowercase() }
                ?: emptyList()
            if (files.isEmpty()) {
                val onlyUexp = baseFolder.listFiles()?.any { it.isFile && it.extension.equals("uexp", true) } == true
                val msg = if (onlyUexp) ".uasset olmadan .uexp tek başına okunamaz." else "Seçilecek .uasset bulunamadı"
                Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG).show()
                return
            }
            val names = files.map { it.name }.toTypedArray()
            AlertDialog.Builder(this)
                .setTitle(".uasset dosyası seç")
                .setItems(names) { _, which ->
                    loadPair(files[which])
                }
                .setNegativeButton("İPTAL", null)
                .show()
        } catch (e: Exception) {
            Snackbar.make(binding.root, "Dosya seçimi açılamadı: ${e.message}", Snackbar.LENGTH_LONG).show()
        }
    }

    private fun loadPair(selectedUasset: File) {
        try {
            val uexp = File(selectedUasset.parentFile, "${selectedUasset.nameWithoutExtension}.uexp")
            if (!uexp.exists()) {
                Snackbar.make(binding.root, "Aynı klasörde .uexp bulunamadı. .uasset okunmadı.", Snackbar.LENGTH_LONG).show()
                return
            }

            val pairData = AssetPairProcessor.loadPair(selectedUasset, uexp)
            activePair = pairData
            val rows = pairData.records.map {
                EditorLineRow(
                    index = it.index,
                    offsetHex = it.displayOffset,
                    name = it.nameMapValue,
                    type = it.type,
                    value = it.value,
                    sourceFilePath = it.sourceFilePath,
                    absoluteOffset = it.absoluteOffset,
                    reservedLength = it.reservedLength
                )
            }
            lineAdapter.submit(rows)
            binding.filePairLabel.text = "Toplam ${rows.size} satır yüklendi"
            Snackbar.make(binding.root, "Toplam ${rows.size} satır yüklendi", Snackbar.LENGTH_LONG).show()
        } catch (e: Exception) {
            Snackbar.make(binding.root, "Dosya okunamadı: ${e.message}", Snackbar.LENGTH_LONG).show()
        }
    }

    private fun dumpTxt() {
        try {
            val pairData = activePair
            if (pairData == null) {
                Snackbar.make(binding.root, "Önce .uasset dosyası açın", Snackbar.LENGTH_LONG).show()
                return
            }
            val outFile = File(baseFolder, "${pairData.uassetFile.nameWithoutExtension}_dump.txt")
            AssetPairProcessor.writeDumpTxt(pairData, outFile)
            Snackbar.make(binding.root, "Dump tamamlandı: ${outFile.absolutePath}", Snackbar.LENGTH_LONG).show()
        } catch (e: Exception) {
            Snackbar.make(binding.root, "Dump başarısız: ${e.message}", Snackbar.LENGTH_LONG).show()
        }
    }

    private fun showExtraFunctionsDialog() {
        val dialogBinding = DialogExtraFunctionsBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this).setView(dialogBinding.root).create()

        dialogBinding.compareUassetButton.text = ".uasset Karşılaştır"
        dialogBinding.compareUexpButton.text = ".uexp Karşılaştır"
        dialogBinding.editNamemapButton.text = "NameMap Düzenle"
        dialogBinding.hexEditorButton.text = "HEX EDITOR"
        dialogBinding.editAimbotButton.text = "AİMBOT 1"
        dialogBinding.sekmme2Button.text = "SEKMME 2"
        dialogBinding.clorBody3Button.text = "CLOR BODY 3"

        dialogBinding.compareUassetButton.setOnClickListener {
            dialog.dismiss()
            runCompare("uasset")
        }
        dialogBinding.compareUexpButton.setOnClickListener {
            dialog.dismiss()
            runCompare("uexp")
        }
        dialogBinding.editAimbotButton.setOnClickListener {
            dialog.dismiss()
            Snackbar.make(binding.root, "AİMBOT 1 çalıştı", Snackbar.LENGTH_SHORT).show()
        }
        dialogBinding.sekmme2Button.setOnClickListener {
            dialog.dismiss()
            Snackbar.make(binding.root, "SEKMME 2 çalıştı", Snackbar.LENGTH_SHORT).show()
        }
        dialogBinding.clorBody3Button.setOnClickListener {
            dialog.dismiss()
            Snackbar.make(binding.root, "CLOR BODY 3 çalıştı", Snackbar.LENGTH_SHORT).show()
        }
        dialogBinding.editNamemapButton.setOnClickListener {
            dialog.dismiss()
            showEditNamemapDialog()
        }
        dialogBinding.hexEditorButton.setOnClickListener {
            dialog.dismiss()
            showHexEditorDialog()
        }

        dialog.show()
    }

    private fun runCompare(extension: String) {
        try {
            val pair = activePair ?: run {
                Snackbar.make(binding.root, "Önce dosya açın", Snackbar.LENGTH_LONG).show()
                return
            }
            val source = if (extension == "uasset") pair.uassetFile else pair.uexpFile
            val target = File(baseFolder, source.name)
            if (!target.exists()) {
                Snackbar.make(binding.root, "Karşılaştırma için aynı isimli dosya bulunamadı", Snackbar.LENGTH_LONG).show()
                return
            }
            val same = source.readBytes().contentEquals(target.readBytes())
            Snackbar.make(binding.root, if (same) "Dosyalar birebir aynı" else "Dosyalar farklı", Snackbar.LENGTH_LONG).show()
        } catch (e: Exception) {
            Snackbar.make(binding.root, "Karşılaştırma hatası: ${e.message}", Snackbar.LENGTH_LONG).show()
        }
    }

    private fun showHexEditorDialog() {
        val pair = activePair ?: run {
            Snackbar.make(binding.root, "Önce dosya açın", Snackbar.LENGTH_LONG).show()
            return
        }

        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(32, 24, 32, 16)
        }
        val offsetInput = EditText(this).apply { hint = "Offset (örn: 1A3F veya 0x1A3F)" }
        val bytesInput = EditText(this).apply { hint = "Yeni byte dizisi (örn: FF 00 AB 12)" }
        layout.addView(offsetInput)
        layout.addView(bytesInput)

        AlertDialog.Builder(this)
            .setTitle("HEX EDITOR")
            .setView(layout)
            .setNegativeButton("İPTAL", null)
            .setPositiveButton("KAYDET") { _, _ ->
                try {
                    val rawOffset = offsetInput.text?.toString().orEmpty().trim().removePrefix("0x")
                    val offset = rawOffset.toInt(16)
                    val newBytes = bytesInput.text?.toString().orEmpty().trim()
                        .split(" ")
                        .filter { it.isNotBlank() }
                        .map { it.toInt(16).toByte() }
                        .toByteArray()

                    val targetFile = pair.uassetFile
                    val old = targetFile.readBytes()
                    if (offset < 0 || offset + newBytes.size > old.size) {
                        throw IllegalArgumentException("Offset aralığı geçersiz")
                    }
                    val backup = File(targetFile.parentFile, "${targetFile.name}.hex.bak")
                    if (!backup.exists()) backup.writeBytes(old)
                    System.arraycopy(newBytes, 0, old, offset, newBytes.size)
                    targetFile.writeBytes(old)
                    Snackbar.make(binding.root, "HEX kaydı yapıldı", Snackbar.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Snackbar.make(binding.root, "HEX kaydı başarısız: ${e.message}", Snackbar.LENGTH_LONG).show()
                }
            }
            .show()
    }

    private fun showEditValueDialog(row: EditorLineRow) {
        val dialogBinding = DialogEditValueBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this).setView(dialogBinding.root).create()

        dialogBinding.editValueTitle.text = "DEĞER DÜZENLE: ${row.name}"
        dialogBinding.editValueInput.setText(row.value)
        dialogBinding.cancelEditButton.text = "İPTAL"
        dialogBinding.saveEditButton.text = "KAYDET"

        dialogBinding.cancelEditButton.setOnClickListener { dialog.dismiss() }
        dialogBinding.saveEditButton.setOnClickListener {
            try {
                val newValue = dialogBinding.editValueInput.text?.toString().orEmpty()
                AssetPairProcessor.writeUpdatedValue(
                    record = com.urzuasset.gui.asset.AssetLineRecord(
                        index = row.index,
                        displayOffset = row.offsetHex,
                        nameMapValue = row.name,
                        type = row.type,
                        value = row.value,
                        sourceFilePath = row.sourceFilePath,
                        absoluteOffset = row.absoluteOffset,
                        reservedLength = row.reservedLength
                    ),
                    newValue = newValue
                )
                row.value = newValue
                lineAdapter.submit(lineAdapter.snapshot())
                dialog.dismiss()
                Snackbar.make(binding.root, "Kayıt güncellendi", Snackbar.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Snackbar.make(binding.root, "Kayıt güncellenemedi: ${e.message}", Snackbar.LENGTH_LONG).show()
            }
        }

        dialog.show()
    }

    private fun showEditNamemapDialog() {
        val pair = activePair ?: run {
            Snackbar.make(binding.root, "Önce dosya açın", Snackbar.LENGTH_LONG).show()
            return
        }

        val dialogBinding = DialogEditNamemapBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this).setView(dialogBinding.root).create()

        val allEntries = pair.nameMapEntries.toMutableList()
        val adapter = NameMapAdapter(allEntries.toMutableList())
        dialogBinding.namemapRecycler.layoutManager = LinearLayoutManager(this)
        dialogBinding.namemapRecycler.adapter = adapter
        dialogBinding.namemapTitle.text = "NAMEMAP DÜZENLE (${allEntries.size})"
        dialogBinding.closeNamemapButton.text = "KAYDET"

        dialogBinding.namemapSearch.addTextChangedListener(SimpleTextWatcher {
            val q = dialogBinding.namemapSearch.text?.toString().orEmpty().trim().lowercase()
            val filtered = if (q.isEmpty()) allEntries else allEntries.filter { it.lowercase().contains(q) }
            adapter.submit(filtered)
            dialogBinding.namemapTitle.text = "NAMEMAP DÜZENLE (${filtered.size})"
        })

        dialogBinding.closeNamemapButton.setOnClickListener {
            try {
                val file = File(baseFolder, "${pair.uassetFile.nameWithoutExtension}_namemap.txt")
                file.writeText(adapter.items().joinToString("\n"))
                dialog.dismiss()
                Snackbar.make(binding.root, "NameMap kaydedildi", Snackbar.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Snackbar.make(binding.root, "NameMap kaydedilemedi: ${e.message}", Snackbar.LENGTH_LONG).show()
            }
        }

        dialog.show()
    }
}

private class NameMapAdapter(
    initial: MutableList<String>
) : RecyclerView.Adapter<NameMapAdapter.NameMapViewHolder>() {

    private val entries = initial

    fun submit(newItems: List<String>) {
        entries.clear()
        entries.addAll(newItems)
        notifyDataSetChanged()
    }

    fun items(): List<String> = entries.toList()

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): NameMapViewHolder {
        val binding = ItemNamemapEntryBinding.inflate(android.view.LayoutInflater.from(parent.context), parent, false)
        return NameMapViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NameMapViewHolder, position: Int) = holder.bind(entries[position])

    override fun getItemCount(): Int = entries.size

    class NameMapViewHolder(private val binding: ItemNamemapEntryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(value: String) {
            binding.entryPath.text = value
            binding.entryMeta.text = "Kayıt"
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
