package com.yannis.thesis.movierecommendationapp.activities;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.RatingBar;

import com.squareup.picasso.Picasso;
import com.yannis.thesis.movierecommendationapp.models.UserRatesMovie;
import com.yannis.thesis.movierecommendationapp.MovieRecommendationApp;
import com.yannis.thesis.movierecommendationapp.R;
import com.yannis.thesis.movierecommendationapp.databinding.MovieDetailActivityBinding;

import java.util.Date;

import android.util.Log;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;


public class MovieDetailActivity extends AppCompatActivity implements RatingBar.OnRatingBarChangeListener {
    private MovieDetailActivityBinding binding;
    private String movieID;
    private String posterPathStr;
    private float mRating;
    private String currentUserId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = MovieDetailActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        currentUserId = MovieRecommendationApp.getInstance().getLoggedInUserId();
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
        Realm realm = Realm.getDefaultInstance();
        RealmResults<UserRatesMovie> result;
        try {
            RealmQuery<UserRatesMovie> query = realm.where(UserRatesMovie.class)
                    .equalTo("userId", currentUserId)
                    .equalTo("movieId", movieID);
            result = query.findAll();
        } finally {
            realm.close();
        }
        return result.size() != 0;
    }

    private void rateMovie(String userid, String movieid, float rating,
                           String title, String release, String description,
                           String poster) {
        Log.d("MovieApp","rating a movie");
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();
            UserRatesMovie urm = realm.createObject(UserRatesMovie.class);
            urm.setUserId(userid);
            urm.setMovieId(movieid);
            urm.setUserId(userid);
            urm.setDateAndTime(new Date());
            urm.setRating(Math.round(rating));
            urm.setMovie_poster(poster);
            urm.setMovie_title(title);
            urm.setMovie_description(description);
            urm.setMovie_release(release);
            realm.commitTransaction();
        } finally {
            realm.close();
//            MovieRecommendationApp.getInstance()
        }
    }

    @Override
    public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
        binding.ratingBar1.setRating(Math.round(ratingBar.getRating()));
        rateMovie(MovieRecommendationApp.getInstance().getLoggedInUserId()
                , movieID, binding.ratingBar1.getRating(), binding.movieTitle.getText().toString(),
                binding.movieReleaseDate.getText().toString(),
                binding.movieDescription.getText().toString(), posterPathStr);
        binding.ratingBar1.setIsIndicator(true);
    }

    public void displayMovieRating() {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<UserRatesMovie> movieRatingResult;
        try {
            RealmQuery<UserRatesMovie> query = realm.where(UserRatesMovie.class)
                    .equalTo("userId", currentUserId)
                    .equalTo("movieId", movieID);
            UserRatesMovie userRatesMovie = query.findFirst();
            Log.d("MovieApp","User has rated this movie with a " + userRatesMovie.getRating());
            binding.ratingBar1.setRating(userRatesMovie.getRating());
        } finally {
            realm.close();
        }
        binding.ratingBar1.setIsIndicator(true);
    }

}
