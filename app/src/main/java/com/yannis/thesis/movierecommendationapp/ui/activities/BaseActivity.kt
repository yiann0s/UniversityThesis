package com.yannis.thesis.movierecommendationapp.ui.activities

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import com.yannis.thesis.movierecommendationapp.MovieRecommendationApp
import com.yannis.thesis.movierecommendationapp.databinding.CustomMsgBinding

open class BaseActivity : AppCompatActivity() {
    private var loadingDialog: Dialog? = null
    private var errorDialog: Dialog? = null
    var isActive = false
    var isBackground = false

    fun getApp(): MovieRecommendationApp = MovieRecommendationApp.getInstance()

    override fun onResume() {
        super.onResume()
        getApp().lastActivity = this
        isBackground = false
    }

    override fun onPause() {
        super.onPause()
        isBackground = true
    }

    override fun onDestroy() {
        loadingDialog?.dismiss()
        isActive = false
        super.onDestroy()
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    override fun finish() {
        isActive = false
        super.finish()
    }

    fun showErrorDialog(message: String) {
        val binding = CustomMsgBinding.inflate(layoutInflater)
        errorDialog = Dialog(this).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setCancelable(false)
            setOwnerActivity(this@BaseActivity)
            setContentView(binding.root)
        }
        binding.text.text = message
        binding.dialogButtonOK.setOnClickListener { errorDialog?.dismiss() }
        errorDialog?.show()
    }

    fun hideErrorDialog() {
        errorDialog?.dismiss()
    }
}
