package com.example.pharmacyapp.tabs.catalog.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import coil.load
import com.example.domain.catalog.models.ProductModel
import com.example.pharmacyapp.databinding.ItemProductsBinding

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
            Log.i("TAG", "product = $product")
            val sumDiscount = (product.discount / 100) * product.price
            val clubPrice = product.price - sumDiscount
            ivProduct.load(product.image)
            tvProductName.text = product.title
            tvClubPrice.text = clubPrice.toString()
            tvPrice.text = product.price.toString()

            root.setOnClickListener {
                onClick(product.product_id)
            }
        }
}