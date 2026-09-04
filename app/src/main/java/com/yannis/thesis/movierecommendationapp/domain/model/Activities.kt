package com.yannis.thesis.movierecommendationapp.domain.model

import android.app.Activity
import android.content.Intent
import com.yannis.thesis.movierecommendationapp.R
import com.yannis.thesis.movierecommendationapp.ui.activities.BaseActivity
import com.yannis.thesis.movierecommendationapp.ui.activities.LoginSignupActivity
import com.yannis.thesis.movierecommendationapp.ui.activities.MainActivity

enum class Activities(@JvmField val activityClass: Class<*>) {
    Base(BaseActivity::class.java),
    LoginNSingUp(LoginSignupActivity::class.java),
    Main(MainActivity::class.java);

    fun open(from: Activity) {
        from.startActivity(Intent(from, activityClass))
        from.overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
    }

    fun replace(from: Activity) {
        from.finish()
        open(from)
    }

    fun openOrReplace(isOpen: Boolean, from: Activity) {
        if (isOpen) open(from) else replace(from)
    }
}
