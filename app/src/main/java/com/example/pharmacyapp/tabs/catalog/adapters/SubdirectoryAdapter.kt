package com.example.pharmacyapp.tabs.catalog.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.pharmacyapp.databinding.ItemSubdirectoryBinding
import com.example.pharmacyapp.toPath

class SubdirectoryAdapter(
    private val listItems: List<String>,
    private val onClick: (String) -> Unit
): Adapter<SubdirectoryAdapter.SubdirectoryHolder>() {

    class SubdirectoryHolder(val binding: ItemSubdirectoryBinding): ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubdirectoryHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemSubdirectoryBinding.inflate(inflater, parent, false)

        return SubdirectoryHolder(binding = binding)
    }

    override fun getItemCount(): Int = listItems.size

    override fun onBindViewHolder(holder: SubdirectoryHolder, position: Int) = with(holder.binding) {
        val item = listItems[position]

        tvSubdirectory.text = item
        root.setOnClickListener {
            val pathSecondary = item.toPath()
            onClick(pathSecondary)
        }
    }
}