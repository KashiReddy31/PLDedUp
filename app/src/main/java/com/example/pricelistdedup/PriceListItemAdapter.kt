package com.example.pricelistdedup

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.pricelistdedup.databinding.ItemPriceBinding

class PriceListItemAdapter(
    private val items: MutableList<PriceListItem>,
    private val onClick: (PriceListItem) -> Unit
) : RecyclerView.Adapter<PriceListItemAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemPriceBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPriceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.tvName.text = item.name
        holder.binding.tvPrice.text = "₹%.2f".format(item.price)
        holder.itemView.setOnClickListener { onClick(item) }
    }

    override fun getItemCount() = items.size

    fun updateList(newList: List<PriceListItem>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }
}
