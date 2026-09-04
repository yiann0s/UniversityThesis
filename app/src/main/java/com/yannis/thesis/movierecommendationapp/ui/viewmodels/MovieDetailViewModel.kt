package com.yannis.thesis.movierecommendationapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yannis.thesis.movierecommendationapp.data.local.UserRatesMovie
import com.yannis.thesis.movierecommendationapp.domain.repositories.RatingRepository
import java.util.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class MovieDetailUiState(
    val existingRating: Int? = null,
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null
)

class MovieDetailViewModel(
    private val ratingRepository: RatingRepository,
    private val userId: String?,
    private val movieId: String?
) : ViewModel() {
    private val _uiState = MutableStateFlow(MovieDetailUiState())
    val uiState: StateFlow<MovieDetailUiState> = _uiState

    init {
        viewModelScope.launch {
            val rating = withContext(Dispatchers.IO) {
                ratingRepository.findForMovie(userId, movieId)
            }
            _uiState.value = MovieDetailUiState(
                existingRating = rating?.rating,
                isLoading = false
            )
        }
    }

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
                    existingRating = rating,
                    isSubmitting = false
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

class MovieDetailViewModelFactory(
    private val ratingRepository: RatingRepository,
    private val userId: String?,
    private val movieId: String?
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MovieDetailViewModel::class.java)) {
            return MovieDetailViewModel(ratingRepository, userId, movieId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
