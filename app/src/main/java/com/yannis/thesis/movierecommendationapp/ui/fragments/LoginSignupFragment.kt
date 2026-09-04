package com.yannis.thesis.movierecommendationapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.Lifecycle
import com.yannis.thesis.movierecommendationapp.R
import com.yannis.thesis.movierecommendationapp.MovieRecommendationApp
import com.yannis.thesis.movierecommendationapp.databinding.LoginSignupActivityBinding
import com.yannis.thesis.movierecommendationapp.ui.activities.BaseActivity
import com.yannis.thesis.movierecommendationapp.ui.activities.MainActivity
import com.yannis.thesis.movierecommendationapp.ui.adapters.LoginSignupPagerAdapter
import com.yannis.thesis.movierecommendationapp.ui.viewmodels.AuthViewModel
import com.yannis.thesis.movierecommendationapp.ui.viewmodels.AuthViewModelFactory
import kotlinx.coroutines.launch

class LoginSignupFragment : Fragment() {
    private lateinit var pagerAdapter: LoginSignupPagerAdapter

    private val viewModel by lazy {
        ViewModelProvider(
            this,
            AuthViewModelFactory(MovieRecommendationApp.getInstance().userRepository)
        )[AuthViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.login_signup_activity, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = LoginSignupActivityBinding.bind(view)
        pagerAdapter = LoginSignupPagerAdapter(
            context = requireContext(),
            onLogin = viewModel::login,
            onSignup = viewModel::signup
        )
        binding.loginsignupViewpager.adapter = pagerAdapter
        binding.tabs.setupWithViewPager(binding.loginsignupViewpager)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    state.errorMessage?.let {
                        (activity as? BaseActivity)?.showErrorDialog(it)
                        viewModel.clearMessages()
                    }
                    state.successMessage?.let {
                        (activity as? BaseActivity)?.showErrorDialog(it)
                        pagerAdapter.clearSignupFields()
                        viewModel.clearMessages()
                    }
                    if (state.loginSuccess) {
                        (activity as? MainActivity)?.showMainFragment()
                        viewModel.clearLoginSuccess()
                    }
                }
            }
        }
    }
}
