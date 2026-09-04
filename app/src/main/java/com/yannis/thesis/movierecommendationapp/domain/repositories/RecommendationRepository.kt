package com.yannis.thesis.movierecommendationapp.domain.repositories

import com.yannis.thesis.movierecommendationapp.data.local.MovieRecommendedForUser

interface RecommendationRepository {
    fun findAllForUserByRating(userId: String?): List<MovieRecommendedForUser>
    fun findForMovie(userId: String?, movieId: String?): List<MovieRecommendedForUser>
    fun insert(recommendation: MovieRecommendedForUser)
    fun delete(recommendations: List<MovieRecommendedForUser>)
}
