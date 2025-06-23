package com.example.pharmacyapp.tabs.catalog.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.domain.models.CatalogMainModel
import com.example.pharmacyapp.databinding.ItemMainCatalogBinding
import com.example.pharmacyapp.toPath

class CatalogMainAdapter(
    private val listItems: List<CatalogMainModel>,
    private val onClick: (String) -> Unit
) : Adapter<CatalogMainAdapter.CatalogMainHolder>() {

    class CatalogMainHolder(val binding: ItemMainCatalogBinding) : ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CatalogMainHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemMainCatalogBinding.inflate(inflater, parent, false)

        return CatalogMainHolder(binding = binding)
    }

    override fun getItemCount(): Int = listItems.size

    override fun onBindViewHolder(holder: CatalogMainHolder, position: Int) = with(holder.binding) {
        val item = listItems[position]

        ivImageMainCatalog.setImageResource(item.image)
        tvTitleMainCatalog.text = item.title

        cardMainCatalog.setOnClickListener {
            try {
                val path = item.title.toPath()
                onClick(path)
            }
            catch (e: Exception){
                Log.e("TAG","Ошибка навигации")
            }
        }
    }
}