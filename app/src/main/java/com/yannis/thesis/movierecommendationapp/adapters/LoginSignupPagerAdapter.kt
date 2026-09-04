package com.yannis.thesis.movierecommendationapp.adapters

import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.viewpager.widget.PagerAdapter
import com.yannis.thesis.movierecommendationapp.MovieRecommendationApp
import com.yannis.thesis.movierecommendationapp.data.AppDatabase
import com.yannis.thesis.movierecommendationapp.databinding.ViewLoginBinding
import com.yannis.thesis.movierecommendationapp.databinding.ViewSignupBinding
import com.yannis.thesis.movierecommendationapp.models.Activities
import com.yannis.thesis.movierecommendationapp.models.LoginSignupPagerEnum
import com.yannis.thesis.movierecommendationapp.models.User
import java.util.UUID

class LoginSignupPagerAdapter : PagerAdapter() {
    private lateinit var loginEmailTxt: EditText
    private lateinit var loginPasswordTxt: EditText
    private lateinit var signupPassword: EditText
    private lateinit var signupEmail: EditText
    private lateinit var database: AppDatabase

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        database = AppDatabase.getInstance(container.context)
        val inflater = LayoutInflater.from(container.context)
        val view: View
        if (position == 0) {
            val binding = ViewLoginBinding.inflate(inflater, container, false)
            view = binding.root
            loginEmailTxt = binding.logEmail
            loginPasswordTxt = binding.logPassword
            binding.loginButton.setOnClickListener { loginEvaluation() }
        } else {
            val binding = ViewSignupBinding.inflate(inflater, container, false)
            view = binding.root
            signupPassword = binding.signupPassword
            signupEmail = binding.signupEmail
            binding.registerButton.setOnClickListener { signupEvaluation() }
        }
        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, view: Any) {
        container.removeView(view as View)
    }

    override fun getCount(): Int = LoginSignupPagerEnum.values().size

    override fun isViewFromObject(view: View, objectValue: Any): Boolean = view === objectValue

    override fun getPageTitle(position: Int): CharSequence {
        val page = LoginSignupPagerEnum.values()[position]
        return MovieRecommendationApp.getInstance().getString(page.titleResId)
    }

    fun signupEvaluation() {
        val email = signupEmail.text.toString()
        if (email.isEmpty()) {
            showError("Must provide an email address")
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError("Must provide a valid email")
            return
        }
        if (signupPassword.text.toString().isEmpty()) {
            showError("Mus provide a password")
            return
        }
        if (!isEmailUnique(email)) {
            showError("This email is already in use")
            return
        }
        registerUser()
        showError("Registration complete")
        emptyFields()
    }

    private fun showError(message: String) {
        MovieRecommendationApp.getInstance().lastActivity?.showErrorDialog(message)
    }

    private fun isEmailUnique(email: String): Boolean = database.userDao().findByEmail(email) == null

    fun registerUser() {
        database.userDao().insert(
            User(UUID.randomUUID().toString(), null, signupEmail.text.toString(), signupPassword.text.toString())
        )
    }

    fun emptyFields() {
        signupEmail.setText("")
        signupPassword.setText("")
    }

    fun loginEvaluation() {
        val email = loginEmailTxt.text.toString()
        if (email.isEmpty()) {
            showError("Email is required")
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            showError("Must provide valid email")
            return
        }
        if (loginPasswordTxt.text.toString().isEmpty()) {
            showError("Password is required")
            return
        }
        validateUser(email.trim(), loginPasswordTxt.text.toString())
    }

    private fun validateUser(email: String, password: String) {
        val userCheck = database.userDao().findByEmail(email)
        val passwordCheck = database.userDao().findByPassword(password)
        if (userCheck == null) {
            showError("No such username exists")
            return
        }
        if (passwordCheck == null) {
            showError("Wrong password :S")
            return
        }
        MovieRecommendationApp.getInstance().loggedInUserId = passwordCheck.id
        Log.d("MovieApp", "Current user id logged in is ${passwordCheck.id}")
        MovieRecommendationApp.getInstance().lastActivity?.let { Activities.Main.replace(it) }
    }
}
