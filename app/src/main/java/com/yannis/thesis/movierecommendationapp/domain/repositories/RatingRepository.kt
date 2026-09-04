package com.yannis.thesis.movierecommendationapp.domain.repositories

import com.yannis.thesis.movierecommendationapp.data.local.UserRatesMovie

interface RatingRepository {
    fun findForMovie(userId: String?, movieId: String?): UserRatesMovie?
    fun findAllForUser(userId: String?): List<UserRatesMovie>
    fun getAll(): List<UserRatesMovie>
    fun insert(rating: UserRatesMovie)
}
