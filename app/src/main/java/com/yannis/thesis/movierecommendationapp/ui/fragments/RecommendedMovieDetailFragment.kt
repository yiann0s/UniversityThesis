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
import com.yannis.thesis.movierecommendationapp.databinding.RecommendedMovieDetailActivityBinding
import java.util.Date
import kotlin.math.roundToInt

class RecommendedMovieDetailFragment : Fragment(),
    RatingBar.OnRatingBarChangeListener {
    private lateinit var binding: RecommendedMovieDetailActivityBinding
    private var movieId: String? = null
    private var posterPath: String? = null
    private val ratingRepository
        get() = MovieRecommendationApp.getInstance().ratingRepository
    private val recommendationRepository
        get() = MovieRecommendationApp.getInstance().recommendationRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.recommended_movie_detail_activity, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = RecommendedMovieDetailActivityBinding.bind(view)
        val arguments = arguments ?: Bundle()
        movieId = arguments.getString(ARG_MOVIE_ID)
        posterPath = arguments.getString(ARG_POSTER_PATH)
        binding.movieTitle.text = arguments.getString(ARG_TITLE)
        binding.movieReleaseDate.text = arguments.getString(ARG_RELEASE_DATE)
        binding.movieDescription.text = arguments.getString(ARG_DESCRIPTION)
        Log.d("MovieApp", "fragment was called from ${arguments.getString(ARG_SOURCE)}")
        Picasso.get().load("https://image.tmdb.org/t/p/w500$posterPath")
            .error(R.color.colorAccent).into(binding.moviePoster)
        binding.ratingBar1.setOnRatingBarChangeListener(this)
    }

    private fun rateMovie(rating: Float) {
        ratingRepository.insert(
            UserRatesMovie(
                MovieRecommendationApp.getInstance().loggedInUserId,
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
        val activeUserId = MovieRecommendationApp.getInstance().loggedInUserId
        deleteRecommendedMovie(activeUserId)
        rateMovie(roundedRating)
        binding.ratingBar1.setIsIndicator(true)
    }

    private fun deleteRecommendedMovie(activeUserId: String?) {
        val result = recommendationRepository.findForMovie(activeUserId, movieId)
        Log.d("MovieApp", "delete recommened movie results before delete${result.size}")
        recommendationRepository.delete(result)
        Log.d("MovieApp", "delete recommened movie results after delete${result.size}")
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
        ) = RecommendedMovieDetailFragment().apply {
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
