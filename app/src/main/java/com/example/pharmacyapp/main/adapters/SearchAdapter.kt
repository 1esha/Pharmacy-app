package com.example.pharmacyapp.main.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import coil.load
import com.example.domain.catalog.models.ProductModel
import com.example.pharmacyapp.databinding.ItemSearchBinding

class SearchAdapter(
    private val listProducts: List<ProductModel>,
    private val onClick: (Int,String) -> Unit
): Adapter<SearchAdapter.SearchHolder>() {

    class SearchHolder(val binding: ItemSearchBinding): ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemSearchBinding.inflate(inflater,parent,false)

        return SearchHolder(binding)
    }

    override fun getItemCount(): Int = listProducts.size

    override fun onBindViewHolder(holder: SearchHolder, position: Int) = with(holder.binding){
        val product = listProducts[position]

        ivProductSearch.load(product.image)
        tvProductNameSearch.text = product.title

        root.setOnClickListener {
            onClick(product.productId,product.title)
        }
    }
}