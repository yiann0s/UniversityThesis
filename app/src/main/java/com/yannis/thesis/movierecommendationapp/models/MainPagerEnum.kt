package com.yannis.thesis.movierecommendationapp.models;

/**
 * Created by yiannos on 14-Feb-18.
 */

import com.yannis.thesis.movierecommendationapp.R;

public enum MainPagerEnum {

    BLIP(R.string.main_tab_str, R.layout.view_main_tab),
    BLOOP(R.string.second_tab_str, R.layout.view_search_tab);

    private int mTitleResId;
    private int mLayoutResId;

    MainPagerEnum(int titleResId, int layoutResId) {
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
