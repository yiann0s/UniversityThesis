package com.yannis.thesis.movierecommendationapp.data.local

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.util.Date

@Entity
class MovieRecommendedForUser constructor(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    var userId: String? = null,
    var movieId: String? = null,
    var predictedRating: Double? = null,
    var dateAndTime: Date? = null,
    var movie_poster: String? = null,
    var movie_title: String? = null,
    var movie_description: String? = null,
    var movie_release: String? = null
) {
    @Ignore
    constructor(
        userId: String?,
        movieId: String?,
        predictedRating: Double?,
        dateAndTime: Date?,
        movie_poster: String?,
        movie_title: String?,
        movie_description: String?,
        movie_release: String?
    ) : this(
        0,
        userId,
        movieId,
        predictedRating,
        dateAndTime,
        movie_poster,
        movie_title,
        movie_description,
        movie_release
    )
}
