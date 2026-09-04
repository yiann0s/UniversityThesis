package com.yannis.thesis.movierecommendationapp.ui.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object BrandColors {
    val PrimaryBlue = Color(0xFF4897D8)
    val DarkBlue = Color(0xFF1F5F93)
    val Coral = Color(0xFFFA6E59)
    val DarkCoral = Color(0xFFF72808)
    val Yellow = Color(0xFFFFDB5C)
    val Orange = Color(0xFFF8A055)
    val Text = Color(0xFF555555)
    val MutedText = Color(0xFF707070)
    val LightBackground = Color(0xFFF7FAFC)
    val LoginField = Color(0xFF2D73A9)
    val SearchBackground = Color(0xFFE5F1FA)
    val RecommendationsBackground = Color(0xFFEAF4FC)
    val RatedBackground = Color(0xFFFFF1E8)
}

private val AppLightColors = lightColors(
    primary = BrandColors.PrimaryBlue,
    primaryVariant = BrandColors.DarkBlue,
    secondary = BrandColors.Coral,
    secondaryVariant = BrandColors.DarkCoral,
    background = BrandColors.LightBackground,
    surface = Color.White,
    error = BrandColors.DarkCoral,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = BrandColors.Text,
    onSurface = BrandColors.Text,
    onError = Color.White
)

@Composable
fun MovieRecommendationTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colors = AppLightColors,
        typography = MaterialTheme.typography,
        shapes = MaterialTheme.shapes,
        content = content
    )
}
