package com.yannis.thesis.movierecommendationapp.models;

import android.app.Activity;
import android.content.Intent;

import com.yannis.thesis.movierecommendationapp.activities.BaseActivity;
import com.yannis.thesis.movierecommendationapp.activities.MainActivity;
import com.yannis.thesis.movierecommendationapp.activities.LoginSignupActivity;
import com.yannis.thesis.movierecommendationapp.R;

/**
 * Created by valexandrof on 06/11/2017.
 */

public enum Activities {
    Base(BaseActivity.class),
    LoginNSingUp(LoginSignupActivity.class),
    Main(MainActivity.class);

    public Class<?> activityClass;

    Activities(Class<?> activityClass) { this.activityClass = activityClass; }

    public void open(Activity from) {
        Intent intent = new Intent(from, activityClass);
        from.startActivity(intent);
        from.overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
    }

    public void replace(Activity from) {
        from.finish();
        open(from);
    }

    public void openOrReplace(boolean isOpen, Activity from) {
        if (isOpen) {
            open(from);
        } else {
            replace(from);
        }
    }
}
