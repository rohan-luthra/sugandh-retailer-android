package com.sbw.auder;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.sbw.auder.HomeScreen.MainActivity;
import com.sbw.auder.LoginAndSignUp.LoginPhoneActivity;
import com.sbw.auder.Models.Token;
import com.sbw.auder.Models.UserModel;
import com.sbw.auder.Utils.TinyDB;

public class SplashScreenActivity extends AppCompatActivity {

    ImageView logo;
    ImageView welcomeText;

    TinyDB tinyDB;

    private static final String TAG = "SplashScreenActivity";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        overridePendingTransition(android.R.anim.bounce_interpolator, android.R.anim.fade_out);


        logo = findViewById(R.id.logo);
        welcomeText = findViewById(R.id.welcomeText);
        tinyDB = new TinyDB(this);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                logo.setVisibility(View.VISIBLE);
                logo.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fadein));
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        welcomeText.setVisibility(View.VISIBLE);
                        welcomeText.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fadein));

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                try {
                                    if (tinyDB.getString(getResources().getString(R.string.authToken)) != null) {
                                        if (!tinyDB.getString(getResources().getString(R.string.authToken)).isEmpty()) {
                                            Token.setAuthToken(tinyDB.getString(getResources().getString(R.string.authToken)));
                                            if (tinyDB.getObject("user", UserModel.class) != null) {
                                                if (tinyDB.getObject("user", UserModel.class).getId() != null) {
                                                    Log.d(TAG, "run: " + tinyDB.getObject("user", UserModel.class));
                                                    if(FirebaseAuth.getInstance().getCurrentUser()!=null)
                                                        startActivity(new Intent(SplashScreenActivity.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                                                    else
                                                        startActivity(new Intent(SplashScreenActivity.this, LoginPhoneActivity.class)
                                                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                                                } else {
                                                    startActivity(new Intent(SplashScreenActivity.this, LoginPhoneActivity.class)
                                                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                                                }
                                            } else {
                                                startActivity(new Intent(SplashScreenActivity.this, LoginPhoneActivity.class)
                                                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                                            }

                                        } else {
                                            startActivity(new Intent(SplashScreenActivity.this, LoginPhoneActivity.class)
                                                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                                        }

                                    } else {
                                        startActivity(new Intent(SplashScreenActivity.this, LoginPhoneActivity.class)
                                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                                    }
                                }catch (Exception e){
                                    startActivity(new Intent(SplashScreenActivity.this, LoginPhoneActivity.class)
                                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                                }




                            }
                        },800);


                    }
                },400);


            }
        }, 400);



    }
}
