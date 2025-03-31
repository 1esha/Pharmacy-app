package com.example.domain.favorite.usecases

import com.example.domain.Result
import com.example.domain.favorite.FavoriteRepository
import com.example.domain.favorite.models.FavoriteModel
import kotlinx.coroutines.flow.Flow

/**
 * Класс [AddFavoriteUseCase] является UseCase для добавления товара в список избранного.
 *
 * Параметры:
 * [favoriteRepository] - репозиторий с функционалом;
 * [favoriteModel] - данные о товаре, которые будут добавлены.
 */
class AddFavoriteUseCase(
    private val favoriteRepository: FavoriteRepository,
    private val favoriteModel: FavoriteModel
) {

    fun execute(): Flow<Result> {
        val result = favoriteRepository.insertFavoriteFlow(favoriteModel = favoriteModel)

        return result
    }

}