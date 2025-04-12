package com.example.pharmacyapp.tabs.profile.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import coil.load
import com.example.domain.models.OrderProductModel
import com.example.pharmacyapp.databinding.ItemPurchaseHistoryBinding
import com.example.pharmacyapp.toDateTime

class PurchaseHistoryAdapter(
    private val listOrderProductModel: List<OrderProductModel>
): Adapter<PurchaseHistoryAdapter.PurchaseHistoryHolder>() {

    class PurchaseHistoryHolder(val binding: ItemPurchaseHistoryBinding): ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PurchaseHistoryHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemPurchaseHistoryBinding.inflate(inflater, parent, false)

        return PurchaseHistoryHolder(binding = binding)
    }

    override fun getItemCount(): Int = listOrderProductModel.size

    override fun onBindViewHolder(holder: PurchaseHistoryHolder, position: Int) = with(holder.binding){
        val item = listOrderProductModel[position]

        ivProductPurchaseHistory.load(item.productModel.image)

        tvProductNamePurchaseHistory.text = item.productModel.title

        tvNumberPiecesPurchaseHistory.text = item.orderModel.numberProduct.toString()

        tvOrderDate.text = item.orderModel.orderDate.toDateTime()
        tvEndDate.text = item.orderModel.endDate!!.toDateTime()
    }

}