package com.yannis.thesis.movierecommendationapp.data.repositories

import com.yannis.thesis.movierecommendationapp.data.local.UserRatesMovieDao
import com.yannis.thesis.movierecommendationapp.domain.repositories.RatingRepository
import com.yannis.thesis.movierecommendationapp.data.local.UserRatesMovie

class RoomRatingRepository(private val dao: UserRatesMovieDao) : RatingRepository {
    override fun findForMovie(userId: String?, movieId: String?) = dao.findForMovie(userId, movieId)
    override fun findAllForUser(userId: String?) = dao.findAllForUser(userId)
    override fun getAll() = dao.getAll()
    override fun insert(rating: UserRatesMovie) = dao.insert(rating)
}
