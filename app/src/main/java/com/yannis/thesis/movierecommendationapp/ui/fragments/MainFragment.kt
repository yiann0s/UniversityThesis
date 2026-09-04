package com.yannis.thesis.movierecommendationapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.yannis.thesis.movierecommendationapp.R
import com.yannis.thesis.movierecommendationapp.databinding.MainActivityBinding
import com.yannis.thesis.movierecommendationapp.ui.activities.MainActivity
import com.yannis.thesis.movierecommendationapp.ui.adapters.MainPagerAdapter
import com.yannis.thesis.movierecommendationapp.ui.adapters.MovieAdapter
import com.yannis.thesis.movierecommendationapp.ui.adapters.MoviesRecommendedAdapter
import com.yannis.thesis.movierecommendationapp.ui.adapters.UserRatesMovieAdapter

class MainFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.main_activity, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = MainActivityBinding.bind(view)
        val mainActivity = activity as? MainActivity ?: return
        binding.mainViewpager.adapter = MainPagerAdapter(
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
            }
        )
        binding.maintabs.setupWithViewPager(binding.mainViewpager)
    }
}
