package com.example.pricelistdedup

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.pricelistdedup.databinding.ActivityAddPriceListBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class AddPriceListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddPriceListBinding
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPriceListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = Firebase.database.reference.child("priceLists")

        binding.btnSave.setOnClickListener {
            val title = binding.etPriceListTitle.text.toString().trim()

            if (title.isEmpty()) {
                Toast.makeText(this, "Enter title", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Check for duplicate title
            database.get().addOnSuccessListener { snapshot ->
                val duplicate = snapshot.children.any {
                    it.child("title").getValue(String::class.java)
                        ?.equals(title, ignoreCase = true) == true
                }

                if (duplicate) {
                    Toast.makeText(
                        this,
                        "A price list with this title already exists",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val priceListId = database.push().key
                    if (priceListId == null) {
                        Toast.makeText(this, "Error generating list ID", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }

                    val priceList = PriceList(
                        id = priceListId,
                        title = title,
                        timestamp = System.currentTimeMillis(),
                        items = emptyMap()
                    )

                    database.child(priceListId).setValue(priceList)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Price list created", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to create list", Toast.LENGTH_SHORT).show()
                        }
                }
            }
        }
    }
}
