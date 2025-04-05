package com.example.pharmacyapp.tabs.basket.adapters

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import coil.load
import com.example.domain.basket.models.BasketModel
import com.example.domain.models.SelectedBasketModel
import com.example.pharmacyapp.CLUB_DISCOUNT
import com.example.pharmacyapp.MAX_NUMBER_PRODUCT_IN_BASKET
import com.example.pharmacyapp.databinding.ItemBasketBinding
import com.example.pharmacyapp.databinding.SpaceBinding
import kotlin.math.roundToInt

class BasketAdapter(
    private var mutableListSelectedBasket: MutableList<SelectedBasketModel>,
    private val onClickCheckBox: (SelectedBasketModel) -> Unit,
    private val onUpdateNumberProducts: (SelectedBasketModel) -> Unit
): Adapter<ViewHolder>() {

    class BasketHolder(val binding: ItemBasketBinding): ViewHolder(binding.root)
    class SpaceHolder(val binding: SpaceBinding): ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemBasketBinding = ItemBasketBinding.inflate(inflater, parent, false)
        val spaceBinding = SpaceBinding.inflate(inflater, parent, false)

        return if (viewType > 0) SpaceHolder(spaceBinding) else BasketHolder(itemBasketBinding)
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == mutableListSelectedBasket.size) 1 else 0
    }

    override fun getItemCount(): Int = mutableListSelectedBasket.size + 1

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (holder is BasketHolder){
            with(holder.binding){
                val selectedBasketModel = mutableListSelectedBasket[position]
                val basketModel = selectedBasketModel.basketModel
                var isSelect = selectedBasketModel.isSelect

                // Заполнение переменных данными товара, вычисление необходимых значений
                val originalPrice = basketModel.productModel.price
                val discount = basketModel.productModel.discount
                val sumDiscount = ((discount / 100) * originalPrice)
                val price = originalPrice - sumDiscount
                val sumClubDiscount = ((CLUB_DISCOUNT / 100) * price)
                val priceClub = price - sumClubDiscount
                //val subcategory = ' '+item.favoriteModel.productPath.toSubcategoryByPath()

                val textOriginalPrice = originalPrice.roundToInt().toString()
                val textDiscount = discount.roundToInt().toString()
                val textPrice = price.roundToInt().toString()
                val textPriceClub = priceClub.roundToInt().toString()
                //val textCategoryFavourite = textCategory+subcategory

                ivProductFromBasket.load(basketModel.productModel.image)

                layoutDiscountFromBasket.visibility = if (discount == 0.0) View.GONE else View.VISIBLE

                tvPriceWithClubCardFromBasket.text = textPriceClub
                tvOriginalPriceFromBasket.text = textOriginalPrice
                tvOriginalPriceFromBasket.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                tvPriceFromBasket.text = textPrice

                tvProductNameFromBasket.text = basketModel.productModel.title

                tvNumberProduct.text = basketModel.numberProducts.toString()

                checkBoxBasketProduct.isChecked = isSelect

                var numberProduct = tvNumberProduct.text.toString().toInt()
                bRemoveNumberProduct.isEnabled = numberProduct > 1
                bAddNumberProduct.isEnabled = numberProduct < MAX_NUMBER_PRODUCT_IN_BASKET

                checkBoxBasketProduct.setOnClickListener {
                    //isSelect = !isSelect
                    isSelect = checkBoxBasketProduct.isChecked

                    updateMutableListSelectedBasket(basketModel = basketModel, isSelect = isSelect)

                    val newSelectedBasketModel = SelectedBasketModel(
                        isSelect = checkBoxBasketProduct.isChecked,
                        basketModel = basketModel
                    )

                    onClickCheckBox(newSelectedBasketModel)
                }


                bAddNumberProduct.setOnClickListener {
                    val newNumberProduct = ++numberProduct

                    val newBasketModel = basketModel.copy(numberProducts = newNumberProduct)

                    updateMutableListSelectedBasket(basketModel = newBasketModel, isSelect = isSelect)

                    onUpdateNumberProducts(
                        SelectedBasketModel(
                            isSelect = checkBoxBasketProduct.isChecked,
                            basketModel = newBasketModel
                        )
                    )

                    notifyItemChanged(position)
                }

                bRemoveNumberProduct.setOnClickListener {
                    val newNumberProduct = --numberProduct

                    val newBasketModel = basketModel.copy(numberProducts = newNumberProduct)

                    updateMutableListSelectedBasket(basketModel = newBasketModel, isSelect = isSelect)

                    onUpdateNumberProducts(
                        SelectedBasketModel(
                            isSelect = checkBoxBasketProduct.isChecked,
                            basketModel = newBasketModel
                        )
                    )

                    notifyItemChanged(position)
                }
            }
        }

    }


    private fun updateMutableListSelectedBasket(basketModel: BasketModel,isSelect:Boolean){

        val productId = basketModel.productModel.productId

        val oldSelectedBasket = mutableListSelectedBasket.find { it.basketModel.productModel.productId == productId }

        if (oldSelectedBasket != null) {
            val index = mutableListSelectedBasket.indexOf(oldSelectedBasket)

            mutableListSelectedBasket.removeAt(index)
            mutableListSelectedBasket.add(index, SelectedBasketModel(
                isSelect = isSelect,
                basketModel = basketModel
            ))
        }

    }

    fun selectAll(isSelect: Boolean){
        mutableListSelectedBasket = mutableListSelectedBasket.map { it.copy(isSelect = isSelect) }.toMutableList()

        notifyItemRangeChanged(0,mutableListSelectedBasket.size)
    }

}