package com.yannis.thesis.movierecommendationapp.adapters;


import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.yannis.thesis.movierecommendationapp.MovieRecommendationApp;
import com.yannis.thesis.movierecommendationapp.models.Activities;
import com.yannis.thesis.movierecommendationapp.models.LoginSignupPagerEnum;
import com.yannis.thesis.movierecommendationapp.models.User;
import com.yannis.thesis.movierecommendationapp.databinding.ViewLoginBinding;
import com.yannis.thesis.movierecommendationapp.databinding.ViewSignupBinding;

import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
//import io.realm.Realm;
//import io.realm.RealmResults;

/**
 * Created by yiannos on 14-Nov-17.
 */

public class LoginSignupPagerAdapter extends PagerAdapter {


    EditText loginEmailTxt;


    EditText loginPasswordTxt;

    EditText signupPassword;

    EditText signupEmail;

    Realm realm;

    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {

        realm = Realm.getDefaultInstance();

        View view;
        LayoutInflater layoutinflater = LayoutInflater.from(container.getContext());
        if (position == 0) {
            ViewLoginBinding binding = ViewLoginBinding.inflate(layoutinflater, container, false);
            view = binding.getRoot();
            loginEmailTxt = binding.logEmail;
            loginPasswordTxt = binding.logPassword;
            binding.loginButton.setOnClickListener(v -> loginEvaluation());
        } else {
            ViewSignupBinding binding = ViewSignupBinding.inflate(layoutinflater, container, false);
            view = binding.getRoot();
            signupPassword = binding.signupPassword;
            signupEmail = binding.signupEmail;
            binding.registerButton.setOnClickListener(v -> signupEvaluation());
        }
        container.addView(view);
        return view;
    }


    @Override
    public void destroyItem(ViewGroup collection, int position, Object view) {
        collection.removeView((View) view);
    }

    @Override
    public int getCount() {
        return LoginSignupPagerEnum.values().length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        LoginSignupPagerEnum customPagerEnum = LoginSignupPagerEnum.values()[position];
        return MovieRecommendationApp.getInstance().getString(customPagerEnum.getTitleResId());
    }

    public void signupEvaluation() {
        if (signupEmail.getText().toString().isEmpty()) {
            MovieRecommendationApp.getInstance().lastActivity.showErrorDialog("Must provide an email address");
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(signupEmail.getText().toString()).matches()) {
            MovieRecommendationApp.getInstance().lastActivity.showErrorDialog("Must provide a valid email");
            return;
        }
        if (signupPassword.getText().toString().isEmpty()) {
            MovieRecommendationApp.getInstance().lastActivity.showErrorDialog("Mus provide a password");
            return;
        }
        if (!isEmailUnique(signupEmail.getText().toString())) {
            MovieRecommendationApp.getInstance().lastActivity.showErrorDialog("This email is already in use");
            return;
        }
        registerUser();
        MovieRecommendationApp.getInstance().lastActivity.showErrorDialog("Registration complete");
        emptyFields();
    }

    private boolean isEmailUnique(String s) {
        RealmQuery<User> query = realm.where(User.class)
                .equalTo("email", s);
        RealmResults<User> result = query.findAll();
        return result.size() == 0;

    }

    public void registerUser() {
        // Persist your data in a transaction
        realm.beginTransaction();
        final User user = realm.createObject(User.class,
                UUID.randomUUID().toString());
        user.setEmail(signupEmail.getText().toString());
        user.setPassword(signupPassword.getText().toString());
        realm.commitTransaction();
    }

    public void emptyFields() {
        signupEmail.setText("");
        signupPassword.setText("");
    }

    public void loginEvaluation() {
        if (loginEmailTxt.getText().toString().isEmpty()) {
            MovieRecommendationApp.getInstance().lastActivity.showErrorDialog("Email is required");
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(loginEmailTxt.getText().toString().trim()).matches()) {
            MovieRecommendationApp.getInstance().lastActivity.showErrorDialog("Must provide valid email");
            return;
        }
        if (loginPasswordTxt.getText().toString().isEmpty()) {
            MovieRecommendationApp.getInstance().lastActivity.showErrorDialog("Password is required");
            return;
        }
        validateUser(loginEmailTxt.getText().toString().trim(), loginPasswordTxt.getText().toString());
    }

    private void validateUser(String email, String password) {
        User userCheck = realm.where(User.class)
                .equalTo("email", email).findFirst();
        User passwordCheck = realm.where(User.class)
                .equalTo("password", password).findFirst();
        if (userCheck == null) {
            MovieRecommendationApp.getInstance().lastActivity.showErrorDialog("No such username exists");
            return;
        }
        if (passwordCheck == null) {
            MovieRecommendationApp.getInstance().lastActivity.showErrorDialog("Wrong password :S");
            return;
        }
        MovieRecommendationApp.getInstance().setLoggedInUserId(passwordCheck.getId());
        Log.d("MovieApp","Current user id logged in is " +
                MovieRecommendationApp.getInstance().getLoggedInUserId());
        Activities.Main.replace(MovieRecommendationApp.getInstance().lastActivity);

    }

}

