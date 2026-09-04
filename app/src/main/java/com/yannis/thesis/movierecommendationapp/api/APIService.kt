package com.yannis.thesis.movierecommendationapp.api

import com.yannis.thesis.movierecommendationapp.models.DirectorResponse
import com.yannis.thesis.movierecommendationapp.models.GenreResponse
import com.yannis.thesis.movierecommendationapp.models.Movie
import com.yannis.thesis.movierecommendationapp.models.MovieResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface APIService {
    @POST("/list")
    fun loadMovie(): Call<Movie>

    @GET("movie/top_rated")
    fun getTopRatedMovies(@Query("api_key") apiKey: String): Call<MovieResponse>

    @GET("movie/popular")
    fun getPopularMovies(@Query("api_key") apiKey: String): Call<MovieResponse>

    @GET("movie/{id}")
    fun getMovieDetails(@Path("id") id: Int, @Query("api_key") apiKey: String): Call<Movie?>

    @GET("search/movie")
    fun getMovieByTitle(@Query("query") title: String, @Query("api_key") apiKey: String): Call<MovieResponse>

    @GET("discover/movie")
    fun getMovieByReleasedYear(
        @Query("primary_release_year") year: String,
        @Query("api_key") apiKey: String
    ): Call<MovieResponse>

    @GET("search/person")
    fun getPersonIdByName(@Query("query") name: String, @Query("api_key") apiKey: String): Call<DirectorResponse>

    @GET("discover/movie")
    fun getMovieByDirector(@Query("with_crew") directorId: Int, @Query("api_key") apiKey: String): Call<MovieResponse>

    @GET("genre/movie/list")
    fun getAllGenres(@Query("api_key") apiKey: String): Call<GenreResponse>

    @GET("discover/movie")
    fun getMovieByGenre(@Query("with_genres") genre: Int, @Query("api_key") apiKey: String): Call<MovieResponse>

    @GET("discover/movie")
    fun getMovieByActor(@Query("with_cast") castId: Int, @Query("api_key") apiKey: String): Call<MovieResponse>
}
