package com.example.pharmacyapp.tabs.catalog.adapters

import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import coil.load
import com.example.domain.favorite.models.FavoriteModel
import com.example.domain.catalog.models.ProductFavoriteModel
import com.example.pharmacyapp.CLUB_DISCOUNT
import com.example.pharmacyapp.R
import com.example.pharmacyapp.UNAUTHORIZED_USER
import com.example.pharmacyapp.databinding.ItemProductsBinding
import kotlin.math.roundToInt

class ProductsAdapter(
    private val userId: Int,
    listProducts: List<*>,
    private val onClickProduct: (Int,Boolean) -> Unit,
    private val onClickFavorite: (FavoriteModel, Boolean) -> Unit) : Adapter<ProductsAdapter.ProductsHolder>() {

        private val mutableListProductFavorite = mutableListOf<ProductFavoriteModel>()

    init {
        listProducts.forEach {
            val productFavoriteModel = it as ProductFavoriteModel

            mutableListProductFavorite.add(
                if (userId == UNAUTHORIZED_USER) {
                    ProductFavoriteModel(
                        isFavorite = false,
                        productModel = productFavoriteModel.productModel
                    )
                }
                else {
                    productFavoriteModel
                }
            )
        }
    }

    class ProductsHolder(val binding: ItemProductsBinding) : ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductsHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemProductsBinding.inflate(inflater, parent, false)

        return ProductsHolder(binding = binding)
    }

    override fun getItemCount(): Int = mutableListProductFavorite.size

    override fun onBindViewHolder(holder: ProductsHolder, position: Int): Unit =
        with(holder.binding) {
            val currentProduct = mutableListProductFavorite[position]
            val product = currentProduct.productModel
            var isFavorite = currentProduct.isFavorite

            val originalPrice = product.price
            val discount = product.discount
            val sumDiscount = ((discount / 100) * originalPrice)
            val price = (originalPrice - sumDiscount)
            val sumClubDiscount = ((CLUB_DISCOUNT / 100) * price)
            val priceClub = price - sumClubDiscount

            val colorDiscount = Color.rgb(198, 1,63)

            ivProduct.load(product.image)
            tvProductName.text = product.title

            val textPrice = price.roundToInt().toString()
            tvPrice.text = textPrice

            val textPriceClub = priceClub.roundToInt().toString()
            tvPriceWithClubCard.text = textPriceClub

            if (product.discount > 0.0) {
                tvPrice.setTextColor(colorDiscount)
                tvRubleSign.setTextColor(colorDiscount)

                tvDiscount.visibility = View.VISIBLE
                tvOriginalPrice.visibility = View.VISIBLE

                val textDiscount = "-${discount.roundToInt()}%"
                tvDiscount.text = textDiscount

                val textOriginalPrice = originalPrice.roundToInt().toString()
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

            ivFavorite.setImageResource(if (isFavorite) R.drawable.ic_favorite else R.drawable.ic_favorite_border)

            root.setOnClickListener {
                onClickProduct(product.product_id,isFavorite)
            }

            ivFavorite.setOnClickListener {

                val favoriteModel = FavoriteModel(
                    productId = product.product_id,
                    title = product.title,
                    productPath = product.product_path,
                    price = product.price,
                    discount = product.discount,
                    image = product.image
                )

                onClickFavorite(favoriteModel,!isFavorite)

                if (userId != UNAUTHORIZED_USER) {
                    isFavorite = !isFavorite
                    mutableListProductFavorite.removeAt(position)
                    mutableListProductFavorite.add(position,ProductFavoriteModel(
                        isFavorite = isFavorite,
                        productModel = product
                    ))

                    notifyItemChanged(position)
                }


            }
        }
}