package com.yannis.thesis.movierecommendationapp.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class GenreResponse(
    @SerializedName("genres") @Expose var genres: List<Genre>? = null
)
