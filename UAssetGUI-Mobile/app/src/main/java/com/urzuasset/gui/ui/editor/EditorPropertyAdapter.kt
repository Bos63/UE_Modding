package com.urzuasset.gui.ui.editor

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.urzuasset.gui.databinding.ItemEditorPropertyRowBinding

data class EditorPropertyRow(
    val index: Int,
    val name: String,
    val type: String,
    val variant: String,
    var value: String
)

class EditorPropertyAdapter : RecyclerView.Adapter<EditorPropertyAdapter.EditorPropertyViewHolder>() {

    private val items = mutableListOf<EditorPropertyRow>()

    fun submit(newItems: List<EditorPropertyRow>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun snapshot(): List<EditorPropertyRow> = items.map { it.copy() }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EditorPropertyViewHolder {
        val binding = ItemEditorPropertyRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EditorPropertyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EditorPropertyViewHolder, position: Int) = holder.bind(items[position])

    override fun getItemCount(): Int = items.size

    class EditorPropertyViewHolder(
        private val binding: ItemEditorPropertyRowBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private var watcher: TextWatcher? = null

        fun bind(item: EditorPropertyRow) {
            binding.rowIndex.text = item.index.toString()
            binding.rowName.text = item.name
            binding.rowType.text = item.type
            binding.rowVariant.text = item.variant

            watcher?.let { binding.rowValue.removeTextChangedListener(it) }
            binding.rowValue.setText(item.value)

            watcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
                override fun afterTextChanged(s: Editable?) {
                    item.value = s?.toString().orEmpty()
                }
            }
            binding.rowValue.addTextChangedListener(watcher)
        }
    }
}
