package com.yannis.thesis.movierecommendationapp.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.yannis.thesis.movierecommendationapp.models.User;
import java.util.List;

@Dao
public interface UserDao {
    @Query("SELECT * FROM User")
    List<User> getAll();
    @Query("SELECT * FROM User WHERE email = :email LIMIT 1")
    User findByEmail(String email);
    @Query("SELECT * FROM User WHERE password = :password LIMIT 1")
    User findByPassword(String password);
    @Query("SELECT * FROM User WHERE id != :id")
    List<User> findAllExcept(String id);
    @Insert
    void insert(User user);
}
