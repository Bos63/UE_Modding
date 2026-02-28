package com.urzuasset.gui.ui.editor

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.urzuasset.gui.asset.AssetPairData
import com.urzuasset.gui.asset.AssetPairProcessor
import com.urzuasset.gui.databinding.ActivityEditorBinding
import com.urzuasset.gui.databinding.DialogEditNamemapBinding
import com.urzuasset.gui.databinding.DialogExtraFunctionsBinding
import com.urzuasset.gui.databinding.ItemNamemapEntryBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EditorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditorBinding
    private var activePair: AssetPairData? = null
    private val uriStorage by lazy { UriStorage(this) }
    private var currentFilterJob: Job? = null

    private val openFileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val uri = result.data?.data
        if (result.resultCode != RESULT_OK || uri == null) {
            Toast.makeText(this, "Dosya seçilmedi", Toast.LENGTH_SHORT).show()
            return@registerForActivityResult
        }
        onUassetPicked(uri)
    }

    private val dumpCreateLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { outUri ->
        if (outUri == null) {
            Toast.makeText(this, "Kayıt iptal edildi", Toast.LENGTH_SHORT).show()
            return@registerForActivityResult
        }
        writeDumpToUri(outUri)
    }

    private val lineAdapter = EditorLineAdapter { row ->
        AlertDialog.Builder(this)
            .setTitle("Kayıt Detayı")
            .setMessage("OFFSET: ${row.offsetHex}\nNAME MAP: ${row.name}\nTYPE: ${row.type}\nVALUE: ${row.value}")
            .setPositiveButton("Kapat", null)
            .show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "UAssetGUI v2.0"
        setupList()
        setupUiTexts()

        binding.searchInput.addTextChangedListener(SimpleTextWatcher {
            currentFilterJob?.cancel()
            currentFilterJob = lifecycleScope.launch {
                delay(300)
                lineAdapter.filter(binding.searchInput.text?.toString().orEmpty())
            }
        })

        binding.openFileButton.setOnClickListener {
            openFileLauncher.launch(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            })
        }

        binding.dumpTxtButton.setOnClickListener {
            val pair = activePair ?: run {
                Snackbar.make(binding.root, "Önce dosya açın", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }
            dumpCreateLauncher.launch("${pair.baseName}_dump.txt")
        }

        binding.extraFunctionButton.setOnClickListener {
            showExtraFunctionsDialog()
        }

        binding.saveButton.setOnClickListener {
            Snackbar.make(binding.root, "Uygulama read-only çalışır. Dosya değiştirme kapalıdır.", Snackbar.LENGTH_LONG).show()
        }
    }

    private fun setupUiTexts() {
        binding.openFileButton.text = "OPEN FILE"
        binding.dumpTxtButton.text = "DUMP TXT"
        binding.searchInput.hint = "Property ara..."

        val lastUri = uriStorage.readLastUri()
        binding.filePairLabel.text = if (lastUri != null) {
            "Dang mở: ${lastUri.lastPathSegment ?: lastUri}"
        } else {
            "Dang mở: -"
        }
    }

    private fun setupList() {
        binding.lineRecycler.layoutManager = LinearLayoutManager(this)
        binding.lineRecycler.adapter = lineAdapter
    }

    private fun onUassetPicked(uri: Uri) {
        try {
            contentResolver.takePersistableUriPermission(uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } catch (_: Exception) {
        }
        uriStorage.saveLastUri(uri)

        lifecycleScope.launch {
            binding.filePairLabel.text = "Dang mở: yükleniyor..."
            val pairResult = withContext(Dispatchers.IO) { buildPairFromUri(uri) }
            if (pairResult == null) {
                binding.filePairLabel.text = "Dang mở: -"
                return@launch
            }

            activePair = pairResult
            val rows = pairResult.records.map {
                EditorLineRow(
                    index = it.index,
                    offsetHex = it.offsetHex,
                    name = it.nameMapValue,
                    type = it.type,
                    value = it.value
                )
            }
            lineAdapter.submit(rows)

            binding.filePairLabel.text = "Dang mở: ${pairResult.uassetName}"
            Snackbar.make(binding.root, "✅ ${rows.size} Properties yüklendi", Snackbar.LENGTH_LONG).show()
            if (pairResult.warnings.isNotEmpty()) {
                Snackbar.make(binding.root, pairResult.warnings.joinToString("\n"), Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun buildPairFromUri(uassetUri: Uri): AssetPairData? {
        return try {
            val match = SafDocumentFinder.findMatchingPair(contentResolver, uassetUri)
            if (match == null) {
                runOnUiThread {
                    Snackbar.make(
                        binding.root,
                        "Geçerli aynı isimde .uexp dosyası bulunamadı. Lütfen aynı klasörde aynı isimde olan dosyayı veriniz.",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
                return null
            }

            val uassetBytes = contentResolver.openInputStream(match.uassetUri)?.use { it.readBytes() }
            val uexpBytes = contentResolver.openInputStream(match.uexpUri)?.use { it.readBytes() }
            val ubulkBytes = match.ubulkUri?.let { contentResolver.openInputStream(it)?.use { s -> s.readBytes() } }

            if (uassetBytes == null || uexpBytes == null) {
                runOnUiThread {
                    Snackbar.make(binding.root, "Dosya okunamadı / desteklenmeyen format.", Snackbar.LENGTH_LONG).show()
                }
                return null
            }

            val uassetName = match.uassetUri.lastPathSegment ?: "selected.uasset"
            val baseName = uassetName.substringBeforeLast('.')

            AssetPairProcessor.loadPair(
                baseName = baseName,
                uassetName = uassetName,
                uassetBytes = uassetBytes,
                uexpName = match.uexpUri.lastPathSegment ?: "selected.uexp",
                uexpBytes = uexpBytes,
                ubulkName = match.ubulkUri?.lastPathSegment,
                ubulkBytes = ubulkBytes
            )
        } catch (_: Exception) {
            runOnUiThread {
                Snackbar.make(binding.root, "Dosya okunamadı / desteklenmeyen format.", Snackbar.LENGTH_LONG).show()
            }
            null
        }
    }

    private fun writeDumpToUri(outputUri: Uri) {
        val pair = activePair ?: return
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                contentResolver.openOutputStream(outputUri)?.use { stream ->
                    BufferedWriter(OutputStreamWriter(stream)).use { writer ->
                        val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
                        writer.appendLine("# File: ${pair.uassetName}")
                        writer.appendLine("# UEXP: ${pair.uexpName}")
                        writer.appendLine("# Date: $date")
                        pair.summaryLines.forEach { writer.appendLine("# $it") }
                        writer.appendLine("offset\tname\ttype\tvalue")
                        pair.records.forEach { row ->
                            writer.appendLine("${row.offsetHex}\t${row.nameMapValue}\t${row.type}\t${row.value}")
                        }
                    }
                }
            }
            Snackbar.make(binding.root, "Dump TXT tamamlandı", Snackbar.LENGTH_LONG).show()
        }
    }

    private fun showExtraFunctionsDialog() {
        val dialogBinding = DialogExtraFunctionsBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this).setView(dialogBinding.root).create()

        dialogBinding.compareUassetButton.setOnClickListener {
            dialog.dismiss()
            Snackbar.make(binding.root, "Read-only mod: karşılaştırma notları dump içinde", Snackbar.LENGTH_SHORT).show()
        }
        dialogBinding.compareUexpButton.setOnClickListener {
            dialog.dismiss()
            Snackbar.make(binding.root, "Read-only mod: karşılaştırma notları dump içinde", Snackbar.LENGTH_SHORT).show()
        }
        dialogBinding.editAimbotButton.setOnClickListener {
            dialog.dismiss()
            Snackbar.make(binding.root, "Read-only mod aktif", Snackbar.LENGTH_SHORT).show()
        }
        dialogBinding.sekmme2Button.setOnClickListener {
            dialog.dismiss()
            Snackbar.make(binding.root, "Read-only mod aktif", Snackbar.LENGTH_SHORT).show()
        }
        dialogBinding.clorBody3Button.setOnClickListener {
            dialog.dismiss()
            Snackbar.make(binding.root, "Read-only mod aktif", Snackbar.LENGTH_SHORT).show()
        }
        dialogBinding.editNamemapButton.setOnClickListener {
            dialog.dismiss()
            showEditNamemapDialog()
        }
        dialogBinding.hexEditorButton.setOnClickListener {
            dialog.dismiss()
            Snackbar.make(binding.root, "Read-only mod aktif", Snackbar.LENGTH_SHORT).show()
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
        dialogBinding.closeNamemapButton.text = "KAPAT"

        dialogBinding.namemapSearch.addTextChangedListener(SimpleTextWatcher {
            val q = dialogBinding.namemapSearch.text?.toString().orEmpty().trim().lowercase()
            val filtered = if (q.isEmpty()) allEntries else allEntries.filter { it.lowercase().contains(q) }
            adapter.submit(filtered)
            dialogBinding.namemapTitle.text = "NAMEMAP DÜZENLE (${filtered.size})"
        })

        dialogBinding.closeNamemapButton.setOnClickListener { dialog.dismiss() }
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
