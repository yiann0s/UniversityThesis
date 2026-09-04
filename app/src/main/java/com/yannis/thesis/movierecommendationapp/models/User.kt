package com.yannis.thesis.movierecommendationapp.models

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity
class User constructor(
    @PrimaryKey
    @NonNull
    var id: String = "",
    var username: String? = null,
    var email: String? = null,
    var password: String? = null
)
