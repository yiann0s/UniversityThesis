package com.yannis.thesis.movierecommendationapp.domain.repositories

import com.yannis.thesis.movierecommendationapp.data.remote.Movie

interface MovieRepository {
    suspend fun getMovieDetails(movieId: Int): Movie?

    suspend fun search(category: String, query: String): List<Movie> = emptyList()
}
