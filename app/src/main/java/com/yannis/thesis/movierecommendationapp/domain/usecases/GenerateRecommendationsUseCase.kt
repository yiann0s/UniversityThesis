package com.yannis.thesis.movierecommendationapp.domain.usecases

import android.util.Log
import com.yannis.thesis.movierecommendationapp.domain.repositories.MovieRepository
import com.yannis.thesis.movierecommendationapp.domain.repositories.RatingRepository
import com.yannis.thesis.movierecommendationapp.domain.repositories.RecommendationRepository
import com.yannis.thesis.movierecommendationapp.domain.repositories.UserRepository
import com.yannis.thesis.movierecommendationapp.data.local.MovieRecommendedForUser
import java.util.Date
import kotlin.math.pow
import kotlin.math.sqrt

class GenerateRecommendationsUseCase(
    private val userRepository: UserRepository,
    private val ratingRepository: RatingRepository,
    private val recommendationRepository: RecommendationRepository,
    private val movieRepository: MovieRepository,
    private val similarityThreshold: Double = 0.5,
    private val predictionThreshold: Double = 3.0
) {
    fun invoke(activeUserId: String) {
        val neighbours = userRepository.findAllExcept(activeUserId)
            .filter { similarity(activeUserId, it.id) >= similarityThreshold }
            .map { it.id }

        val ratedMovieIds = ratingRepository.findAllForUser(activeUserId)
            .mapNotNull { it.movieId }
            .toSet()
        val notRatedMovieIds = ratingRepository.getAll()
            .mapNotNull { it.movieId }
            .filterNot(ratedMovieIds::contains)
            .distinct()

        notRatedMovieIds.forEach { movieId ->
            predict(activeUserId, movieId, neighbours)
        }
    }

    private fun predict(activeUserId: String, movieId: String, neighbours: List<String>) {
        val activeAverage = averageRating(activeUserId)
        var numerator = 0.0
        var denominator = 0.0

        neighbours.forEach { neighbourId ->
            val similarity = similarity(activeUserId, neighbourId)
            val neighbourRating = ratingRepository.findForMovie(neighbourId, movieId)?.rating
                ?.toDouble() ?: return@forEach
            numerator += similarity * (neighbourRating - averageRating(neighbourId))
            denominator += similarity
        }

        if (denominator == 0.0) return
        val prediction = activeAverage + numerator / denominator
        if (prediction < predictionThreshold) return

        movieRepository.getMovieDetails(
            movieId = movieId.toInt(),
            onSuccess = { movie ->
                if (movie == null) return@getMovieDetails
                recommendationRepository.delete(
                    recommendationRepository.findForMovie(activeUserId, movieId)
                )
                recommendationRepository.insert(
                    MovieRecommendedForUser(
                        activeUserId,
                        movieId,
                        prediction,
                        Date(),
                        movie.posterPath,
                        movie.title,
                        movie.overview,
                        movie.releaseDate
                    )
                )
            },
            onFailure = { error ->
                Log.e("MovieApp", "Failure loading movie details", error)
            }
        )
    }

    private fun similarity(activeUserId: String, otherUserId: String): Double {
        val activeRatings = ratingRepository.findAllForUser(activeUserId)
            .mapNotNull { rating -> rating.movieId?.let { it to (rating.rating ?: 0).toDouble() } }
            .toMap()
        val otherRatings = ratingRepository.findAllForUser(otherUserId)
            .mapNotNull { rating -> rating.movieId?.let { it to (rating.rating ?: 0).toDouble() } }
            .toMap()
        val commonMovieIds = activeRatings.keys.intersect(otherRatings.keys)
        if (commonMovieIds.isEmpty()) return 0.0

        val activeAverage = commonMovieIds.map { activeRatings.getValue(it) }.average()
        val otherAverage = commonMovieIds.map { otherRatings.getValue(it) }.average()
        val numerator = commonMovieIds.sumOf {
            (activeRatings.getValue(it) - activeAverage) *
                (otherRatings.getValue(it) - otherAverage)
        }
        val left = sqrt(commonMovieIds.sumOf {
            (activeRatings.getValue(it) - activeAverage).pow(2)
        })
        val right = sqrt(commonMovieIds.sumOf {
            (otherRatings.getValue(it) - otherAverage).pow(2)
        })
        return if (left == 0.0 || right == 0.0) 0.0 else numerator / (left * right)
    }

    private fun averageRating(userId: String): Double {
        val ratings = ratingRepository.findAllForUser(userId)
            .map { (it.rating ?: 0).toDouble() }
        return if (ratings.isEmpty()) 0.0 else ratings.average()
    }
}
