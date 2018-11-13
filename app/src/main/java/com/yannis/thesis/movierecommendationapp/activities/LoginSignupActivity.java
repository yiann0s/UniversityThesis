package com.yannis.thesis.movierecommendationapp.activities;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.support.design.widget.TabLayout;

import com.yannis.thesis.movierecommendationapp.adapters.LoginSignupPagerAdapter;
import com.yannis.thesis.movierecommendationapp.R;


import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by yiannos on 13-Feb-18.
 */

public class LoginSignupActivity extends BaseActivity {

    @BindView(R.id.loginsignup_viewpager)
    ViewPager vp;

    @BindView(R.id.tabs)
    TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.login_signup_activity);

        ButterKnife.bind(this);

        vp.setAdapter(new LoginSignupPagerAdapter());

        tabLayout.setupWithViewPager(vp);
    }

}