package com.yannis.thesis.movierecommendationapp.models

import com.yannis.thesis.movierecommendationapp.R

enum class MainPagerEnum(val titleResId: Int, val layoutResId: Int) {
    BLIP(R.string.main_tab_str, R.layout.view_main_tab),
    BLOOP(R.string.second_tab_str, R.layout.view_search_tab)
}
