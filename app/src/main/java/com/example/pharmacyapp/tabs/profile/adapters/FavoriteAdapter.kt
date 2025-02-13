package com.example.pharmacyapp.tabs.profile.adapters

import android.graphics.Paint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import coil.load
import com.example.domain.favorite.models.FavoriteModel
import com.example.pharmacyapp.CLUB_DISCOUNT
import com.example.pharmacyapp.databinding.ItemFavoriteBinding
import kotlin.math.roundToInt

class FavoriteAdapter(private val listItems: List<*>): RecyclerView.Adapter<FavoriteAdapter.FavoriteHolder>() {
class FavoriteAdapter(
    listItems: List<*>,
    private val deleteFromFavoritesListener: (Int, List<FavoriteModel>) -> Unit,
    ): RecyclerView.Adapter<FavoriteAdapter.FavoriteHolder>() {

        private val mutableListFavorite = mutableListOf<FavoriteModel>()

    init {
        listItems.forEach {
            val favoriteModel = it as FavoriteModel
            mutableListFavorite.add(favoriteModel)
        }
    }

    class FavoriteHolder(val binding: ItemFavoriteBinding): ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemFavoriteBinding.inflate(inflater, parent, false)

        return FavoriteHolder(binding = binding)
    }

    override fun getItemCount(): Int = mutableListFavorite.size

    override fun onBindViewHolder(holder: FavoriteHolder, position: Int): Unit = with(holder.binding) {

        val item = mutableListFavorite[position]
        val originalPrice = item.price
        val discount = item.discount
        val sumDiscount = ((discount / 100) * originalPrice)
        val price = originalPrice - sumDiscount
        val sumClubDiscount = ((CLUB_DISCOUNT / 100) * price)
        val priceClub = price - sumClubDiscount

        val textOriginalPrice = originalPrice.roundToInt().toString()
        val textDiscount = discount.roundToInt().toString()
        val textPrice = price.roundToInt().toString()
        val textPriceClub = priceClub.roundToInt().toString()

        tvProductNameFavorite.text = item.title
        tvPriceWithClubCardFavorite.text = textPriceClub

        tvOriginalPriceFavorite.text = textOriginalPrice
        tvOriginalPriceFavorite.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
        tvDiscountFavorite.text = "$textDiscount%"
        tvPriceFavorite.text = textPrice

        if (discount == 0.0) {
            layoutDiscount.visibility = View.GONE
            layoutOriginalPrice.visibility = View.GONE
        }
        else {
            layoutDiscount.visibility = View.VISIBLE
            layoutOriginalPrice.visibility = View.VISIBLE
        }
        ivProductFavorite.load(item.image)

        bDeleteFromFavorites.setOnClickListener {
            try {
                mutableListFavorite.remove(item)
                deleteFromFavoritesListener(item.productId, mutableListFavorite)
            }
            catch (e: Exception) {
                Log.e("TAG",e.stackTraceToString())
            }
        }

    }

}