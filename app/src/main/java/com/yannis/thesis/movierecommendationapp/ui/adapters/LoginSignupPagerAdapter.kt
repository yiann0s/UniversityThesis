package com.yannis.thesis.movierecommendationapp.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.viewpager.widget.PagerAdapter
import com.yannis.thesis.movierecommendationapp.databinding.ViewLoginBinding
import com.yannis.thesis.movierecommendationapp.databinding.ViewSignupBinding
import com.yannis.thesis.movierecommendationapp.domain.model.LoginSignupPagerEnum

class LoginSignupPagerAdapter(
    private val context: Context,
    private val onLogin: (String, String) -> Unit,
    private val onSignup: (String, String) -> Unit
) : PagerAdapter() {
    private var signupEmail: EditText? = null
    private var signupPassword: EditText? = null

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val inflater = LayoutInflater.from(container.context)
        val view: View
        if (position == 0) {
            val binding = ViewLoginBinding.inflate(inflater, container, false)
            view = binding.root
            binding.loginButton.setOnClickListener {
                onLogin(binding.logEmail.text.toString(), binding.logPassword.text.toString())
            }
        } else {
            val binding = ViewSignupBinding.inflate(inflater, container, false)
            view = binding.root
            signupEmail = binding.signupEmail
            signupPassword = binding.signupPassword
            binding.registerButton.setOnClickListener {
                onSignup(
                    binding.signupEmail.text.toString(),
                    binding.signupPassword.text.toString()
                )
            }
        }
        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, view: Any) {
        container.removeView(view as View)
    }

    override fun getCount(): Int = LoginSignupPagerEnum.entries.size

    override fun isViewFromObject(view: View, objectValue: Any): Boolean = view === objectValue

    override fun getPageTitle(position: Int): CharSequence =
        context.getString(LoginSignupPagerEnum.entries[position].titleResId)

    fun clearSignupFields() {
        signupEmail?.setText("")
        signupPassword?.setText("")
    }
}
