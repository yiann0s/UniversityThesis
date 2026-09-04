package com.yannis.thesis.movierecommendationapp.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.DefaultItemAnimator;

import com.google.android.material.tabs.TabLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import android.util.Log;
import com.squareup.picasso.Picasso;
import com.yannis.thesis.movierecommendationapp.MovieRecommendationApp;
import com.yannis.thesis.movierecommendationapp.R;
import com.yannis.thesis.movierecommendationapp.activities.RecommendedMovieDetailActivity;
import com.yannis.thesis.movierecommendationapp.databinding.MovieListRowBinding;
import com.yannis.thesis.movierecommendationapp.models.MovieRecommendedForUser;
import com.yannis.thesis.movierecommendationapp.models.UserRatesMovie;

import java.util.List;


class MoviesRecommendedAdapter extends RecyclerView.Adapter<MoviesRecommendedAdapter.MovieViewHolder> {
    private Context mContext;
    private List<MovieRecommendedForUser> moviesRecommendedForUser;
    public MoviesRecommendedAdapter(List<MovieRecommendedForUser> moviesRecommendedForUser, Context context) {
        this.moviesRecommendedForUser = moviesRecommendedForUser;
        this.mContext = context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MoviesRecommendedAdapter.MovieViewHolder onCreateViewHolder(ViewGroup parent,
                                                                    int viewType) {
        MovieListRowBinding binding = MovieListRowBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new MovieViewHolder(binding);
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MovieViewHolder extends RecyclerView.ViewHolder { //implements View.OnClickListener {
        // each data item is just a string in this case
        LinearLayout moviesLayout;
        TextView movieTitle;
        TextView releaseDate;
        TextView movieDescription;
//        TextView rating;
        ImageView imageView;

        public MovieViewHolder(MovieListRowBinding binding) {
            super(binding.getRoot());
            moviesLayout = binding.moviesLayout;
            imageView = binding.imageView;
            movieTitle = binding.title;
            releaseDate = binding.release;
            movieDescription = binding.description;
        }

    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MovieViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final MovieRecommendedForUser movieRecommendedForUser = moviesRecommendedForUser.get(position);
        holder.movieTitle.setText(movieRecommendedForUser.getMovie_title());
        holder.releaseDate.setText(movieRecommendedForUser.getMovie_release());
        holder.movieDescription.setText(movieRecommendedForUser.getMovie_description());
//        holder.rating.setText(userRatesMovie.getRating().toString());
        // This is how we use Picasso to load images from the internet.
        Picasso.get()
                .load("https://image.tmdb.org/t/p/w500" +movieRecommendedForUser.getMovie_poster())
                .error(R.color.colorAccent)
                .into(holder.imageView);
        holder.moviesLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext,RecommendedMovieDetailActivity.class);
                intent.putExtra("adapterName",MoviesRecommendedAdapter.class.getName());
                Log.d("MovieApp","INTENT + "+ MoviesRecommendedAdapter.class.getName());
                intent.putExtra("movie_id",movieRecommendedForUser.getMovieId().toString());
                Log.d("MovieApp","INTENT + "+ movieRecommendedForUser.getMovieId().toString());
                intent.putExtra("movie_title",movieRecommendedForUser.getMovie_title());
                Log.d("MovieApp","INTENT + " +movieRecommendedForUser.getMovie_title().toString());
                intent.putExtra("movie_release_date",movieRecommendedForUser.getMovie_release());
                Log.d("MovieApp","INTENT + " +movieRecommendedForUser.getMovie_release().toString());
                intent.putExtra("movie_description",movieRecommendedForUser.getMovie_description());
                Log.d("MovieApp","INTENT + " +movieRecommendedForUser.getMovie_description().toString());
                intent.putExtra("movie_poster_path",movieRecommendedForUser.getMovie_poster());
                Log.d("MovieApp","INTENT + " +movieRecommendedForUser.getMovie_poster().toString());

                if (!(mContext instanceof Activity)) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }
                mContext.startActivity(intent);
            }
        });

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return moviesRecommendedForUser.size();
    }
}
