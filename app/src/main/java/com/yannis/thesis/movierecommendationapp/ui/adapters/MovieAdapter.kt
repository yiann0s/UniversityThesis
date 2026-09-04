package com.yannis.thesis.movierecommendationapp.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.yannis.thesis.movierecommendationapp.R
import com.yannis.thesis.movierecommendationapp.databinding.MovieListRowBinding
import com.yannis.thesis.movierecommendationapp.data.remote.Movie

class MovieAdapter(
    private var movies: List<Movie>,
    private val onMovieClick: (Movie) -> Unit
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
            onMovieClick(movie)
        }
    }

    override fun getItemCount(): Int = movies.size

    fun submitList(movies: List<Movie>) {
        this.movies = movies
        notifyDataSetChanged()
    }
}
