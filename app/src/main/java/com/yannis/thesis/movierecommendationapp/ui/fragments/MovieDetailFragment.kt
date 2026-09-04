package com.yannis.thesis.movierecommendationapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.squareup.picasso.Picasso
import com.yannis.thesis.movierecommendationapp.MovieRecommendationApp
import com.yannis.thesis.movierecommendationapp.R
import com.yannis.thesis.movierecommendationapp.databinding.MovieDetailActivityBinding
import com.yannis.thesis.movierecommendationapp.ui.activities.BaseActivity
import com.yannis.thesis.movierecommendationapp.ui.viewmodels.MovieDetailViewModel
import com.yannis.thesis.movierecommendationapp.ui.viewmodels.MovieDetailViewModelFactory
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

class MovieDetailFragment : Fragment(), RatingBar.OnRatingBarChangeListener {
    private lateinit var binding: MovieDetailActivityBinding
    private var movieId: String? = null
    private var posterPath: String? = null
    private var title: String? = null
    private var description: String? = null
    private var releaseDate: String? = null

    private val viewModel by lazy {
        ViewModelProvider(
            this,
            MovieDetailViewModelFactory(
                MovieRecommendationApp.getInstance().ratingRepository,
                MovieRecommendationApp.getInstance().loggedInUserId,
                movieId
            )
        )[MovieDetailViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.movie_detail_activity, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = MovieDetailActivityBinding.bind(view)
        val arguments = arguments ?: Bundle()
        movieId = arguments.getString(ARG_MOVIE_ID)
        posterPath = arguments.getString(ARG_POSTER_PATH)
        title = arguments.getString(ARG_TITLE)
        description = arguments.getString(ARG_DESCRIPTION)
        releaseDate = arguments.getString(ARG_RELEASE_DATE)
        binding.movieTitle.text = title
        binding.movieReleaseDate.text = releaseDate
        binding.movieDescription.text = description
        Picasso.get().load("https://image.tmdb.org/t/p/w500$posterPath")
            .error(R.color.colorAccent).into(binding.moviePoster)
        binding.ratingBar1.setOnRatingBarChangeListener(this)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    state.existingRating?.let {
                        binding.ratingBar1.rating = it.toFloat()
                    }
                    binding.ratingBar1.setIsIndicator(
                        state.existingRating != null || state.isSubmitting
                    )
                    state.errorMessage?.let {
                        (activity as? BaseActivity)?.showErrorDialog(it)
                        viewModel.clearError()
                    }
                }
            }
        }
    }

    override fun onRatingChanged(ratingBar: RatingBar, rating: Float, fromUser: Boolean) {
        if (!fromUser) return
        val roundedRating = rating.roundToInt().toFloat()
        binding.ratingBar1.rating = roundedRating
        viewModel.submitRating(
            roundedRating.roundToInt(),
            posterPath,
            title,
            description,
            releaseDate
        )
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
