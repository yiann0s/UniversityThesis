package com.yannis.thesis.movierecommendationapp.ui.navigation

import android.net.Uri
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.yannis.thesis.movierecommendationapp.MovieRecommendationApp
import com.yannis.thesis.movierecommendationapp.data.local.MovieRecommendedForUser
import com.yannis.thesis.movierecommendationapp.data.local.UserRatesMovie
import com.yannis.thesis.movierecommendationapp.data.remote.Movie
import com.yannis.thesis.movierecommendationapp.ui.components.LoginSignupScreen
import com.yannis.thesis.movierecommendationapp.ui.components.MainScreen
import com.yannis.thesis.movierecommendationapp.ui.components.MovieDetailScreen
import com.yannis.thesis.movierecommendationapp.ui.components.RecommendedMovieDetailScreen
import com.yannis.thesis.movierecommendationapp.ui.theme.MovieRecommendationTheme
import com.yannis.thesis.movierecommendationapp.ui.viewmodels.AuthViewModel
import com.yannis.thesis.movierecommendationapp.ui.viewmodels.AuthViewModelFactory
import com.yannis.thesis.movierecommendationapp.ui.viewmodels.MainViewModel
import com.yannis.thesis.movierecommendationapp.ui.viewmodels.MainViewModelFactory
import com.yannis.thesis.movierecommendationapp.ui.viewmodels.MovieDetailViewModel
import com.yannis.thesis.movierecommendationapp.ui.viewmodels.MovieDetailViewModelFactory
import com.yannis.thesis.movierecommendationapp.ui.viewmodels.RecommendedMovieDetailViewModel
import com.yannis.thesis.movierecommendationapp.ui.viewmodels.RecommendedMovieDetailViewModelFactory

private object Routes {
    const val LOGIN = "login"
    const val MAIN = "main/{userId}"
    const val MOVIE_DETAIL =
        "movie_detail/{userId}/{movieId}?title={title}&releaseDate={releaseDate}" +
            "&description={description}&posterPath={posterPath}"
    const val RECOMMENDED_MOVIE_DETAIL =
        "recommended_movie_detail/{userId}/{movieId}?title={title}&releaseDate={releaseDate}" +
            "&description={description}&posterPath={posterPath}"

    fun main(userId: String) = "main/${encode(userId)}"

    fun movieDetail(userId: String, movie: Movie) =
        "movie_detail/${encode(userId)}/${encode(movie.id?.toString())}" +
            "?title=${encode(movie.title)}&releaseDate=${encode(movie.releaseDate)}" +
            "&description=${encode(movie.overview)}&posterPath=${encode(movie.posterPath)}"

    fun movieDetail(userId: String, movie: UserRatesMovie) =
        "movie_detail/${encode(userId)}/${encode(movie.movieId)}" +
            "?title=${encode(movie.movie_title)}&releaseDate=${encode(movie.movie_release)}" +
            "&description=${encode(movie.movie_description)}&posterPath=${encode(movie.movie_poster)}"

    fun recommendedMovieDetail(userId: String, movie: MovieRecommendedForUser) =
        "recommended_movie_detail/${encode(userId)}/${encode(movie.movieId)}" +
            "?title=${encode(movie.movie_title)}&releaseDate=${encode(movie.movie_release)}" +
            "&description=${encode(movie.movie_description)}&posterPath=${encode(movie.movie_poster)}"
}

private fun encode(value: String?): String = Uri.encode(value.orEmpty())

@Composable
fun AppNavHost() {
    val app = LocalContext.current.applicationContext as MovieRecommendationApp
    val navController = rememberNavController()

    MovieRecommendationTheme {
        NavHost(navController = navController, startDestination = Routes.LOGIN) {
        composable(Routes.LOGIN) {
            val viewModel: AuthViewModel = viewModel(
                factory = AuthViewModelFactory(app.userRepository)
            )
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            var message by remember { mutableStateOf<String?>(null) }

            LoginSignupScreen(
                state = state,
                onLogin = viewModel::login,
                onSignup = viewModel::signup,
                onErrorMessageShown = {
                    message = state.errorMessage
                    viewModel.clearMessages()
                },
                onSuccessMessageShown = {
                    message = state.successMessage
                    viewModel.clearMessages()
                },
                onLoginSuccess = {
                    val userId = app.loggedInUserId
                    if (userId == null) {
                        message = "Unable to determine the logged-in user"
                        viewModel.clearLoginSuccess()
                    } else {
                        viewModel.clearLoginSuccess()
                        navController.navigate(Routes.main(userId)) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            )
            MessageDialog(message = message, onDismiss = { message = null })
        }

        composable(
            route = Routes.MAIN,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { entry ->
            val userId = entry.arguments?.getString("userId").orEmpty()
            val viewModel: MainViewModel = viewModel(
                factory = MainViewModelFactory(
                    userId = userId,
                    recommendationRepository = app.recommendationRepository,
                    ratingRepository = app.ratingRepository,
                    movieRepository = app.movieRepository
                )
            )
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            MainScreen(
                state = state,
                onSearch = viewModel::search,
                onMovieClick = { movie ->
                    navController.navigate(Routes.movieDetail(userId, movie))
                },
                onRatedMovieClick = { movie ->
                    navController.navigate(Routes.movieDetail(userId, movie))
                },
                onRecommendedMovieClick = { movie ->
                    navController.navigate(Routes.recommendedMovieDetail(userId, movie))
                },
                onClearError = viewModel::clearError
            )
        }

        composable(
            route = Routes.MOVIE_DETAIL,
            arguments = detailArguments()
        ) { entry ->
            val userId = entry.arguments?.getString("userId").orNull()
            val movieId = entry.arguments?.getString("movieId").orNull()
            val title = entry.arguments?.getString("title").orNull()
            val releaseDate = entry.arguments?.getString("releaseDate").orNull()
            val description = entry.arguments?.getString("description").orNull()
            val posterPath = entry.arguments?.getString("posterPath").orNull()
            val viewModel: MovieDetailViewModel = viewModel(
                factory = MovieDetailViewModelFactory(
                    ratingRepository = app.ratingRepository,
                    userId = userId,
                    movieId = movieId
                )
            )
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            var errorMessage by remember { mutableStateOf<String?>(null) }
            LaunchedEffect(state.errorMessage) {
                state.errorMessage?.let {
                    errorMessage = it
                    viewModel.clearError()
                }
            }
            MovieDetailScreen(
                title = title,
                releaseDate = releaseDate,
                description = description,
                posterPath = posterPath,
                state = state,
                onRatingSelected = { rating ->
                    viewModel.submitRating(
                        rating = rating,
                        posterPath = posterPath,
                        title = title,
                        description = description,
                        releaseDate = releaseDate
                    )
                }
            )
            MessageDialog(errorMessage, onDismiss = { errorMessage = null })
        }

        composable(
            route = Routes.RECOMMENDED_MOVIE_DETAIL,
            arguments = detailArguments()
        ) { entry ->
            val userId = entry.arguments?.getString("userId").orNull()
            val movieId = entry.arguments?.getString("movieId").orNull()
            val title = entry.arguments?.getString("title").orNull()
            val releaseDate = entry.arguments?.getString("releaseDate").orNull()
            val description = entry.arguments?.getString("description").orNull()
            val posterPath = entry.arguments?.getString("posterPath").orNull()
            val viewModel: RecommendedMovieDetailViewModel = viewModel(
                factory = RecommendedMovieDetailViewModelFactory(
                    ratingRepository = app.ratingRepository,
                    recommendationRepository = app.recommendationRepository,
                    userId = userId,
                    movieId = movieId
                )
            )
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            var errorMessage by remember { mutableStateOf<String?>(null) }
            LaunchedEffect(state.errorMessage) {
                state.errorMessage?.let {
                    errorMessage = it
                    viewModel.clearError()
                }
            }
            RecommendedMovieDetailScreen(
                title = title,
                releaseDate = releaseDate,
                description = description,
                posterPath = posterPath,
                state = state,
                onRatingSelected = { rating ->
                    viewModel.submitRating(
                        rating = rating,
                        posterPath = posterPath,
                        title = title,
                        description = description,
                        releaseDate = releaseDate
                    )
                }
            )
            MessageDialog(errorMessage, onDismiss = { errorMessage = null })
        }
    }
    }
}

private fun detailArguments() = listOf(
    navArgument("userId") { type = NavType.StringType },
    navArgument("movieId") { type = NavType.StringType },
    navArgument("title") { type = NavType.StringType; nullable = true; defaultValue = "" },
    navArgument("releaseDate") { type = NavType.StringType; nullable = true; defaultValue = "" },
    navArgument("description") { type = NavType.StringType; nullable = true; defaultValue = "" },
    navArgument("posterPath") { type = NavType.StringType; nullable = true; defaultValue = "" }
)

private fun String?.orNull(): String? = this?.takeUnless { it.isEmpty() }

@Composable
private fun MessageDialog(message: String?, onDismiss: () -> Unit) {
    if (message == null) return
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Movie Recommendation App") },
        text = { Text(message) },
        confirmButton = {
            Button(onClick = onDismiss) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Dismiss") }
        }
    )
}
