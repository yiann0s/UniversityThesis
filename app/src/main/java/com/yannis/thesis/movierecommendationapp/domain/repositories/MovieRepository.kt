package com.yannis.thesis.movierecommendationapp.domain.repositories

import com.yannis.thesis.movierecommendationapp.data.remote.Movie

interface MovieRepository {
    fun getMovieDetails(
        movieId: Int,
        onSuccess: (Movie?) -> Unit,
        onFailure: (Throwable) -> Unit
    )

    suspend fun search(category: String, query: String): List<Movie> = emptyList()
}
