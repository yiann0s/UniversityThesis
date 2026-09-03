package com.yannis.thesis.movierecommendationapp.activities;

import android.os.Bundle;

import com.yannis.thesis.movierecommendationapp.adapters.LoginSignupPagerAdapter;
import com.yannis.thesis.movierecommendationapp.databinding.LoginSignupActivityBinding;

/**
 * Created by yiannos on 13-Feb-18.
 */

public class LoginSignupActivity extends BaseActivity {

    private LoginSignupActivityBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = LoginSignupActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.loginsignupViewpager.setAdapter(new LoginSignupPagerAdapter());
        binding.tabs.setupWithViewPager(binding.loginsignupViewpager);
    }

}
