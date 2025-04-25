package com.example.pricelistdedup

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class BarChartView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private var items = listOf<PriceListItem>()
    private val paint = Paint()

    fun setItems(data: List<PriceListItem>) {
        items = data
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (items.isEmpty()) return

        val max = items.maxOf { it.price }
        val barWidth = width / (items.size * 2).toFloat()

        items.forEachIndexed { index, item ->
            val barHeight = (item.price / max) * height
            paint.color = Color.BLUE
            val left = index * 2 * barWidth
            canvas.drawRect(left,
                (height - barHeight).toFloat(), left + barWidth, height.toFloat(), paint)
        }
    }
}
