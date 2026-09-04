package com.yannis.thesis.movierecommendationapp.data.repositories

import com.yannis.thesis.movierecommendationapp.data.local.MovieRecommendedForUserDao
import com.yannis.thesis.movierecommendationapp.domain.repositories.RecommendationRepository
import com.yannis.thesis.movierecommendationapp.data.local.MovieRecommendedForUser

class RoomRecommendationRepository(private val dao: MovieRecommendedForUserDao) : RecommendationRepository {
    override fun findAllForUserByRating(userId: String?) = dao.findAllForUserByRating(userId)
    override fun findForMovie(userId: String?, movieId: String?) = dao.findForMovie(userId, movieId)
    override fun insert(recommendation: MovieRecommendedForUser) = dao.insert(recommendation)
    override fun delete(recommendations: List<MovieRecommendedForUser>) = dao.delete(recommendations)
}
