package com.sbw.auder.LoginAndSignUp;

import android.app.Activity;
import android.app.MediaRouteButton;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.sbw.auder.HomeScreen.MainActivity;
import com.sbw.auder.Models.Token;
import com.sbw.auder.Models.UserModel;
import com.sbw.auder.R;
import com.sbw.auder.Utils.GetData;
import com.sbw.auder.Utils.TinyDB;
import com.zplesac.connectionbuddy.ConnectionBuddy;
import com.zplesac.connectionbuddy.ConnectionBuddyConfiguration;
import com.zplesac.connectionbuddy.interfaces.ConnectivityChangeListener;
import com.zplesac.connectionbuddy.models.ConnectivityEvent;
import com.zplesac.connectionbuddy.models.ConnectivityState;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class OTPVerificationActivity extends AppCompatActivity implements ConnectivityChangeListener {

    private static final String TAG = "OTPVerificationActivity";
    private static final long AUTOVERIFYTEXTDURATION = 15000;

    EditText otp;

    Button nextButton;
    ImageView appLogoDesc;

    TextView otpForNumberMessage,
            resendBtn;

    String phone;
    boolean isNetwork = false;


    ProgressDialog progressDialog;
    PhoneAuthProvider.ForceResendingToken mResendToken;
    FirebaseAuth mAuth;

    private String mVerificationId;
    private int ENABLE_RESEND_OTP_AFTER_SECONDS=30;
    int counter=ENABLE_RESEND_OTP_AFTER_SECONDS;
    private long OTP_TIMEOUT_DURATION=90;
    private TextView fadeinoutMsg;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (new TinyDB(this).getString("Lang")!=null)
        {
            setLanguageForApp(new TinyDB(this).getString("Lang"), this);
        }
        setContentView(R.layout.activity_otpverification);

        mAuth = FirebaseAuth.getInstance();
        ConnectionBuddyConfiguration networkInspectorConfiguration = new ConnectionBuddyConfiguration.Builder(this).build();
        ConnectionBuddy.getInstance().init(networkInspectorConfiguration);
        fadeinoutMsg=findViewById(R.id.forFadeInOut);
        new CountDownTimer(AUTOVERIFYTEXTDURATION, AUTOVERIFYTEXTDURATION) {
            @Override
            public void onTick(long millisUntilFinished) {
                fadeinoutMsg.setVisibility(View.VISIBLE);
                fadeinoutMsg.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fadeinout));
            }

            @Override
            public void onFinish() {

                try{
                    fadeinoutMsg.clearAnimation();
                    fadeinoutMsg.setText(getResources().getString(R.string.verificationMsgOTPtimeExceed));
                }catch (Exception e)
                {
                }
            }
        }.start();


        progressDialog = new ProgressDialog(this);
        progressDialog.setCanceledOnTouchOutside(true);
        progressDialog.setCancelable(true);
        appLogoDesc=findViewById(R.id.nandi);
        fadeIn(appLogoDesc);

        phone = getIntent().getStringExtra("phone");

        otp = findViewById(R.id.otp);
        nextButton = findViewById(R.id.nextBtn);
        resendBtn = findViewById(R.id.resendBtn);
        otpForNumberMessage = findViewById(R.id.phoneTxt);
        otpForNumberMessage.setText(getResources().getString(R.string.otpsentmsg)+" " + phone);



        progressDialog.setMessage(getResources().getString(R.string.sending_otp));
        progressDialog.show();
        sendVerificationCode(phone);
        resendBtn.setVisibility(View.VISIBLE);
        resendBtn.setEnabled(false);

        setOnTouchListnerToView(nextButton);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(isNetwork) {

                    if (!otp.getText().toString().isEmpty()) {
                        try{
                            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, otp.getText().toString().trim());
                            signInWithPhoneAuthCredential(credential);

                        }catch (Exception e){
                            Toast.makeText(OTPVerificationActivity.this, ""+e, Toast.LENGTH_SHORT).show();
                        }

                    } else {
                         Toast.makeText(OTPVerificationActivity.this, "Please enter OTP", Toast.LENGTH_SHORT).show();
                    }
                }else{
                   // Snackbar.make(findViewById(android.R.id.content), getResources().getString(R.string.no_internet_connection_snackbar), Snackbar.LENGTH_LONG).show();
                    Toast.makeText(OTPVerificationActivity.this, ""+getResources().getString(R.string.no_internet_connection_snackbar), Toast.LENGTH_SHORT).show();

                }

            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                resendBtn.setVisibility(View.VISIBLE);
                resendBtn.setEnabled(true);
            }
        },ENABLE_RESEND_OTP_AFTER_SECONDS*1000);
        resendBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    resendBtn.setAlpha(.5f);

                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    resendBtn.setAlpha(1f);
                }
                return false;
            }
        });
        new CountDownTimer(ENABLE_RESEND_OTP_AFTER_SECONDS*1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                resendBtn.setText(getResources().getString(R.string.resendOtpIn)+" "+counter+"s");
                counter--;
            }

            @Override
            public void onFinish() {
                resendBtn.setText(getResources().getString(R.string.resendOtpNow));
                counter=ENABLE_RESEND_OTP_AFTER_SECONDS;

            }
        }.start();

        resendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(isNetwork) {
                    progressDialog.setMessage(getResources().getString(R.string.sending_otp));
                    progressDialog.show();
                    sendVerificationCode(phone);
                }else{
                    Snackbar.make(findViewById(android.R.id.content), getResources().getString(R.string.no_internet_connection_snackbar), Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    private void sendVerificationCode(String phone) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(phone,        // Phone number to verify
                OTP_TIMEOUT_DURATION,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                OTPVerificationActivity.this,               // Activity (for callback binding)
                mCallbacks);
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onVerificationCompleted(PhoneAuthCredential credential) {
            Log.d(TAG, "onVerificationCompleted:" + credential);
            signInWithPhoneAuthCredential(credential);

        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            Log.w(TAG, "onVerificationFailed", e);
            if (e instanceof FirebaseAuthInvalidCredentialsException) {
                Log.d(TAG, "onVerificationFailed: " + e.getLocalizedMessage());

            } else if (e instanceof FirebaseTooManyRequestsException) {
                Log.d(TAG, "onVerificationFailed: " + e.getLocalizedMessage());
            }

            finish();
            overridePendingTransition(R.anim.exit_to_left, android.R.anim.fade_out);

        }

        @Override
        public void onCodeAutoRetrievalTimeOut(String s) {
            super.onCodeAutoRetrievalTimeOut(s);

            progressDialog.cancel();

        }

        @Override
        public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
            // The SMS verification code has been sent to the provided phone number, we
            // now need to ask the user to enter the code and then construct a credential
            // by combining the code with a verification ID.
            Log.d(TAG, "onCodeSent:" + verificationId);
          //  Toast.makeText(OTPVerificationActivity.this, "OTP sent", Toast.LENGTH_SHORT).show();
            progressDialog.cancel();
            // Save verification ID and resending token so we can use them later
            mVerificationId = verificationId;
            mResendToken = token;

            // ...
        }


    };


    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {


        progressDialog.setMessage(getResources().getString(R.string.verifying));
        try{
            progressDialog.show();

        }catch (Exception e)
        {

        }

            mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success");
                        //  Toast.makeText(OTPVerificationActivity.this, "OTP SUCCESS", Toast.LENGTH_SHORT).show();

                        try{
                            progressDialog.cancel();

                        }catch (Exception e)
                        {

                        }
                    checkUser(task.getResult().getUser().getPhoneNumber());

                } else {
                    progressDialog.cancel();
                    // Sign in failed, display a message and update the UI
                    Log.w(TAG, "signInWithCredential:failure", task.getException());
                    if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                        Toast.makeText(OTPVerificationActivity.this, "" + getResources().getString(R.string.toast_otp_incorrect), Toast.LENGTH_SHORT).show();

                        }
                    }
                }
            });

    }

    private void checkUser(final String phone) {

        progressDialog.setMessage(getResources().getString(R.string.loading));
        progressDialog.show();
        GetData getData = new GetData(new GetData.AsyncResponse() {
            @Override
            public void processFinish(String output) throws JSONException {

                Log.d(TAG, "processFinish: checkUser" + output);
                JSONObject jsonObject = new JSONObject(output);
                if (jsonObject.getBoolean("status")) {

                    if (jsonObject.getJSONArray("msg") != null) {
                        if (jsonObject.getJSONArray("msg").length() > 0) {
                            loginUser(phone);
                        } else {
                            registerNewUser(phone);
                        }
                    }

                } else {
                    progressDialog.cancel();
                 //   Toast.makeText(OTPVerificationActivity.this, "Error Occured", Toast.LENGTH_SHORT).show();
                }

            }
        });

        getData.execute(getResources().getString(R.string.localhost) + "/api/adminSugandh/retailer/" + phone);

    }

    private void registerNewUser(String phone) {

        progressDialog.setMessage(getResources().getString(R.string.registering));
        progressDialog.show();

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("_id", phone);
            JSONObject finalData = new JSONObject();
            finalData.put("data", jsonObject);

            sendPost(getResources().getString(R.string.localhost) + "/api/adminSugandh" + "/retailer/insert", finalData);


        } catch (Exception e) {
            e.printStackTrace();
            // Toast.makeText(this, "Some Eror Occured", Toast.LENGTH_SHORT).show();
        }


    }

    public void sendPost(final String urlAdress, final JSONObject data) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(urlAdress);
                    Log.d(TAG, "run: " + urlAdress);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setDoOutput(true);
                    conn.setDoInput(true);

                    Log.d(TAG, "run: " + data.toString());
                    DataOutputStream os = new DataOutputStream(conn.getOutputStream());
//                    os.writeBytes(URLEncoder.encode(data.toString(), "UTF-8"));
                    os.writeBytes(data.toString());
                    os.flush();
                    os.close();

                    Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                    StringBuilder sb = new StringBuilder();
                    for (int c; (c = in.read()) >= 0; )
                        sb.append((char) c);
                    final String response = sb.toString();

                    Log.i("STATUS", String.valueOf(conn.getResponseCode()));
                    Log.i("MSG123", response);

                    final JSONObject jsonObject = new JSONObject(response);
//
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.cancel();

                            try {
                                if (jsonObject.getBoolean("status")) {
//                                    Toast.makeText(OTPVerificationActivity.this, "Success", Toast.LENGTH_SHORT).show();
                                    String authToken = jsonObject.getString("token");
                                    new TinyDB(getApplicationContext()).putString(getResources().getString(R.string.authToken), authToken);
                                    Token.setAuthToken(authToken);
                                    JSONObject userData = jsonObject.getJSONArray("msg").getJSONObject(0);

                                    Log.d(TAG, "run: authToken " + authToken);
                                    Log.d(TAG, "run: userData" + userData);
                                    TinyDB tinyDB = new TinyDB(getApplicationContext());
                                    UserModel userModel = new UserModel();
                                    userModel.setId(userData.getString("_id"));
                                    Token.setAuthToken(tinyDB.getString(getResources().getString(R.string.authToken)));
                                    if (userData.has("firstName") && userData.has("LastName")) {
                                        if (userData.has("firstName")) {
                                            userModel.setFirstName(userData.getString("firstName"));
                                        }
                                        if (userData.has("LastName")) {
                                            userModel.setLastName(userData.getString("LastName"));
                                        }
                                        if (userData.has("birthdate")) {
                                            userModel.setBirthdate(userData.getString("birthdate"));
                                        }
                                        if (userData.has("email")) {
                                            userModel.setEmail(userData.getString("email"));
                                        }
                                        if (userData.has("latitude")) {
                                            userModel.setLatitude(userData.getDouble("latitude"));
                                        }
                                        if (userData.has("longitude")) {
                                            userModel.setLongitude(userData.getDouble("longitude"));
                                        }
                                        tinyDB.putObject("user", userModel);
                                    }

                                    loginUser(phone);


                                } else {

                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    });

                    conn.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        thread.start();
    }

    private void loginUser(String phone) {

        progressDialog.setMessage(getResources().getString(R.string.logging_in));
        progressDialog.show();

        try {
            JSONObject data = new JSONObject();
            data.put("id", phone);
            sendLoginPost(getResources().getString(R.string.localhost) + "/api/retailer/login", data);

        } catch (JSONException e) {
            e.printStackTrace();
          //  Toast.makeText(this, "Error Logging In", Toast.LENGTH_SHORT).show();
            finish();
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.fade_out);

        }

    }

    public void sendLoginPost(final String urlAdress, final JSONObject data) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(urlAdress);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setDoOutput(true);
                    conn.setDoInput(true);

                    Log.d(TAG, "run:1 " + data.toString());
                    DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                    os.writeBytes(data.toString());
                    os.flush();
                    os.close();

                    Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                    StringBuilder sb = new StringBuilder();
                    for (int c; (c = in.read()) >= 0; )
                        sb.append((char) c);
                    final String response = sb.toString();

                    Log.i("STATUS", String.valueOf(conn.getResponseCode()));
                    Log.i("MSG123", response);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "run: " + response);

                            try {

                                JSONObject userDataJson = new JSONObject(response);
                                Log.d(TAG, "run: " + userDataJson);
                                if (userDataJson.has("status")) {

                                    if (userDataJson.getInt("status") == 200) {
                                        Log.d(TAG, "run: success");

                                        String token = userDataJson.getJSONObject("msg").getString("token");
                                        final JSONObject userData = userDataJson.getJSONObject("msg").getJSONObject("userData");
                                        TinyDB tinyDB = new TinyDB(getApplicationContext());
                                        tinyDB.putString(getResources().getString(R.string.authToken), token);
                                        Token.setAuthToken(token);

                                        FirebaseInstanceId.getInstance().getInstanceId()
                                                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                                        if (!task.isSuccessful()) {
                                                            Log.w(TAG, "getInstanceId failed", task.getException());
                                                            return;
                                                        }
                                                        // Get new Instance ID token
                                                        try {
                                                            String token = task.getResult().getToken();
                                                            JSONObject tokenData = new JSONObject();
                                                            tokenData.put("id", userData.getString("_id"));
                                                            tokenData.put("token", token);
                                                            Log.d(TAG, "onComplete: " + token);
                                                            updateToken(getResources().getString(R.string.localhost) + "/api/" + Token.getAuthToken() + "/retailer/firebaseToken/", tokenData, userData.toString());
                                                        } catch (JSONException e) {
                                                            e.printStackTrace();
                                                        }

                                                    }
                                                });


                                    }

                                }


                            } catch (Exception e) {
                                e.printStackTrace();

                            }
                            progressDialog.cancel();
                        }
                    });

                    conn.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        });

        thread.start();
    }


    public void updateToken(final String urlAdress, final JSONObject data, final String userData) {

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(urlAdress);
                    final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setDoOutput(true);
                    conn.setDoInput(true);

                    Log.d(TAG, "run: " + data.toString());
                    DataOutputStream os = new DataOutputStream(conn.getOutputStream());
//                    os.writeBytes(URLEncoder.encode(data.toString(), "UTF-8"));
                    os.writeBytes(data.toString());
                    os.flush();
                    os.close();

                    Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                    StringBuilder sb = new StringBuilder();
                    for (int c; (c = in.read()) >= 0; )
                        sb.append((char) c);
                    final String response = sb.toString();

                    Log.i("MSG123", response);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "run: " + response);
//                            ageText.setText(response);
                            try {

                                if (response.equals("Ok")) {


                                    Intent intent = new Intent(OTPVerificationActivity.this, MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

                                    TinyDB tinyDB = new TinyDB(getApplicationContext());
                                    tinyDB.putString("userData", userData);

                                    startActivity(intent);
                                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

                                } else {

                                }

                            } catch (Exception e) {

                            }
                            progressDialog.cancel();
                        }
                    });

                    conn.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        });

        thread.start();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.fade_out);

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
        if(event.getState().getValue() == ConnectivityState.CONNECTED){
            // device has active internet connection
            isNetwork = true;
        }
        else{
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
                //appLogoDesc.setImageResource(R.drawable.nandi_login_screen_logo_2);
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
                // appLogoDesc.setImageResource(R.drawable.nandi_login_screen_logo);
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
