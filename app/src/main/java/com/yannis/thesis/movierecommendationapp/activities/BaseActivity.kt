package com.yannis.thesis.movierecommendationapp.activities;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import androidx.appcompat.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Window;

import com.yannis.thesis.movierecommendationapp.MovieRecommendationApp;
import com.yannis.thesis.movierecommendationapp.databinding.CustomMsgBinding;

/**
 * Created by yiannos on 12-Feb-18.
 */

public class BaseActivity extends AppCompatActivity {

        private Dialog loadingDialog;
        boolean isActive = false;
        boolean isBackground = false;
        private Dialog errorDialog;

        public MovieRecommendationApp getApp() {
            return MovieRecommendationApp.getInstance();
        }


        @Override
        protected void onResume() {
            super.onResume();
            getApp().lastActivity = this;
            isBackground = false;
        }

        @Override
        protected void onPause() {
            super.onPause();
            isBackground = true;
        }

        @Override
        protected void onDestroy() {
            if (loadingDialog != null) {
                loadingDialog.dismiss();
            }

            isActive = false;
            super.onDestroy();
        }

        @Override
        public void onBackPressed() {
            super.onBackPressed();
        }

        @Override
        public void finish() {
            isActive = false;
            super.finish();
        }

        public void showErrorDialog(String message) {
            CustomMsgBinding binding = CustomMsgBinding.inflate(getLayoutInflater());
            errorDialog = new Dialog(this);
            errorDialog.getWindow().getCurrentFocus();
            errorDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

            errorDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            errorDialog.setCancelable(false);
            errorDialog.setOwnerActivity(this);
            errorDialog.setContentView(binding.getRoot());

            binding.text.setText(message);
            binding.dialogButtonOK.setOnClickListener(v -> errorDialog.dismiss());

            errorDialog.show();
        }

        public void hideErrorDialog() {
            CustomMsgBinding binding = CustomMsgBinding.inflate(getLayoutInflater());
            errorDialog = new Dialog(this);
            errorDialog.getWindow().getCurrentFocus();
            errorDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

            errorDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            errorDialog.setCancelable(false);
            errorDialog.setOwnerActivity(this);
            errorDialog.setContentView(binding.getRoot());
            if (errorDialog != null && errorDialog.isShowing()) {
                errorDialog.dismiss();
            }
        }
    }
