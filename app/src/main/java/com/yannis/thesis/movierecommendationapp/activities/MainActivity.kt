package com.yannis.thesis.movierecommendationapp.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.yannis.thesis.movierecommendationapp.adapters.MainPagerAdapter
import com.yannis.thesis.movierecommendationapp.databinding.MainActivityBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: MainActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MainActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.mainViewpager.adapter = MainPagerAdapter()
        binding.maintabs.setupWithViewPager(binding.mainViewpager)
    }
}
