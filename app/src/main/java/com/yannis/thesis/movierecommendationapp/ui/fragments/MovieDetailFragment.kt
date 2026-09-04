package com.yannis.thesis.movierecommendationapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.LaunchedEffect
import com.yannis.thesis.movierecommendationapp.MovieRecommendationApp
import com.yannis.thesis.movierecommendationapp.ui.activities.BaseActivity
import com.yannis.thesis.movierecommendationapp.ui.components.MovieDetailScreen
import com.yannis.thesis.movierecommendationapp.ui.viewmodels.MovieDetailViewModel
import com.yannis.thesis.movierecommendationapp.ui.viewmodels.MovieDetailViewModelFactory

class MovieDetailFragment : Fragment() {
    private lateinit var composeView: ComposeView
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
    ): View = ComposeView(requireContext()).also {
        composeView = it
        it.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val arguments = arguments ?: Bundle()
        movieId = arguments.getString(ARG_MOVIE_ID)
        posterPath = arguments.getString(ARG_POSTER_PATH)
        title = arguments.getString(ARG_TITLE)
        description = arguments.getString(ARG_DESCRIPTION)
        releaseDate = arguments.getString(ARG_RELEASE_DATE)

        composeView.setContent {
            val state = viewModel.uiState.collectAsStateWithLifecycle().value
            LaunchedEffect(state.errorMessage) {
                state.errorMessage?.let { message ->
                    (activity as? BaseActivity)?.showErrorDialog(message)
                    viewModel.clearError()
                }
            }
            MovieDetailScreen(
                title = title,
                releaseDate = releaseDate,
                description = description,
                posterPath = posterPath,
                state = state,
                onRatingSelected = { rating ->
                    viewModel.submitRating(
                        rating,
                        posterPath,
                        title,
                        description,
                        releaseDate
                    )
                }
            )
        }
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
