package com.urzuasset.gui.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.urzuasset.gui.databinding.ItemProjectBinding
import com.urzuasset.gui.model.PairedAssetFiles

class ProjectAdapter(
    private val onClick: (PairedAssetFiles) -> Unit
) : RecyclerView.Adapter<ProjectAdapter.ProjectViewHolder>() {

    private val items = mutableListOf<PairedAssetFiles>()

    fun submit(newItems: List<PairedAssetFiles>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectViewHolder {
        val binding = ItemProjectBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProjectViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProjectViewHolder, position: Int) = holder.bind(items[position])

    override fun getItemCount(): Int = items.size

    inner class ProjectViewHolder(private val binding: ItemProjectBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: PairedAssetFiles) {
            binding.projectTitle.text = item.projectName
            binding.projectSubtitle.text = "UAsset: ${item.uassetPath}\nUEXP: ${item.uexpPath}"
            binding.openButton.setOnClickListener { onClick(item) }
        }
    }
}
