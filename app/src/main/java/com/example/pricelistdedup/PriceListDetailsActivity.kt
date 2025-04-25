package com.example.pricelistdedup

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pricelistdedup.databinding.ActivityPriceListDetailsBinding
import com.google.firebase.database.*

class PriceListDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPriceListDetailsBinding
    private lateinit var adapter: PriceListItemAdapter
    private lateinit var database: DatabaseReference
    private val items = mutableListOf<PriceListItem>()
    private lateinit var priceListId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPriceListDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        priceListId = intent.getStringExtra("priceListId") ?: run {
            Toast.makeText(this, "Invalid price list", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        val title = intent.getStringExtra("title") ?: "Price List"

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            this.title = title
        }

        database = FirebaseDatabase.getInstance()
            .getReference("priceLists")
            .child(priceListId)
            .child("items")

        setupRecyclerView()
        loadItems()

        binding.fabAdd.setOnClickListener {
            startActivity(Intent(this, AddItemActivity::class.java).apply {
                putExtra("priceListId", priceListId)
            })
        }
    }

    private fun setupRecyclerView() {
        adapter = PriceListItemAdapter(items) { item ->
            startActivity(Intent(this, ItemDetailsActivity::class.java).apply {
                putExtra("itemName", item.name)
                putExtra("itemPrice", item.price)
            })
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@PriceListDetailsActivity)
            adapter = this@PriceListDetailsActivity.adapter
            setHasFixedSize(true)
        }

        // Swipe-to-delete functionality
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val item = items[position]
                database.child(item.id).removeValue()
                items.removeAt(position)
                adapter.notifyItemRemoved(position)
                Toast.makeText(this@PriceListDetailsActivity, "Deleted: ${item.name}", Toast.LENGTH_SHORT).show()
            }
        }).attachToRecyclerView(binding.recyclerView)
    }

    private fun loadItems() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val tempList = mutableListOf<PriceListItem>()
                snapshot.children.forEach { child ->
                    try {
                        val item = child.getValue(PriceListItem::class.java)
                        item?.let {
                            tempList.add(it.copy(id = child.key ?: ""))
                        }
                    } catch (e: Exception) {
                        Log.e("PriceListDetails", "Error parsing item ${child.key}", e)
                    }
                }
                items.clear()
                items.addAll(tempList)
                adapter.notifyDataSetChanged()

                if (items.isEmpty()) {
                    Toast.makeText(this@PriceListDetailsActivity, "No items found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@PriceListDetailsActivity, "Failed to load items", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun filterItems(query: String) {
        val filteredList = if (query.isEmpty()) {
            items
        } else {
            items.filter { it.name.contains(query, true) }
        }
        adapter.updateList(filteredList)
    }
}
