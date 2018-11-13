package com.yannis.thesis.movierecommendationapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.luseen.logger.Logger;
import com.squareup.picasso.Picasso;
import com.yannis.thesis.movierecommendationapp.activities.MovieDetailActivity;
import com.yannis.thesis.movierecommendationapp.models.Movie;
import com.yannis.thesis.movierecommendationapp.R;

import java.util.List;

/**
 * Created by yiannos on 10-Feb-18.
 */

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {

    private Context mContext;
    private List<Movie> movies;
    private int rowLayout;



    public MovieAdapter(List<Movie> movies, int rowLayout, Context context) {
        this.movies = movies;
        this.mContext = context;
        this.rowLayout = rowLayout;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MovieAdapter.MovieViewHolder onCreateViewHolder(ViewGroup parent,
                                                           int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(rowLayout,parent,false);
        Logger.w("Inside here");
        return new MovieViewHolder(view);
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
        TextView rating;
        ImageView imageView;

        public MovieViewHolder(View v) {
            super(v);
            moviesLayout = (LinearLayout) v.findViewById(R.id.movies_layout);
            imageView = (ImageView) itemView.findViewById(R.id.imageView);

            movieTitle = (TextView)v.findViewById(R.id.title);
            releaseDate = (TextView) v.findViewById(R.id.release);
            movieDescription = (TextView)v.findViewById(R.id.description);
//            rating = (TextView) v.findViewById(R.id.rating);
        }

    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MovieViewHolder holder,final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
//        holder.mTextView.setText(movieList.get(position).getName());
        final Movie movie = movies.get(position);
        holder.movieTitle.setText(movie.getTitle());
        holder.releaseDate.setText(movie.getReleaseDate());
        holder.movieDescription.setText(movie.getOverview());
//        holder.rating.setText(movie.getVoteAverage().toString());
        // This is how we use Picasso to load images from the internet.
        Picasso.get()
                .load("http://image.tmdb.org/t/p/w500" +movie.getPosterPath())
                .error(R.color.colorAccent)
                .into(holder.imageView);
        holder.moviesLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Logger.d("CLICKED " + movie.getId().toString());

                Intent intent = new Intent(mContext,MovieDetailActivity.class);
                intent.putExtra("adapterName",MovieAdapter.class.getName());
                intent.putExtra("movie_id",movie.getId().toString());
                intent.putExtra("movie_title",movie.getTitle());
                intent.putExtra("movie_release_date",movie.getReleaseDate());
                intent.putExtra("movie_description",movie.getOverview());
                intent.putExtra("movie_poster_path",movie.getPosterPath());
                Logger.d("INTENT + "+ movie.getId().toString());
                Logger.d("INTENT + " +movie.getTitle().toString());
                Logger.d("INTENT + " +movie.getReleaseDate().toString());
                Logger.d("INTENT + " +movie.getOverview().toString());
                Logger.d("INTENT + " +movie.getPosterPath().toString());

                mContext.startActivity(intent);
            }
        });

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return movies.size();
    }
}
