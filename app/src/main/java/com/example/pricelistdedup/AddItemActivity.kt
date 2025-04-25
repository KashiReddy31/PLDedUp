package com.example.pricelistdedup

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.pricelistdedup.databinding.ActivityAddItemBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class AddItemActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddItemBinding
    private lateinit var databaseRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddItemBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize database reference
        val priceListId = intent.getStringExtra("priceListId") ?: run {
            Toast.makeText(this, "Invalid price list", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        databaseRef = Firebase.database.reference
            .child("priceLists")
            .child(priceListId)
            .child("items")


        binding.btnSave.setOnClickListener {
            val itemName = binding.etItemName.text.toString().trim()
            val itemPriceText = binding.etItemPrice.text.toString().trim()

            if (itemName.isEmpty() || itemPriceText.isEmpty()) {
                Toast.makeText(this, "Please enter both name and price", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val itemPrice = itemPriceText.toDoubleOrNull() ?: run {
                Toast.makeText(this, "Please enter a valid price", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Check for duplicate item name
            databaseRef.get().addOnSuccessListener { snapshot ->
                val duplicate = snapshot.children.any {
                    it.child("name").getValue(String::class.java)
                        ?.equals(itemName, ignoreCase = true) == true
                }

                if (duplicate) {
                    Toast.makeText(this, "Item with this name already exists", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    val itemId = databaseRef.push().key ?: run {
                        Toast.makeText(this, "Error generating item ID", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }

                    val newItem = PriceListItem(id = itemId, name = itemName, price = itemPrice)

                    databaseRef.child(itemId).setValue(newItem)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Item added successfully", Toast.LENGTH_SHORT)
                                .show()
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                this,
                                "Failed to add item: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            }
        }
    }
}
