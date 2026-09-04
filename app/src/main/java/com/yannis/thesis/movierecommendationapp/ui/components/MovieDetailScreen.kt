package com.yannis.thesis.movierecommendationapp.ui.components

import android.widget.ImageView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.squareup.picasso.Picasso
import com.yannis.thesis.movierecommendationapp.R
import com.yannis.thesis.movierecommendationapp.ui.viewmodels.MovieDetailUiState

@Composable
fun MovieDetailScreen(
    title: String?,
    releaseDate: String?,
    description: String?,
    posterPath: String?,
    state: MovieDetailUiState,
    onRatingSelected: (Int) -> Unit
) {
    MovieDetailContent(
        title = title,
        releaseDate = releaseDate,
        description = description,
        posterPath = posterPath,
        rating = state.existingRating,
        isRatingEnabled = state.existingRating == null && !state.isSubmitting,
        isLoading = state.isLoading,
        onRatingSelected = onRatingSelected
    )
}

@Composable
internal fun MovieDetailContent(
    title: String?,
    releaseDate: String?,
    description: String?,
    posterPath: String?,
    rating: Int?,
    isRatingEnabled: Boolean,
    isLoading: Boolean,
    onRatingSelected: (Int) -> Unit
) {
    var selectedRating by remember { mutableStateOf(rating) }
    LaunchedEffect(rating) {
        if (rating != null) selectedRating = rating
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFDB5C))
            .verticalScroll(rememberScrollState())
            .padding(bottom = 16.dp)
    ) {
        AndroidView(
            factory = { context ->
                ImageView(context).apply {
                    scaleType = ImageView.ScaleType.FIT_CENTER
                }
            },
            update = { imageView ->
                Picasso.get()
                    .load("https://image.tmdb.org/t/p/w500${posterPath.orEmpty()}")
                    .error(R.color.colorAccent)
                    .into(imageView)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(360.dp)
        )
        Text(
            text = title.orEmpty(),
            style = MaterialTheme.typography.h5,
            color = Color(0xFFF72808),
            modifier = Modifier.padding(start = 10.dp, top = 10.dp)
        )
        Text(
            text = releaseDate.orEmpty(),
            style = MaterialTheme.typography.subtitle1,
            color = Color(0xFFF8A055),
            modifier = Modifier.padding(start = 10.dp, top = 4.dp)
        )
        Text(
            text = description.orEmpty(),
            color = Color(0xFFFA6E59),
            modifier = Modifier.padding(start = 10.dp, top = 5.dp, end = 20.dp)
        )
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
        }
        RatingStars(
            rating = selectedRating,
            enabled = isRatingEnabled,
            onRatingSelected = {
                selectedRating = it
                onRatingSelected(it)
            },
            modifier = Modifier.padding(start = 10.dp, top = 12.dp)
        )
    }
}

@Composable
internal fun RatingStars(
    rating: Int?,
    enabled: Boolean,
    onRatingSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        (1..5).forEach { value ->
            Text(
                text = if (rating != null && value <= rating) "★" else "☆",
                color = Color(0xFFF72808),
                style = MaterialTheme.typography.h4,
                modifier = Modifier
                    .size(42.dp)
                    .then(
                        if (enabled) Modifier
                            .padding(end = 2.dp)
                            .clickable { onRatingSelected(value) }
                        else Modifier.padding(end = 2.dp)
                    )
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun MovieDetailScreenPreview() {
    MovieDetailScreen(
        title = "The Godfather",
        releaseDate = "1972-03-14",
        description = "The aging patriarch of an organized crime dynasty transfers control to his reluctant son.",
        posterPath = null,
        state = MovieDetailUiState(existingRating = 4, isLoading = false),
        onRatingSelected = {}
    )
}
