package com.urzuasset.gui.ui.editor

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.urzuasset.gui.databinding.ItemEditorTreeNodeBinding

data class EditorTreeNode(
    val label: String,
    val indentLevel: Int = 0,
    val highlighted: Boolean = false
)

class EditorTreeAdapter : RecyclerView.Adapter<EditorTreeAdapter.EditorTreeViewHolder>() {

    private val items = mutableListOf<EditorTreeNode>()

    fun submit(newItems: List<EditorTreeNode>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EditorTreeViewHolder {
        val binding = ItemEditorTreeNodeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EditorTreeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EditorTreeViewHolder, position: Int) = holder.bind(items[position])

    override fun getItemCount(): Int = items.size

    class EditorTreeViewHolder(
        private val binding: ItemEditorTreeNodeBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: EditorTreeNode) {
            binding.nodeText.text = item.label
            val paddingStartPx = (item.indentLevel * 18 * binding.root.resources.displayMetrics.density).toInt()
            binding.nodeText.setPaddingRelative(
                paddingStartPx,
                binding.nodeText.paddingTop,
                binding.nodeText.paddingEnd,
                binding.nodeText.paddingBottom
            )
            binding.nodeText.alpha = if (item.highlighted) 1f else 0.85f
        }
    }
}
