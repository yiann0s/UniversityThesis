package com.yannis.thesis.movierecommendationapp.activities;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.RatingBar;

import com.squareup.picasso.Picasso;
import com.yannis.thesis.movierecommendationapp.models.UserRatesMovie;
import com.yannis.thesis.movierecommendationapp.MovieRecommendationApp;
import com.yannis.thesis.movierecommendationapp.data.AppDatabase;
import com.yannis.thesis.movierecommendationapp.R;
import com.yannis.thesis.movierecommendationapp.databinding.MovieDetailActivityBinding;

import java.util.Date;

import android.util.Log;



public class MovieDetailActivity extends AppCompatActivity implements RatingBar.OnRatingBarChangeListener {
    private MovieDetailActivityBinding binding;
    private String movieID;
    private String posterPathStr;
    private float mRating;
    private String currentUserId;
    private AppDatabase database;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = MovieDetailActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        currentUserId = MovieRecommendationApp.getInstance().loggedInUserId;
        database = AppDatabase.getInstance(this);
        getIncomingIntent();

        if (!isMovieAlreadyRatedByCurrentUser()) {
            Log.d("MovieApp","It's not yet rated");
            binding.ratingBar1.setOnRatingBarChangeListener(this);
        } else {
            Log.d("MovieApp","It's rated");
            displayMovieRating();
        }

    }

    private void getIncomingIntent() {
        if (getIntent().hasExtra("movie_title") && getIntent().hasExtra("movie_release_date")
                && getIntent().hasExtra("movie_description") && getIntent().hasExtra("movie_id")
                && getIntent().hasExtra("movie_poster_path") && getIntent().hasExtra("adapterName")) {
            Log.d("MovieApp","intent was called from " + getIntent().getStringExtra("adapterName") );
            binding.movieTitle.setText(getIntent().getStringExtra("movie_title"));
            binding.movieReleaseDate.setText(getIntent().getStringExtra("movie_release_date"));
            binding.movieDescription.setText(getIntent().getStringExtra("movie_description"));
            movieID = getIntent().getStringExtra("movie_id");
            posterPathStr = getIntent().getStringExtra("movie_poster_path");
            Picasso.get()
                    .load("https://image.tmdb.org/t/p/w500" + posterPathStr)
                    .error(R.color.colorAccent)
                    .into(binding.moviePoster);
        }
    }

    private boolean isMovieAlreadyRatedByCurrentUser() {
        return database.userRatesMovieDao().findForMovie(currentUserId, movieID) != null;
    }

    private void rateMovie(String userid, String movieid, float rating,
                           String title, String release, String description,
                           String poster) {
        Log.d("MovieApp","rating a movie");
        UserRatesMovie urm = new UserRatesMovie(userid, movieid, Math.round(rating), new Date(),
                poster, title, description, release);
        database.userRatesMovieDao().insert(urm);
    }

    @Override
    public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
        binding.ratingBar1.setRating(Math.round(ratingBar.getRating()));
        rateMovie(MovieRecommendationApp.getInstance().loggedInUserId
                , movieID, binding.ratingBar1.getRating(), binding.movieTitle.getText().toString(),
                binding.movieReleaseDate.getText().toString(),
                binding.movieDescription.getText().toString(), posterPathStr);
        binding.ratingBar1.setIsIndicator(true);
    }

    public void displayMovieRating() {
        UserRatesMovie userRatesMovie = database.userRatesMovieDao().findForMovie(currentUserId, movieID);
        Log.d("MovieApp","User has rated this movie with a " + userRatesMovie.getRating());
        binding.ratingBar1.setRating(userRatesMovie.getRating());
        binding.ratingBar1.setIsIndicator(true);
    }

}
