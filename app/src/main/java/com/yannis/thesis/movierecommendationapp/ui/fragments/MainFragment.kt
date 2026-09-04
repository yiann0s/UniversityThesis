package com.yannis.thesis.movierecommendationapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.yannis.thesis.movierecommendationapp.MovieRecommendationApp
import com.yannis.thesis.movierecommendationapp.R
import com.yannis.thesis.movierecommendationapp.databinding.MainActivityBinding
import com.yannis.thesis.movierecommendationapp.ui.activities.MainActivity
import com.yannis.thesis.movierecommendationapp.ui.adapters.MainPagerAdapter
import com.yannis.thesis.movierecommendationapp.ui.adapters.MovieAdapter
import com.yannis.thesis.movierecommendationapp.ui.adapters.MoviesRecommendedAdapter
import com.yannis.thesis.movierecommendationapp.ui.adapters.UserRatesMovieAdapter
import com.yannis.thesis.movierecommendationapp.ui.viewmodels.MainViewModel
import com.yannis.thesis.movierecommendationapp.ui.viewmodels.MainViewModelFactory
import kotlinx.coroutines.launch

class MainFragment : Fragment() {
    private lateinit var pagerAdapter: MainPagerAdapter

    private val viewModel by lazy {
        val app = MovieRecommendationApp.getInstance()
        ViewModelProvider(
            this,
            MainViewModelFactory(
                app.loggedInUserId,
                app.recommendationRepository,
                app.ratingRepository,
                app.movieRepository
            )
        )[MainViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.main_activity, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = MainActivityBinding.bind(view)
        val mainActivity = activity as? MainActivity ?: return
        pagerAdapter = MainPagerAdapter(
            context = requireContext(),
            recommendations = viewModel.uiState.value.recommendations,
            recentlyRated = viewModel.uiState.value.recentlyRated,
            searchResults = viewModel.uiState.value.searchResults,
            onMovieClick = { movie ->
                mainActivity.openMovieDetail(movie, MovieAdapter::class.java.name)
            },
            onRatedMovieClick = { movie ->
                mainActivity.openMovieDetail(movie, UserRatesMovieAdapter::class.java.name)
            },
            onRecommendedMovieClick = { movie ->
                mainActivity.openRecommendedMovieDetail(
                    movie,
                    MoviesRecommendedAdapter::class.java.name
                )
            },
            onSearch = viewModel::search
        )
        binding.mainViewpager.adapter = pagerAdapter
        binding.maintabs.setupWithViewPager(binding.mainViewpager)
        viewModel.loadHome()
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    pagerAdapter.updateRecommendations(state.recommendations)
                    pagerAdapter.updateRecentlyRated(state.recentlyRated)
                    pagerAdapter.updateSearchResults(state.searchResults)
                    state.errorMessage?.let {
                        (activity as? MainActivity)?.showErrorDialog(it)
                        viewModel.clearError()
                    }
                }
            }
        }
    }
}
