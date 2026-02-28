package com.urzuasset.gui.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.urzuasset.gui.data.SessionManager
import com.urzuasset.gui.databinding.ActivityMainBinding
import com.urzuasset.gui.model.PairedAssetFiles
import com.urzuasset.gui.storage.ProjectRepository
import com.urzuasset.gui.ui.editor.EditorActivity
import com.urzuasset.gui.ui.login.LoginActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: ProjectAdapter
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        setupList()

        binding.newProjectButton.setOnClickListener {
            val projectName = binding.projectNameInput.text?.toString().orEmpty().trim()
            val uasset = binding.uassetInput.text?.toString().orEmpty().trim()
            val uexp = binding.uexpInput.text?.toString().orEmpty().trim()
            if (projectName.isBlank() || uasset.isBlank() || uexp.isBlank()) {
                Snackbar.make(binding.root, "UAsset + UEXP + proje adı zorunlu", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val project = PairedAssetFiles(projectName, uasset, uexp)
            ProjectRepository.add(project)
            refreshProjects()
        }

        binding.themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        binding.logoutButton.setOnClickListener {
            sessionManager.clear()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun setupList() {
        adapter = ProjectAdapter { project ->
            startActivity(Intent(this, EditorActivity::class.java).putExtra(EditorActivity.EXTRA_PROJECT_NAME, project.projectName))
        }
        binding.projectRecycler.layoutManager = LinearLayoutManager(this)
        binding.projectRecycler.adapter = adapter
        refreshProjects()
    }

    private fun refreshProjects() {
        adapter.submit(ProjectRepository.all())
    }
}
