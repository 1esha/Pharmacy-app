package com.example.pharmacyapp.tabs.catalog.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.domain.models.DetailsProductModel
import com.example.pharmacyapp.databinding.ItemInstructionManualBinding

class InstructionManualAdapter(
    private val listDetailsProduct: List<DetailsProductModel>
): Adapter<InstructionManualAdapter.InstructionManualHolder>() {

    class InstructionManualHolder(val binding: ItemInstructionManualBinding): ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InstructionManualHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemInstructionManualBinding.inflate(inflater, parent, false)

        return InstructionManualHolder(binding)
    }

    override fun getItemCount(): Int = listDetailsProduct.size

    override fun onBindViewHolder(holder: InstructionManualHolder, position: Int) = with(holder.binding) {

        val detailsProduct = listDetailsProduct[position]

        tvTitleInstruction.text = detailsProduct.title
        tvBodyInstruction.text = detailsProduct.body

    }


}