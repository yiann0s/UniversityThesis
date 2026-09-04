package com.yannis.thesis.movierecommendationapp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yannis.thesis.movierecommendationapp.data.local.MovieRecommendedForUser
import com.yannis.thesis.movierecommendationapp.data.local.UserRatesMovie
import com.yannis.thesis.movierecommendationapp.data.remote.Movie
import com.yannis.thesis.movierecommendationapp.ui.viewmodels.MainUiState

private val categories = listOf("Title", "Released", "Director", "Genre", "Actors")

@Composable
fun MainScreen(
    state: MainUiState,
    onSearch: (String, String) -> Unit,
    onMovieClick: (Movie) -> Unit,
    onRatedMovieClick: (UserRatesMovie) -> Unit,
    onRecommendedMovieClick: (MovieRecommendedForUser) -> Unit,
    onClearError: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    Column(Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                Text("Recommendations", Modifier.padding(16.dp))
            }
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                Text("Search", Modifier.padding(16.dp))
            }
        }
        if (state.isLoading || state.isSearching) LinearProgressIndicator(Modifier.fillMaxWidth())
        state.errorMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colors.error,
                modifier = Modifier.padding(16.dp)
            )
            Button(onClick = onClearError, Modifier.padding(horizontal = 16.dp)) {
                Text("Dismiss")
            }
        }
        if (selectedTab == 0) {
            HomeContent(state, onRatedMovieClick, onRecommendedMovieClick)
        } else {
            SearchContent(state, onSearch, onMovieClick)
        }
    }
}

@Composable
private fun HomeContent(
    state: MainUiState,
    onRatedMovieClick: (UserRatesMovie) -> Unit,
    onRecommendedMovieClick: (MovieRecommendedForUser) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item { Text("Recommended movies", style = MaterialTheme.typography.h6) }
        items(state.recommendations) { movie ->
            MovieRow(movie.movie_title, movie.movie_release, movie.movie_description) {
                onRecommendedMovieClick(movie)
            }
        }
        item {
            Spacer(Modifier.height(16.dp))
            Text("Recently rated movies", style = MaterialTheme.typography.h6)
        }
        items(state.recentlyRated) { movie ->
            MovieRow(movie.movie_title, movie.movie_release, movie.movie_description) {
                onRatedMovieClick(movie)
            }
        }
    }
}

@Composable
private fun SearchContent(
    state: MainUiState,
    onSearch: (String, String) -> Unit,
    onMovieClick: (Movie) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(categories.first()) }
    var expanded by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Column(Modifier.weight(1f)) {
                Button(onClick = { expanded = true }) { Text(category) }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    categories.forEach {
                        DropdownMenuItem(onClick = { category = it; expanded = false }) {
                            Text(it)
                        }
                    }
                }
            }
            TextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Keyword") },
                modifier = Modifier.weight(2f)
            )
        }
        Button(onClick = { onSearch(category, query) }) { Text("Search") }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.searchResults) { movie ->
                MovieRow(movie.title, movie.releaseDate, movie.overview) {
                    onMovieClick(movie)
                }
            }
        }
    }
}

@Composable
private fun MovieRow(title: String?, release: String?, description: String?, onClick: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(12.dp)
    ) {
        Text(title.orEmpty(), style = MaterialTheme.typography.subtitle1)
        Text(release.orEmpty(), style = MaterialTheme.typography.caption)
        Text(description.orEmpty(), maxLines = 3)
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun MainScreenPreview() {
    MainScreen(
        state = MainUiState(
            recommendations = listOf(
                MovieRecommendedForUser(
                    userId = "user-1",
                    movieId = "238",
                    predictedRating = 4.8,
                    movie_title = "The Godfather",
                    movie_release = "1972-03-14",
                    movie_description = "The aging patriarch of an organized crime dynasty transfers control of his clandestine empire to his reluctant son."
                ),
                MovieRecommendedForUser(
                    userId = "user-1",
                    movieId = "680",
                    predictedRating = 4.4,
                    movie_title = "Pulp Fiction",
                    movie_release = "1994-09-10",
                    movie_description = "The lives of several characters intertwine in a collection of crime stories."
                )
            ),
            recentlyRated = listOf(
                UserRatesMovie(
                    userId = "user-1",
                    movieId = "278",
                    rating = 5,
                    movie_title = "The Shawshank Redemption",
                    movie_release = "1994-09-23",
                    movie_description = "Two imprisoned men bond over a number of years, finding solace and eventual redemption."
                )
            )
        ),
        onSearch = { _, _ -> },
        onMovieClick = {},
        onRatedMovieClick = {},
        onRecommendedMovieClick = {},
        onClearError = {}
    )
}
