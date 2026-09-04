package com.yannis.thesis.movierecommendationapp.activities

import android.os.Bundle
import android.util.Log
import android.widget.RatingBar
import androidx.appcompat.app.AppCompatActivity
import com.squareup.picasso.Picasso
import com.yannis.thesis.movierecommendationapp.MovieRecommendationApp
import com.yannis.thesis.movierecommendationapp.R
import com.yannis.thesis.movierecommendationapp.data.AppDatabase
import com.yannis.thesis.movierecommendationapp.databinding.MovieDetailActivityBinding
import com.yannis.thesis.movierecommendationapp.models.UserRatesMovie
import java.util.Date
import kotlin.math.roundToInt

class MovieDetailActivity : AppCompatActivity(), RatingBar.OnRatingBarChangeListener {
    private lateinit var binding: MovieDetailActivityBinding
    private var movieID: String? = null
    private var posterPathStr: String? = null
    private var currentUserId: String? = null
    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MovieDetailActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        currentUserId = MovieRecommendationApp.getInstance().loggedInUserId
        database = AppDatabase.getInstance(this)
        getIncomingIntent()
        if (!isMovieAlreadyRatedByCurrentUser()) {
            Log.d("MovieApp", "It's not yet rated")
            binding.ratingBar1.setOnRatingBarChangeListener(this)
        } else {
            Log.d("MovieApp", "It's rated")
            displayMovieRating()
        }
    }

    private fun getIncomingIntent() {
        if (intent.hasExtra("movie_title") && intent.hasExtra("movie_release_date") &&
            intent.hasExtra("movie_description") && intent.hasExtra("movie_id") &&
            intent.hasExtra("movie_poster_path") && intent.hasExtra("adapterName")
        ) {
            Log.d("MovieApp", "intent was called from ${intent.getStringExtra("adapterName")}")
            binding.movieTitle.text = intent.getStringExtra("movie_title")
            binding.movieReleaseDate.text = intent.getStringExtra("movie_release_date")
            binding.movieDescription.text = intent.getStringExtra("movie_description")
            movieID = intent.getStringExtra("movie_id")
            posterPathStr = intent.getStringExtra("movie_poster_path")
            Picasso.get().load("https://image.tmdb.org/t/p/w500$posterPathStr")
                .error(R.color.colorAccent).into(binding.moviePoster)
        }
    }

    private fun isMovieAlreadyRatedByCurrentUser(): Boolean =
        database.userRatesMovieDao().findForMovie(currentUserId, movieID) != null

    private fun rateMovie(
        userid: String?,
        movieid: String?,
        rating: Float,
        title: String,
        release: String,
        description: String,
        poster: String?
    ) {
        Log.d("MovieApp", "rating a movie")
        database.userRatesMovieDao().insert(
            UserRatesMovie(userid, movieid, rating.roundToInt(), Date(), poster, title, description, release)
        )
    }

    override fun onRatingChanged(ratingBar: RatingBar, rating: Float, fromUser: Boolean) {
        val roundedRating = ratingBar.rating.roundToInt().toFloat()
        binding.ratingBar1.setRating(roundedRating)
        rateMovie(
            MovieRecommendationApp.getInstance().loggedInUserId,
            movieID,
            roundedRating,
            binding.movieTitle.text.toString(),
            binding.movieReleaseDate.text.toString(),
            binding.movieDescription.text.toString(),
            posterPathStr
        )
        binding.ratingBar1.setIsIndicator(true)
    }

    fun displayMovieRating() {
        val userRatesMovie = database.userRatesMovieDao().findForMovie(currentUserId, movieID)
        Log.d("MovieApp", "User has rated this movie with a ${userRatesMovie?.rating}")
        binding.ratingBar1.setRating(userRatesMovie?.rating?.toFloat() ?: 0f)
        binding.ratingBar1.setIsIndicator(true)
    }
}
