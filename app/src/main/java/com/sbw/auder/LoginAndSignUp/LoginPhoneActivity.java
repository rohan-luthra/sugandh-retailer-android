package com.sbw.auder.LoginAndSignUp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hbb20.CountryCodePicker;
import com.sbw.auder.R;
import com.sbw.auder.Utils.TinyDB;
import com.zplesac.connectionbuddy.ConnectionBuddy;
import com.zplesac.connectionbuddy.ConnectionBuddyConfiguration;
import com.zplesac.connectionbuddy.interfaces.ConnectivityChangeListener;
import com.zplesac.connectionbuddy.models.ConnectivityEvent;
import com.zplesac.connectionbuddy.models.ConnectivityState;

import java.util.Locale;

public class LoginPhoneActivity extends AppCompatActivity implements ConnectivityChangeListener {

    Button nextBtn;
    ImageView appLogoDesc;

    EditText userPhoneNumber;

    ProgressDialog progressDialog;

    CountryCodePicker ccp;

    String countryCode;

    boolean isNetwork = false;

    private static final String TAG = "LoginPhoneActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (new TinyDB(this).getString("Lang")!=null)
        {
            setLanguageForApp(new TinyDB(this).getString("Lang"), this);
        }
        setContentView(R.layout.activity_login_phone);

        nextBtn = findViewById(R.id.nextBtn);
        userPhoneNumber = findViewById(R.id.phoneNumber);
        progressDialog = new ProgressDialog(this);
        appLogoDesc=findViewById(R.id.nandi);
        fadeIn(appLogoDesc);


        setOnTouchListnerToView(nextBtn);
        ConnectionBuddyConfiguration networkInspectorConfiguration = new ConnectionBuddyConfiguration.Builder(this).build();
        ConnectionBuddy.getInstance().init(networkInspectorConfiguration);

        ccp = (CountryCodePicker) findViewById(R.id.ccp);
        try{
            ccp.setAutoDetectedCountry(true);
            countryCode = ccp.getSelectedCountryCodeWithPlus();
            ccp.setOnCountryChangeListener(new CountryCodePicker.OnCountryChangeListener() {
                @Override
                public void onCountrySelected() {
                    countryCode = ccp.getSelectedCountryCodeWithPlus();
                }
            });
        }catch (Exception e)
        {
            Log.d(TAG, "onCreate: "+e);
        }



        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (isNetwork) {

                    if (userPhoneNumber.getText() != null) {

                        if (!userPhoneNumber.getText().toString().isEmpty()) {

                            String phoneNumber = userPhoneNumber.getText().toString().trim();
                            if (android.util.Patterns.PHONE.matcher(phoneNumber).matches()) {


                                startActivity(new Intent(LoginPhoneActivity.this, OTPVerificationActivity.class)
                                        .putExtra("phone", countryCode + phoneNumber));
                                overridePendingTransition(R.anim.enter_right, android.R.anim.fade_out);

                            } else {
                                Toast.makeText(LoginPhoneActivity.this, "Enter a valid phone number", Toast.LENGTH_SHORT).show();

                            }
                        } else {
                              Toast.makeText(LoginPhoneActivity.this, "Please enter your phone number", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                         Toast.makeText(LoginPhoneActivity.this, "Please enter your phone number", Toast.LENGTH_SHORT).show();

                    }

                } else {
                    Toast.makeText(LoginPhoneActivity.this, ""+getResources().getString(R.string.no_internet_connection_snackbar), Toast.LENGTH_SHORT).show();

                }

            }
        });


    }


    @Override
    protected void onStart() {
        super.onStart();
        ConnectionBuddy.getInstance().registerForConnectivityEvents(this, this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        ConnectionBuddy.getInstance().unregisterFromConnectivityEvents(this);
    }

    @Override
    public void onConnectionChange(ConnectivityEvent event) {
        if (event.getState().getValue() == ConnectivityState.CONNECTED) {
            // device has active internet connection
            isNetwork = true;
        } else {
            // there is no active internet connection on this device
            isNetwork = false;

        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }


    Animation fadeIn = new AlphaAnimation(0, 1);
    Animation fadeOut = new AlphaAnimation(1, 0);

    public void fadeIn(final ImageView appLogoDesc) {

        fadeIn.setInterpolator(new AccelerateInterpolator());
        fadeIn.setDuration(1000);
        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                fadeOut(appLogoDesc);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        appLogoDesc.startAnimation(fadeIn);

    }

    public void fadeOut(final ImageView appLogoDesc) {
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setDuration(1000);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                fadeIn(appLogoDesc);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        appLogoDesc.startAnimation(fadeOut);

    }
    private void setLanguageForApp(String languageToLoad, Activity activity){
        Locale locale;
        if(languageToLoad.equals("not-set")){ //use any value for default
            locale = Locale.getDefault();
        }
        else {
            locale = new Locale(languageToLoad);
        }
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        activity.getBaseContext().getResources().updateConfiguration(config,
                activity.getBaseContext().getResources().getDisplayMetrics());

    }
    private void setOnTouchListnerToView(final Button target)
    {
        target.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    target.setTextColor(Color.parseColor("#80ffffff"));

                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    target.setTextColor(getResources().getColor(R.color.grey_11));
                }
                return false;
            }
        });
    }

}
