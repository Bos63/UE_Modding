package com.urzuasset.gui.ui.editor

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.urzuasset.gui.databinding.ItemEditorPropertyRowBinding

data class EditorPropertyRow(
    val index: Int,
    val offsetHex: String,
    val name: String,
    val type: String,
    var value: String
)

class EditorPropertyAdapter(
    private val onRowClick: (EditorPropertyRow) -> Unit
) : RecyclerView.Adapter<EditorPropertyAdapter.EditorPropertyViewHolder>() {

    private val allItems = mutableListOf<EditorPropertyRow>()
    private val items = mutableListOf<EditorPropertyRow>()

    fun submit(newItems: List<EditorPropertyRow>) {
        allItems.clear()
        allItems.addAll(newItems)
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun filter(query: String) {
        val q = query.trim().lowercase()
        items.clear()
        if (q.isEmpty()) {
            items.addAll(allItems)
        } else {
            items.addAll(allItems.filter { it.name.lowercase().contains(q) || it.type.lowercase().contains(q) || it.offsetHex.lowercase().contains(q) })
        }
        notifyDataSetChanged()
    }

    fun snapshot(): List<EditorPropertyRow> = allItems.map { it.copy() }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EditorPropertyViewHolder {
        val binding = ItemEditorPropertyRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EditorPropertyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EditorPropertyViewHolder, position: Int) = holder.bind(items[position], onRowClick)

    override fun getItemCount(): Int = items.size

    class EditorPropertyViewHolder(
        private val binding: ItemEditorPropertyRowBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: EditorPropertyRow, onRowClick: (EditorPropertyRow) -> Unit) {
            binding.rowIndex.text = "${item.index} |"
            binding.rowOffset.text = item.offsetHex
            binding.rowName.text = item.name
            binding.rowType.text = item.type
            binding.rowValue.text = item.value
            binding.root.setOnClickListener { onRowClick(item) }
        }
    }
}
