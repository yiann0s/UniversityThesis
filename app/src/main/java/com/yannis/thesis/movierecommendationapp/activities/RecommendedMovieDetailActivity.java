package com.yannis.thesis.movierecommendationapp.activities;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.RatingBar;

import com.squareup.picasso.Picasso;
import com.yannis.thesis.movierecommendationapp.models.MovieRecommendedForUser;
import com.yannis.thesis.movierecommendationapp.models.UserRatesMovie;
import com.yannis.thesis.movierecommendationapp.MovieRecommendationApp;
import com.yannis.thesis.movierecommendationapp.data.AppDatabase;
import com.yannis.thesis.movierecommendationapp.R;
import com.yannis.thesis.movierecommendationapp.databinding.RecommendedMovieDetailActivityBinding;

import java.util.Date;
import java.util.List;

import android.util.Log;


public class RecommendedMovieDetailActivity extends AppCompatActivity implements RatingBar.OnRatingBarChangeListener {
    private RecommendedMovieDetailActivityBinding binding;
    private String movieID;
    private String posterPathStr;
    private float mRating;
    private String currentUserId;

    private AppDatabase database;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = RecommendedMovieDetailActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        currentUserId = MovieRecommendationApp.getInstance().loggedInUserId;
        database = AppDatabase.getInstance(this);
        getIncomingIntent();

        binding.ratingBar1.setOnRatingBarChangeListener(this);

    }

    private void getIncomingIntent() {
        if (getIntent().hasExtra("movie_title") && getIntent().hasExtra("movie_release_date")
                && getIntent().hasExtra("movie_description") && getIntent().hasExtra("movie_id")
                && getIntent().hasExtra("movie_poster_path") && getIntent().hasExtra("adapterName")) {
            Log.d("MovieApp","intent was called from " + getIntent().getStringExtra("adapterName"));
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

    private void rateMovie(String userid, String movieid, float rating,
                           String title, String release, String description,
                           String poster) {
        Log.d("MovieApp","rating a movie");
        UserRatesMovie urm = new UserRatesMovie(userid, movieid, Math.round(rating), new Date(),
                poster, title, description, release);
        database.userRatesMovieDao().insert(urm);
    }


    //otan vathmologhsei o xrhsths thn tainia , tha prepei na :
    //1. thn afairesoume apo ton pinaka MovieRecommendedForUser
    //2. thn prosthesoume me th vathmologia pou evale o xrhsths ston pinaka UserRatesMovie
    @Override
    public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
        binding.ratingBar1.setRating(Math.round(ratingBar.getRating()));
        String activeUserId = MovieRecommendationApp.getInstance().loggedInUserId;
        deleteRecommendedMovie(movieID,activeUserId);
        rateMovie(activeUserId, movieID,
                binding.ratingBar1.getRating(), binding.movieTitle.getText().toString(),
                binding.movieReleaseDate.getText().toString(),
                binding.movieDescription.getText().toString(), posterPathStr);
        binding.ratingBar1.setIsIndicator(true);
    }


    public void deleteRecommendedMovie(String movieId,String activeUserId) {
        final List<MovieRecommendedForUser> result =
                database.movieRecommendedForUserDao().findForMovie(activeUserId, movieId);
        Log.d("MovieApp","delete recommened movie results before delete" + result.size());
        database.movieRecommendedForUserDao().delete(result);
        Log.d("MovieApp","delete recommened movie results after delete" + result.size());
    }
}
