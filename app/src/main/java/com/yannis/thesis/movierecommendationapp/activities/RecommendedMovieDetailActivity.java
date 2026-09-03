package com.yannis.thesis.movierecommendationapp.activities;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.RatingBar;

import com.squareup.picasso.Picasso;
import com.yannis.thesis.movierecommendationapp.models.MovieRecommendedForUser;
import com.yannis.thesis.movierecommendationapp.models.UserRatesMovie;
import com.yannis.thesis.movierecommendationapp.MovieRecommendationApp;
import com.yannis.thesis.movierecommendationapp.R;
import com.yannis.thesis.movierecommendationapp.databinding.RecommendedMovieDetailActivityBinding;

import java.util.Date;

import android.util.Log;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

public class RecommendedMovieDetailActivity extends AppCompatActivity implements RatingBar.OnRatingBarChangeListener {
    private RecommendedMovieDetailActivityBinding binding;
    private String movieID;
    private String posterPathStr;
    private float mRating;
    private String currentUserId;

    Realm realm = Realm.getDefaultInstance();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = RecommendedMovieDetailActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        currentUserId = MovieRecommendationApp.getInstance().getLoggedInUserId();
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
                    .load("http://image.tmdb.org/t/p/w500" + posterPathStr)
                    .error(R.color.colorAccent)
                    .into(binding.moviePoster);
        }
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
        }
    }


    //otan vathmologhsei o xrhsths thn tainia , tha prepei na :
    //1. thn afairesoume apo ton pinaka MovieRecommendedForUser
    //2. thn prosthesoume me th vathmologia pou evale o xrhsths ston pinaka UserRatesMovie
    @Override
    public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
        binding.ratingBar1.setRating(Math.round(ratingBar.getRating()));
        String activeUserId = MovieRecommendationApp.getInstance().getLoggedInUserId();
        deleteRecommendedMovie(movieID,activeUserId);
        rateMovie(activeUserId, movieID,
                binding.ratingBar1.getRating(), binding.movieTitle.getText().toString(),
                binding.movieReleaseDate.getText().toString(),
                binding.movieDescription.getText().toString(), posterPathStr);
        binding.ratingBar1.setIsIndicator(true);
    }


    public void deleteRecommendedMovie(String movieId,String activeUserId) {
        RealmQuery<MovieRecommendedForUser> query = realm.where(MovieRecommendedForUser.class)
                .equalTo("userId", activeUserId)
                .and()
                .equalTo("movieId", movieId);
        final RealmResults<MovieRecommendedForUser> result = query.findAll();
        Log.d("MovieApp","delete recommened movie results before delete" + result.size());
        // All changes to data must happen in a transaction
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                // Delete all matches
                result.deleteAllFromRealm();
            }
        });
        Log.d("MovieApp","delete recommened movie results after delete" + result.size());
    }
}

