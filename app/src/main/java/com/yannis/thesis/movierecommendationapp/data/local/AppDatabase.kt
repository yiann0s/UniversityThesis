package com.yannis.thesis.movierecommendationapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.yannis.thesis.movierecommendationapp.data.local.MovieRecommendedForUser
import com.yannis.thesis.movierecommendationapp.data.local.User
import com.yannis.thesis.movierecommendationapp.data.local.UserRatesMovie
import java.util.Date

class RoomConverters {
    @TypeConverter
    fun fromDate(date: Date?): Long? = date?.time

    @TypeConverter
    fun toDate(value: Long?): Date? = value?.let(::Date)
}

@Database(
    entities = [User::class, UserRatesMovie::class, MovieRecommendedForUser::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(RoomConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun userRatesMovieDao(): UserRatesMovieDao
    abstract fun movieRecommendedForUserDao(): MovieRecommendedForUserDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        @JvmStatic
        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "myroomdb"
                ).allowMainThreadQueries().build().also { instance = it }
            }
    }
}
