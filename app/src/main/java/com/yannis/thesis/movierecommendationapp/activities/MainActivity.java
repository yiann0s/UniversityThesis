package com.yannis.thesis.movierecommendationapp.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.yannis.thesis.movierecommendationapp.adapters.MainPagerAdapter;
import com.yannis.thesis.movierecommendationapp.databinding.MainActivityBinding;

public class MainActivity extends AppCompatActivity {

    private MainActivityBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = MainActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.mainViewpager.setAdapter(new MainPagerAdapter());
        binding.maintabs.setupWithViewPager(binding.mainViewpager);
    }

}
