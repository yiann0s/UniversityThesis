package com.yannis.thesis.movierecommendationapp.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.yannis.thesis.movierecommendationapp.models.User;
import com.yannis.thesis.movierecommendationapp.models.UserRatesMovie;
import com.yannis.thesis.movierecommendationapp.MovieRecommendationApp;
import com.yannis.thesis.movierecommendationapp.R;

import java.util.Date;

import com.luseen.logger.Logger;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;


public class MovieDetailActivity extends AppCompatActivity implements RatingBar.OnRatingBarChangeListener {
    @BindView(R.id.movie_title)
    TextView mTitle;
    @BindView(R.id.movie_release_date)
    TextView mReleaseDate;
    @BindView(R.id.movie_description)
    TextView mDescription;
    @BindView(R.id.movie_poster)
    ImageView mPosterPath;

    private RatingBar mRatingBar;
    private String movieID;
    private String posterPathStr;
    private float mRating;
    private String currentUserId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movie_detail_activity);
        ButterKnife.bind(this);

        currentUserId = MovieRecommendationApp.getInstance().getLoggedInUserId();
        getIncomingIntent();

        mRatingBar = findViewById(R.id.ratingBar1);
        if (!isMovieAlreadyRatedByCurrentUser()) {
            Logger.d("It's not yet rated");
            mRatingBar.setOnRatingBarChangeListener(this);
        } else {
            Logger.d("It's rated");
            displayMovieRating();
        }

    }

    private void getIncomingIntent() {
        if (getIntent().hasExtra("movie_title") && getIntent().hasExtra("movie_release_date")
                && getIntent().hasExtra("movie_description") && getIntent().hasExtra("movie_id")
                && getIntent().hasExtra("movie_poster_path") && getIntent().hasExtra("adapterName")) {
            Logger.w("intent was called from " + getIntent().getStringExtra("adapterName") );
            mTitle.setText(getIntent().getStringExtra("movie_title"));
            mReleaseDate.setText(getIntent().getStringExtra("movie_release_date"));
            mDescription.setText(getIntent().getStringExtra("movie_description"));
            movieID = getIntent().getStringExtra("movie_id");
            posterPathStr = getIntent().getStringExtra("movie_poster_path");
            Picasso.get()
                    .load("http://image.tmdb.org/t/p/w500" + posterPathStr)
                    .error(R.color.colorAccent)
                    .into(mPosterPath);
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
        Logger.w("rating a movie");
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
        mRatingBar.setRating(Math.round(ratingBar.getRating()));
        rateMovie(MovieRecommendationApp.getInstance().getLoggedInUserId()
                , movieID, mRatingBar.getRating(), mTitle.getText().toString(),
                mReleaseDate.getText().toString(),
                mDescription.getText().toString(), posterPathStr);
        mRatingBar.setIsIndicator(true);
    }

    public void displayMovieRating() {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<UserRatesMovie> movieRatingResult;
        try {
            RealmQuery<UserRatesMovie> query = realm.where(UserRatesMovie.class)
                    .equalTo("userId", currentUserId)
                    .equalTo("movieId", movieID);
            UserRatesMovie userRatesMovie = query.findFirst();
            Logger.w("User has rated this movie with a " + userRatesMovie.getRating());
            mRatingBar.setRating(userRatesMovie.getRating());
        } finally {
            realm.close();
        }
        mRatingBar.setIsIndicator(true);
    }

}
