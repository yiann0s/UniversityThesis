package com.yannis.thesis.movierecommendationapp.models;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class MovieRecommendedForUser extends RealmObject {

    private String userId;

    private String movieId;

    private Double predictedRating;

    private Date dateAndTime;

    private String movie_poster;

    private String movie_title;

    private String movie_description;

    private String movie_release;

    public MovieRecommendedForUser() {
    }

    public MovieRecommendedForUser(String userId, String movieId, Double predictedRating, Date dateAndTime, String movie_poster, String movie_title, String movie_description, String movie_release) {
        this.userId = userId;
        this.movieId = movieId;
        this.predictedRating = predictedRating;
        this.dateAndTime = dateAndTime;
        this.movie_poster = movie_poster;
        this.movie_title = movie_title;
        this.movie_description = movie_description;
        this.movie_release = movie_release;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMovieId() {
        return movieId;
    }

    public void setMovieId(String movieId) {
        this.movieId = movieId;
    }

    public Double getPredictedRating() {
        return predictedRating;
    }

    public void setPredictedRating(Double predictedRating) {
        this.predictedRating = predictedRating;
    }

    public Date getDateAndTime() {
        return dateAndTime;
    }

    public void setDateAndTime(Date dateAndTime) {
        this.dateAndTime = dateAndTime;
    }

    public String getMovie_poster() {
        return movie_poster;
    }

    public void setMovie_poster(String movie_poster) {
        this.movie_poster = movie_poster;
    }

    public String getMovie_title() {
        return movie_title;
    }

    public void setMovie_title(String movie_title) {
        this.movie_title = movie_title;
    }

    public String getMovie_description() {
        return movie_description;
    }

    public void setMovie_description(String movie_description) {
        this.movie_description = movie_description;
    }

    public String getMovie_release() {
        return movie_release;
    }

    public void setMovie_release(String movie_release) {
        this.movie_release = movie_release;
    }
}
