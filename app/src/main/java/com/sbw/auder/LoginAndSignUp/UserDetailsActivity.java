package com.sbw.auder.LoginAndSignUp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;
import com.sbw.auder.HomeScreen.MainActivity;
import com.sbw.auder.Models.Token;
import com.sbw.auder.Models.UserModel;
import com.sbw.auder.R;
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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserDetailsActivity extends AppCompatActivity implements ConnectivityChangeListener {

    private static final String TAG = "UserDetailsActivity";

    EditText firstName, lastName, email;
    TextView openCal, phone_verified_msg;

    Button next;

    ProgressDialog progressDialog;
    ImageView img1, img2;
    CheckBox locationCheckBox;
    boolean isNetwork = false;


    private FusedLocationProviderClient mFusedLocationClient;
    private int locationRequestCode = 1000;
    private double wayLatitude = 0.0, wayLongitude = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (new TinyDB(this).getString("Lang")!=null)
        {
            setLanguageForApp(new TinyDB(this).getString("Lang"), this);
        }
        setContentView(R.layout.activity_user_details);
        ConnectionBuddyConfiguration networkInspectorConfiguration = new ConnectionBuddyConfiguration.Builder(this).build();
        ConnectionBuddy.getInstance().init(networkInspectorConfiguration);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        img1=findViewById(R.id.nandi);
        img2=findViewById(R.id.nandi2);
        fadeIn(img1);

        firstName = findViewById(R.id.firstName);
        lastName = findViewById(R.id.lastName);
        email = findViewById(R.id.email);
        next = findViewById(R.id.nextBtn);
        locationCheckBox = findViewById(R.id.locationCheckBox);
        phone_verified_msg=findViewById(R.id.phone_verified);
        email.setVisibility(View.GONE);


        setOnTouchListnerToView(next);

        openCal = findViewById(R.id.openCal);
        openCal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               getDate();
            }
        });
        final ImageView birthdayIcon= findViewById(R.id.birthday_cake);

        birthdayIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDate();
            }
        });
        openCal.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    openCal.setAlpha(.5f);
                    birthdayIcon.setAlpha(.5f);

                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    openCal.setAlpha(1f);
                    birthdayIcon.setAlpha(1f);
                }
                return false;
            }
        });
        birthdayIcon.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    openCal.setAlpha(.5f);
                    birthdayIcon.setAlpha(.5f);

                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    openCal.setAlpha(1f);
                    birthdayIcon.setAlpha(1f);
                }
                return false;
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(isNetwork) {

                    if (!firstName.getText().toString().trim().isEmpty() && !lastName.getText().toString().trim().isEmpty()) {



                        progressDialog.setMessage(getResources().getString(R.string.saving_user_data));
                        progressDialog.show();
                        sendPostUserData();


                    } else {
                         Toast.makeText(UserDetailsActivity.this, ""+getResources().getString(R.string.toast_signup_compulsory_fields_not_filled), Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Snackbar.make(findViewById(android.R.id.content), getResources().getString(R.string.no_internet_connection_snackbar), Snackbar.LENGTH_LONG).show();

                }

            }
        });
        locationCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(UserDetailsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                                locationRequestCode);

                    } else {
                        // already permission granted
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            //locationCheckBox.setChecked(false);
                            return;
                        }
                        mFusedLocationClient.getLastLocation().addOnSuccessListener(UserDetailsActivity.this, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                if (location != null) {
                                    wayLatitude = location.getLatitude();
                                    wayLongitude = location.getLongitude();
                                }
                            }
                        });
                    }
                }
            }
        });
        phone_verified_msg.setText(" "+ FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()+" "+getResources().getString(R.string.sucessfully_verified));


    }

    private void sendPostUserData() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    URL url = new URL(getResources().getString(R.string.localhost) + "/api/" + new TinyDB(getApplicationContext()).getString(getResources().getString(R.string.authToken)) + "/retailer/update/" + FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());
                    Log.d(TAG, "run: " + getResources().getString(R.string.localhost) + "/api/" + new TinyDB(getApplicationContext()).getString(getResources().getString(R.string.authToken)) + "/retailer/update/" + FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());
                    final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setDoOutput(true);
                    conn.setDoInput(true);

                    String emailStr = email.getText().toString().trim();
                    String firstNameStr = firstName.getText().toString().trim();
                    String lastNameStr = lastName.getText().toString().trim();


                    final JSONObject data = new JSONObject();
                    data.put("firstName", firstNameStr);
                    data.put("name", firstNameStr);
                    data.put("LastName", lastNameStr);
                    data.put("email", emailStr);
                    if (locationCheckBox.isChecked()) {
                        if (wayLatitude != 0 && wayLongitude != 0) {
                            data.put("latitude", wayLatitude);
                            data.put("longitude", wayLongitude);
                        }
                    }


                    if(!openCal.getText().toString().isEmpty() && !openCal.getText().equals(getResources().getString(R.string.birthday_text)))
                    {
                        data.put("birthdate", openCal.getText().toString());

                    }
                    JSONObject finalData = new JSONObject();
                    finalData.put("data", data);

                    Log.d(TAG, "run: " + finalData.toString());
                    DataOutputStream os = new DataOutputStream(conn.getOutputStream());
//                    os.writeBytes(URLEncoder.encode(data.toString(), "UTF-8"));
                    os.writeBytes(finalData.toString());
                    os.flush();
                    os.close();

                    final Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                    StringBuilder sb = new StringBuilder();
                    for (int c; (c = in.read()) >= 0; )
                        sb.append((char) c);
                    final String response = sb.toString();

                    Log.i("STATUS", String.valueOf(conn.getResponseCode()));
                    Log.i("MSG123", response);


                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            progressDialog.hide();
                            progressDialog.dismiss();

                            try {
                                JSONObject jsonObject = new JSONObject(response);


                                if(jsonObject.getJSONArray("msg").getJSONObject(0)!=null){

                                    new TinyDB(getApplicationContext()).putString("userData", jsonObject.getJSONArray("msg").getJSONObject(0).toString());
                                    Intent intent = new Intent(UserDetailsActivity.this, MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);


                                }else{
                     //               Toast.makeText(UserDetailsActivity.this, "Error Occured", Toast.LENGTH_SHORT).show();
                                }



                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    });


                    conn.disconnect();

                } catch (final Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//                            Log.d(TAG, "run: " + e.getMessage);
                       //     Toast.makeText(UserDetailsActivity.this, "" + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();

                        }
                    });
                }


            }
        });

        thread.start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1000: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    mFusedLocationClient.getLastLocation().addOnSuccessListener(UserDetailsActivity.this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                wayLatitude = location.getLatitude();
                                wayLongitude = location.getLongitude();
                            }
                        }
                    });
                } else {
                    locationCheckBox.setChecked(false);
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    public static boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
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
    @Override
    protected void onStart() {
        super.onStart();
        ConnectionBuddy.getInstance().registerForConnectivityEvents(this, this);
    }
    Animation fadeIn = new AlphaAnimation(0, 1);
    Animation fadeOut = new AlphaAnimation(1, 0);

    public void fadeIn(final ImageView img1) {

        fadeIn.setInterpolator(new AccelerateInterpolator());
        fadeIn.setDuration(1000);
        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                //img1.setImageResource(R.drawable.nandi_login_screen_logo_2);
                fadeOut(img1);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        img1.startAnimation(fadeIn);

    }

    public void fadeOut(final ImageView img1) {
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setDuration(1000);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // img1.setImageResource(R.drawable.nandi_login_screen_logo);
                fadeIn(img1);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        img1.startAnimation(fadeOut);

    }
    private void getDate() {
        final Calendar calendar = Calendar.getInstance();
        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int day = calendar.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog dpd =new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf=new SimpleDateFormat("dd/MM/yy");
                calendar.set(year, month, dayOfMonth);
                openCal.setText(sdf.format(calendar.getTime()));
            }
        }, year, month, day);
        dpd.getDatePicker().setMaxDate(new Date().getTime());
        dpd.show();
        dpd.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(final DialogInterface dialog) {
                dialog.dismiss();

            }
        });

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
