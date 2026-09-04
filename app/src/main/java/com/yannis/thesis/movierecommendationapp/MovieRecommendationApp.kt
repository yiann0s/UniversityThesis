package com.yannis.thesis.movierecommendationapp

import android.app.Application
import com.yannis.thesis.movierecommendationapp.ui.activities.BaseActivity
import com.yannis.thesis.movierecommendationapp.data.remote.APIService
import com.yannis.thesis.movierecommendationapp.data.local.AppDatabase
import com.yannis.thesis.movierecommendationapp.data.repositories.RetrofitMovieRepository
import com.yannis.thesis.movierecommendationapp.data.repositories.RoomRatingRepository
import com.yannis.thesis.movierecommendationapp.data.repositories.RoomRecommendationRepository
import com.yannis.thesis.movierecommendationapp.data.repositories.RoomUserRepository
import com.yannis.thesis.movierecommendationapp.domain.repositories.RatingRepository
import com.yannis.thesis.movierecommendationapp.domain.repositories.RecommendationRepository
import com.yannis.thesis.movierecommendationapp.domain.repositories.UserRepository
import com.yannis.thesis.movierecommendationapp.domain.usecases.GenerateRecommendationsUseCase
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MovieRecommendationApp : Application() {
    @JvmField var lastActivity: BaseActivity? = null
    @JvmField var loggedInUserId: String? = null
    lateinit var userRepository: UserRepository
        private set
    lateinit var ratingRepository: RatingRepository
        private set
    lateinit var recommendationRepository: RecommendationRepository
        private set
    private lateinit var generateRecommendations: GenerateRecommendationsUseCase

    override fun onCreate() {
        super.onCreate()
        appInstance = this
        val database = AppDatabase.getInstance(this)
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BASIC
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.themoviedb.org/3/")
            .client(OkHttpClient.Builder().addInterceptor(logging).build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val api = retrofit.create(APIService::class.java)
        userRepository = RoomUserRepository(database.userDao())
        ratingRepository = RoomRatingRepository(database.userRatesMovieDao())
        recommendationRepository =
            RoomRecommendationRepository(database.movieRecommendedForUserDao())
        generateRecommendations = GenerateRecommendationsUseCase(
            userRepository = userRepository,
            ratingRepository = ratingRepository,
            recommendationRepository = recommendationRepository,
            movieRepository = RetrofitMovieRepository(api, apiKey)
        )
        showAllUsers()
        MovieRecommendationAlgorithm()
    }

    fun showAllUsers() {
        userRepository.getAll()
    }

    fun MovieRecommendationAlgorithm() =
        generateRecommendations.invoke("3c5303e9-0b5e-493a-98e8-184893dbb261")

    companion object {
        private var appInstance: MovieRecommendationApp? = null
            set
        const val apiKey = "efbdebf1b30ffab728c49495748e9dfa"
        @JvmStatic fun getApiKey() = apiKey
        @JvmStatic fun getInstance() = appInstance!!
    }
}
