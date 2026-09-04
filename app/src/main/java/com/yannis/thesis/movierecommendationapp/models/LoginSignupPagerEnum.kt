package com.yannis.thesis.movierecommendationapp.models

import com.yannis.thesis.movierecommendationapp.R

enum class LoginSignupPagerEnum(val titleResId: Int, val layoutResId: Int) {
    RED(R.string.login_str, R.layout.view_login),
    BLUE(R.string.signup_str, R.layout.view_signup)
}
