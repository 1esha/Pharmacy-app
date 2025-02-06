package com.example.domain.catalog.models

import kotlin.math.roundToInt

data class ProductModel(
    val product_id: Int,
    val title: String,
    val product_path: String,
    val price: Double,
    val discount: Double,
    val product_basic_info: List<Map<String,String>>,
    val product_detailed_info: List<Map<String,String>>,
    val image: String
): Comparable<ProductModel> {

    private fun getPrice(discount: Double, price: Double): Int {

        val clubDiscount = 3.0

        val sumDiscount = ((discount) / 100) * price
        val priceDiscounted = price - sumDiscount
        val sumClubDiscount = (clubDiscount / 100) * priceDiscounted
        val priceClubDiscounted = priceDiscounted - sumClubDiscount

        return priceClubDiscounted.roundToInt()
    }

    override fun compareTo(other: ProductModel): Int {

        val currentPrice = getPrice(
            discount = this.discount,
            price = this.price
        )

        val otherPrice = getPrice(
            discount = other.discount,
            price = other.price
        )

       return currentPrice.compareTo(otherPrice)
    }

}
