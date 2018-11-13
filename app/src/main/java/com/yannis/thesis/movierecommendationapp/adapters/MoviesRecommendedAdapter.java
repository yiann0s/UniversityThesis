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
import com.yannis.thesis.movierecommendationapp.MovieRecommendationApp;
import com.yannis.thesis.movierecommendationapp.R;
import com.yannis.thesis.movierecommendationapp.activities.RecommendedMovieDetailActivity;
import com.yannis.thesis.movierecommendationapp.models.MovieRecommendedForUser;
import com.yannis.thesis.movierecommendationapp.models.UserRatesMovie;

import java.util.List;

import io.realm.RealmResults;

class MoviesRecommendedAdapter extends RecyclerView.Adapter<MoviesRecommendedAdapter.MovieViewHolder> {
    private Context mContext;
    private List<MovieRecommendedForUser> moviesRecommendedForUser;
    private int rowLayout;



    public MoviesRecommendedAdapter(List<MovieRecommendedForUser> moviesRecommendedForUser, int rowLayout, Context context) {
        this.moviesRecommendedForUser = moviesRecommendedForUser;
        this.mContext = context;
        this.rowLayout = rowLayout;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MoviesRecommendedAdapter.MovieViewHolder onCreateViewHolder(ViewGroup parent,
                                                                    int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(rowLayout,parent,false);
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
//        TextView rating;
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
                .load("http://image.tmdb.org/t/p/w500" +movieRecommendedForUser.getMovie_poster())
                .error(R.color.colorAccent)
                .into(holder.imageView);
        holder.moviesLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext,RecommendedMovieDetailActivity.class);
                intent.putExtra("adapterName",MoviesRecommendedAdapter.class.getName());
                Logger.d("INTENT + "+ MoviesRecommendedAdapter.class.getName());
                intent.putExtra("movie_id",movieRecommendedForUser.getMovieId().toString());
                Logger.d("INTENT + "+ movieRecommendedForUser.getMovieId().toString());
                intent.putExtra("movie_title",movieRecommendedForUser.getMovie_title());
                Logger.d("INTENT + " +movieRecommendedForUser.getMovie_title().toString());
                intent.putExtra("movie_release_date",movieRecommendedForUser.getMovie_release());
                Logger.d("INTENT + " +movieRecommendedForUser.getMovie_release().toString());
                intent.putExtra("movie_description",movieRecommendedForUser.getMovie_description());
                Logger.d("INTENT + " +movieRecommendedForUser.getMovie_description().toString());
                intent.putExtra("movie_poster_path",movieRecommendedForUser.getMovie_poster());
                Logger.d("INTENT + " +movieRecommendedForUser.getMovie_poster().toString());

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
