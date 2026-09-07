package com.yannis.thesis.movierecommendationapp.data.repositories

import com.yannis.thesis.movierecommendationapp.data.remote.APIService
import com.yannis.thesis.movierecommendationapp.domain.repositories.MovieRepository
import com.yannis.thesis.movierecommendationapp.data.remote.Movie
import com.yannis.thesis.movierecommendationapp.data.remote.DirectorResponse
import com.yannis.thesis.movierecommendationapp.data.remote.GenreResponse
import com.yannis.thesis.movierecommendationapp.data.remote.MovieResponse
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine
import retrofit2.Call
import retrofit2.HttpException
import retrofit2.Response

class RetrofitMovieRepository(
    private val api: APIService,
    private val apiKey: String
) : MovieRepository {
    override suspend fun getMovieDetails(movieId: Int): Movie? =
        api.getMovieDetails(movieId, apiKey).awaitBody()

    override suspend fun search(category: String, query: String): List<Movie> {
        return when (category) {
            "Title" -> api.getMovieByTitle(query, apiKey).awaitBody()?.results.orEmpty()
            "Released" -> api.getMovieByReleasedYear(query, apiKey).awaitBody()?.results.orEmpty()
            "Director" -> {
                val person = api.getPersonIdByName(query, apiKey).awaitBody()
                val id = person?.results?.firstOrNull()?.id
                if (id == null) emptyList()
                else api.getMovieByDirector(id, apiKey).awaitBody()?.results.orEmpty()
            }
            "Genre" -> {
                val genres = api.getAllGenres(apiKey).awaitBody()
                val id = genres?.genres?.firstOrNull { it.name == query }?.id
                    ?: throw IllegalArgumentException("such genre not found :/")
                api.getMovieByGenre(id, apiKey).awaitBody()?.results.orEmpty()
            }
            "Actors" -> {
                val person = api.getPersonIdByName(query, apiKey).awaitBody()
                val id = person?.results?.firstOrNull()?.id
                if (id == null) emptyList()
                else api.getMovieByActor(id, apiKey).awaitBody()?.results.orEmpty()
            }
            else -> emptyList()
        }
    }

    private suspend fun <T> Call<T>.awaitBody(): T? =
        suspendCancellableCoroutine { continuation ->
            enqueue(object : retrofit2.Callback<T> {
                override fun onResponse(call: Call<T>, response: Response<T>) {
                    if (response.isSuccessful) {
                        continuation.resume(response.body())
                    } else {
                        continuation.resumeWithException(HttpException(response))
                    }
                }

                override fun onFailure(call: Call<T>, throwable: Throwable) {
                    continuation.resumeWithException(throwable)
                }
            })
            continuation.invokeOnCancellation { cancel() }
        }
}
