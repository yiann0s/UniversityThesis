package com.yannis.thesis.movierecommendationapp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Card
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yannis.thesis.movierecommendationapp.data.local.MovieRecommendedForUser
import com.yannis.thesis.movierecommendationapp.data.local.UserRatesMovie
import com.yannis.thesis.movierecommendationapp.data.remote.Movie
import com.yannis.thesis.movierecommendationapp.ui.viewmodels.MainUiState
import com.yannis.thesis.movierecommendationapp.ui.theme.BrandColors

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
    Scaffold(
        backgroundColor = BrandColors.LightBackground,
        bottomBar = {
            BottomNavigation(
                backgroundColor = BrandColors.DarkBlue,
                contentColor = Color.White
            ) {
                BottomNavigationItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    label = { Text("Search") },
                    selectedContentColor = BrandColors.Yellow,
                    unselectedContentColor = Color.White.copy(alpha = 0.7f)
                )
                BottomNavigationItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Favorite, contentDescription = "Recently rated") },
                    label = { Text("Rated") },
                    selectedContentColor = BrandColors.Yellow,
                    unselectedContentColor = Color.White.copy(alpha = 0.7f)
                )
                BottomNavigationItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Movie, contentDescription = "Recommendations") },
                    label = { Text("Recommended") },
                    selectedContentColor = BrandColors.Yellow,
                    unselectedContentColor = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    ) { contentPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(contentPadding)
        ) {
            if (state.isLoading || state.isSearching) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
            }
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
            when (selectedTab) {
                0 -> SearchContent(state, onSearch, onMovieClick)
                1 -> RecentlyRatedContent(state, onRatedMovieClick)
                else -> RecommendationsContent(state, onRecommendedMovieClick)
            }
        }
    }
}

@Composable
private fun RecommendationsContent(
    state: MainUiState,
    onRecommendedMovieClick: (MovieRecommendedForUser) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            MovieSection(
                title = "Recommended movies",
                backgroundColor = BrandColors.RecommendationsBackground
            ) {
                if (state.recommendations.isEmpty()) {
                    Text("No movie recommendations available")
                } else {
                    state.recommendations.forEach { movie ->
                        MovieRow(movie.movie_title, movie.movie_release, movie.movie_description) {
                            onRecommendedMovieClick(movie)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentlyRatedContent(
    state: MainUiState,
    onRatedMovieClick: (UserRatesMovie) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            MovieSection(
                title = "Recently rated movies",
                backgroundColor = BrandColors.RatedBackground
            ) {
                if (state.recentlyRated.isEmpty()) {
                    Text("Not any movies rated recently")
                } else {
                    state.recentlyRated.forEach { movie ->
                        MovieRow(movie.movie_title, movie.movie_release, movie.movie_description) {
                            onRatedMovieClick(movie)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MovieSection(
    title: String,
    backgroundColor: Color,
    content: @Composable () -> Unit
) {
    Card(
        backgroundColor = backgroundColor,
        elevation = 3.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.h6, color = BrandColors.DarkBlue)
            content()
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
    Column(
        Modifier
            .fillMaxSize()
            .background(BrandColors.SearchBackground)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            IconButton(onClick = { onSearch(category, query) }) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = BrandColors.DarkBlue
                )
            }
            TextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Search movies") },
                singleLine = true,
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearch(category, query) }),
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.White.copy(alpha = 0.7f),
                    focusedIndicatorColor = BrandColors.DarkBlue,
                    unfocusedIndicatorColor = BrandColors.PrimaryBlue
                )
            )
            Box {
                Button(onClick = { expanded = true }) { Text(category) }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    categories.forEach {
                        DropdownMenuItem(onClick = { category = it; expanded = false }) {
                            Text(it)
                        }
                    }
                }
            }
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
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
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(12.dp)
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
