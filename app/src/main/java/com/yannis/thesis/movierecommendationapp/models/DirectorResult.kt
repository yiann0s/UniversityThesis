package com.yannis.thesis.movierecommendationapp.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class DirectorResult(
    @SerializedName("popularity") @Expose var popularity: Double? = null,
    @SerializedName("id") @Expose var id: Int? = null,
    @SerializedName("profile_path") @Expose var profilePath: Any? = null,
    @SerializedName("name") @Expose var name: String? = null,
    @SerializedName("known_for") @Expose var knownFor: List<Movie>? = null,
    @SerializedName("adult") @Expose var adult: Boolean? = null
)
