package com.yannis.thesis.movierecommendationapp.ui.movie_detail_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yannis.thesis.movierecommendationapp.data.local.UserRatesMovie
import com.yannis.thesis.movierecommendationapp.domain.repositories.RatingRepository
import com.yannis.thesis.movierecommendationapp.domain.repositories.RecommendationRepository
import java.util.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class RecommendedMovieDetailUiState(
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val submitted: Boolean = false,
    val submittedRating: Int? = null
)

class RecommendedMovieDetailViewModel(
    private val ratingRepository: RatingRepository,
    private val recommendationRepository: RecommendationRepository,
    private val userId: String?,
    private val movieId: String?
) : ViewModel() {
    private val _uiState = MutableStateFlow(RecommendedMovieDetailUiState())
    val uiState: StateFlow<RecommendedMovieDetailUiState> = _uiState

    fun submitRating(
        rating: Int,
        posterPath: String?,
        title: String?,
        description: String?,
        releaseDate: String?
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, errorMessage = null)
            runCatching {
                withContext(Dispatchers.IO) {
                    recommendationRepository.delete(
                        recommendationRepository.findForMovie(userId, movieId)
                    )
                    ratingRepository.insert(
                        UserRatesMovie(
                            userId,
                            movieId,
                            rating,
                            Date(),
                            posterPath,
                            title,
                            description,
                            releaseDate
                        )
                    )
                }
            }.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    submitted = true,
                    submittedRating = rating
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    errorMessage = error.message ?: "Unable to save rating"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

class RecommendedMovieDetailViewModelFactory(
    private val ratingRepository: RatingRepository,
    private val recommendationRepository: RecommendationRepository,
    private val userId: String?,
    private val movieId: String?
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecommendedMovieDetailViewModel::class.java)) {
            return RecommendedMovieDetailViewModel(
                ratingRepository,
                recommendationRepository,
                userId,
                movieId
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
