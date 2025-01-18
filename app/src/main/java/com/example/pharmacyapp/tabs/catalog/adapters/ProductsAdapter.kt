package com.example.pharmacyapp.tabs.catalog.adapters

import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import coil.load
import com.example.domain.catalog.models.ProductModel
import com.example.pharmacyapp.databinding.ItemProductsBinding
import kotlin.math.roundToInt

class ProductsAdapter(
    private val listProducts: List<*>,
    private val onClick: (Int) -> Unit) : Adapter<ProductsAdapter.ProductsHolder>() {

    class ProductsHolder(val binding: ItemProductsBinding) : ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductsHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemProductsBinding.inflate(inflater, parent, false)

        return ProductsHolder(binding = binding)
    }

    override fun getItemCount(): Int = listProducts.size

    override fun onBindViewHolder(holder: ProductsHolder, position: Int): Unit =
        with(holder.binding) {
            val product = listProducts[position] as ProductModel
            val originalPrice = product.price.roundToInt()
            val clubDiscount = 3.0
            val discount = product.discount
            val sumDiscount = ((discount / 100) * product.price)
            val price = (product.price - sumDiscount).roundToInt()
            val sumClubDiscount = ((clubDiscount / 100) * price)
            val priceClub = ((product.price - sumDiscount) - sumClubDiscount).roundToInt()

            val colorDiscount = Color.rgb(198, 1,63)

            ivProduct.load(product.image)
            tvProductName.text = product.title

            val textPrice = price.toString()
            tvPrice.text = textPrice

            val textPriceClub = priceClub.toString()
            tvPriceWithClubCard.text = textPriceClub

            if (product.discount > 0.0) {
                tvPrice.setTextColor(colorDiscount)
                tvRubleSign.setTextColor(colorDiscount)

                tvDiscount.visibility = View.VISIBLE
                tvOriginalPrice.visibility = View.VISIBLE

                val textDiscount = "-${discount.roundToInt()}%"
                tvDiscount.text = textDiscount

                val textOriginalPrice = originalPrice.toString()
                tvOriginalPrice.text = textOriginalPrice
                tvOriginalPrice.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG

            }
            else {
                val defaultColor  = tvProductName.textColors.defaultColor
                tvPrice.setTextColor(defaultColor)
                tvRubleSign.setTextColor(defaultColor)

                tvDiscount.visibility = View.GONE
                tvOriginalPrice.visibility = View.GONE

            }

            root.setOnClickListener {
                onClick(product.product_id)
            }
        }
}