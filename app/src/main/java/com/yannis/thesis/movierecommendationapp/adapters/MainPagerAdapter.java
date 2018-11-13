package com.yannis.thesis.movierecommendationapp.adapters;

import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

import com.luseen.logger.Logger;
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
import com.yannis.thesis.movierecommendationapp.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;
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

    String API_BASE_URL = "http://api.themoviedb.org/3/";
    private final static String API_KEY = "efbdebf1b30ffab728c49495748e9dfa";

    OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

    private Button btnSearch;

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

    Realm realm = Realm.getDefaultInstance();



    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {

        View view;
        LayoutInflater layoutinflater = LayoutInflater.from(container.getContext());
        //sto prwto tab poy exei tis protaseis gia to xrhsth
        if (position == 0) {

            //recommended movies
            view = layoutinflater.inflate(R.layout.view_main_tab, container, false);

            recyclerViewRecommendedMovies = view.findViewById(R.id.recycler_viewRec);
            recyclerViewRecommendedMovies.setLayoutManager(new LinearLayoutManager(container.getContext()));

            RealmQuery<MovieRecommendedForUser> queryRecommendation = realm
                    .where(MovieRecommendedForUser.class)
                    .equalTo("userId",MovieRecommendationApp.getInstance().getLoggedInUserId())
                    .sort("predictedRating", Sort.DESCENDING);
            RealmResults<MovieRecommendedForUser> movieRecommendations = queryRecommendation.findAll();

            recyclerViewRecommendedMovies.setAdapter(new MoviesRecommendedAdapter(movieRecommendations,
                    R.layout.movie_list_row,
                    MovieRecommendationApp.getInstance()));

            // recently rated
            recyclerViewRecentlyRatedMovies = view.findViewById(R.id.recycler_viewRat);
            recyclerViewRecentlyRatedMovies.setLayoutManager(new LinearLayoutManager(container.getContext()));

            RealmQuery<UserRatesMovie> queryRecentlyRated = realm.
                    where(UserRatesMovie.class)
                    .equalTo("userId",MovieRecommendationApp.getInstance().getLoggedInUserId())
                    .sort("dateAndTime", Sort.DESCENDING);
            RealmResults<UserRatesMovie> moviesRecentlyRated = queryRecentlyRated.findAll();
            recyclerViewRecentlyRatedMovies.setAdapter(new UserRatesMovieAdapter(moviesRecentlyRated,
                    R.layout.movie_list_row,
                    MovieRecommendationApp.getInstance()));
        } else {
            view = layoutinflater.inflate(R.layout.view_search_tab, container, false);
            recyclerViewSearch = view.findViewById(R.id.recycler_search);
            recyclerViewSearch.setLayoutManager(new LinearLayoutManager(container.getContext()));
            editText = view.findViewById(R.id.keyword_txt);

            spinner = view.findViewById(R.id.catspinner);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(container.getContext(),
                    android.R.layout.simple_dropdown_item_1line, CATEGORIES);
            // Apply the adapter to the spinner
            spinner.setAdapter(adapter);
            btnSearch = view.findViewById(R.id.searchBut);
            btnSearch.setOnClickListener(new View.OnClickListener() {
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
                        recyclerViewSearch.setAdapter(new MovieAdapter(movies, R.layout.movie_list_row, MovieRecommendationApp.getInstance()));
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
                        recyclerViewSearch.setAdapter(new MovieAdapter(movies, R.layout.movie_list_row, MovieRecommendationApp.getInstance()));
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
                            Logger.w("unsuccessful w status"+ String.valueOf(statusCode));
                        }
                        List<DirectorResult> directorResults = response.body().getResults();
                        //List<Movie> movies = directorResults.get(0).getKnownFor();
                        Logger.w("ID of this director: " + directorResults.get(0).getId());
                        call = client.getMovieByDirector(directorResults.get(0).getId(), MovieRecommendationApp.getApiKey());
                        call.enqueue(new retrofit2.Callback<MovieResponse>() {
                            @Override
                            public void onResponse(retrofit2.Call<MovieResponse> call, retrofit2.Response<MovieResponse> response) {
                                int statusCode = response.code();
                                if (response.isSuccessful() == false) {
                                    Logger.w("unsuccessful w status"+ String.valueOf(statusCode));
                                }
                                List<Movie> movies = response.body().getResults();
                                Logger.w( "Number of movies received: " + movies.size());
                                recyclerViewSearch.setAdapter(new MovieAdapter(movies, R.layout.movie_list_row, MovieRecommendationApp.getInstance()));
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
                        Log.w("AAA status", String.valueOf(statusCode));
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
                                    recyclerViewSearch.setAdapter(new MovieAdapter(movies, R.layout.movie_list_row, MovieRecommendationApp.getInstance()));
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
                                recyclerViewSearch.setAdapter(new MovieAdapter(movies, R.layout.movie_list_row, MovieRecommendationApp.getInstance()));
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

    @OnClick
    public void filterMovies() {

        Toast.makeText(MovieRecommendationApp.getInstance(),
                "Selected: " + spinner.getListSelection(),
                Toast.LENGTH_SHORT).show();
    }


}
