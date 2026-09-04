package com.yannis.thesis.movierecommendationapp.models;

/**
 * Created by yiannos on 14-Feb-18.
 */

import com.yannis.thesis.movierecommendationapp.R;

public enum LoginSignupPagerEnum {

    RED(R.string.login_str, R.layout.view_login),
    BLUE(R.string.signup_str, R.layout.view_signup);

    private int mTitleResId;
    private int mLayoutResId;

    LoginSignupPagerEnum(int titleResId, int layoutResId) {
        mTitleResId = titleResId;
        mLayoutResId = layoutResId;
    }

    public int getTitleResId() {
        return mTitleResId;
    }

    public int getLayoutResId() {
        return mLayoutResId;
    }
}
