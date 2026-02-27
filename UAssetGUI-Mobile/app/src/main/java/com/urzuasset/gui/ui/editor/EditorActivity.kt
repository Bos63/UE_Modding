package com.urzuasset.gui.ui.editor

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.urzuasset.gui.databinding.ActivityEditorBinding
import com.urzuasset.gui.storage.ProjectRepository

class EditorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditorBinding
    private var projectName: String = ""

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

        if (project.content.isEmpty()) {
            project.content.addAll(
                listOf(
                    "; UAsset + UEXP paired editing session",
                    "NameMap[0] = ExampleValue",
                    "Export[0].Properties.Damage = 100"
                )
            )
        }
        binding.editorInput.setText(project.content.joinToString("\n"))
        refreshLineNumbers()

        binding.editorInput.addTextChangedListener(SimpleTextWatcher { refreshLineNumbers() })

        binding.saveButton.setOnClickListener {
            val currentLines = binding.editorInput.text.toString().lines()
            project.history.add(project.content.toList())
            project.content.clear()
            project.content.addAll(currentLines)
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
            binding.editorInput.setText(previous.joinToString("\n"))
            refreshLineNumbers()
        }

        binding.previewButton.setOnClickListener {
            val preview = binding.editorInput.text.toString().take(200)
            binding.previewOutput.text = "Canlı Önizleme:\n$preview"
        }
    }

    private fun refreshLineNumbers() {
        val count = binding.editorInput.lineCount.coerceAtLeast(1)
        binding.lineNumbers.text = (1..count).joinToString("\n")
    }

    companion object {
        const val EXTRA_PROJECT_NAME = "extra_project_name"
    }
}
