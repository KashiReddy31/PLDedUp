package com.example.pricelistdedup

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.pricelistdedup.databinding.ActivityItemDetailsBinding

class ItemDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityItemDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityItemDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val itemName = intent.getStringExtra("itemName") ?: "No Name"
        val itemPrice = intent.getDoubleExtra("itemPrice", 0.0)

        binding.tvItemDetailName.text = itemName
        binding.tvItemDetailPrice.text = "$${itemPrice}"
    }
}
