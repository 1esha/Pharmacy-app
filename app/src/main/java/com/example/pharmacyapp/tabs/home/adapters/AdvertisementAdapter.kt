package com.example.pharmacyapp.tabs.home.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import coil.load
import com.example.pharmacyapp.databinding.ItemAdvertisementBinding

class AdvertisementAdapter(
    private val listAdvertisement: List<String>
): Adapter<AdvertisementAdapter.AdvertisementHolder>() {

    class AdvertisementHolder(val binding: ItemAdvertisementBinding): ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdvertisementHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemAdvertisementBinding.inflate(inflater,parent,false)

        return AdvertisementHolder(binding)
    }

    override fun getItemCount(): Int = listAdvertisement.size

    override fun onBindViewHolder(holder: AdvertisementHolder, position: Int): Unit = with(holder.binding) {
        val item = listAdvertisement[position]

        ivAdvertisement.load(item)
    }
    
}