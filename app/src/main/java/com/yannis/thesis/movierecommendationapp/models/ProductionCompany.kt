package com.yannis.thesis.movierecommendationapp.models

import com.google.gson.annotations.SerializedName

class ProductionCompany(
    @SerializedName("id") var id: Int? = null,
    @SerializedName("logo_path") var logoPath: String? = null,
    @SerializedName("name") var name: String? = null,
    @SerializedName("origin_country") var originCountry: String? = null
)
