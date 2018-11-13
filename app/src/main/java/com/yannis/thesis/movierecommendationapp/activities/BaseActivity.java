package com.yannis.thesis.movierecommendationapp.activities;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.yannis.thesis.movierecommendationapp.MovieRecommendationApp;
import com.yannis.thesis.movierecommendationapp.R;

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
            errorDialog = new Dialog(this);
            errorDialog.getWindow().getCurrentFocus();
            errorDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

            errorDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            errorDialog.setCancelable(false);
            errorDialog.setOwnerActivity(this);
            errorDialog.setContentView(R.layout.custom_msg);

            // set the custom dialog components - text, image and button
            TextView text = errorDialog.findViewById(R.id.text);
            text.setText(message);
            Button dialogButton = errorDialog.findViewById(R.id.dialogButtonOK);
            // if button is clicked, close the custom dialog
            dialogButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    errorDialog.dismiss();
                }
            });

            errorDialog.show();
        }

        public void hideErrorDialog() {
            errorDialog = new Dialog(this);
            errorDialog.getWindow().getCurrentFocus();
            errorDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

            errorDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            errorDialog.setCancelable(false);
            errorDialog.setOwnerActivity(this);
            errorDialog.setContentView(R.layout.custom_msg);
            if (errorDialog != null && errorDialog.isShowing()) {
                errorDialog.dismiss();
            }
        }
    }
