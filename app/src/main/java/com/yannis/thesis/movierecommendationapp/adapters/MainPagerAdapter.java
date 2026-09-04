package com.yannis.thesis.movierecommendationapp.adapters;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.DefaultItemAnimator;

import com.google.android.material.tabs.TabLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import android.util.Log;
import com.weiwangcn.betterspinner.library.BetterSpinner;
import com.yannis.thesis.movierecommendationapp.activities.MainActivity;
import com.yannis.thesis.movierecommendationapp.api.APIService;
import com.yannis.thesis.movierecommendationapp.models.DirectorResponse;
import com.yannis.thesis.movierecommendationapp.models.DirectorResult;
import com.yannis.thesis.movierecommendationapp.models.Genre;
import com.yannis.thesis.movierecommendationapp.models.GenreResponse;
import com.yannis.thesis.movierecommendationapp.models.MainPagerEnum;
import com.yannis.thesis.movierecommendationapp.models.Movie;
import com.yannis.thesis.movierecommendationapp.models.MovieResponse;
import com.yannis.thesis.movierecommendationapp.models.MovieRecommendedForUser;
import com.yannis.thesis.movierecommendationapp.models.UserRatesMovie;
import com.yannis.thesis.movierecommendationapp.MovieRecommendationApp;
import com.yannis.thesis.movierecommendationapp.data.AppDatabase;
import com.yannis.thesis.movierecommendationapp.R;
import com.yannis.thesis.movierecommendationapp.databinding.ViewMainTabBinding;
import com.yannis.thesis.movierecommendationapp.databinding.ViewSearchTabBinding;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by yiannos on 23-Feb-18.
 */

public class MainPagerAdapter extends PagerAdapter {

    private List<Movie> movieListRecommended = new ArrayList<>();
    private List<Movie> movieListRated = new ArrayList<>();
    private RecyclerView recyclerViewRecommendedMovies;
    private RecyclerView recyclerViewRecentlyRatedMovies;
    private RecyclerView recyclerViewSearch;
    private MovieAdapter mAdapterRecent;
    private MovieAdapter mAdapterRated;
    private RecyclerView.LayoutManager mLayoutManagerRecent;
    private RecyclerView.LayoutManager mLayoutManagerRated;
    private BetterSpinner spinner;
    private APIService client;
    private EditText editText;

    private static final String[] CATEGORIES = new String[]{
            "Title", "Released", "Director", "Genre", "Actors"
    };


    private static final String TAG = MainActivity.class.getSimpleName();

    String API_BASE_URL = "https://api.themoviedb.org/3/";
    private final static String API_KEY = "efbdebf1b30ffab728c49495748e9dfa";

    OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

    Retrofit.Builder builder =
            new Retrofit.Builder()
                    .baseUrl(API_BASE_URL)
                    .addConverterFactory(
                            GsonConverterFactory.create()
                    );

    Retrofit retrofit =
            builder
                    .client(
                            httpClient.build()
                    )
                    .build();

    retrofit2.Call<MovieResponse> call;
    retrofit2.Call<DirectorResponse> dcall;
    retrofit2.Call<GenreResponse> genreCall;

    AppDatabase database;



    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {

        database = AppDatabase.getInstance(container.getContext());
        View view;
        LayoutInflater layoutinflater = LayoutInflater.from(container.getContext());
        //sto prwto tab poy exei tis protaseis gia to xrhsth
        if (position == 0) {

            //recommended movies
            ViewMainTabBinding binding = ViewMainTabBinding.inflate(layoutinflater, container, false);
            view = binding.getRoot();

            recyclerViewRecommendedMovies = binding.recyclerViewRec;
            recyclerViewRecommendedMovies.setLayoutManager(new LinearLayoutManager(container.getContext()));

            List<MovieRecommendedForUser> movieRecommendations =
                    database.movieRecommendedForUserDao().findAllForUserByRating(
                            MovieRecommendationApp.getInstance().loggedInUserId);

            recyclerViewRecommendedMovies.setAdapter(new MoviesRecommendedAdapter(
                    movieRecommendations, MovieRecommendationApp.getInstance()));

            // recently rated
            recyclerViewRecentlyRatedMovies = binding.recyclerViewRat;
            recyclerViewRecentlyRatedMovies.setLayoutManager(new LinearLayoutManager(container.getContext()));

            List<UserRatesMovie> moviesRecentlyRated =
                    database.userRatesMovieDao().findAllForUser(
                            MovieRecommendationApp.getInstance().loggedInUserId);
            recyclerViewRecentlyRatedMovies.setAdapter(new UserRatesMovieAdapter(
                    moviesRecentlyRated, MovieRecommendationApp.getInstance()));
        } else {
            ViewSearchTabBinding binding = ViewSearchTabBinding.inflate(layoutinflater, container, false);
            view = binding.getRoot();
            recyclerViewSearch = binding.recyclerSearch;
            recyclerViewSearch.setLayoutManager(new LinearLayoutManager(container.getContext()));
            editText = binding.keywordTxt;

            spinner = binding.catspinner;
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(container.getContext(),
                    android.R.layout.simple_dropdown_item_1line, CATEGORIES);
            // Apply the adapter to the spinner
            spinner.setAdapter(adapter);
            binding.searchBut.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String Selection = spinner.getText().toString();
                    if (editText.getText().toString().equals("")) {
                        Toast.makeText(MovieRecommendationApp.getInstance(),
                                "Must provide category",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        searchForSelectedCategory(Selection);
                    }
                }

            });
        }
        container.addView(view);
        return view;
    }

    private void searchForSelectedCategory(String category) {
        client = retrofit.create(APIService.class);
        switch (category) {
            case "Title":
                call = client.getMovieByTitle(editText.getText().toString(), MovieRecommendationApp.getApiKey());
                call.enqueue(new retrofit2.Callback<MovieResponse>() {
                    @Override
                    public void onResponse(retrofit2.Call<MovieResponse> call, retrofit2.Response<MovieResponse> response) {
                        int statusCode = response.code();
                        Log.w("AAA status", String.valueOf(statusCode));
                        List<Movie> movies = response.body().getResults();
                        Log.d(TAG, "Number of movies received: " + movies.size());
                        recyclerViewSearch.setAdapter(new MovieAdapter(movies, MovieRecommendationApp.getInstance()));
                    }

                    @Override
                    public void onFailure(retrofit2.Call<MovieResponse> call, Throwable t) {
                        // Log error here since request failed
                        Log.e(TAG, t.toString());
                    }
                });
                break;
            case "Released":
                call = client.getMovieByReleasedYear(editText.getText().toString(), MovieRecommendationApp.getApiKey());
                call.enqueue(new retrofit2.Callback<MovieResponse>() {
                    @Override
                    public void onResponse(retrofit2.Call<MovieResponse> call, retrofit2.Response<MovieResponse> response) {
                        int statusCode = response.code();
                        if (response.isSuccessful() == false) {
                            Log.w("unsuccessful w status", String.valueOf(statusCode));
                        }
                        List<Movie> movies = response.body().getResults();
                        // Log.w(TAG, "Number of movies received: " + movies.size());
                        recyclerViewSearch.setAdapter(new MovieAdapter(movies, MovieRecommendationApp.getInstance()));
                    }

                    @Override
                    public void onFailure(retrofit2.Call<MovieResponse> call, Throwable t) {
                        // Log error here since request failed
                        Log.e(TAG, t.toString());
                    }
                });
                break;
            case "Director":
                dcall = client.getPersonIdByName(editText.getText().toString(), MovieRecommendationApp.getApiKey());
                dcall.enqueue(new retrofit2.Callback<DirectorResponse>() {
                    @Override
                    public void onResponse(retrofit2.Call<DirectorResponse> dcall, retrofit2.Response<DirectorResponse> response) {
                        int statusCode = response.code();
                        if (response.isSuccessful() == false) {
                            Log.d("MovieApp","unsuccessful w status"+ String.valueOf(statusCode));
                        }
                        List<DirectorResult> directorResults = response.body().getResults();
                        //List<Movie> movies = directorResults.get(0).getKnownFor();
                        Log.d("MovieApp","ID of this director: " + directorResults.get(0).getId());
                        call = client.getMovieByDirector(directorResults.get(0).getId(), MovieRecommendationApp.getApiKey());
                        call.enqueue(new retrofit2.Callback<MovieResponse>() {
                            @Override
                            public void onResponse(retrofit2.Call<MovieResponse> call, retrofit2.Response<MovieResponse> response) {
                                int statusCode = response.code();
                                if (response.isSuccessful() == false) {
                                    Log.d("MovieApp","unsuccessful w status"+ String.valueOf(statusCode));
                                }
                                List<Movie> movies = response.body().getResults();
                                Log.d("MovieApp", "Number of movies received: " + movies.size());
                                recyclerViewSearch.setAdapter(new MovieAdapter(movies, MovieRecommendationApp.getInstance()));
                            }

                            @Override
                            public void onFailure(retrofit2.Call<MovieResponse> call, Throwable t) {
                                // Log error here since request failed
                                Log.e(TAG, t.toString());
                            }
                        });
                    }

                    @Override
                    public void onFailure(retrofit2.Call<DirectorResponse> dcall, Throwable t) {
                        // Log error here since request failed
                        Log.e(TAG, t.toString());
                    }
                });
                break;
            case "Genre":
                genreCall = client.getAllGenres(MovieRecommendationApp.getApiKey());
                genreCall.enqueue(new retrofit2.Callback<GenreResponse>() {
                    @Override
                    public void onResponse(retrofit2.Call<GenreResponse> gcall, retrofit2.Response<GenreResponse> response) {
                        int statusCode = response.code();
                        int genreId = -1;
                        Log.d("MovieApp", "AAA status: " + String.valueOf(statusCode));
                        List<Genre> genres = response.body().getGenres();
                        // Log.d(TAG, "Number of genres received: " + genres.size());
                        for (Genre g : genres) {
                            if (g.getName().equals(editText.getText().toString())) {
                                genreId = g.getId();
                                break;
                            }
                            Log.d(TAG, "ID" + g.getId() + "|" + g.getName());
                        }
                        if (genreId == -1) {
                            Toast.makeText(MovieRecommendationApp.getInstance(),
                                    "such genre not found :/",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            call = client.getMovieByGenre(genreId, MovieRecommendationApp.getApiKey());
                            call.enqueue(new retrofit2.Callback<MovieResponse>() {
                                @Override
                                public void onResponse(retrofit2.Call<MovieResponse> call, retrofit2.Response<MovieResponse> response) {
                                    int statusCode = response.code();
                                    if (response.isSuccessful() == false) {
                                        Log.w("unsuccessful w status", String.valueOf(statusCode));
                                    }
                                    List<Movie> movies = response.body().getResults();
                                    //Log.w(TAG, "Number of movies received: " + movies.size());
                                    recyclerViewSearch.setAdapter(new MovieAdapter(movies, MovieRecommendationApp.getInstance()));
                                }

                                @Override
                                public void onFailure(retrofit2.Call<MovieResponse> call, Throwable t) {
                                    // Log error here since request failed
                                    Log.e(TAG, t.toString());
                                }
                            });
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<GenreResponse> call, Throwable t) {
                        // Log error here since request failed
                        Log.e(TAG, t.toString());
                    }
                });
                break;
            case "Actors":
                dcall = client.getPersonIdByName(editText.getText().toString(), MovieRecommendationApp.getApiKey());
                dcall.enqueue(new retrofit2.Callback<DirectorResponse>() {
                    @Override
                    public void onResponse(retrofit2.Call<DirectorResponse> dcall, retrofit2.Response<DirectorResponse> response) {
                        int statusCode = response.code();
                        if (response.isSuccessful() == false) {
                            Log.w("unsuccessful w status", String.valueOf(statusCode));
                        }
                        List<DirectorResult> directorResults = response.body().getResults();
                        //List<Movie> movies = directorResults.get(0).getKnownFor();
                        Log.w(TAG, "ID of this actor: " + directorResults.get(0).getId());
                        call = client.getMovieByActor(directorResults.get(0).getId(), MovieRecommendationApp.getApiKey());
                        call.enqueue(new retrofit2.Callback<MovieResponse>() {
                            @Override
                            public void onResponse(retrofit2.Call<MovieResponse> call, retrofit2.Response<MovieResponse> response) {
                                int statusCode = response.code();
                                if (response.isSuccessful() == false) {
                                    Log.w("unsuccessful w status", String.valueOf(statusCode));
                                }
                                List<Movie> movies = response.body().getResults();
                                Log.w(TAG, "Number of movies received: " + movies.size());
                                recyclerViewSearch.setAdapter(new MovieAdapter(movies, MovieRecommendationApp.getInstance()));
                            }

                            @Override
                            public void onFailure(retrofit2.Call<MovieResponse> call, Throwable t) {
                                // Log error here since request failed
                                Log.e(TAG, t.toString());
                            }
                        });
                    }

                    @Override
                    public void onFailure(retrofit2.Call<DirectorResponse> dcall, Throwable t) {
                        // Log error here since request failed
                        Log.e(TAG, t.toString());
                    }
                });
                break;
            default:
                Toast.makeText(MovieRecommendationApp.getInstance(),
                        "Must provide keywords",
                        Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getCount() {
        return MainPagerEnum.values().length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        MainPagerEnum customPagerEnum = MainPagerEnum.values()[position];
        return MovieRecommendationApp.getInstance().getString(customPagerEnum.getTitleResId());
    }

}
