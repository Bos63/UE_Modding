package com.urzuasset.gui.ui.editor

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.urzuasset.gui.databinding.ItemEditorLineRowBinding

data class EditorLineRow(
    val index: Int,
    val offsetHex: String,
    val name: String,
    val type: String,
    var value: String,
    val sourceFilePath: String,
    val absoluteOffset: Int,
    val reservedLength: Int
)

class EditorLineAdapter(
    private val onRowClick: (EditorLineRow) -> Unit
) : RecyclerView.Adapter<EditorLineAdapter.EditorLineViewHolder>() {

    private val allItems = mutableListOf<EditorLineRow>()
    private val items = mutableListOf<EditorLineRow>()

    fun submit(newItems: List<EditorLineRow>) {
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
            items.addAll(allItems.filter {
                it.name.lowercase().contains(q) ||
                    it.type.lowercase().contains(q) ||
                    it.offsetHex.lowercase().contains(q) ||
                    it.value.lowercase().contains(q)
            })
        }
        notifyDataSetChanged()
    }

    fun snapshot(): List<EditorLineRow> = allItems.map { it.copy() }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EditorLineViewHolder {
        val binding = ItemEditorLineRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EditorLineViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EditorLineViewHolder, position: Int) = holder.bind(items[position], onRowClick)

    override fun getItemCount(): Int = items.size

    class EditorLineViewHolder(
        private val binding: ItemEditorLineRowBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: EditorLineRow, onRowClick: (EditorLineRow) -> Unit) {
            binding.rowIndex.text = "${item.index} |"
            binding.rowOffset.text = item.offsetHex
            binding.rowName.text = item.name
            binding.rowType.text = item.type
            binding.rowValue.text = item.value
            binding.root.setOnClickListener { onRowClick(item) }
        }
    }
}
