package com.yannis.thesis.movierecommendationapp.activities;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;

import com.yannis.thesis.movierecommendationapp.MovieRecommendationApp;
import com.yannis.thesis.movierecommendationapp.adapters.MainPagerAdapter;
import com.yannis.thesis.movierecommendationapp.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.maintabs)
    TabLayout tabLayout;

    @BindView(R.id.main_viewpager)
    ViewPager vp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        ButterKnife.bind(this);

        vp.setAdapter(new MainPagerAdapter());
        tabLayout.setupWithViewPager(vp);
    }

}
