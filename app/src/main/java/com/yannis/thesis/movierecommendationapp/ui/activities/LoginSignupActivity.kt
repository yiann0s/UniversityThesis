package com.yannis.thesis.movierecommendationapp.ui.activities

import android.os.Bundle
import com.yannis.thesis.movierecommendationapp.ui.adapters.LoginSignupPagerAdapter
import com.yannis.thesis.movierecommendationapp.databinding.LoginSignupActivityBinding

class LoginSignupActivity : BaseActivity() {
    private lateinit var binding: LoginSignupActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LoginSignupActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.loginsignupViewpager.adapter = LoginSignupPagerAdapter()
        binding.tabs.setupWithViewPager(binding.loginsignupViewpager)
    }
}
