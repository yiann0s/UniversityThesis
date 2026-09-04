package com.yannis.thesis.movierecommendationapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import com.weiwangcn.betterspinner.library.BetterSpinner
import com.yannis.thesis.movierecommendationapp.MovieRecommendationApp
import com.yannis.thesis.movierecommendationapp.api.APIService
import com.yannis.thesis.movierecommendationapp.data.AppDatabase
import com.yannis.thesis.movierecommendationapp.databinding.ViewMainTabBinding
import com.yannis.thesis.movierecommendationapp.databinding.ViewSearchTabBinding
import com.yannis.thesis.movierecommendationapp.models.DirectorResponse
import com.yannis.thesis.movierecommendationapp.models.GenreResponse
import com.yannis.thesis.movierecommendationapp.models.MainPagerEnum
import com.yannis.thesis.movierecommendationapp.models.Movie
import com.yannis.thesis.movierecommendationapp.models.MovieResponse
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainPagerAdapter : PagerAdapter() {
    private val database: AppDatabase by lazy { AppDatabase.getInstance(MovieRecommendationApp.getInstance()) }
    private lateinit var searchRecyclerView: RecyclerView
    private lateinit var keyword: android.widget.EditText
    private lateinit var spinner: BetterSpinner
    private val api: APIService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.themoviedb.org/3/")
            .client(OkHttpClient.Builder().build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(APIService::class.java)
    }

    private val categories = arrayOf("Title", "Released", "Director", "Genre", "Actors")

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val inflater = LayoutInflater.from(container.context)
        val view: View
        if (position == 0) {
            val binding = ViewMainTabBinding.inflate(inflater, container, false)
            view = binding.root
            binding.recyclerViewRec.layoutManager = LinearLayoutManager(container.context)
            val recommendations = database.movieRecommendedForUserDao()
                .findAllForUserByRating(MovieRecommendationApp.getInstance().loggedInUserId)
            binding.recyclerViewRec.adapter =
                MoviesRecommendedAdapter(recommendations, MovieRecommendationApp.getInstance())

            binding.recyclerViewRat.layoutManager = LinearLayoutManager(container.context)
            val recentlyRated = database.userRatesMovieDao()
                .findAllForUser(MovieRecommendationApp.getInstance().loggedInUserId)
            binding.recyclerViewRat.adapter =
                UserRatesMovieAdapter(recentlyRated, MovieRecommendationApp.getInstance())
        } else {
            val binding = ViewSearchTabBinding.inflate(inflater, container, false)
            view = binding.root
            searchRecyclerView = binding.recyclerSearch
            searchRecyclerView.layoutManager = LinearLayoutManager(container.context)
            keyword = binding.keywordTxt
            spinner = binding.catspinner
            spinner.setAdapter(
                ArrayAdapter(
                    container.context,
                    android.R.layout.simple_dropdown_item_1line,
                    categories
                )
            )
            binding.searchBut.setOnClickListener {
                val selection = spinner.text.toString()
                if (keyword.text.toString().isEmpty()) {
                    Toast.makeText(
                        MovieRecommendationApp.getInstance(),
                        "Must provide category",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    searchForSelectedCategory(selection)
                }
            }
        }
        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, objectValue: Any) {
        container.removeView(objectValue as View)
    }

    private fun showMovies(response: Response<MovieResponse>) {
        searchRecyclerView.adapter = MovieAdapter(response.body()?.results.orEmpty(), MovieRecommendationApp.getInstance())
    }

    private fun searchForSelectedCategory(category: String) {
        val query = keyword.text.toString()
        when (category) {
            "Title" -> api.getMovieByTitle(query, MovieRecommendationApp.apiKey)
                .enqueue(movieCallback())
            "Released" -> api.getMovieByReleasedYear(query, MovieRecommendationApp.apiKey)
                .enqueue(movieCallback())
            "Director" -> api.getPersonIdByName(query, MovieRecommendationApp.apiKey)
                .enqueue(object : Callback<DirectorResponse> {
                    override fun onResponse(call: Call<DirectorResponse>, response: Response<DirectorResponse>) {
                        val id = response.body()?.results?.firstOrNull()?.id ?: return
                        api.getMovieByDirector(id, MovieRecommendationApp.apiKey).enqueue(movieCallback())
                    }

                    override fun onFailure(call: Call<DirectorResponse>, t: Throwable) = Unit
                })
            "Genre" -> api.getAllGenres(MovieRecommendationApp.apiKey)
                .enqueue(object : Callback<GenreResponse> {
                    override fun onResponse(call: Call<GenreResponse>, response: Response<GenreResponse>) {
                        val genreId = response.body()?.genres?.firstOrNull { it.name == query }?.id
                        if (genreId == null) {
                            Toast.makeText(
                                MovieRecommendationApp.getInstance(),
                                "such genre not found :/",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            api.getMovieByGenre(genreId, MovieRecommendationApp.apiKey).enqueue(movieCallback())
                        }
                    }

                    override fun onFailure(call: Call<GenreResponse>, t: Throwable) = Unit
                })
            "Actors" -> api.getPersonIdByName(query, MovieRecommendationApp.apiKey)
                .enqueue(object : Callback<DirectorResponse> {
                    override fun onResponse(call: Call<DirectorResponse>, response: Response<DirectorResponse>) {
                        val id = response.body()?.results?.firstOrNull()?.id ?: return
                        api.getMovieByActor(id, MovieRecommendationApp.apiKey).enqueue(movieCallback())
                    }

                    override fun onFailure(call: Call<DirectorResponse>, t: Throwable) = Unit
                })
            else -> Toast.makeText(
                MovieRecommendationApp.getInstance(),
                "Must provide keywords",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun movieCallback() = object : Callback<MovieResponse> {
        override fun onResponse(call: Call<MovieResponse>, response: Response<MovieResponse>) {
            showMovies(response)
        }

        override fun onFailure(call: Call<MovieResponse>, t: Throwable) = Unit
    }

    override fun getCount(): Int = MainPagerEnum.values().size

    override fun isViewFromObject(view: View, objectValue: Any): Boolean = view === objectValue

    override fun getPageTitle(position: Int): CharSequence {
        return MovieRecommendationApp.getInstance()
            .getString(MainPagerEnum.values()[position].titleResId)
    }
}
