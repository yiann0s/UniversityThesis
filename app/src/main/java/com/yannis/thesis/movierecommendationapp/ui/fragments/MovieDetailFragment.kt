package com.yannis.thesis.movierecommendationapp.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import androidx.fragment.app.Fragment
import com.squareup.picasso.Picasso
import com.yannis.thesis.movierecommendationapp.MovieRecommendationApp
import com.yannis.thesis.movierecommendationapp.R
import com.yannis.thesis.movierecommendationapp.data.local.UserRatesMovie
import com.yannis.thesis.movierecommendationapp.databinding.MovieDetailActivityBinding
import java.util.Date
import kotlin.math.roundToInt

class MovieDetailFragment : Fragment(),
    RatingBar.OnRatingBarChangeListener {
    private lateinit var binding: MovieDetailActivityBinding
    private var movieId: String? = null
    private var posterPath: String? = null
    private var currentUserId: String? = null
    private val ratingRepository
        get() = MovieRecommendationApp.getInstance().ratingRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.movie_detail_activity, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = MovieDetailActivityBinding.bind(view)
        currentUserId = MovieRecommendationApp.getInstance().loggedInUserId
        val arguments = arguments ?: Bundle()
        movieId = arguments.getString(ARG_MOVIE_ID)
        posterPath = arguments.getString(ARG_POSTER_PATH)
        binding.movieTitle.text = arguments.getString(ARG_TITLE)
        binding.movieReleaseDate.text = arguments.getString(ARG_RELEASE_DATE)
        binding.movieDescription.text = arguments.getString(ARG_DESCRIPTION)
        Log.d("MovieApp", "fragment was called from ${arguments.getString(ARG_SOURCE)}")
        Picasso.get().load("https://image.tmdb.org/t/p/w500$posterPath")
            .error(R.color.colorAccent).into(binding.moviePoster)

        if (!isMovieAlreadyRatedByCurrentUser()) {
            binding.ratingBar1.setOnRatingBarChangeListener(this)
        } else {
            displayMovieRating()
        }
    }

    private fun isMovieAlreadyRatedByCurrentUser(): Boolean =
        ratingRepository.findForMovie(currentUserId, movieId) != null

    private fun rateMovie(rating: Float) {
        ratingRepository.insert(
            UserRatesMovie(
                currentUserId,
                movieId,
                rating.roundToInt(),
                Date(),
                posterPath,
                binding.movieTitle.text.toString(),
                binding.movieDescription.text.toString(),
                binding.movieReleaseDate.text.toString()
            )
        )
    }

    override fun onRatingChanged(ratingBar: RatingBar, rating: Float, fromUser: Boolean) {
        val roundedRating = ratingBar.rating.roundToInt().toFloat()
        binding.ratingBar1.setRating(roundedRating)
        rateMovie(roundedRating)
        binding.ratingBar1.setIsIndicator(true)
    }

    private fun displayMovieRating() {
        val userRatesMovie = ratingRepository.findForMovie(currentUserId, movieId)
        Log.d("MovieApp", "User has rated this movie with a ${userRatesMovie?.rating}")
        binding.ratingBar1.setRating(userRatesMovie?.rating?.toFloat() ?: 0f)
        binding.ratingBar1.setIsIndicator(true)
    }

    companion object {
        private const val ARG_MOVIE_ID = "movie_id"
        private const val ARG_TITLE = "movie_title"
        private const val ARG_RELEASE_DATE = "movie_release_date"
        private const val ARG_DESCRIPTION = "movie_description"
        private const val ARG_POSTER_PATH = "movie_poster_path"
        private const val ARG_SOURCE = "adapterName"

        fun newInstance(
            movieId: String?,
            title: String?,
            releaseDate: String?,
            description: String?,
            posterPath: String?,
            source: String
        ) = MovieDetailFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_MOVIE_ID, movieId)
                putString(ARG_TITLE, title)
                putString(ARG_RELEASE_DATE, releaseDate)
                putString(ARG_DESCRIPTION, description)
                putString(ARG_POSTER_PATH, posterPath)
                putString(ARG_SOURCE, source)
            }
        }
    }
}
