package com.example.pricelistdedup

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.pricelistdedup.databinding.ItemPriceListBinding

class PriceListAdapter( private val onItemClick: (PriceList) -> Unit ) : RecyclerView.Adapter<PriceListAdapter.ViewHolder>() {

    private val items = mutableListOf<PriceList>()

    inner class ViewHolder(private val binding: ItemPriceListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(priceList: PriceList) {
            binding.tvTitle.text = priceList.title
            binding.root.setOnClickListener { onItemClick(priceList) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemPriceListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    fun updateList(newList: List<PriceList>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }
    fun getCurrentList(): List<PriceList> = items
}
