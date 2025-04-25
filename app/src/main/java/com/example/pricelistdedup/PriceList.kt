package com.example.pricelistdedup

data class PriceList( val id: String = "", val title: String = "", val timestamp: Long = 0L, val items: Map<String, PriceListItem>? = emptyMap() ) {
    constructor() : this("", "", 0L, emptyMap())

}