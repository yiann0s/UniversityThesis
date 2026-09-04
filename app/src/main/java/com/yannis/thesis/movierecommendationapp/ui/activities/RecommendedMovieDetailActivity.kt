package com.yannis.thesis.movierecommendationapp.ui.activities

import android.os.Bundle
import android.util.Log
import android.widget.RatingBar
import androidx.appcompat.app.AppCompatActivity
import com.squareup.picasso.Picasso
import com.yannis.thesis.movierecommendationapp.MovieRecommendationApp
import com.yannis.thesis.movierecommendationapp.R
import com.yannis.thesis.movierecommendationapp.databinding.RecommendedMovieDetailActivityBinding
import com.yannis.thesis.movierecommendationapp.data.local.MovieRecommendedForUser
import com.yannis.thesis.movierecommendationapp.data.local.UserRatesMovie
import java.util.Date
import kotlin.math.roundToInt

class RecommendedMovieDetailActivity : AppCompatActivity(), RatingBar.OnRatingBarChangeListener {
    private lateinit var binding: RecommendedMovieDetailActivityBinding
    private var movieID: String? = null
    private var posterPathStr: String? = null
    private var currentUserId: String? = null
    private val ratingRepository
        get() = MovieRecommendationApp.getInstance().ratingRepository
    private val recommendationRepository
        get() = MovieRecommendationApp.getInstance().recommendationRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = RecommendedMovieDetailActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        currentUserId = MovieRecommendationApp.getInstance().loggedInUserId
        getIncomingIntent()
        binding.ratingBar1.setOnRatingBarChangeListener(this)
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
        ratingRepository.insert(
            UserRatesMovie(userid, movieid, rating.roundToInt(), Date(), poster, title, description, release)
        )
    }

    override fun onRatingChanged(ratingBar: RatingBar, rating: Float, fromUser: Boolean) {
        val roundedRating = ratingBar.rating.roundToInt().toFloat()
        binding.ratingBar1.setRating(roundedRating)
        val activeUserId = MovieRecommendationApp.getInstance().loggedInUserId
        deleteRecommendedMovie(movieID, activeUserId)
        rateMovie(
            activeUserId,
            movieID,
            roundedRating,
            binding.movieTitle.text.toString(),
            binding.movieReleaseDate.text.toString(),
            binding.movieDescription.text.toString(),
            posterPathStr
        )
        binding.ratingBar1.setIsIndicator(true)
    }

    fun deleteRecommendedMovie(movieId: String?, activeUserId: String?) {
        val result = recommendationRepository.findForMovie(activeUserId, movieId)
        Log.d("MovieApp", "delete recommened movie results before delete${result.size}")
        recommendationRepository.delete(result)
        Log.d("MovieApp", "delete recommened movie results after delete${result.size}")
    }
}
