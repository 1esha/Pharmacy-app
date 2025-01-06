package com.example.pharmacyapp.tabs.catalog.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.domain.models.SelectedPharmacyAddressesModel
import com.example.pharmacyapp.databinding.ItemPharmacyAddressesBinding

class PharmacyAddressesAdapter(
    private val listItems: List<*>,
    private val onClick: (Int, Boolean) -> Unit
): Adapter<PharmacyAddressesAdapter.PharmacyAddressesHolder>() {

    class PharmacyAddressesHolder(val biding: ItemPharmacyAddressesBinding): ViewHolder(biding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PharmacyAddressesHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemPharmacyAddressesBinding.inflate(inflater, parent, false)

        return PharmacyAddressesHolder(binding)
    }

    override fun getItemCount(): Int = listItems.size

    override fun onBindViewHolder(holder: PharmacyAddressesHolder, position: Int) = with(holder.biding) {
        val item = listItems[position] as SelectedPharmacyAddressesModel

        tvAddressPharmacy.text = item.pharmacyAddressesModel.address
        tvCityAddressPharmacy.text = item.pharmacyAddressesModel.city

        checkBoxAddressPharmacy.isChecked = item.isSelected

        checkBoxAddressPharmacy.setOnClickListener {
            val isChecked = checkBoxAddressPharmacy.isChecked
            onClick(position,isChecked)
        }

    }

}