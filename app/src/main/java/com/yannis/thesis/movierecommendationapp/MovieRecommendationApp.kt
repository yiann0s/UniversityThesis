package com.yannis.thesis.movierecommendationapp

import android.app.Application
import android.util.Log
import com.yannis.thesis.movierecommendationapp.activities.BaseActivity
import com.yannis.thesis.movierecommendationapp.api.APIService
import com.yannis.thesis.movierecommendationapp.models.Movie
import com.yannis.thesis.movierecommendationapp.models.MovieRecommendedForUser
import com.yannis.thesis.movierecommendationapp.models.User
import com.yannis.thesis.movierecommendationapp.models.UserRatesMovie
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmResults
import io.realm.Sort
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Date
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.MutableMap

/**
 * Created by yiannos on 12-Feb-18.
 */
class MovieRecommendationApp : Application() {
    var lastActivity: BaseActivity? = null

    var API_BASE_URL: String = "https://api.themoviedb.org/3/"
    var loggedInUserId: String? = null
    private var realm: Realm? = null
    val SIMILARITY_PILLOW: Double = 0.5
    val PREDICTION_PILLOW: Double = 3.0

    private var client: APIService? = null


    var call: Call<Movie?>? = null

    var retrofit: Retrofit? = null

    var builder: Retrofit.Builder? = null

    var httpClient: OkHttpClient.Builder? = null

    var logging: HttpLoggingInterceptor? = null

    override fun onCreate() {
        super.onCreate()
        instance = this

        Realm.init(this)
        //
//        //.deleteRealmIfMigrationNeeded() -> This means that if you are in the middle of development
//        // and changing your schema
//        //frequently—and it’s all right to lose all your data—you can delete your .realm file on
//        // disk instead of writing a migration. This can be helpful when tinkering with models
//        // early in the development cycle of your app.
        val config = RealmConfiguration.Builder()
            .name("myrealmDB.realm")
            .deleteRealmIfMigrationNeeded()
            .build()
        Realm.setDefaultConfiguration(config)

        realm = Realm.getDefaultInstance()


        httpClient = OkHttpClient.Builder()

        builder =
            Retrofit.Builder()
                .baseUrl(API_BASE_URL)
                .addConverterFactory(
                    GsonConverterFactory.create()
                )

        retrofit =
            builder!!
                .client(
                    httpClient!!.build()
                )
                .build()

        //addUsers();
        //addUserRatesMovie();
        //showRecommendedMoviesFosActiveUser();
        showAllUsers()
        MovieRecommendationAlgorithm()
        //showAllUserRatedMovies();
    }

    fun showRecommendedMoviesFosActiveUser() {
        val activeUserID = "3c5303e9-0b5e-493a-98e8-184893dbb261"
        val queryRecommendation = realm!!
            .where<MovieRecommendedForUser?>(MovieRecommendedForUser::class.java)
            .equalTo("userId", activeUserID)
            .sort("predictedRating", Sort.DESCENDING)
        val movieRecommendations: RealmResults<MovieRecommendedForUser> =
            queryRecommendation.findAll()
        for (movieRecommendedForUser in movieRecommendations) {
            Log.d(
                "MovieApp",
                ("Recommended movie for user is " + movieRecommendedForUser.getMovie_title()
                        + " rating " + movieRecommendedForUser.getPredictedRating())
            )
        }
    }

    fun showAllUsers() {
        val queryUsers = realm!!
            .where<User?>(User::class.java)
        val users: RealmResults<User> = queryUsers.findAll()
        for (u in users) {
            Log.d(
                "MovieApp",
                "User has mail " + u.getEmail() + " and id " + u.getId() + " and password " + u.getPassword()
            )
        }
    }

    fun showAllUserRatedMovies() {
        val userList = ArrayList<String?>()
        val queryUsers = realm!!
            .where<User?>(User::class.java)
        val users: RealmResults<User> = queryUsers.findAll()
        for (u in users) {
            Log.d("MovieApp", "User " + u.getEmail())
            val queryUserRatesMovie = realm!!
                .where<UserRatesMovie?>(UserRatesMovie::class.java)

            val userRatesMovies: RealmResults<UserRatesMovie> = queryUserRatesMovie
                .equalTo("userId", u.getId())
                .findAll()
            for (urm in userRatesMovies) {
                Log.d("MovieApp", "has rated" + urm.getMovie_title() + " with a " + urm.getRating())
            }
        }
    }

    //h synarthsh afth dexetai to userID tou xrhsth kai kanei populate ton pinaka
    // sth vash dedomenwn me tis tainies pou tha aresoun sto xrhsth
    fun MovieRecommendationAlgorithm() {
        // bloper@gmail.com einai o active user mas
        // to id tou bloper@gmail.com  einai 75ed56fd-1f80-47ff-819b-fe35be3bc85e
        val activeUserID = "3c5303e9-0b5e-493a-98e8-184893dbb261"
        val users = realm!!.where<User?>(User::class.java)
            .notEqualTo("id", activeUserID)
            .findAll()

        val neightboursList = ArrayList<String?>()
        for (user in users) {
            Log.d(
                "MovieApp",
                "Similarity of active user and user " + user.getEmail() + " is " + similarity(
                    activeUserID,
                    user.getId()
                )
            )
            if (similarity(activeUserID, user.getId()) >= SIMILARITY_PILLOW) {
                neightboursList.add(user.getId())
            }
        }

        /**////////////////// */
        val movieMapActiveUser: MutableMap<String?, Double?> = HashMap<String?, Double?>()
        val notRatedMoviesIds = ArrayList<String>()

        val activeUserResults = realm!!.where<UserRatesMovie?>(UserRatesMovie::class.java)
            .equalTo("userId", activeUserID)
            .findAll()
        for (activeUserRatesMovie in activeUserResults) {
            movieMapActiveUser.put(
                activeUserRatesMovie.getMovieId(),
                activeUserRatesMovie.getRating()
            )
        }
        //tha paroume oles tis tainies pou den exei vathmologhsei o active User
        val movieIds = realm!!.where<UserRatesMovie?>(UserRatesMovie::class.java)
            .findAll()
        var currentMovieId: String
        for (userRatesMovie in movieIds) {
            currentMovieId = userRatesMovie.getMovieId()
            //an den einai mesa sth lista twn ids twn tainian pou exei vathmologhsei o active user
            if (!(movieMapActiveUser.containsKey(currentMovieId)) && (!(notRatedMoviesIds.contains(
                    currentMovieId
                )))
            ) {
                notRatedMoviesIds.add(currentMovieId)
            }
        }
        //gia oles tis tainies pou den exei o xrhsths akomh vathmologhsei
        for (i in notRatedMoviesIds.indices) {
            currentMovieId = notRatedMoviesIds.get(i)
            prediction(activeUserID, currentMovieId, neightboursList)
        }
    }

    fun prediction(
        activeUserId: String?,
        notYetRatedMovieId: String,
        neightbours: ArrayList<String?>
    ) {
        val activeAVG = avgRating(activeUserId)
        var A = 0.0
        var B = 0.0
        for (i in neightbours.indices) {
            avgRating(neightbours.get(i))
            A = A + similarity(activeUserId, neightbours.get(i)) * (getUser_i_MovieRating(
                neightbours.get(i),
                notYetRatedMovieId
            ) - avgRating(neightbours.get(i)))
            B = B + similarity(activeUserId, neightbours.get(i))
        }
        val prediction = activeAVG + A / B
        //an h provlepomenh vathmologia den einai panw apo to katwfli pou exoume orisei
        // ,de xreiazetai na apothikeftei sth vash dedomenwn
        Log.d("MovieApp", "prediction of movie id " + notYetRatedMovieId + " is " + prediction)
        if (prediction < PREDICTION_PILLOW) {
            return
        }
        //        Log.d("MovieApp","prediction of movie id " + notYetRatedMovieId + " is " + prediction);
        val movieId: Int = notYetRatedMovieId.toInt()
        client = retrofit!!.create<APIService>(APIService::class.java)
        call = client!!.getMovieDetails(movieId, apiKey)
        call!!.enqueue(object : Callback<Movie?> {
            override fun onResponse(call: Call<Movie?>, response: Response<Movie?>) {
                val statusCode = response.code()
                if (!response.isSuccessful()) {
                    Log.d("MovieApp", "unsuccessful w status" + statusCode.toString())
                } else if (response.isSuccessful()) {
                    val m = response.body()
                    //an h tainia einai hdh stis proteinomenew gia to xrhsth
                    //thn diagrafoume apo thn database gia na thn antikatasthsoume me th neoterh timh ths
                    if (!movieIsUnique(notYetRatedMovieId, activeUserId)) {
                        Log.d("MovieApp", "movie exists, so im gonna first delete")
                        deleteMovie(notYetRatedMovieId, activeUserId)
                    } //alliws dhmirgoume antikeeimeno tak ito eisagoume sth vash mas

                    Log.d("MovieApp", "adding recommeneed movie")
                    realm!!.beginTransaction()
                    val movieRecommendedForUser =
                        realm!!.createObject<MovieRecommendedForUser>(MovieRecommendedForUser::class.java)
                    movieRecommendedForUser.setMovieId(notYetRatedMovieId)
                    movieRecommendedForUser.setPredictedRating(prediction)
                    movieRecommendedForUser.setUserId(activeUserId)
                    movieRecommendedForUser.setDateAndTime(Date())
                    movieRecommendedForUser.setMovie_description(m!!.getOverview())
                    movieRecommendedForUser.setMovie_poster(m.getPosterPath())
                    movieRecommendedForUser.setMovie_release(m.getReleaseDate())
                    movieRecommendedForUser.setMovie_title(m.getTitle())
                    realm!!.commitTransaction()
                }
            }

            override fun onFailure(call: Call<Movie?>, t: Throwable) {
                Log.d("MovieApp", "Failure at getMoviedetails callback:" + t.message.toString())
            }
        })
    }

    private fun movieIsUnique(movieId: String?, activeUserId: String?): Boolean {
        val query = realm!!.where<MovieRecommendedForUser?>(MovieRecommendedForUser::class.java)
            .equalTo("userId", activeUserId)
            .and()
            .equalTo("movieId", movieId)
        val result = query.findAll()
        return result.size == 0
    }

    fun deleteMovie(movieId: String?, activeUserId: String?) {
        val query = realm!!.where<MovieRecommendedForUser?>(MovieRecommendedForUser::class.java)
            .equalTo("userId", activeUserId)
            .and()
            .equalTo("movieId", movieId)
        val result = query.findAll()
        // All changes to data must happen in a transaction
        realm!!.executeTransaction(object : Realm.Transaction {
            override fun execute(realm: Realm) {
                // Delete all matches
                result.deleteAllFromRealm()
            }
        })
    }

    fun getUser_i_MovieRating(userId: String?, movieId: String?): Double {
        val userResults = realm!!.where<UserRatesMovie?>(UserRatesMovie::class.java)
            .equalTo("userId", userId)
            .and()
            .equalTo("movieId", movieId)
            .findFirst()
        return userResults!!.getRating().toDouble()
    }

    fun similarity(activeUserID: String?, user_i: String?): Double {
        val helperA: MutableMap<String?, Double?> = HashMap<String?, Double?>()

        val helperB: MutableMap<String?, Double?> = HashMap<String?, Double?>()
        val sameMoviesIds = ArrayList<String?>()
        val activeUserAVG = avgRating(activeUserID)
        var user_i_AVG = 0.0

        val activeUserResults = realm!!.where<UserRatesMovie?>(UserRatesMovie::class.java)
            .equalTo("userId", activeUserID)
            .findAll()
        val user_i_Results = realm!!.where<UserRatesMovie?>(UserRatesMovie::class.java)
            .equalTo("userId", user_i)
            .findAll()
        for (activeUserRatesMovie in activeUserResults) {
            helperA.put(activeUserRatesMovie.getMovieId(), activeUserRatesMovie.getRating())
        }

        for (user_i_RatesMovie in user_i_Results) {
            val currentMovieId = user_i_RatesMovie.getMovieId()
            val currentMovieRating = user_i_RatesMovie.getRating().toDouble()
            //an vrhkame koinh tainia tou active user kai tou I xrhsth
            // tha krathsoume to id ths kai th vathmologia ths sth lista
            if (helperA.containsKey(currentMovieId)) {
                helperB.put(currentMovieId, currentMovieRating)
                user_i_AVG = user_i_AVG + currentMovieRating
                sameMoviesIds.add(currentMovieId) //lista pou kratame ta id twn koinwn
            }
        }
        user_i_AVG = user_i_AVG / helperB.size

        var K_sum = 0.0
        var L_sum = 0.0
        var M_sum = 0.0
        for (i in sameMoviesIds.indices) {
            val currentId = sameMoviesIds.get(i)
            // h vathmologia ths tainias me to current id gia ton active user A
            val r_A = helperA.get(currentId)
            // h vathmologia ths tainias me to current id gia ton user i
            val r_i = helperB.get(currentId)
            K_sum = K_sum + (r_A!! - activeUserAVG) * (r_i!! - user_i_AVG)
            L_sum = L_sum + (r_A - activeUserAVG).pow(2.0)
            M_sum = M_sum + (r_i - user_i_AVG).pow(2.0)
        }
        return (K_sum / (sqrt(L_sum) * sqrt(M_sum)))
    }

    //genikhs xrhshs synrthash gia ypologismou mesou orou
    fun avgRating(userId: String?): Double {
        var userAVG = 0.0
        val userResults = realm!!.where<UserRatesMovie?>(UserRatesMovie::class.java)
            .equalTo("userId", userId)
            .findAll()
        for (userRatesMovie in userResults) {
            userAVG = userAVG + userRatesMovie.getRating()
        }
        return (userAVG / userResults.size)
    }


    fun addUsers() {
        try {
            realm!!.beginTransaction()
            val user = realm!!.createObject<User>(
                User::class.java,
                "3c5303e9-0b5e-493a-98e8-184893dbb261"
            )
            user.setEmail("bloper@gmail.com")
            user.setPassword("123456")
            Log.d("MovieApp", "bloper  id: " + user.getId())
            realm!!.commitTransaction()
        } finally {
            realm!!.close()
        }
        realm = Realm.getDefaultInstance()
        try {
            realm!!.beginTransaction()
            val user = realm!!.createObject<User>(
                User::class.java,
                "402ab71d-02af-4c93-9f43-7ed21cc3acd8"
            )
            user.setEmail("bibou@hotmail.com")
            user.setPassword("234567")
            Log.d("MovieApp", "bibou  id: " + user.getId())
            realm!!.commitTransaction()
        } finally {
            realm!!.close()
        }
        realm = Realm.getDefaultInstance()
        try {
            realm!!.beginTransaction()
            val user = realm!!.createObject<User>(
                User::class.java,
                "3f4d09f8-d499-43ab-a24f-86ccb3d546cb"
            )
            user.setEmail("zaze@gmail.com")
            user.setPassword("345678")
            Log.d("MovieApp", "zaze  id: " + user.getId())
            realm!!.commitTransaction()
        } finally {
            realm!!.close()
        }
        realm = Realm.getDefaultInstance()
        try {
            realm!!.beginTransaction()
            val user = realm!!.createObject<User>(
                User::class.java,
                "4343ad42-d303-42ca-a324-c5c0b520bd8f"
            )
            user.setEmail("tinton@hotmail.gr")
            user.setPassword("456789")
            Log.d("MovieApp", "tinton  id: " + user.getId())
            realm!!.commitTransaction()
        } finally {
            realm!!.close()
        }
        realm = Realm.getDefaultInstance()
        try {
            realm!!.beginTransaction()
            val user = realm!!.createObject<User>(
                User::class.java,
                "cbed9f61-b7f0-4a0d-ab4a-953e22c25473"
            )
            user.setEmail("ezziz@yahoo.net")
            user.setPassword("567890")
            Log.d("MovieApp", "ezziz  id: " + user.getId())
            realm!!.commitTransaction()
        } finally {
            realm!!.close()
        }
    }

    fun addUserRatesMovie() {
        //bloper
        realm = Realm.getDefaultInstance()
        try {
            realm!!.beginTransaction()
            val usm = realm!!.createObject<UserRatesMovie>(UserRatesMovie::class.java)
            usm.setUserId("3c5303e9-0b5e-493a-98e8-184893dbb261")
            usm.setMovieId("238")
            usm.setRating(5)
            usm.setDateAndTime(Date())
            usm.setMovie_release("1972-03-14")
            usm.setMovie_description("Spanning the years 1945 to 1955, a chronicle of the fictional Italian-American Corleone crime family. When organized crime family patriarch, Vito Corleone barely survives an attempt on his life, his youngest son, Michael steps in to take care of the would-be killers, launching a campaign of bloody revenge.")
            usm.setMovie_title("The Godfather")
            usm.setMovie_poster("/rPdtLWNsZmAtoZl9PK7S2wE3qiS.jpg")
            realm!!.commitTransaction()
        } finally {
            realm!!.close()
        }
        realm = Realm.getDefaultInstance()
        try {
            realm!!.beginTransaction()
            val usm = realm!!.createObject<UserRatesMovie>(UserRatesMovie::class.java)
            usm.setUserId("3c5303e9-0b5e-493a-98e8-184893dbb261")
            usm.setMovieId("680")
            usm.setRating(3)
            usm.setDateAndTime(Date())
            usm.setMovie_release("1994-09-10")
            usm.setMovie_description("A burger-loving hit man, his philosophical partner, a drug-addled gangster's moll and a washed-up boxer converge in this sprawling, comedic crime caper. Their adventures unfurl in three stories that ingeniously trip back and forth in time.")
            usm.setMovie_title("Pulp Fiction")
            usm.setMovie_poster("/dM2w364MScsjFf8pfMbaWUcWrR.jpg")
            realm!!.commitTransaction()
        } finally {
            realm!!.close()
        }
        realm = Realm.getDefaultInstance()
        try {
            realm!!.beginTransaction()
            val usm = realm!!.createObject<UserRatesMovie>(UserRatesMovie::class.java)
            usm.setUserId("3c5303e9-0b5e-493a-98e8-184893dbb261")
            usm.setMovieId("372058")
            usm.setRating(4)
            usm.setDateAndTime(Date())
            usm.setMovie_release("2016-08-26")
            usm.setMovie_description("High schoolers Mitsuha and Taki are complete strangers living separate lives. But one night, they suddenly switch places. Mitsuha wakes up in Taki’s body, and he in hers. This bizarre occurrence continues to happen randomly, and the two must adjust their lives around each other.")
            usm.setMovie_title("Your Name.")
            usm.setMovie_poster("/xq1Ugd62d23K2knRUx6xxuALTZB.jpg")
            realm!!.commitTransaction()
        } finally {
            realm!!.close()
        }
        realm = Realm.getDefaultInstance()
        try {
            realm!!.beginTransaction()
            val usm = realm!!.createObject<UserRatesMovie>(UserRatesMovie::class.java)
            usm.setUserId("3c5303e9-0b5e-493a-98e8-184893dbb261")
            usm.setMovieId("278")
            usm.setRating(4)
            usm.setDateAndTime(Date())
            usm.setMovie_release("1994-09-23")
            usm.setMovie_description("Framed in the 1940s for the double murder of his wife and her lover, upstanding banker Andy Dufresne begins a new life at the Shawshank prison, where he puts his accounting skills to work for an amoral warden. During his long stretch in prison, Dufresne comes to be admired by the other inmates -- including an older prisoner named Red -- for his integrity and unquenchable sense of hope.")
            usm.setMovie_title("The Shawshank Redemption")
            usm.setMovie_poster("/9O7gLzmreU0nGkIB6K3BsJbzvNv.jpg")
            realm!!.commitTransaction()
        } finally {
            realm!!.close()
        }
        realm = Realm.getDefaultInstance()
        //bibou
        try {
            realm!!.beginTransaction()
            val usm = realm!!.createObject<UserRatesMovie>(UserRatesMovie::class.java)
            usm.setUserId("402ab71d-02af-4c93-9f43-7ed21cc3acd8")
            usm.setMovieId("238")
            usm.setRating(3)
            usm.setDateAndTime(Date())
            usm.setMovie_release("1972-03-14")
            usm.setMovie_description("Spanning the years 1945 to 1955, a chronicle of the fictional Italian-American Corleone crime family. When organized crime family patriarch, Vito Corleone barely survives an attempt on his life, his youngest son, Michael steps in to take care of the would-be killers, launching a campaign of bloody revenge.")
            usm.setMovie_title("The Godfather")
            usm.setMovie_poster("/rPdtLWNsZmAtoZl9PK7S2wE3qiS.jpg")
            realm!!.commitTransaction()
        } finally {
            realm!!.close()
        }
        realm = Realm.getDefaultInstance()
        try {
            realm!!.beginTransaction()
            val usm = realm!!.createObject<UserRatesMovie>(UserRatesMovie::class.java)
            usm.setUserId("402ab71d-02af-4c93-9f43-7ed21cc3acd8")
            usm.setMovieId("680")
            usm.setRating(1)
            usm.setDateAndTime(Date())
            usm.setMovie_release("1994-09-10")
            usm.setMovie_description("A burger-loving hit man, his philosophical partner, a drug-addled gangster's moll and a washed-up boxer converge in this sprawling, comedic crime caper. Their adventures unfurl in three stories that ingeniously trip back and forth in time.")
            usm.setMovie_title("Pulp Fiction")
            usm.setMovie_poster("/dM2w364MScsjFf8pfMbaWUcWrR.jpg")
            realm!!.commitTransaction()
        } finally {
            realm!!.close()
        }
        realm = Realm.getDefaultInstance()
        try {
            realm!!.beginTransaction()
            val usm = realm!!.createObject<UserRatesMovie>(UserRatesMovie::class.java)
            usm.setUserId("402ab71d-02af-4c93-9f43-7ed21cc3acd8")
            usm.setMovieId("372058")
            usm.setRating(2)
            usm.setDateAndTime(Date())
            usm.setMovie_release("2016-08-26")
            usm.setMovie_description("High schoolers Mitsuha and Taki are complete strangers living separate lives. But one night, they suddenly switch places. Mitsuha wakes up in Taki’s body, and he in hers. This bizarre occurrence continues to happen randomly, and the two must adjust their lives around each other.")
            usm.setMovie_title("Your Name.")
            usm.setMovie_poster("/xq1Ugd62d23K2knRUx6xxuALTZB.jpg")
            realm!!.commitTransaction()
        } finally {
            realm!!.close()
        }
        realm = Realm.getDefaultInstance()
        try {
            realm!!.beginTransaction()
            val usm = realm!!.createObject<UserRatesMovie>(UserRatesMovie::class.java)
            usm.setUserId("402ab71d-02af-4c93-9f43-7ed21cc3acd8")
            usm.setMovieId("278")
            usm.setRating(3)
            usm.setDateAndTime(Date())
            usm.setMovie_release("1994-09-23")
            usm.setMovie_description("Framed in the 1940s for the double murder of his wife and her lover, upstanding banker Andy Dufresne begins a new life at the Shawshank prison, where he puts his accounting skills to work for an amoral warden. During his long stretch in prison, Dufresne comes to be admired by the other inmates -- including an older prisoner named Red -- for his integrity and unquenchable sense of hope.")
            usm.setMovie_title("The Shawshank Redemption")
            usm.setMovie_poster("/9O7gLzmreU0nGkIB6K3BsJbzvNv.jpg")
            realm!!.commitTransaction()
        } finally {
            realm!!.close()
        }
        realm = Realm.getDefaultInstance()
        try {
            realm!!.beginTransaction()
            val usm = realm!!.createObject<UserRatesMovie>(UserRatesMovie::class.java)
            usm.setUserId("402ab71d-02af-4c93-9f43-7ed21cc3acd8")
            usm.setMovieId("637")
            usm.setRating(3)
            usm.setDateAndTime(Date())
            usm.setMovie_release("1997-12-20")
            usm.setMovie_description("A touching story of an Italian book seller of Jewish ancestry who lives in his own little fairy tale. His creative and happy life would come to an abrupt halt when his entire family is deported to a concentration camp during World War II. While locked up he tries to convince his son that the whole thing is just a game.")
            usm.setMovie_title("Life Is Beautiful")
            usm.setMovie_poster("/f7DImXDebOs148U4uPjI61iDvaK.jpg")
            realm!!.commitTransaction()
        } finally {
            realm!!.close()
        }
        realm = Realm.getDefaultInstance()
        //zaze
        try {
            realm!!.beginTransaction()
            val usm = realm!!.createObject<UserRatesMovie>(UserRatesMovie::class.java)
            usm.setUserId("3f4d09f8-d499-43ab-a24f-86ccb3d546cb")
            usm.setMovieId("238")
            usm.setRating(4)
            usm.setDateAndTime(Date())
            usm.setMovie_release("1972-03-14")
            usm.setMovie_description("Spanning the years 1945 to 1955, a chronicle of the fictional Italian-American Corleone crime family. When organized crime family patriarch, Vito Corleone barely survives an attempt on his life, his youngest son, Michael steps in to take care of the would-be killers, launching a campaign of bloody revenge.")
            usm.setMovie_title("The Godfather")
            usm.setMovie_poster("/rPdtLWNsZmAtoZl9PK7S2wE3qiS.jpg")
            realm!!.commitTransaction()
        } finally {
            realm!!.close()
        }
        realm = Realm.getDefaultInstance()
        try {
            realm!!.beginTransaction()
            val usm = realm!!.createObject<UserRatesMovie>(UserRatesMovie::class.java)
            usm.setUserId("3f4d09f8-d499-43ab-a24f-86ccb3d546cb")
            usm.setMovieId("680")
            usm.setRating(3)
            usm.setDateAndTime(Date())
            usm.setMovie_release("1994-09-10")
            usm.setMovie_description("A burger-loving hit man, his philosophical partner, a drug-addled gangster's moll and a washed-up boxer converge in this sprawling, comedic crime caper. Their adventures unfurl in three stories that ingeniously trip back and forth in time.")
            usm.setMovie_title("Pulp Fiction")
            usm.setMovie_poster("/dM2w364MScsjFf8pfMbaWUcWrR.jpg")
            realm!!.commitTransaction()
        } finally {
            realm!!.close()
        }
        realm = Realm.getDefaultInstance()
        try {
            realm!!.beginTransaction()
            val usm = realm!!.createObject<UserRatesMovie>(UserRatesMovie::class.java)
            usm.setUserId("3f4d09f8-d499-43ab-a24f-86ccb3d546cb")
            usm.setMovieId("372058")
            usm.setRating(4)
            usm.setDateAndTime(Date())
            usm.setMovie_release("2016-08-26")
            usm.setMovie_description("High schoolers Mitsuha and Taki are complete strangers living separate lives. But one night, they suddenly switch places. Mitsuha wakes up in Taki’s body, and he in hers. This bizarre occurrence continues to happen randomly, and the two must adjust their lives around each other.")
            usm.setMovie_title("Your Name.")
            usm.setMovie_poster("/xq1Ugd62d23K2knRUx6xxuALTZB.jpg")
            realm!!.commitTransaction()
        } finally {
            realm!!.close()
        }
        realm = Realm.getDefaultInstance()
        try {
            realm!!.beginTransaction()
            val usm = realm!!.createObject<UserRatesMovie>(UserRatesMovie::class.java)
            usm.setUserId("3f4d09f8-d499-43ab-a24f-86ccb3d546cb")
            usm.setMovieId("278")
            usm.setRating(3)
            usm.setDateAndTime(Date())
            usm.setMovie_release("1994-09-23")
            usm.setMovie_description("Framed in the 1940s for the double murder of his wife and her lover, upstanding banker Andy Dufresne begins a new life at the Shawshank prison, where he puts his accounting skills to work for an amoral warden. During his long stretch in prison, Dufresne comes to be admired by the other inmates -- including an older prisoner named Red -- for his integrity and unquenchable sense of hope.")
            usm.setMovie_title("The Shawshank Redemption")
            usm.setMovie_poster("/9O7gLzmreU0nGkIB6K3BsJbzvNv.jpg")
            realm!!.commitTransaction()
        } finally {
            realm!!.close()
        }
        realm = Realm.getDefaultInstance()
        try {
            realm!!.beginTransaction()
            val usm = realm!!.createObject<UserRatesMovie>(UserRatesMovie::class.java)
            usm.setUserId("3f4d09f8-d499-43ab-a24f-86ccb3d546cb")
            usm.setMovieId("637")
            usm.setRating(5)
            usm.setDateAndTime(Date())
            usm.setMovie_release("1997-12-20")
            usm.setMovie_description("A touching story of an Italian book seller of Jewish ancestry who lives in his own little fairy tale. His creative and happy life would come to an abrupt halt when his entire family is deported to a concentration camp during World War II. While locked up he tries to convince his son that the whole thing is just a game.")
            usm.setMovie_title("Life Is Beautiful")
            usm.setMovie_poster("/f7DImXDebOs148U4uPjI61iDvaK.jpg")
            realm!!.commitTransaction()
        } finally {
            realm!!.close()
        }
        realm = Realm.getDefaultInstance()
        //tinton
        try {
            realm!!.beginTransaction()
            val usm = realm!!.createObject<UserRatesMovie>(UserRatesMovie::class.java)
            usm.setUserId("4343ad42-d303-42ca-a324-c5c0b520bd8f")
            usm.setMovieId("238")
            usm.setRating(3)
            usm.setDateAndTime(Date())
            usm.setMovie_release("1972-03-14")
            usm.setMovie_description("Spanning the years 1945 to 1955, a chronicle of the fictional Italian-American Corleone crime family. When organized crime family patriarch, Vito Corleone barely survives an attempt on his life, his youngest son, Michael steps in to take care of the would-be killers, launching a campaign of bloody revenge.")
            usm.setMovie_title("The Godfather")
            usm.setMovie_poster("/rPdtLWNsZmAtoZl9PK7S2wE3qiS.jpg")
            realm!!.commitTransaction()
        } finally {
            realm!!.close()
        }
        realm = Realm.getDefaultInstance()
        try {
            realm!!.beginTransaction()
            val usm = realm!!.createObject<UserRatesMovie>(UserRatesMovie::class.java)
            usm.setUserId("4343ad42-d303-42ca-a324-c5c0b520bd8f")
            usm.setMovieId("680")
            usm.setRating(3)
            usm.setDateAndTime(Date())
            usm.setMovie_release("1994-09-10")
            usm.setMovie_description("A burger-loving hit man, his philosophical partner, a drug-addled gangster's moll and a washed-up boxer converge in this sprawling, comedic crime caper. Their adventures unfurl in three stories that ingeniously trip back and forth in time.")
            usm.setMovie_title("Pulp Fiction")
            usm.setMovie_poster("/dM2w364MScsjFf8pfMbaWUcWrR.jpg")
            realm!!.commitTransaction()
        } finally {
            realm!!.close()
        }
        realm = Realm.getDefaultInstance()
        try {
            realm!!.beginTransaction()
            val usm = realm!!.createObject<UserRatesMovie>(UserRatesMovie::class.java)
            usm.setUserId("4343ad42-d303-42ca-a324-c5c0b520bd8f")
            usm.setMovieId("372058")
            usm.setRating(1)
            usm.setDateAndTime(Date())
            usm.setMovie_release("2016-08-26")
            usm.setMovie_description("High schoolers Mitsuha and Taki are complete strangers living separate lives. But one night, they suddenly switch places. Mitsuha wakes up in Taki’s body, and he in hers. This bizarre occurrence continues to happen randomly, and the two must adjust their lives around each other.")
            usm.setMovie_title("Your Name.")
            usm.setMovie_poster("/xq1Ugd62d23K2knRUx6xxuALTZB.jpg")
            realm!!.commitTransaction()
        } finally {
            realm!!.close()
        }
        realm = Realm.getDefaultInstance()
        try {
            realm!!.beginTransaction()
            val usm = realm!!.createObject<UserRatesMovie>(UserRatesMovie::class.java)
            usm.setUserId("4343ad42-d303-42ca-a324-c5c0b520bd8f")
            usm.setMovieId("278")
            usm.setRating(5)
            usm.setDateAndTime(Date())
            usm.setMovie_release("1994-09-23")
            usm.setMovie_description("Framed in the 1940s for the double murder of his wife and her lover, upstanding banker Andy Dufresne begins a new life at the Shawshank prison, where he puts his accounting skills to work for an amoral warden. During his long stretch in prison, Dufresne comes to be admired by the other inmates -- including an older prisoner named Red -- for his integrity and unquenchable sense of hope.")
            usm.setMovie_title("The Shawshank Redemption")
            usm.setMovie_poster("/9O7gLzmreU0nGkIB6K3BsJbzvNv.jpg")
            realm!!.commitTransaction()
        } finally {
            realm!!.close()
        }
        realm = Realm.getDefaultInstance()
        try {
            realm!!.beginTransaction()
            val usm = realm!!.createObject<UserRatesMovie>(UserRatesMovie::class.java)
            usm.setUserId("4343ad42-d303-42ca-a324-c5c0b520bd8f")
            usm.setMovieId("637")
            usm.setRating(4)
            usm.setDateAndTime(Date())
            usm.setMovie_release("1997-12-20")
            usm.setMovie_description("A touching story of an Italian book seller of Jewish ancestry who lives in his own little fairy tale. His creative and happy life would come to an abrupt halt when his entire family is deported to a concentration camp during World War II. While locked up he tries to convince his son that the whole thing is just a game.")
            usm.setMovie_title("Life Is Beautiful")
            usm.setMovie_poster("/f7DImXDebOs148U4uPjI61iDvaK.jpg")
            realm!!.commitTransaction()
        } finally {
            realm!!.close()
        }
        realm = Realm.getDefaultInstance()
        //ezziz
        try {
            realm!!.beginTransaction()
            val usm = realm!!.createObject<UserRatesMovie>(UserRatesMovie::class.java)
            usm.setUserId("cbed9f61-b7f0-4a0d-ab4a-953e22c25473")
            usm.setMovieId("238")
            usm.setRating(1)
            usm.setDateAndTime(Date())
            usm.setMovie_release("1972-03-14")
            usm.setMovie_description("Spanning the years 1945 to 1955, a chronicle of the fictional Italian-American Corleone crime family. When organized crime family patriarch, Vito Corleone barely survives an attempt on his life, his youngest son, Michael steps in to take care of the would-be killers, launching a campaign of bloody revenge.")
            usm.setMovie_title("The Godfather")
            usm.setMovie_poster("/rPdtLWNsZmAtoZl9PK7S2wE3qiS.jpg")
            realm!!.commitTransaction()
        } finally {
            realm!!.close()
        }
        realm = Realm.getDefaultInstance()
        try {
            realm!!.beginTransaction()
            val usm = realm!!.createObject<UserRatesMovie>(UserRatesMovie::class.java)
            usm.setUserId("cbed9f61-b7f0-4a0d-ab4a-953e22c25473")
            usm.setMovieId("680")
            usm.setRating(5)
            usm.setDateAndTime(Date())
            usm.setMovie_release("1994-09-10")
            usm.setMovie_description("A burger-loving hit man, his philosophical partner, a drug-addled gangster's moll and a washed-up boxer converge in this sprawling, comedic crime caper. Their adventures unfurl in three stories that ingeniously trip back and forth in time.")
            usm.setMovie_title("Pulp Fiction")
            usm.setMovie_poster("/dM2w364MScsjFf8pfMbaWUcWrR.jpg")
            realm!!.commitTransaction()
        } finally {
            realm!!.close()
        }
        realm = Realm.getDefaultInstance()
        try {
            realm!!.beginTransaction()
            val usm = realm!!.createObject<UserRatesMovie>(UserRatesMovie::class.java)
            usm.setUserId("cbed9f61-b7f0-4a0d-ab4a-953e22c25473")
            usm.setMovieId("372058")
            usm.setRating(5)
            usm.setDateAndTime(Date())
            usm.setMovie_release("2016-08-26")
            usm.setMovie_description("High schoolers Mitsuha and Taki are complete strangers living separate lives. But one night, they suddenly switch places. Mitsuha wakes up in Taki’s body, and he in hers. This bizarre occurrence continues to happen randomly, and the two must adjust their lives around each other.")
            usm.setMovie_title("Your Name.")
            usm.setMovie_poster("/xq1Ugd62d23K2knRUx6xxuALTZB.jpg")
            realm!!.commitTransaction()
        } finally {
            realm!!.close()
        }
        realm = Realm.getDefaultInstance()
        try {
            realm!!.beginTransaction()
            val usm = realm!!.createObject<UserRatesMovie>(UserRatesMovie::class.java)
            usm.setUserId("cbed9f61-b7f0-4a0d-ab4a-953e22c25473")
            usm.setMovieId("278")
            usm.setRating(2)
            usm.setDateAndTime(Date())
            usm.setMovie_release("1994-09-23")
            usm.setMovie_description("Framed in the 1940s for the double murder of his wife and her lover, upstanding banker Andy Dufresne begins a new life at the Shawshank prison, where he puts his accounting skills to work for an amoral warden. During his long stretch in prison, Dufresne comes to be admired by the other inmates -- including an older prisoner named Red -- for his integrity and unquenchable sense of hope.")
            usm.setMovie_title("The Shawshank Redemption")
            usm.setMovie_poster("/9O7gLzmreU0nGkIB6K3BsJbzvNv.jpg")
            realm!!.commitTransaction()
        } finally {
            realm!!.close()
        }
        realm = Realm.getDefaultInstance()
        try {
            realm!!.beginTransaction()
            val usm = realm!!.createObject<UserRatesMovie>(UserRatesMovie::class.java)
            usm.setUserId("cbed9f61-b7f0-4a0d-ab4a-953e22c25473")
            usm.setMovieId("637")
            usm.setRating(1)
            usm.setDateAndTime(Date())
            usm.setMovie_release("1997-12-20")
            usm.setMovie_description("A touching story of an Italian book seller of Jewish ancestry who lives in his own little fairy tale. His creative and happy life would come to an abrupt halt when his entire family is deported to a concentration camp during World War II. While locked up he tries to convince his son that the whole thing is just a game.")
            usm.setMovie_title("Life Is Beautiful")
            usm.setMovie_poster("/f7DImXDebOs148U4uPjI61iDvaK.jpg")
            realm!!.commitTransaction()
        } finally {
            realm!!.close()
        }
        realm = Realm.getDefaultInstance()
    }

    companion object {
        var instance: MovieRecommendationApp? = null
            private set
        const val apiKey: String = "efbdebf1b30ffab728c49495748e9dfa"
        val retrofitInstance: Retrofit? = null
    }
}
