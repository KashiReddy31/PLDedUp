package com.example.pricelistdedup

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pricelistdedup.databinding.ActivityMainBinding
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var database: DatabaseReference
    private val priceLists = mutableListOf<PriceList>()
    private val allPriceLists = mutableListOf<PriceList>()
    private lateinit var adapter: PriceListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        val isNight = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
        setTheme(if (isNight) R.style.AppPopupMenuDark else R.style.AppPopupMenuLight)

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        database = FirebaseDatabase.getInstance().reference

        adapter = PriceListAdapter{ selectedPriceList ->
            startActivity(Intent(this, PriceListDetailsActivity::class.java).apply {
                putExtra("priceListId", selectedPriceList.id)
                putExtra("title", selectedPriceList.title)
            })
        }

        binding.priceListRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
            setHasFixedSize(true)
        }

        setupSwipeToDelete()

        binding.fabAddPriceList.setOnClickListener {
            startActivity(Intent(this, AddPriceListActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        migrateOrphanedItems()
        fetchPriceLists()
    }

    private fun setupSwipeToDelete() {
        val swipeCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val priceList = priceLists[position]
                database.child("priceLists").child(priceList.id).removeValue()
                    .addOnSuccessListener {
                        priceLists.removeAt(position)
                        adapter.notifyItemRemoved(position)
                        Toast.makeText(this@MainActivity, "Deleted: ${priceList.title}", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this@MainActivity, "Failed to delete", Toast.LENGTH_SHORT).show()
                        adapter.notifyItemChanged(position)
                    }
            }
        }
        ItemTouchHelper(swipeCallback).attachToRecyclerView(binding.priceListRecyclerView)
    }

    private fun fetchPriceLists() {
        database.child("priceLists").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val newPriceLists = mutableListOf<PriceList>()
                for (child in snapshot.children) {
                    val priceList = child.getValue(PriceList::class.java)
                    priceList?.let {
                        newPriceLists.add(it.copy(id = child.key ?: ""))
                    }
                }
                allPriceLists.clear()
                allPriceLists.addAll(newPriceLists)
                priceLists.clear()
                priceLists.addAll(newPriceLists)
                adapter.updateList(priceLists)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Database error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun migrateOrphanedItems() {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val orphanedItems = snapshot.children.filterNot { it.key == "priceLists" }
                orphanedItems.forEach { itemSnapshot ->
                    val item = itemSnapshot.getValue(PriceListItem::class.java)
                    val listId = guessListIdFromItem(item!!)
                    if (listId != "unknown") {
                        val itemId = itemSnapshot.key ?: database.push().key!!
                        database.child("priceLists/$listId/items/$itemId").setValue(item)
                        database.child(itemSnapshot.key!!).removeValue()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Migration", "Failed: ${error.message}")
            }
        })
    }

    private fun guessListIdFromItem(item: PriceListItem): String {
        return when (item.name.lowercase()) {
            "bread", "biscuit" -> "-OOUMVRYee_rgxjTDEEG"
            "sofa" -> "-OOUNK8pRM40guTBiyap"
            else -> "unknown"
        }
    }

    private fun filterPriceLists(query: String?) {
        val filteredList = if (query.isNullOrEmpty()) {
            allPriceLists
        } else {
            allPriceLists.filter { it.title.contains(query, ignoreCase = true) }
        }
        priceLists.clear()
        priceLists.addAll(filteredList)
        adapter.updateList(filteredList)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        val searchItem = menu.findItem(R.id.menu_search)
        val searchView = searchItem.actionView as SearchView
        searchView.queryHint = "Search price lists..."

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                filterPriceLists(newText)
                return true
            }
        })

        return true
    }

        override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_home -> {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
                true
            }
            R.id.menu_add -> {
                startActivity(Intent(this, AddPriceListActivity::class.java))
                true
            }
            R.id.menu_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            R.id.menu_export -> {
                startActivity(Intent(this, PDFExportActivity::class.java))
                true
            }
            R.id.menu_chart -> {
                startActivity(Intent(this, BarChartView::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}

