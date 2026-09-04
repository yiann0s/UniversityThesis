package com.yannis.thesis.movierecommendationapp.models

import com.google.gson.annotations.SerializedName

class ProductionCountry(
    @SerializedName("iso_3166_1") var iso31661: String? = null,
    @SerializedName("name") var name: String? = null
)
