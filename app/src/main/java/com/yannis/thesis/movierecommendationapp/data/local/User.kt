package com.yannis.thesis.movierecommendationapp.data.local

import androidx.annotation.NonNull
import androidx.room.Entity
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
