package com.yannis.thesis.movierecommendationapp.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.yannis.thesis.movierecommendationapp.models.UserRatesMovie;
import java.util.List;

@Dao
public interface UserRatesMovieDao {
    @Query("SELECT * FROM UserRatesMovie WHERE userId = :userId AND movieId = :movieId LIMIT 1")
    UserRatesMovie findForMovie(String userId, String movieId);
    @Query("SELECT * FROM UserRatesMovie WHERE userId = :userId ORDER BY dateAndTime DESC")
    List<UserRatesMovie> findAllForUser(String userId);
    @Query("SELECT * FROM UserRatesMovie")
    List<UserRatesMovie> getAll();
    @Insert
    void insert(UserRatesMovie movie);
}
