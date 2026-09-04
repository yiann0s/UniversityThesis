package com.yannis.thesis.movierecommendationapp.ui.activities

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.yannis.thesis.movierecommendationapp.R
import com.yannis.thesis.movierecommendationapp.data.local.MovieRecommendedForUser
import com.yannis.thesis.movierecommendationapp.data.local.UserRatesMovie
import com.yannis.thesis.movierecommendationapp.data.remote.Movie
import com.yannis.thesis.movierecommendationapp.ui.fragments.LoginSignupFragment
import com.yannis.thesis.movierecommendationapp.ui.fragments.MainFragment
import com.yannis.thesis.movierecommendationapp.ui.fragments.MovieDetailFragment
import com.yannis.thesis.movierecommendationapp.ui.fragments.RecommendedMovieDetailFragment

class MainActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_host)
        if (savedInstanceState == null) {
            showFragment(LoginSignupFragment())
        }
    }

    fun showMainFragment() {
        showFragment(MainFragment())
    }

    fun openMovieDetail(movie: Movie, source: String) {
        showFragment(
            MovieDetailFragment.newInstance(
                movieId = movie.id?.toString(),
                title = movie.title,
                releaseDate = movie.releaseDate,
                description = movie.overview,
                posterPath = movie.posterPath,
                source = source
            ),
            addToBackStack = true
        )
    }

    fun openMovieDetail(movie: UserRatesMovie, source: String) {
        showFragment(
            MovieDetailFragment.newInstance(
                movieId = movie.movieId,
                title = movie.movie_title,
                releaseDate = movie.movie_release,
                description = movie.movie_description,
                posterPath = movie.movie_poster,
                source = source
            ),
            addToBackStack = true
        )
    }

    fun openRecommendedMovieDetail(movie: MovieRecommendedForUser, source: String) {
        showFragment(
            RecommendedMovieDetailFragment.newInstance(
                movieId = movie.movieId,
                title = movie.movie_title,
                releaseDate = movie.movie_release,
                description = movie.movie_description,
                posterPath = movie.movie_poster,
                source = source
            ),
            addToBackStack = true
        )
    }

    private fun showFragment(fragment: Fragment, addToBackStack: Boolean = false) {
        val transaction = supportFragmentManager.beginTransaction()
            .replace(R.id.main_fragment_container, fragment)
        if (addToBackStack) {
            transaction.addToBackStack(null)
        }
        transaction.commit()
    }
}
