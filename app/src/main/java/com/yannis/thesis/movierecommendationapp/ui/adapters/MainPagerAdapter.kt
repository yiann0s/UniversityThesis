package com.yannis.thesis.movierecommendationapp.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager.widget.PagerAdapter
import com.yannis.thesis.movierecommendationapp.data.local.MovieRecommendedForUser
import com.yannis.thesis.movierecommendationapp.data.local.UserRatesMovie
import com.yannis.thesis.movierecommendationapp.data.remote.Movie
import com.yannis.thesis.movierecommendationapp.databinding.ViewMainTabBinding
import com.yannis.thesis.movierecommendationapp.databinding.ViewSearchTabBinding
import com.yannis.thesis.movierecommendationapp.domain.model.MainPagerEnum

class MainPagerAdapter(
    private val context: Context,
    recommendations: List<MovieRecommendedForUser>,
    recentlyRated: List<UserRatesMovie>,
    searchResults: List<Movie>,
    private val onMovieClick: (Movie) -> Unit,
    private val onRatedMovieClick: (UserRatesMovie) -> Unit,
    private val onRecommendedMovieClick: (MovieRecommendedForUser) -> Unit,
    private val onSearch: (String, String) -> Unit
) : PagerAdapter() {
    private var recommendations = recommendations
    private var recentlyRated = recentlyRated
    private var searchResults = searchResults
    private var recommendationsAdapter: MoviesRecommendedAdapter? = null
    private var recentlyRatedAdapter: UserRatesMovieAdapter? = null
    private var searchAdapter: MovieAdapter? = null

    private val categories = arrayOf("Title", "Released", "Director", "Genre", "Actors")

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val inflater = LayoutInflater.from(container.context)
        val view: View
        if (position == 0) {
            val binding = ViewMainTabBinding.inflate(inflater, container, false)
            view = binding.root
            binding.recyclerViewRec.layoutManager = LinearLayoutManager(container.context)
            recommendationsAdapter =
                MoviesRecommendedAdapter(recommendations, onRecommendedMovieClick)
            binding.recyclerViewRec.adapter = recommendationsAdapter

            binding.recyclerViewRat.layoutManager = LinearLayoutManager(container.context)
            recentlyRatedAdapter = UserRatesMovieAdapter(recentlyRated, onRatedMovieClick)
            binding.recyclerViewRat.adapter = recentlyRatedAdapter
        } else {
            val binding = ViewSearchTabBinding.inflate(inflater, container, false)
            view = binding.root
            binding.recyclerSearch.layoutManager = LinearLayoutManager(container.context)
            searchAdapter = MovieAdapter(searchResults, onMovieClick)
            binding.recyclerSearch.adapter = searchAdapter
            binding.catspinner.setAdapter(
                ArrayAdapter(
                    container.context,
                    android.R.layout.simple_dropdown_item_1line,
                    categories
                )
            )
            binding.searchBut.setOnClickListener {
                onSearch(
                    binding.catspinner.text.toString(),
                    binding.keywordTxt.text.toString()
                )
            }
        }
        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, objectValue: Any) {
        container.removeView(objectValue as View)
    }

    fun updateRecommendations(items: List<MovieRecommendedForUser>) {
        recommendations = items
        recommendationsAdapter?.submitList(items)
    }

    fun updateRecentlyRated(items: List<UserRatesMovie>) {
        recentlyRated = items
        recentlyRatedAdapter?.submitList(items)
    }

    fun updateSearchResults(items: List<Movie>) {
        searchResults = items
        searchAdapter?.submitList(items)
    }

    override fun getCount(): Int = MainPagerEnum.entries.size

    override fun isViewFromObject(view: View, objectValue: Any): Boolean = view === objectValue

    override fun getPageTitle(position: Int): CharSequence =
        context.getString(MainPagerEnum.entries[position].titleResId)
}
