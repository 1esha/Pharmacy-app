package com.example.pharmacyapp.tabs.profile.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import coil.load
import com.example.domain.models.OrderProductModel
import com.example.pharmacyapp.databinding.ItemBookedGoodsBinding
import com.example.pharmacyapp.toDateTime

class BookedGoodsAdapter(
    private val listOrderProductModel: List<OrderProductModel>,
    private val getStringOrderDate: (String) -> String,
    private val navigateToProductInfo: (Int) -> Unit
): Adapter<BookedGoodsAdapter.BookedGoodsHolder>() {

    class BookedGoodsHolder(val binding: ItemBookedGoodsBinding): ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookedGoodsHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemBookedGoodsBinding.inflate(inflater, parent, false)

        return BookedGoodsHolder(binding = binding)
    }

    override fun getItemCount(): Int = listOrderProductModel.size

    override fun onBindViewHolder(holder: BookedGoodsHolder, position: Int) = with(holder.binding){
        val item = listOrderProductModel[position]

        ivProductBookedGoods.load(item.productModel.image)

        tvProductNameBookedGoods.text = item.productModel.title

        tvNumberPiecesBookedGoods.text = item.orderModel.numberProduct.toString()

        tvOrderDateBookedGoods.text = getStringOrderDate(item.orderModel.orderDate.toDateTime())

        root.setOnClickListener {
            navigateToProductInfo(item.productModel.productId)
        }
    }

}