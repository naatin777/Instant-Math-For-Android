package com.naatin777.instantmath.ui.home

import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.naatin777.instantmath.databinding.ItemMathSymbolBinding

class MathSymbolAdapter(
    private val symbols: List<String>,
    private val onSymbolClick: (String) -> Unit
) : RecyclerView.Adapter<MathSymbolAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemMathSymbolBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMathSymbolBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val symbol = symbols[position]
        holder.binding.btnSymbol.text = symbol
        holder.binding.btnSymbol.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            onSymbolClick(symbol)
        }
    }

    override fun getItemCount(): Int = symbols.size
}
