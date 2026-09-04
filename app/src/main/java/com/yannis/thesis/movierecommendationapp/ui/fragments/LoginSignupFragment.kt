package com.yannis.thesis.movierecommendationapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yannis.thesis.movierecommendationapp.MovieRecommendationApp
import com.yannis.thesis.movierecommendationapp.ui.activities.BaseActivity
import com.yannis.thesis.movierecommendationapp.ui.activities.MainActivity
import com.yannis.thesis.movierecommendationapp.ui.components.LoginSignupScreen
import com.yannis.thesis.movierecommendationapp.ui.viewmodels.AuthViewModel
import com.yannis.thesis.movierecommendationapp.ui.viewmodels.AuthViewModelFactory

class LoginSignupFragment : Fragment() {
    private lateinit var composeView: ComposeView

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
    ): View = ComposeView(requireContext()).also {
        composeView = it
        it.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        composeView.setContent {
            val state = viewModel.uiState.collectAsStateWithLifecycle().value
            LoginSignupScreen(
                state = state,
                onLogin = viewModel::login,
                onSignup = viewModel::signup,
                onErrorMessageShown = {
                    (activity as? BaseActivity)?.showErrorDialog(state.errorMessage.orEmpty())
                    viewModel.clearMessages()
                },
                onSuccessMessageShown = {
                    (activity as? BaseActivity)?.showErrorDialog(state.successMessage.orEmpty())
                    viewModel.clearMessages()
                },
                onLoginSuccess = {
                    (activity as? MainActivity)?.showMainFragment()
                    viewModel.clearLoginSuccess()
                }
            )
        }
    }
}
