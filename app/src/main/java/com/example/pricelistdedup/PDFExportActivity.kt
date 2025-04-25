package com.example.pricelistdedup

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pricelistdedup.databinding.ActivityPdfExportBinding
import com.google.firebase.database.*
import java.io.File
import java.io.FileOutputStream

class PDFExportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPdfExportBinding
    private lateinit var database: DatabaseReference
    private val priceLists = mutableListOf<PriceList>()
    private lateinit var adapter: MultiSelectPriceListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfExportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance().reference.child("priceLists")

        adapter = MultiSelectPriceListAdapter(priceLists)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        binding.btnExportSelected.setOnClickListener {
            val selectedLists = adapter.getSelectedPriceLists()
            if (selectedLists.isEmpty()) {
                Toast.makeText(this, "No price lists selected", Toast.LENGTH_SHORT).show()
            } else {
                selectedLists.forEach { list ->
                    fetchItemsAndExport(list)
                }
            }
        }

        fetchPriceLists()
    }

    private fun fetchPriceLists() {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<PriceList>()
                for (child in snapshot.children) {
                    val pl = child.getValue(PriceList::class.java)
                    pl?.let { list.add(it.copy(id = child.key ?: "")) }
                }
                priceLists.clear()
                priceLists.addAll(list)
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@PDFExportActivity, "Failed to load price lists", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchItemsAndExport(priceList: PriceList) {
        val itemRef = database.child(priceList.id).child("items")
        itemRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = snapshot.children.mapNotNull { it.getValue(PriceListItem::class.java) }
                if (items.isEmpty()) {
                    Toast.makeText(this@PDFExportActivity, "No items found", Toast.LENGTH_SHORT).show()
                } else {
                    exportPriceListAsPdf(priceList, items)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@PDFExportActivity, "Failed to load items", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun exportPriceListAsPdf(priceList: PriceList, items: List<PriceListItem>) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()

        paint.textSize = 18f
        paint.isFakeBoldText = true
        canvas.drawText("Price List: ${priceList.title}", 40f, 50f, paint)

        paint.textSize = 14f
        paint.isFakeBoldText = false
        var y = 100f
        items.forEach {
            canvas.drawText("${it.name}: ₹${it.price}", 40f, y, paint)
            y += 30
        }

        pdfDocument.finishPage(page)

        val file = File(getExternalFilesDir(null), "${priceList.title}.pdf")
        try {
            pdfDocument.writeTo(FileOutputStream(file))
            openPdf(file)
        } catch (e: Exception) {
            Toast.makeText(this, "Error writing PDF: ${e.message}", Toast.LENGTH_LONG).show()
        } finally {
            pdfDocument.close()
        }
    }

    private fun openPdf(file: File) {
        val uri: Uri = FileProvider.getUriForFile(this, "$packageName.provider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NO_HISTORY
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "No app found to open PDF", Toast.LENGTH_SHORT).show()
        }
    }
}
