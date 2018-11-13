package com.yannis.thesis.movierecommendationapp;

import android.app.Application;
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

import com.luseen.logger.LogType;
import com.luseen.logger.Logger;
import com.weiwangcn.betterspinner.library.BetterSpinner;
import com.yannis.thesis.movierecommendationapp.activities.BaseActivity;
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
import com.yannis.thesis.movierecommendationapp.models.User;
import com.yannis.thesis.movierecommendationapp.models.UserRatesMovie;
import com.yannis.thesis.movierecommendationapp.MovieRecommendationApp;
import com.yannis.thesis.movierecommendationapp.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import io.realm.Realm;
import io.realm.RealmConfiguration;
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
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
/**
 * Created by yiannos on 12-Feb-18.
 */

public class MovieRecommendationApp extends Application {

    private static MovieRecommendationApp instance;
    public BaseActivity lastActivity;

    String API_BASE_URL = "http://api.themoviedb.org/3/";
    private final static String API_KEY = "efbdebf1b30ffab728c49495748e9dfa";
    private static Retrofit retrofitinstance;

    private String loggedInUserId;
    private Realm realm;
    final Double SIMILARITY_PILLOW = 0.5;
    final Double PREDICTION_PILLOW = 3.0;

    private APIService client;


    retrofit2.Call<Movie> call;

    Retrofit retrofit;

    Retrofit.Builder builder;

    OkHttpClient.Builder httpClient;

    HttpLoggingInterceptor logging;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        Realm.init(this);
//
//        //.deleteRealmIfMigrationNeeded() -> This means that if you are in the middle of development
//        // and changing your schema
//        //frequently—and it’s all right to lose all your data—you can delete your .realm file on
//        // disk instead of writing a migration. This can be helpful when tinkering with models
//        // early in the development cycle of your app.
        RealmConfiguration config = new RealmConfiguration.Builder()
                .name("myrealmDB.realm")
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(config);

        new Logger.Builder()
                .isLoggable(BuildConfig.DEBUG)
                .logType(LogType.WARN)
                .tag("Iamerror")
                .build();

        realm = Realm.getDefaultInstance();


        httpClient = new OkHttpClient.Builder();

        builder =
                new Retrofit.Builder()
                        .baseUrl(API_BASE_URL)
                        .addConverterFactory(
                                GsonConverterFactory.create()
                        );

        retrofit =
                builder
                        .client(
                                httpClient.build()
                        )
                        .build();

        //addUsers();
        //addUserRatesMovie();
        //showRecommendedMoviesFosActiveUser();
        showAllUsers();
        MovieRecommendationAlgorithm();
        //showAllUserRatedMovies();
    }

    public void showRecommendedMoviesFosActiveUser(){
        String activeUserID = "3c5303e9-0b5e-493a-98e8-184893dbb261";
        RealmQuery<MovieRecommendedForUser> queryRecommendation = realm
                .where(MovieRecommendedForUser.class)
                .equalTo("userId",activeUserID)
                .sort("predictedRating", Sort.DESCENDING);
        RealmResults<MovieRecommendedForUser> movieRecommendations = queryRecommendation.findAll();
        for (MovieRecommendedForUser movieRecommendedForUser : movieRecommendations) {
            Logger.w("Recommended movie for user is " + movieRecommendedForUser.getMovie_title()
                    + " rating " + movieRecommendedForUser.getPredictedRating());
        }
    }

    public void showAllUsers(){
        RealmQuery<User> queryUsers = realm
                .where(User.class);
        RealmResults<User> users = queryUsers.findAll();
        for (User u : users) {
            Logger.w("User has mail " + u.getEmail() + " and id " + u.getId() + " and password " + u.getPassword());
        }
    }

    public void showAllUserRatedMovies(){
        ArrayList<String> userList = new ArrayList<>();
        RealmQuery<User> queryUsers = realm
                .where(User.class);
        RealmResults<User> users = queryUsers.findAll();
        for (User u : users) {
            Logger.w("User " + u.getEmail());
            RealmQuery<UserRatesMovie> queryUserRatesMovie = realm
                    .where(UserRatesMovie.class);

            RealmResults<UserRatesMovie> userRatesMovies = queryUserRatesMovie
                    .equalTo("userId",u.getId())
                    .findAll();
            for (UserRatesMovie urm : userRatesMovies) {
                Logger.w("has rated" + urm.getMovie_title() + " with a " + urm.getRating());
            }
        }
    }

    public static MovieRecommendationApp getInstance() {
        return instance;
    }

    public static Retrofit getRetrofitInstance() {
        return retrofitinstance;
    }

    public static String getApiKey() {
        return API_KEY;
    }

    //h synarthsh afth dexetai to userID tou xrhsth kai kanei populate ton pinaka
    // sth vash dedomenwn me tis tainies pou tha aresoun sto xrhsth
    public void MovieRecommendationAlgorithm() {
//        Logger.w("im here");
        // bloper@gmail.com einai o active user mas
        // to id tou bloper@gmail.com  einai 75ed56fd-1f80-47ff-819b-fe35be3bc85e
        String activeUserID = "3c5303e9-0b5e-493a-98e8-184893dbb261";
        RealmResults<User> users = realm.where(User.class)
                .notEqualTo("id", activeUserID)
                .findAll();

        ArrayList<String> neightboursList = new ArrayList<>();
        for (User user : users) {
            Logger.w("Similarioty of active user and user "+ user.getEmail() + " is " +similarity(activeUserID,user.getId()));
            if (similarity(activeUserID, user.getId()) >= SIMILARITY_PILLOW) {
                neightboursList.add(user.getId());
            }
        }

        /////////////////////
        Map<String, Double> movieMapActiveUser = new HashMap<>();
        ArrayList<String> notRatedMoviesIds = new ArrayList<>();

        RealmResults<UserRatesMovie> activeUserResults = realm.where(UserRatesMovie.class)
                .equalTo("userId", activeUserID)
                .findAll();
        for (UserRatesMovie activeUserRatesMovie : activeUserResults) {
            movieMapActiveUser.put(activeUserRatesMovie.getMovieId(), Double.valueOf(activeUserRatesMovie.getRating()));
        }
        //tha paroume oles tis tainies pou den exei vathmologhsei o active User
        RealmResults<UserRatesMovie> movieIds = realm.where(UserRatesMovie.class)
                .findAll();
        String currentMovieId;
        for (UserRatesMovie userRatesMovie : movieIds) {
            currentMovieId = userRatesMovie.getMovieId();
            //an den einai mesa sth lista twn ids twn tainian pou exei vathmologhsei o active user
            if (!(movieMapActiveUser.containsKey(currentMovieId)) && (!(notRatedMoviesIds.contains(currentMovieId)))) {
                notRatedMoviesIds.add(currentMovieId);
            }
        }
        //gia oles tis tainies pou den exei o xrhsths akomh vathmologhsei
        for (int i = 0; i < notRatedMoviesIds.size(); i++) {
            currentMovieId = notRatedMoviesIds.get(i);
            prediction(activeUserID, currentMovieId, neightboursList);
        }
    }

    public void prediction(final String activeUserId, final String notYetRatedMovieId, ArrayList<String> neightbours) {
        Double activeAVG = avgRating(activeUserId);
        Double A = 0.0;
        Double B = 0.0;
        for (int i = 0; i < neightbours.size(); i++) {
            avgRating(neightbours.get(i));
            A = A + similarity(activeUserId, neightbours.get(i)) * (getUser_i_MovieRating(neightbours.get(i), notYetRatedMovieId) - avgRating(neightbours.get(i)));
            B = B + similarity(activeUserId, neightbours.get(i));
        }
        final Double prediction = activeAVG + A / B;
        //an h provlepomenh vathmologia den einai panw apo to katwfli pou exoume orisei
        // ,de xreiazetai na apothikeftei sth vash dedomenwn
        Logger.w("prediction of movie id " + notYetRatedMovieId + " is " + prediction);
        if (prediction < PREDICTION_PILLOW) {
            return;
        }
//        Logger.w("prediction of movie id " + notYetRatedMovieId + " is " + prediction);
        int movieId = Integer.parseInt(notYetRatedMovieId);
        client = retrofit.create(APIService.class);
        call = client.getMovieDetails(movieId, MovieRecommendationApp.getApiKey());
        call.enqueue(new retrofit2.Callback<Movie>() {
            @Override
            public void onResponse(retrofit2.Call<Movie> call, retrofit2.Response<Movie> response) {
                int statusCode = response.code();
                if (!response.isSuccessful()) {
                    Logger.w("unsuccessful w status"+String.valueOf(statusCode));
                } else if (response.isSuccessful()){
                    Movie m = response.body();
                    //an h tainia einai hdh stis proteinomenew gia to xrhsth
                    //thn diagrafoume apo thn database gia na thn antikatasthsoume me th neoterh timh ths
                    if (!movieIsUnique(notYetRatedMovieId,activeUserId)) {
                        Logger.w("movie exists, so im gonna first delete");
                        deleteMovie(notYetRatedMovieId, activeUserId);
                    }   //alliws dhmirgoume antikeeimeno tak ito eisagoume sth vash mas
                    Logger.w("adding recommeneed movie");
                    realm.beginTransaction();
                    final MovieRecommendedForUser movieRecommendedForUser =
                                realm.createObject(MovieRecommendedForUser.class);
                    movieRecommendedForUser.setMovieId(notYetRatedMovieId);
                    movieRecommendedForUser.setPredictedRating(prediction);
                    movieRecommendedForUser.setUserId(activeUserId);
                    movieRecommendedForUser.setDateAndTime(new Date());
                    movieRecommendedForUser.setMovie_description(m.getOverview());
                    movieRecommendedForUser.setMovie_poster(m.getPosterPath());
                    movieRecommendedForUser.setMovie_release(m.getReleaseDate());
                    movieRecommendedForUser.setMovie_title(m.getTitle());
                    realm.commitTransaction();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<Movie> call, Throwable t) {
                Logger.w("Failure at getMoviedetails callback:"+t.getMessage().toString());
            }
        });
    }

    private boolean movieIsUnique(String movieId,String activeUserId) {
        RealmQuery<MovieRecommendedForUser> query = realm.where(MovieRecommendedForUser.class)
                .equalTo("userId", activeUserId)
                .and()
                .equalTo("movieId", movieId);
        RealmResults<MovieRecommendedForUser> result = query.findAll();
        return result.size() == 0;
    }

    public void deleteMovie(String movieId,String activeUserId) {
        RealmQuery<MovieRecommendedForUser> query = realm.where(MovieRecommendedForUser.class)
                .equalTo("userId", activeUserId)
                .and()
                .equalTo("movieId", movieId);
        final RealmResults<MovieRecommendedForUser> result = query.findAll();
        // All changes to data must happen in a transaction
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                // Delete all matches
                result.deleteAllFromRealm();
            }
        });
    }

    public Double getUser_i_MovieRating(String userId, String movieId) {
        UserRatesMovie userResults = realm.where(UserRatesMovie.class)
                .equalTo("userId", userId)
                .and()
                .equalTo("movieId", movieId)
                .findFirst();
        return Double.valueOf(userResults.getRating());
    }

    public Double similarity(String activeUserID, String user_i) {

        Map<String, Double> helperA = new HashMap<>();

        Map<String, Double> helperB = new HashMap<>();
        ArrayList<String> sameMoviesIds = new ArrayList<>();
        Double activeUserAVG = avgRating(activeUserID);
        Double user_i_AVG = 0.0;

        RealmResults<UserRatesMovie> activeUserResults = realm.where(UserRatesMovie.class)
                .equalTo("userId", activeUserID)
                .findAll();
        RealmResults<UserRatesMovie> user_i_Results = realm.where(UserRatesMovie.class)
                .equalTo("userId", user_i)
                .findAll();
        for (UserRatesMovie activeUserRatesMovie : activeUserResults) {
            helperA.put(activeUserRatesMovie.getMovieId(), Double.valueOf(activeUserRatesMovie.getRating()));
        }

        for (UserRatesMovie user_i_RatesMovie : user_i_Results) {
            String currentMovieId = user_i_RatesMovie.getMovieId();
            Double currentMovieRating = Double.valueOf(user_i_RatesMovie.getRating());
            //an vrhkame koinh tainia tou active user kai tou I xrhsth
            // tha krathsoume to id ths kai th vathmologia ths sth lista
            if (helperA.containsKey(currentMovieId)) {
                helperB.put(currentMovieId, currentMovieRating);
                user_i_AVG = user_i_AVG + currentMovieRating;
                sameMoviesIds.add(currentMovieId); //lista pou kratame ta id twn koinwn
            }
        }
        user_i_AVG = user_i_AVG / helperB.size();

        Double K_sum = 0.0;
        Double L_sum = 0.0;
        Double M_sum = 0.0;
        for (int i = 0; i < sameMoviesIds.size(); i++) {
            String currentId = sameMoviesIds.get(i);
            // h vathmologia ths tainias me to current id gia ton active user A
            Double r_A = helperA.get(currentId);
            // h vathmologia ths tainias me to current id gia ton user i
            Double r_i = helperB.get(currentId);
            K_sum = K_sum + (r_A - activeUserAVG) * (r_i - user_i_AVG);
            L_sum = L_sum + Math.pow(r_A - activeUserAVG, 2);
            M_sum = M_sum + Math.pow(r_i - user_i_AVG, 2);
        }
        return (K_sum / (Math.sqrt(L_sum) * Math.sqrt(M_sum)));
    }

    //genikhs xrhshs synrthash gia ypologismou mesou orou
    public Double avgRating(String userId) {
        Double userAVG = 0.0;
        RealmResults<UserRatesMovie> userResults = realm.where(UserRatesMovie.class)
                .equalTo("userId", userId)
                .findAll();
        for (UserRatesMovie userRatesMovie : userResults) {
            userAVG = userAVG + Double.valueOf(userRatesMovie.getRating());
        }
        return (userAVG / userResults.size());
    }

    public String getLoggedInUserId() {
        return loggedInUserId;
    }


    public void setLoggedInUserId(String loggedInUserId) {
        this.loggedInUserId = loggedInUserId;
    }

    public void addUsers() {
        try {
            realm.beginTransaction();
            User user = realm.createObject(User.class,
                    "3c5303e9-0b5e-493a-98e8-184893dbb261");
            user.setEmail("bloper@gmail.com");
            user.setPassword("123456");
            Logger.w("bloper  id: " + user.getId());
            realm.commitTransaction();
        } finally {
            realm.close();
        }
        realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();
            User user = realm.createObject(User.class,
                    "402ab71d-02af-4c93-9f43-7ed21cc3acd8");
            user.setEmail("bibou@hotmail.com");
            user.setPassword("234567");
            Logger.w("bibou  id: " + user.getId());
            realm.commitTransaction();
        } finally {
            realm.close();
        }
        realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();
            User user = realm.createObject(User.class,
                    "3f4d09f8-d499-43ab-a24f-86ccb3d546cb");
            user.setEmail("zaze@gmail.com");
            user.setPassword("345678");
            Logger.w("zaze  id: " + user.getId());
            realm.commitTransaction();
        } finally {
            realm.close();
        }
        realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();
            User user = realm.createObject(User.class,
                    "4343ad42-d303-42ca-a324-c5c0b520bd8f");
            user.setEmail("tinton@hotmail.gr");
            user.setPassword("456789");
            Logger.w("tinton  id: " + user.getId());
            realm.commitTransaction();
        } finally {
            realm.close();
        }
        realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();
            User user = realm.createObject(User.class,
                    "cbed9f61-b7f0-4a0d-ab4a-953e22c25473");
            user.setEmail("ezziz@yahoo.net");
            user.setPassword("567890");
            Logger.w("ezziz  id: " + user.getId());
            realm.commitTransaction();
        } finally {
            realm.close();
        }
    }

    public void addUserRatesMovie() {
        //bloper
        realm = Realm.getDefaultInstance();
         try {
             realm.beginTransaction();
             UserRatesMovie usm = realm.createObject(UserRatesMovie.class);
             usm.setUserId("3c5303e9-0b5e-493a-98e8-184893dbb261");
             usm.setMovieId("238");
             usm.setRating(5);
             usm.setDateAndTime(new Date());
             usm.setMovie_release("1972-03-14");
             usm.setMovie_description("Spanning the years 1945 to 1955, a chronicle of the fictional Italian-American Corleone crime family. When organized crime family patriarch, Vito Corleone barely survives an attempt on his life, his youngest son, Michael steps in to take care of the would-be killers, launching a campaign of bloody revenge.");
             usm.setMovie_title("The Godfather");
             usm.setMovie_poster("/rPdtLWNsZmAtoZl9PK7S2wE3qiS.jpg");
             realm.commitTransaction();
         } finally { realm.close(); }
        realm = Realm.getDefaultInstance();
         try {
             realm.beginTransaction();
              UserRatesMovie usm = realm.createObject(UserRatesMovie.class);
             usm.setUserId("3c5303e9-0b5e-493a-98e8-184893dbb261");
             usm.setMovieId("680");
             usm.setRating(3);
             usm.setDateAndTime(new Date());
             usm.setMovie_release("1994-09-10");
             usm.setMovie_description("A burger-loving hit man, his philosophical partner, a drug-addled gangster's moll and a washed-up boxer converge in this sprawling, comedic crime caper. Their adventures unfurl in three stories that ingeniously trip back and forth in time.");
             usm.setMovie_title("Pulp Fiction");
             usm.setMovie_poster("/dM2w364MScsjFf8pfMbaWUcWrR.jpg");
             realm.commitTransaction();
         } finally { realm.close(); }
        realm = Realm.getDefaultInstance();
          try {
              realm.beginTransaction();
              UserRatesMovie usm = realm.createObject(UserRatesMovie.class);
              usm.setUserId("3c5303e9-0b5e-493a-98e8-184893dbb261");
              usm.setMovieId("372058");
              usm.setRating(4);
              usm.setDateAndTime(new Date());
              usm.setMovie_release("2016-08-26");
              usm.setMovie_description("High schoolers Mitsuha and Taki are complete strangers living separate lives. But one night, they suddenly switch places. Mitsuha wakes up in Taki’s body, and he in hers. This bizarre occurrence continues to happen randomly, and the two must adjust their lives around each other.");
              usm.setMovie_title("Your Name.");
              usm.setMovie_poster("/xq1Ugd62d23K2knRUx6xxuALTZB.jpg");
              realm.commitTransaction();
          } finally { realm.close(); }
        realm = Realm.getDefaultInstance();
          try {
              realm.beginTransaction();
              UserRatesMovie usm = realm.createObject(UserRatesMovie.class);
              usm.setUserId("3c5303e9-0b5e-493a-98e8-184893dbb261");
              usm.setMovieId("278");
              usm.setRating(4);
              usm.setDateAndTime(new Date());
              usm.setMovie_release("1994-09-23");
              usm.setMovie_description("Framed in the 1940s for the double murder of his wife and her lover, upstanding banker Andy Dufresne begins a new life at the Shawshank prison, where he puts his accounting skills to work for an amoral warden. During his long stretch in prison, Dufresne comes to be admired by the other inmates -- including an older prisoner named Red -- for his integrity and unquenchable sense of hope.");
              usm.setMovie_title("The Shawshank Redemption");
              usm.setMovie_poster("/9O7gLzmreU0nGkIB6K3BsJbzvNv.jpg");
              realm.commitTransaction();
          } finally { realm.close(); }
        realm = Realm.getDefaultInstance();
        //bibou
        try {
            realm.beginTransaction();
            UserRatesMovie usm = realm.createObject(UserRatesMovie.class);
            usm.setUserId("402ab71d-02af-4c93-9f43-7ed21cc3acd8");
            usm.setMovieId("238");
            usm.setRating(3);
            usm.setDateAndTime(new Date());
            usm.setMovie_release("1972-03-14");
            usm.setMovie_description("Spanning the years 1945 to 1955, a chronicle of the fictional Italian-American Corleone crime family. When organized crime family patriarch, Vito Corleone barely survives an attempt on his life, his youngest son, Michael steps in to take care of the would-be killers, launching a campaign of bloody revenge.");
            usm.setMovie_title("The Godfather");
            usm.setMovie_poster("/rPdtLWNsZmAtoZl9PK7S2wE3qiS.jpg");
            realm.commitTransaction();
        } finally { realm.close(); }
        realm = Realm.getDefaultInstance();
         try {
             realm.beginTransaction();
             UserRatesMovie usm = realm.createObject(UserRatesMovie.class);
             usm.setUserId("402ab71d-02af-4c93-9f43-7ed21cc3acd8");
             usm.setMovieId("680");
             usm.setRating(1);
             usm.setDateAndTime(new Date());
             usm.setMovie_release("1994-09-10");
             usm.setMovie_description("A burger-loving hit man, his philosophical partner, a drug-addled gangster's moll and a washed-up boxer converge in this sprawling, comedic crime caper. Their adventures unfurl in three stories that ingeniously trip back and forth in time.");
             usm.setMovie_title("Pulp Fiction");
             usm.setMovie_poster("/dM2w364MScsjFf8pfMbaWUcWrR.jpg");
             realm.commitTransaction();
         } finally { realm.close(); }
        realm = Realm.getDefaultInstance();
         try {
             realm.beginTransaction();
             UserRatesMovie usm = realm.createObject(UserRatesMovie.class);
             usm.setUserId("402ab71d-02af-4c93-9f43-7ed21cc3acd8");
             usm.setMovieId("372058");
             usm.setRating(2);
             usm.setDateAndTime(new Date());
             usm.setMovie_release("2016-08-26");
             usm.setMovie_description("High schoolers Mitsuha and Taki are complete strangers living separate lives. But one night, they suddenly switch places. Mitsuha wakes up in Taki’s body, and he in hers. This bizarre occurrence continues to happen randomly, and the two must adjust their lives around each other.");
             usm.setMovie_title("Your Name.");
             usm.setMovie_poster("/xq1Ugd62d23K2knRUx6xxuALTZB.jpg");
             realm.commitTransaction();
         } finally { realm.close(); }
        realm = Realm.getDefaultInstance();
         try {
             realm.beginTransaction();
             UserRatesMovie usm = realm.createObject(UserRatesMovie.class);
             usm.setUserId("402ab71d-02af-4c93-9f43-7ed21cc3acd8");
             usm.setMovieId("278");
             usm.setRating(3);
             usm.setDateAndTime(new Date());
             usm.setMovie_release("1994-09-23");
             usm.setMovie_description("Framed in the 1940s for the double murder of his wife and her lover, upstanding banker Andy Dufresne begins a new life at the Shawshank prison, where he puts his accounting skills to work for an amoral warden. During his long stretch in prison, Dufresne comes to be admired by the other inmates -- including an older prisoner named Red -- for his integrity and unquenchable sense of hope.");
             usm.setMovie_title("The Shawshank Redemption");
             usm.setMovie_poster("/9O7gLzmreU0nGkIB6K3BsJbzvNv.jpg");
             realm.commitTransaction();
         } finally { realm.close(); }
        realm = Realm.getDefaultInstance();
         try {
             realm.beginTransaction();
             UserRatesMovie usm = realm.createObject(UserRatesMovie.class);
             usm.setUserId("402ab71d-02af-4c93-9f43-7ed21cc3acd8");
             usm.setMovieId("637");
             usm.setRating(3);
             usm.setDateAndTime(new Date());
             usm.setMovie_release("1997-12-20");
             usm.setMovie_description("A touching story of an Italian book seller of Jewish ancestry who lives in his own little fairy tale. His creative and happy life would come to an abrupt halt when his entire family is deported to a concentration camp during World War II. While locked up he tries to convince his son that the whole thing is just a game.");
             usm.setMovie_title("Life Is Beautiful");
             usm.setMovie_poster("/f7DImXDebOs148U4uPjI61iDvaK.jpg");
             realm.commitTransaction();
         } finally { realm.close(); }
        realm = Realm.getDefaultInstance();
        //zaze
        try {
            realm.beginTransaction();
            UserRatesMovie usm = realm.createObject(UserRatesMovie.class);
            usm.setUserId("3f4d09f8-d499-43ab-a24f-86ccb3d546cb");
            usm.setMovieId("238");
            usm.setRating(4);
            usm.setDateAndTime(new Date());
            usm.setMovie_release("1972-03-14");
            usm.setMovie_description("Spanning the years 1945 to 1955, a chronicle of the fictional Italian-American Corleone crime family. When organized crime family patriarch, Vito Corleone barely survives an attempt on his life, his youngest son, Michael steps in to take care of the would-be killers, launching a campaign of bloody revenge.");
            usm.setMovie_title("The Godfather");
            usm.setMovie_poster("/rPdtLWNsZmAtoZl9PK7S2wE3qiS.jpg");
            realm.commitTransaction();
        } finally { realm.close(); }
        realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();
            UserRatesMovie usm = realm.createObject(UserRatesMovie.class);
            usm.setUserId("3f4d09f8-d499-43ab-a24f-86ccb3d546cb");
            usm.setMovieId("680");
            usm.setRating(3);
            usm.setDateAndTime(new Date());
            usm.setMovie_release("1994-09-10");
            usm.setMovie_description("A burger-loving hit man, his philosophical partner, a drug-addled gangster's moll and a washed-up boxer converge in this sprawling, comedic crime caper. Their adventures unfurl in three stories that ingeniously trip back and forth in time.");
            usm.setMovie_title("Pulp Fiction");
            usm.setMovie_poster("/dM2w364MScsjFf8pfMbaWUcWrR.jpg");
            realm.commitTransaction();
        } finally { realm.close(); }
        realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();
            UserRatesMovie usm = realm.createObject(UserRatesMovie.class);
            usm.setUserId("3f4d09f8-d499-43ab-a24f-86ccb3d546cb");
            usm.setMovieId("372058");
            usm.setRating(4);
            usm.setDateAndTime(new Date());
            usm.setMovie_release("2016-08-26");
            usm.setMovie_description("High schoolers Mitsuha and Taki are complete strangers living separate lives. But one night, they suddenly switch places. Mitsuha wakes up in Taki’s body, and he in hers. This bizarre occurrence continues to happen randomly, and the two must adjust their lives around each other.");
            usm.setMovie_title("Your Name.");
            usm.setMovie_poster("/xq1Ugd62d23K2knRUx6xxuALTZB.jpg");
            realm.commitTransaction();
        } finally { realm.close(); }
        realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();
            UserRatesMovie usm = realm.createObject(UserRatesMovie.class);
            usm.setUserId("3f4d09f8-d499-43ab-a24f-86ccb3d546cb");
            usm.setMovieId("278");
            usm.setRating(3);
            usm.setDateAndTime(new Date());
            usm.setMovie_release("1994-09-23");
            usm.setMovie_description("Framed in the 1940s for the double murder of his wife and her lover, upstanding banker Andy Dufresne begins a new life at the Shawshank prison, where he puts his accounting skills to work for an amoral warden. During his long stretch in prison, Dufresne comes to be admired by the other inmates -- including an older prisoner named Red -- for his integrity and unquenchable sense of hope.");
            usm.setMovie_title("The Shawshank Redemption");
            usm.setMovie_poster("/9O7gLzmreU0nGkIB6K3BsJbzvNv.jpg");
            realm.commitTransaction();
        } finally { realm.close(); }
        realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();
            UserRatesMovie usm = realm.createObject(UserRatesMovie.class);
            usm.setUserId("3f4d09f8-d499-43ab-a24f-86ccb3d546cb");
            usm.setMovieId("637");
            usm.setRating(5);
            usm.setDateAndTime(new Date());
            usm.setMovie_release("1997-12-20");
            usm.setMovie_description("A touching story of an Italian book seller of Jewish ancestry who lives in his own little fairy tale. His creative and happy life would come to an abrupt halt when his entire family is deported to a concentration camp during World War II. While locked up he tries to convince his son that the whole thing is just a game.");
            usm.setMovie_title("Life Is Beautiful");
            usm.setMovie_poster("/f7DImXDebOs148U4uPjI61iDvaK.jpg");
            realm.commitTransaction();
        } finally { realm.close(); }
        realm = Realm.getDefaultInstance();
        //tinton
        try {
            realm.beginTransaction();
            UserRatesMovie usm = realm.createObject(UserRatesMovie.class);
            usm.setUserId("4343ad42-d303-42ca-a324-c5c0b520bd8f");
            usm.setMovieId("238");
            usm.setRating(3);
            usm.setDateAndTime(new Date());
            usm.setMovie_release("1972-03-14");
            usm.setMovie_description("Spanning the years 1945 to 1955, a chronicle of the fictional Italian-American Corleone crime family. When organized crime family patriarch, Vito Corleone barely survives an attempt on his life, his youngest son, Michael steps in to take care of the would-be killers, launching a campaign of bloody revenge.");
            usm.setMovie_title("The Godfather");
            usm.setMovie_poster("/rPdtLWNsZmAtoZl9PK7S2wE3qiS.jpg");
            realm.commitTransaction();
        } finally { realm.close(); }
        realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();
            UserRatesMovie usm = realm.createObject(UserRatesMovie.class);
            usm.setUserId("4343ad42-d303-42ca-a324-c5c0b520bd8f");
            usm.setMovieId("680");
            usm.setRating(3);
            usm.setDateAndTime(new Date());
            usm.setMovie_release("1994-09-10");
            usm.setMovie_description("A burger-loving hit man, his philosophical partner, a drug-addled gangster's moll and a washed-up boxer converge in this sprawling, comedic crime caper. Their adventures unfurl in three stories that ingeniously trip back and forth in time.");
            usm.setMovie_title("Pulp Fiction");
            usm.setMovie_poster("/dM2w364MScsjFf8pfMbaWUcWrR.jpg");
            realm.commitTransaction();
        } finally { realm.close(); }
        realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();
            UserRatesMovie usm = realm.createObject(UserRatesMovie.class);
            usm.setUserId("4343ad42-d303-42ca-a324-c5c0b520bd8f");
            usm.setMovieId("372058");
            usm.setRating(1);
            usm.setDateAndTime(new Date());
            usm.setMovie_release("2016-08-26");
            usm.setMovie_description("High schoolers Mitsuha and Taki are complete strangers living separate lives. But one night, they suddenly switch places. Mitsuha wakes up in Taki’s body, and he in hers. This bizarre occurrence continues to happen randomly, and the two must adjust their lives around each other.");
            usm.setMovie_title("Your Name.");
            usm.setMovie_poster("/xq1Ugd62d23K2knRUx6xxuALTZB.jpg");
            realm.commitTransaction();
        } finally { realm.close(); }
        realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();
            UserRatesMovie usm = realm.createObject(UserRatesMovie.class);
            usm.setUserId("4343ad42-d303-42ca-a324-c5c0b520bd8f");
            usm.setMovieId("278");
            usm.setRating(5);
            usm.setDateAndTime(new Date());
            usm.setMovie_release("1994-09-23");
            usm.setMovie_description("Framed in the 1940s for the double murder of his wife and her lover, upstanding banker Andy Dufresne begins a new life at the Shawshank prison, where he puts his accounting skills to work for an amoral warden. During his long stretch in prison, Dufresne comes to be admired by the other inmates -- including an older prisoner named Red -- for his integrity and unquenchable sense of hope.");
            usm.setMovie_title("The Shawshank Redemption");
            usm.setMovie_poster("/9O7gLzmreU0nGkIB6K3BsJbzvNv.jpg");
            realm.commitTransaction();
        } finally { realm.close(); }
        realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();
            UserRatesMovie usm = realm.createObject(UserRatesMovie.class);
            usm.setUserId("4343ad42-d303-42ca-a324-c5c0b520bd8f");
            usm.setMovieId("637");
            usm.setRating(4);
            usm.setDateAndTime(new Date());
            usm.setMovie_release("1997-12-20");
            usm.setMovie_description("A touching story of an Italian book seller of Jewish ancestry who lives in his own little fairy tale. His creative and happy life would come to an abrupt halt when his entire family is deported to a concentration camp during World War II. While locked up he tries to convince his son that the whole thing is just a game.");
            usm.setMovie_title("Life Is Beautiful");
            usm.setMovie_poster("/f7DImXDebOs148U4uPjI61iDvaK.jpg");
            realm.commitTransaction();
        } finally { realm.close(); }
        realm = Realm.getDefaultInstance();
        //ezziz
        try {
            realm.beginTransaction();
            UserRatesMovie usm = realm.createObject(UserRatesMovie.class);
            usm.setUserId("cbed9f61-b7f0-4a0d-ab4a-953e22c25473");
            usm.setMovieId("238");
            usm.setRating(1);
            usm.setDateAndTime(new Date());
            usm.setMovie_release("1972-03-14");
            usm.setMovie_description("Spanning the years 1945 to 1955, a chronicle of the fictional Italian-American Corleone crime family. When organized crime family patriarch, Vito Corleone barely survives an attempt on his life, his youngest son, Michael steps in to take care of the would-be killers, launching a campaign of bloody revenge.");
            usm.setMovie_title("The Godfather");
            usm.setMovie_poster("/rPdtLWNsZmAtoZl9PK7S2wE3qiS.jpg");
            realm.commitTransaction();
        } finally { realm.close(); }
        realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();
            UserRatesMovie usm = realm.createObject(UserRatesMovie.class);
            usm.setUserId("cbed9f61-b7f0-4a0d-ab4a-953e22c25473");
            usm.setMovieId("680");
            usm.setRating(5);
            usm.setDateAndTime(new Date());
            usm.setMovie_release("1994-09-10");
            usm.setMovie_description("A burger-loving hit man, his philosophical partner, a drug-addled gangster's moll and a washed-up boxer converge in this sprawling, comedic crime caper. Their adventures unfurl in three stories that ingeniously trip back and forth in time.");
            usm.setMovie_title("Pulp Fiction");
            usm.setMovie_poster("/dM2w364MScsjFf8pfMbaWUcWrR.jpg");
            realm.commitTransaction();
        } finally { realm.close(); }
        realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();
            UserRatesMovie usm = realm.createObject(UserRatesMovie.class);
            usm.setUserId("cbed9f61-b7f0-4a0d-ab4a-953e22c25473");
            usm.setMovieId("372058");
            usm.setRating(5);
            usm.setDateAndTime(new Date());
            usm.setMovie_release("2016-08-26");
            usm.setMovie_description("High schoolers Mitsuha and Taki are complete strangers living separate lives. But one night, they suddenly switch places. Mitsuha wakes up in Taki’s body, and he in hers. This bizarre occurrence continues to happen randomly, and the two must adjust their lives around each other.");
            usm.setMovie_title("Your Name.");
            usm.setMovie_poster("/xq1Ugd62d23K2knRUx6xxuALTZB.jpg");
            realm.commitTransaction();
        } finally { realm.close(); }
        realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();
            UserRatesMovie usm = realm.createObject(UserRatesMovie.class);
            usm.setUserId("cbed9f61-b7f0-4a0d-ab4a-953e22c25473");
            usm.setMovieId("278");
            usm.setRating(2);
            usm.setDateAndTime(new Date());
            usm.setMovie_release("1994-09-23");
            usm.setMovie_description("Framed in the 1940s for the double murder of his wife and her lover, upstanding banker Andy Dufresne begins a new life at the Shawshank prison, where he puts his accounting skills to work for an amoral warden. During his long stretch in prison, Dufresne comes to be admired by the other inmates -- including an older prisoner named Red -- for his integrity and unquenchable sense of hope.");
            usm.setMovie_title("The Shawshank Redemption");
            usm.setMovie_poster("/9O7gLzmreU0nGkIB6K3BsJbzvNv.jpg");
            realm.commitTransaction();
        } finally { realm.close(); }
        realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();
            UserRatesMovie usm = realm.createObject(UserRatesMovie.class);
            usm.setUserId("cbed9f61-b7f0-4a0d-ab4a-953e22c25473");
            usm.setMovieId("637");
            usm.setRating(1);
            usm.setDateAndTime(new Date());
            usm.setMovie_release("1997-12-20");
            usm.setMovie_description("A touching story of an Italian book seller of Jewish ancestry who lives in his own little fairy tale. His creative and happy life would come to an abrupt halt when his entire family is deported to a concentration camp during World War II. While locked up he tries to convince his son that the whole thing is just a game.");
            usm.setMovie_title("Life Is Beautiful");
            usm.setMovie_poster("/f7DImXDebOs148U4uPjI61iDvaK.jpg");
            realm.commitTransaction();
        }finally { realm.close(); }
        realm = Realm.getDefaultInstance();
    }
}
