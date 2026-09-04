package com.yannis.thesis.movierecommendationapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.yannis.thesis.movierecommendationapp.R
import com.yannis.thesis.movierecommendationapp.databinding.LoginSignupActivityBinding
import com.yannis.thesis.movierecommendationapp.ui.activities.BaseActivity
import com.yannis.thesis.movierecommendationapp.ui.activities.MainActivity
import com.yannis.thesis.movierecommendationapp.ui.adapters.LoginSignupPagerAdapter

class LoginSignupFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.login_signup_activity, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = LoginSignupActivityBinding.bind(view)
        binding.loginsignupViewpager.adapter = LoginSignupPagerAdapter(
            onLoginSuccess = { (activity as? MainActivity)?.showMainFragment() },
            onError = { message -> (activity as? BaseActivity)?.showErrorDialog(message) }
        )
        binding.tabs.setupWithViewPager(binding.loginsignupViewpager)
    }
}
