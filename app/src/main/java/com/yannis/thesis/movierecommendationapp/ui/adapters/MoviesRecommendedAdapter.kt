package com.yannis.thesis.movierecommendationapp.ui.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.yannis.thesis.movierecommendationapp.R
import com.yannis.thesis.movierecommendationapp.ui.activities.RecommendedMovieDetailActivity
import com.yannis.thesis.movierecommendationapp.databinding.MovieListRowBinding
import com.yannis.thesis.movierecommendationapp.data.local.MovieRecommendedForUser

class MoviesRecommendedAdapter(
    private val movies: List<MovieRecommendedForUser>,
    private val context: Context
) : RecyclerView.Adapter<MoviesRecommendedAdapter.MovieViewHolder>() {

    class MovieViewHolder(binding: MovieListRowBinding) : RecyclerView.ViewHolder(binding.root) {
        val moviesLayout = binding.moviesLayout
        val imageView = binding.imageView
        val movieTitle = binding.title
        val releaseDate = binding.release
        val movieDescription = binding.description
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder =
        MovieViewHolder(MovieListRowBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        val movie = movies[position]
        holder.movieTitle.text = movie.movie_title
        holder.releaseDate.text = movie.movie_release
        holder.movieDescription.text = movie.movie_description
        Picasso.get().load("https://image.tmdb.org/t/p/w500${movie.movie_poster}")
            .error(R.color.colorAccent).into(holder.imageView)
        holder.moviesLayout.setOnClickListener {
            val intent = Intent(context, RecommendedMovieDetailActivity::class.java).apply {
                putExtra("adapterName", MoviesRecommendedAdapter::class.java.name)
                putExtra("movie_id", movie.movieId)
                putExtra("movie_title", movie.movie_title)
                putExtra("movie_release_date", movie.movie_release)
                putExtra("movie_description", movie.movie_description)
                putExtra("movie_poster_path", movie.movie_poster)
                if (context !is Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            Log.d("MovieApp", "INTENT + ${movie.movieId}")
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = movies.size
}
