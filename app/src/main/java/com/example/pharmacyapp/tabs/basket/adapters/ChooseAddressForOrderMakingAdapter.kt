package com.example.pharmacyapp.tabs.basket.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.domain.models.AvailabilityInPharmacyModel
import com.example.domain.models.AvailabilityProductsForOrderMakingModel
import com.example.pharmacyapp.R
import com.example.pharmacyapp.databinding.ItemAddressForOrderMakingBinding

class ChooseAddressForOrderMakingAdapter(
    private val listAvailabilityProductsForOrderMakingModel: List<AvailabilityProductsForOrderMakingModel>,
    private val totalNumber: Int,
    private val availabilityInPharmacyModel: AvailabilityInPharmacyModel,
    private val onClick: (Int) -> Unit
): Adapter<ChooseAddressForOrderMakingAdapter.OrderMakingHolder>() {

    class OrderMakingHolder(val binding: ItemAddressForOrderMakingBinding): ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderMakingHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemAddressForOrderMakingBinding.inflate(inflater,parent,false)

        return OrderMakingHolder(binding)
    }

    override fun getItemCount(): Int = listAvailabilityProductsForOrderMakingModel.size

    override fun onBindViewHolder(holder: OrderMakingHolder, position: Int) = with(holder.binding) {
        val item = listAvailabilityProductsForOrderMakingModel[position]

        tvAddressPharmacyForOrderMaking.text = item.address
        tvCityAddressPharmacyForOrderMaking.text = item.city

        when {
            totalNumber == item.availableQuantity -> {
                ivAvailabilityStatusForOrderMaking.setImageResource(R.drawable.ic_check_circle)
                tvAvailabilityInPharmacy.text = availabilityInPharmacyModel.textInStock
                tvAvailabilityInPharmacy.setTextColor(availabilityInPharmacyModel.colorInStock)

                bChooseAddressForOrderMaking.isEnabled = true
            }
            item.availableQuantity in 1..< totalNumber -> {
                ivAvailabilityStatusForOrderMaking.setImageResource(R.drawable.ic_warning)
                val textWarning = "${availabilityInPharmacyModel.textWarning} ${item.availableQuantity} / $totalNumber"
                tvAvailabilityInPharmacy.text = textWarning
                tvAvailabilityInPharmacy.setTextColor(availabilityInPharmacyModel.colorWarning)

                bChooseAddressForOrderMaking.isEnabled = true
            }
            item.availableQuantity == 0 -> {
                ivAvailabilityStatusForOrderMaking.setImageResource(R.drawable.ic_remove_circle)
                tvAvailabilityInPharmacy.text = availabilityInPharmacyModel.textOutOfStock
                tvAvailabilityInPharmacy.setTextColor(availabilityInPharmacyModel.colorOutOfStock)

                bChooseAddressForOrderMaking.isEnabled = false
            }
        }

        bChooseAddressForOrderMaking.setOnClickListener {
            onClick(item.addressId)
        }
    }
}