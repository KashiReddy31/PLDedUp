package com.example.pricelistdedup

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.pricelistdedup.databinding.ItemMultiSelectPriceListBinding

class MultiSelectPriceListAdapter(
    private val priceLists: List<PriceList>
) : RecyclerView.Adapter<MultiSelectPriceListAdapter.ViewHolder>() {

    private val selectedItems = mutableSetOf<PriceList>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMultiSelectPriceListBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val priceList = priceLists[position]
        holder.bind(priceList, selectedItems.contains(priceList))
    }

    override fun getItemCount(): Int = priceLists.size

    fun getSelectedPriceLists(): List<PriceList> = selectedItems.toList()

    inner class ViewHolder(private val binding: ItemMultiSelectPriceListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(priceList: PriceList, isSelected: Boolean) {
            binding.textViewTitle.text = priceList.title
            binding.checkbox.isChecked = isSelected
            binding.root.setOnClickListener {
                if (selectedItems.contains(priceList)) {
                    selectedItems.remove(priceList)
                    binding.checkbox.isChecked = false
                } else {
                    selectedItems.add(priceList)
                    binding.checkbox.isChecked = true
                }
            }
        }
    }
}
