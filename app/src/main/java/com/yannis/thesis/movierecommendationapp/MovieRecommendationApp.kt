package com.yannis.thesis.movierecommendationapp

import android.app.Application
import android.util.Log
import com.yannis.thesis.movierecommendationapp.activities.BaseActivity
import com.yannis.thesis.movierecommendationapp.api.APIService
import com.yannis.thesis.movierecommendationapp.data.AppDatabase
import com.yannis.thesis.movierecommendationapp.models.Movie
import com.yannis.thesis.movierecommendationapp.models.MovieRecommendedForUser
import com.yannis.thesis.movierecommendationapp.models.UserRatesMovie
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Date
import kotlin.math.pow
import kotlin.math.sqrt

class MovieRecommendationApp : Application() {
    @JvmField var lastActivity: BaseActivity? = null
    @JvmField var loggedInUserId: String? = null
    lateinit var database: AppDatabase
    private var client: APIService? = null
    private var retrofit: Retrofit? = null
    private val similarityPillow = 0.5
    private val predictionPillow = 3.0

    override fun onCreate() {
        super.onCreate()
        appInstance = this
        database = AppDatabase.getInstance(this)
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BASIC
        retrofit = Retrofit.Builder()
            .baseUrl("https://api.themoviedb.org/3/")
            .client(OkHttpClient.Builder().addInterceptor(logging).build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        showAllUsers()
        MovieRecommendationAlgorithm()
    }

    fun showAllUsers() {
        for (user in database.userDao().getAll()) {
            Log.d("MovieApp", "User has mail ${user.email} and id ${user.id}")
        }
    }

    fun MovieRecommendationAlgorithm() {
        val activeUserId = "3c5303e9-0b5e-493a-98e8-184893dbb261"
        val neighbours = database.userDao().findAllExcept(activeUserId)
            .filter { similarity(activeUserId, it.id) >= similarityPillow }
            .map { it.id }
        val rated = database.userRatesMovieDao().findAllForUser(activeUserId).associate {
            it.movieId to (it.rating ?: 0).toDouble()
        }
        val notRated = database.userRatesMovieDao().getAll().mapNotNull { it.movieId }
            .filter { it !in rated }.distinct()
        notRated.forEach { prediction(activeUserId, it, ArrayList(neighbours)) }
    }

    fun prediction(activeUserId: String?, movieId: String, neighbours: ArrayList<String>) {
        val activeAverage = avgRating(activeUserId)
        var numerator = 0.0
        var denominator = 0.0
        neighbours.forEach { neighbour ->
            val similarity = similarity(activeUserId, neighbour)
            numerator += similarity * (getUser_i_MovieRating(neighbour, movieId) - avgRating(neighbour))
            denominator += similarity
        }
        val predicted = activeAverage + numerator / denominator
        if (predicted < predictionPillow) return
        client = retrofit!!.create(APIService::class.java)
        client!!.getMovieDetails(movieId.toInt(), apiKey).enqueue(object : Callback<Movie?> {
            override fun onResponse(call: Call<Movie?>, response: Response<Movie?>) {
                if (!response.isSuccessful || response.body() == null) return
                if (!movieIsUnique(movieId, activeUserId)) deleteMovie(movieId, activeUserId)
                val movie = response.body()!!
                database.movieRecommendedForUserDao().insert(
                    MovieRecommendedForUser(
                        activeUserId, movieId, predicted, Date(), movie.posterPath,
                        movie.title, movie.overview, movie.releaseDate
                    )
                )
            }

            override fun onFailure(call: Call<Movie?>, t: Throwable) {
                Log.e("MovieApp", "Failure loading movie details", t)
            }
        })
    }

    private fun movieIsUnique(movieId: String?, userId: String?) =
        database.movieRecommendedForUserDao().findForMovie(userId, movieId).isEmpty()

    fun deleteMovie(movieId: String?, userId: String?) {
        val result = database.movieRecommendedForUserDao().findForMovie(userId, movieId)
        database.movieRecommendedForUserDao().delete(result)
    }

    fun getUser_i_MovieRating(userId: String?, movieId: String?) =
        database.userRatesMovieDao().findForMovie(userId, movieId)!!.rating!!.toDouble()

    fun similarity(activeUserId: String?, otherUserId: String?): Double {
        val active = database.userRatesMovieDao().findAllForUser(activeUserId).associate {
            it.movieId to (it.rating ?: 0).toDouble()
        }
        val other = database.userRatesMovieDao().findAllForUser(otherUserId).associate {
            it.movieId to (it.rating ?: 0).toDouble()
        }
        val common = active.keys.intersect(other.keys)
        if (common.isEmpty()) return 0.0
        val activeAverage = common.map { active[it]!! }.average()
        val otherAverage = common.map { other[it]!! }.average()
        val numerator = common.sumOf { (active[it]!! - activeAverage) * (other[it]!! - otherAverage) }
        val left = sqrt(common.sumOf { (active[it]!! - activeAverage).pow(2) })
        val right = sqrt(common.sumOf { (other[it]!! - otherAverage).pow(2) })
        return if (left == 0.0 || right == 0.0) 0.0 else numerator / (left * right)
    }

    fun avgRating(userId: String?): Double {
        val ratings = database.userRatesMovieDao().findAllForUser(userId).map { (it.rating ?: 0).toDouble() }
        return if (ratings.isEmpty()) 0.0 else ratings.average()
    }

    companion object {
        private var appInstance: MovieRecommendationApp? = null
            private set
        const val apiKey = "efbdebf1b30ffab728c49495748e9dfa"
        @JvmStatic fun getApiKey() = apiKey
        @JvmStatic fun getInstance() = appInstance!!
    }
}
