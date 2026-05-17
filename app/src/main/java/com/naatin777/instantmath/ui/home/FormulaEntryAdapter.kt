package com.naatin777.instantmath.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.naatin777.instantmath.databinding.ItemFormulaEntryBinding

class FormulaEntryAdapter(
    private val entries: List<FormulaEntry>,
) : RecyclerView.Adapter<FormulaEntryAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemFormulaEntryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFormulaEntryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false,
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entry = entries[position]
        holder.binding.tvFormula.text = entry.formula
        holder.binding.ivFavorite.visibility = if (entry.isFavorite) View.VISIBLE else View.GONE
        holder.binding.tvTimestamp.text = entry.timestamp
    }

    override fun getItemCount(): Int = entries.size
}
