package com.example.pharmacyapp.tabs.catalog.adapters

import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import coil.load
import com.example.domain.favorite.models.FavoriteModel
import com.example.domain.catalog.models.ProductFavoriteModel
import com.example.domain.models.ButtonModel
import com.example.pharmacyapp.CLUB_DISCOUNT
import com.example.pharmacyapp.R
import com.example.pharmacyapp.UNAUTHORIZED_USER
import com.example.pharmacyapp.databinding.ItemProductsBinding
import kotlin.math.roundToInt

/**
 * Класс [ProductsAdapter] является адаптером для списка товаров во фрагиенте ProductsFragment.
 *
 * Парметры:
 * [userId] - идентификатор пользователя;
 * listProducts - список товаров;
 * [onClickProduct] - обработка нажатия на товар;
 * [onClickFavorite] - обработка нажатия "В Избранное";
 * [onClickInBasket] - обработка нажатия "В корзину";
 * [buttonModel] - модель кнопки для отрисовки.
 */
class ProductsAdapter(
    private val userId: Int,
    listProducts: List<*>,
    private val onClickProduct: (Int,Boolean) -> Unit,
    private val onClickFavorite: (FavoriteModel, Boolean) -> Unit,
    private val onClickInBasket: (Int,Boolean) -> Unit,
    private val buttonModel: ButtonModel
) : Adapter<ProductsAdapter.ProductsHolder>() {

    /**
     * Изменяемый список товаров, который будет отрисовываться на экране.
     */
    private val mutableListProductFavorite = mutableListOf<ProductFavoriteModel>()

    /**
     * Заполнение mutableListProductFavorite при инициализации класса.
     */
    init {
        listProducts.forEach {
            val productFavoriteModel = it as ProductFavoriteModel

            mutableListProductFavorite.add(
                if (userId == UNAUTHORIZED_USER) {
                    ProductFavoriteModel(
                        isFavorite = false,
                        productModel = productFavoriteModel.productModel,
                        isInBasket = false
                    )
                }
                else {
                    productFavoriteModel
                }
            )
        }
    }

    class ProductsHolder(val binding: ItemProductsBinding) : ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductsHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemProductsBinding.inflate(inflater, parent, false)

        return ProductsHolder(binding = binding)
    }

    override fun getItemCount(): Int = mutableListProductFavorite.size

    override fun onBindViewHolder(holder: ProductsHolder, position: Int): Unit =
        with(holder.binding) {
            val currentProduct = mutableListProductFavorite[position]
            val product = currentProduct.productModel
            var isFavorite = currentProduct.isFavorite
            var isInBasket = currentProduct.isInBasket

            val originalPrice = product.price
            val discount = product.discount
            val sumDiscount = ((discount / 100) * originalPrice)
            val price = (originalPrice - sumDiscount)
            val sumClubDiscount = ((CLUB_DISCOUNT / 100) * price)
            val priceClub = price - sumClubDiscount

            val colorDiscount = Color.rgb(198, 1,63)

            // Установка внешнего вида элемета списка
            ivProduct.load(product.image)
            tvProductName.text = product.title

            val textPrice = price.roundToInt().toString()
            tvPrice.text = textPrice

            val textPriceClub = priceClub.roundToInt().toString()
            tvPriceWithClubCard.text = textPriceClub

            if (product.discount > 0.0) {
                tvPrice.setTextColor(colorDiscount)
                tvRubleSign.setTextColor(colorDiscount)

                tvDiscount.visibility = View.VISIBLE
                tvOriginalPrice.visibility = View.VISIBLE

                val textDiscount = "-${discount.roundToInt()}%"
                tvDiscount.text = textDiscount

                val textOriginalPrice = originalPrice.roundToInt().toString()
                tvOriginalPrice.text = textOriginalPrice
                tvOriginalPrice.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG

            }
            else {
                val defaultColor  = tvProductName.textColors.defaultColor
                tvPrice.setTextColor(defaultColor)
                tvRubleSign.setTextColor(defaultColor)

                tvDiscount.visibility = View.GONE
                tvOriginalPrice.visibility = View.GONE

            }

            ivFavorite.setImageResource(if (isFavorite) R.drawable.ic_favorite else R.drawable.ic_favorite_border)

            // Установка внешнего вида кнопки "В корзину" в зависимости от значения isInBasket
            if (isInBasket) {
                bInBasketProduct.text = buttonModel.textSecondary
                bInBasketProduct.setBackgroundColor(buttonModel.colorSecondaryContainer)
                bInBasketProduct.setTextColor(buttonModel.colorOnSecondaryContainer)
            }
            else {
                bInBasketProduct.text = buttonModel.textPrimary
                bInBasketProduct.setBackgroundColor(buttonModel.colorPrimary)
                bInBasketProduct.setTextColor(buttonModel.colorOnPrimary)
            }

            root.setOnClickListener {
                onClickProduct(product.productId,isFavorite)
            }

            // Обработка нажатия на "сердечко"
            ivFavorite.setOnClickListener {

                val favoriteModel = FavoriteModel(
                    productId = product.productId,
                    title = product.title,
                    productPath = product.productPath,
                    price = product.price,
                    discount = product.discount,
                    image = product.image
                )

                onClickFavorite(favoriteModel,!isFavorite)

                if (userId != UNAUTHORIZED_USER) {
                    isFavorite = !isFavorite
                    // Изменение элемента списка
                    mutableListProductFavorite.removeAt(position)
                    mutableListProductFavorite.add(position,ProductFavoriteModel(
                        isFavorite = isFavorite,
                        productModel = product,
                        isInBasket = isInBasket
                    ))

                    // Обновление элемента списка
                    notifyItemChanged(position)
                }


            }

            // Обработка нажатия на кнопку добавить/удалить в корзину.
            bInBasketProduct.setOnClickListener {
                isInBasket = !isInBasket

                onClickInBasket(product.productId,isInBasket)

                // Изменение элемента списка
                mutableListProductFavorite.removeAt(position)
                mutableListProductFavorite.add(position,ProductFavoriteModel(
                    isFavorite = isFavorite,
                    productModel = product,
                    isInBasket = isInBasket
                ))

                // Обновление элемента списка
                notifyItemChanged(position)
            }
        }
}