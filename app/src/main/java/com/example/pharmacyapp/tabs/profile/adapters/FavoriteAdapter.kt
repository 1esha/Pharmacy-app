package com.example.pharmacyapp.tabs.profile.adapters

import android.graphics.Paint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import coil.load
import com.example.domain.models.FavouriteBasketModel
import com.example.pharmacyapp.CLUB_DISCOUNT
import com.example.pharmacyapp.databinding.ItemFavoriteBinding
import kotlin.math.roundToInt

/**
 * Класс [FavoriteAdapter] является адаптером для списка избранных товаров во фрагиенте FavoriteFragment.
 *
 * Параметры:
 * [mutableListFavouriteBasketModel] - список товаров;
 * [deleteFromFavoritesListener] - обработка удаления из "Избранного";
 * [addInBasketFromFavoritesListener] - обработка добавления в корзину;
 * [navigateToProductInfo] - обработка перехода на экран с подробной информацией о товаре;
 * [textCategory] - текст категории товара.
 */
class FavoriteAdapter(
    private val mutableListFavouriteBasketModel: MutableList<FavouriteBasketModel>,
    private val deleteFromFavoritesListener: (Int) -> Unit,
    private val addInBasketFromFavoritesListener: (Int) -> Unit,
    private val navigateToProductInfo: (Int) -> Unit,
    private val textCategory: String
    ): RecyclerView.Adapter<FavoriteAdapter.FavoriteHolder>() {


    class FavoriteHolder(val binding: ItemFavoriteBinding): ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemFavoriteBinding.inflate(inflater, parent, false)

        return FavoriteHolder(binding = binding)
    }

    override fun getItemCount(): Int = mutableListFavouriteBasketModel.size

    override fun onBindViewHolder(holder: FavoriteHolder, position: Int): Unit = with(holder.binding) {

        // Заполнение переменных данными товара, вычисление необходимых значений
        val item = mutableListFavouriteBasketModel[position]
        val originalPrice = item.favoriteModel.price
        val discount = item.favoriteModel.discount
        val sumDiscount = ((discount / 100) * originalPrice)
        val price = originalPrice - sumDiscount
        val sumClubDiscount = ((CLUB_DISCOUNT / 100) * price)
        val priceClub = price - sumClubDiscount
        val subcategory = ' '+item.favoriteModel.productPath.toSubcategoryByPath()

        val textOriginalPrice = originalPrice.roundToInt().toString()
        val textDiscount = discount.roundToInt().toString()
        val textPrice = price.roundToInt().toString()
        val textPriceClub = priceClub.roundToInt().toString()
        val textCategoryFavourite = textCategory+subcategory

        // Установка данных элемета списка
        tvProductNameFavorite.text = item.favoriteModel.title
        tvPriceWithClubCardFavorite.text = textPriceClub

        tvOriginalPriceFavorite.text = textOriginalPrice
        tvOriginalPriceFavorite.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
        tvDiscountFavorite.text = "$textDiscount%"
        tvPriceFavorite.text = textPrice

        // Если нет скидки, то надо убрать TextView со скидками, иначе наоборот
        if (discount == 0.0) {
            layoutDiscount.visibility = View.GONE
            layoutOriginalPrice.visibility = View.GONE
        }
        else {
            layoutDiscount.visibility = View.VISIBLE
            layoutOriginalPrice.visibility = View.VISIBLE
        }

        ivProductFavorite.load(item.favoriteModel.image)

        tvCategoryFavourite.text = textCategoryFavourite

        // Если товар в корзине, то кнопка добавления в корзину не включена
        bAddInBasketFromFavorites.isEnabled = !item.isInBasket

        // Оюработка удаления из списка
        bDeleteFromFavorites.setOnClickListener {
            try {
                mutableListFavouriteBasketModel.remove(item)

                deleteFromFavoritesListener(item.favoriteModel.productId)

                notifyItemRemoved(position)
            }
            catch (e: Exception){
                Log.e("TAG",e.stackTraceToString())
            }

        }

        layoutFavoritePanel.setOnClickListener {
            navigateToProductInfo(item.favoriteModel.productId)
        }

        // Обработка добавления в корзину
        bAddInBasketFromFavorites.setOnClickListener {
            try {
                // Обновление значения isInBasket текущего товара на значение true
                val index = mutableListFavouriteBasketModel.indexOf(item)

                mutableListFavouriteBasketModel.removeAt(index)
                mutableListFavouriteBasketModel.add(index,item.copy(isInBasket = true))

                addInBasketFromFavoritesListener(item.favoriteModel.productId)

                notifyItemChanged(position)
            }
            catch (e: Exception){
                Log.e("TAG",e.stackTraceToString())
            }
        }

    }

    /**
     * Преобразование строки пути товара в строку подкатегории товара.
     */
    private fun String.toSubcategoryByPath(): String{
        var indexStart = 0
        val indexEnd = this.length
        var counter = 0
        var result = ""
        this.forEach { char ->

            if (char == '/') indexStart = counter + 1
            counter++
        }
        val pathSubCategory = substring(indexStart,indexEnd)

        counter = 0
        pathSubCategory.forEach { char ->
            var currentChar = char

            if (counter == 0) currentChar = char.uppercaseChar()

            if (char == '_') currentChar = ' '

            result += currentChar
            counter++
        }
        return result
    }

}