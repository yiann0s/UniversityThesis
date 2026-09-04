package com.yannis.thesis.movierecommendationapp.data.repositories

import com.yannis.thesis.movierecommendationapp.data.remote.APIService
import com.yannis.thesis.movierecommendationapp.domain.repositories.MovieRepository
import com.yannis.thesis.movierecommendationapp.data.remote.Movie
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RetrofitMovieRepository(
    private val api: APIService,
    private val apiKey: String
) : MovieRepository {
    override fun getMovieDetails(
        movieId: Int,
        onSuccess: (Movie?) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        api.getMovieDetails(movieId, apiKey).enqueue(object : Callback<Movie?> {
            override fun onResponse(
                call: Call<Movie?>,
                response: Response<Movie?>
            ) = onSuccess(if (response.isSuccessful) response.body() else null)

            override fun onFailure(
                call: Call<Movie?>,
                throwable: Throwable
            ) = onFailure(throwable)
        })
    }
}
