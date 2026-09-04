package com.yannis.thesis.movierecommendationapp.api;

import com.yannis.thesis.movierecommendationapp.models.DirectorResponse;
import com.yannis.thesis.movierecommendationapp.models.GenreResponse;
import com.yannis.thesis.movierecommendationapp.models.Movie;
import com.yannis.thesis.movierecommendationapp.models.MovieResponse;
import com.yannis.thesis.movierecommendationapp.models.PrimaryMovieInfo;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by yiannos on 20-Feb-18.
 */

public interface APIService {

    @POST("/list")
    Call<Movie> loadMovie();

    @GET("movie/top_rated")
    Call<MovieResponse> getTopRatedMovies(@Query("api_key") String apiKey);

    @GET("movie/popular")
    Call<MovieResponse> getPopularMovies(@Query("api_key") String apiKey);

    @GET("movie/{id}")
    Call<Movie> getMovieDetails(@Path("id")Integer id, @Query("api_key") String apiKey);

    @GET("search/movie")
    Call<MovieResponse> getMovieByTitle(@Query("query") String title, @Query("api_key") String apiKey);

    @GET("discover/movie")
    Call<MovieResponse> getMovieByReleasedYear(@Query("primary_release_year") String year, @Query("api_key") String apiKey);

    @GET("search/person")
    Call<DirectorResponse> getPersonIdByName(@Query("query") String name, @Query("api_key") String apiKey);

    @GET("discover/movie")
    Call<MovieResponse> getMovieByDirector(@Query("with_crew") int directorId, @Query("api_key") String apiKey);

    //https://api.themoviedb.org/3/genre/movie/list?api_key=efbdebf1b30ffab728c49495748e9dfa
    @GET("genre/movie/list")
    Call<GenreResponse> getAllGenres(@Query("api_key")String apiKey);

    @GET("discover/movie")
    Call<MovieResponse> getMovieByGenre(@Query("with_genres") int genre, @Query("api_key") String apiKey);

    //@Query("with_cast")
    @GET("discover/movie")
    Call<MovieResponse> getMovieByActor(@Query("with_cast") int castId, @Query("api_key") String apiKey);




}
