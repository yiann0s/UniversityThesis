package com.yannis.thesis.movierecommendationapp.models

import com.google.gson.annotations.SerializedName

class SpokenLanguage(
    @SerializedName("iso_639_1") var iso6391: String? = null,
    @SerializedName("name") var name: String? = null
)
