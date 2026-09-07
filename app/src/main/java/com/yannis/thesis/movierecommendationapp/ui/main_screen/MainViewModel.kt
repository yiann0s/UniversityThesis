package com.yannis.thesis.movierecommendationapp.ui.main_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yannis.thesis.movierecommendationapp.data.local.MovieRecommendedForUser
import com.yannis.thesis.movierecommendationapp.data.local.UserRatesMovie
import com.yannis.thesis.movierecommendationapp.data.remote.Movie
import com.yannis.thesis.movierecommendationapp.domain.repositories.MovieRepository
import com.yannis.thesis.movierecommendationapp.domain.repositories.RatingRepository
import com.yannis.thesis.movierecommendationapp.domain.repositories.RecommendationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class MainUiState(
    val recommendations: List<MovieRecommendedForUser> = emptyList(),
    val recentlyRated: List<UserRatesMovie> = emptyList(),
    val searchResults: List<Movie> = emptyList(),
    val isLoading: Boolean = false,
    val isSearching: Boolean = false,
    val errorMessage: String? = null
)

class MainViewModel(
    private val userId: String?,
    private val recommendationRepository: RecommendationRepository,
    private val ratingRepository: RatingRepository,
    private val movieRepository: MovieRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(MainUiState(isLoading = true))
    val uiState: StateFlow<MainUiState> = _uiState

    init {
        loadHome()
    }

    fun loadHome() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val home = withContext(Dispatchers.IO) {
                recommendationRepository.findAllForUserByRating(userId) to
                    ratingRepository.findAllForUser(userId)
            }
            _uiState.value = _uiState.value.copy(
                recommendations = home.first,
                recentlyRated = home.second,
                isLoading = false
            )
        }
    }

    fun search(category: String, query: String) {
        if (query.isEmpty()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Must provide category")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSearching = true, errorMessage = null)
            runCatching {
                withContext(Dispatchers.IO) {
                    movieRepository.search(category, query)
                }
            }.onSuccess { movies ->
                _uiState.value = _uiState.value.copy(
                    searchResults = movies,
                    isSearching = false
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isSearching = false,
                    errorMessage = error.message ?: "Search failed"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

class MainViewModelFactory(
    private val userId: String?,
    private val recommendationRepository: RecommendationRepository,
    private val ratingRepository: RatingRepository,
    private val movieRepository: MovieRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(
                userId,
                recommendationRepository,
                ratingRepository,
                movieRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
