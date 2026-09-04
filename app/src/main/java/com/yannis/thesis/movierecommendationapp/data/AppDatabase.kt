package com.yannis.thesis.movierecommendationapp.data;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverter;
import androidx.room.TypeConverters;
import com.yannis.thesis.movierecommendationapp.models.MovieRecommendedForUser;
import com.yannis.thesis.movierecommendationapp.models.User;
import com.yannis.thesis.movierecommendationapp.models.UserRatesMovie;
import java.util.Date;

class RoomConverters {
    @TypeConverter
    public Long fromDate(Date date) {
        return date == null ? null : date.getTime();
    }

    @TypeConverter
    public Date toDate(Long value) {
        return value == null ? null : new Date(value);
    }
}

@Database(entities = {User.class, UserRatesMovie.class, MovieRecommendedForUser.class},
        version = 1, exportSchema = false)
@TypeConverters(RoomConverters.class)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserDao userDao();
    public abstract UserRatesMovieDao userRatesMovieDao();
    public abstract MovieRecommendedForUserDao movieRecommendedForUserDao();

    private static volatile AppDatabase instance;

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "myroomdb")
                            .allowMainThreadQueries()
                            .build();
                }
            }
        }
        return instance;
    }
}
