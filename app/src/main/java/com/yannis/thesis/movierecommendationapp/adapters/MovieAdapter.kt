package com.yannis.thesis.movierecommendationapp.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.yannis.thesis.movierecommendationapp.R
import com.yannis.thesis.movierecommendationapp.activities.MovieDetailActivity
import com.yannis.thesis.movierecommendationapp.databinding.MovieListRowBinding
import com.yannis.thesis.movierecommendationapp.models.Movie

class MovieAdapter(
    private val movies: List<Movie>,
    private val context: Context
) : RecyclerView.Adapter<MovieAdapter.MovieViewHolder>() {

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
        holder.movieTitle.text = movie.title
        holder.releaseDate.text = movie.releaseDate
        holder.movieDescription.text = movie.overview
        Picasso.get().load("https://image.tmdb.org/t/p/w500${movie.posterPath}")
            .error(R.color.colorAccent).into(holder.imageView)
        holder.moviesLayout.setOnClickListener {
            Log.d("MovieApp", "CLICKED ${movie.id}")
            val intent = Intent(context, MovieDetailActivity::class.java).apply {
                putExtra("adapterName", MovieAdapter::class.java.name)
                putExtra("movie_id", movie.id?.toString())
                putExtra("movie_title", movie.title)
                putExtra("movie_release_date", movie.releaseDate)
                putExtra("movie_description", movie.overview)
                putExtra("movie_poster_path", movie.posterPath)
                if (context !is Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = movies.size
}
