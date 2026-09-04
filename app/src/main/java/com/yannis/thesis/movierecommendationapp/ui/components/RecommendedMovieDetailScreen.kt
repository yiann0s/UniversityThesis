package com.yannis.thesis.movierecommendationapp.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.yannis.thesis.movierecommendationapp.ui.viewmodels.RecommendedMovieDetailUiState

@Composable
fun RecommendedMovieDetailScreen(
    title: String?,
    releaseDate: String?,
    description: String?,
    posterPath: String?,
    state: RecommendedMovieDetailUiState,
    onRatingSelected: (Int) -> Unit
) {
    MovieDetailContent(
        title = title,
        releaseDate = releaseDate,
        description = description,
        posterPath = posterPath,
        rating = state.submittedRating,
        isRatingEnabled = !state.isSubmitting && !state.submitted,
        isLoading = state.isSubmitting,
        onRatingSelected = onRatingSelected
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun RecommendedMovieDetailScreenPreview() {
    RecommendedMovieDetailScreen(
        title = "Pulp Fiction",
        releaseDate = "1994-09-10",
        description = "The lives of several characters intertwine in a collection of crime stories.",
        posterPath = null,
        state = RecommendedMovieDetailUiState(),
        onRatingSelected = {}
    )
}
