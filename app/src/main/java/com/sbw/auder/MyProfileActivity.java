package com.sbw.auder;

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
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.sbw.auder.LoginAndSignUp.LoginPhoneActivity;
import com.sbw.auder.LoginAndSignUp.UserDetailsActivity;
import com.sbw.auder.Models.Token;
import com.sbw.auder.Models.UserModel;
import com.sbw.auder.Utils.TinyDB;
import com.sbw.auder.R;
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

public class MyProfileActivity extends AppCompatActivity implements ConnectivityChangeListener {


    TextView openCal, phone;
    EditText email, fName, LName;
    Button save, submitname;
    ImageView editname;
    ProgressDialog progressDialog;
    TinyDB tinyDB;

    boolean editFlag = true, locationDisabled = false;
    CheckBox locationCheckBox;
    boolean isNetwork = false;



    private static final String TAG = "MyProfileActivity";

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
        setContentView(R.layout.activity_my_profile);

        ConnectionBuddyConfiguration networkInspectorConfiguration = new ConnectionBuddyConfiguration.Builder(this).build();
        ConnectionBuddy.getInstance().init(networkInspectorConfiguration);

        Toolbar toolbar2 = (Toolbar) findViewById(R.id.tb_toolbarsearchmyprofile);
        setSupportActionBar(toolbar2);

        setTitle(getResources().getString(R.string.myProfile));
        toolbar2.setTitleTextAppearance(this, R.style.toolbarText2);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // add back arrow to toolbar2
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar2.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // back button pressed
                finish();
                overridePendingTransition(android.R.anim.fade_in, R.anim.slide_down);

            }
        });
        toolbar2.setNavigationIcon(R.drawable.ic_cross_white);


        tinyDB = new TinyDB(this);
        phone = findViewById(R.id.phone);
        openCal = findViewById(R.id.openCal);
        email = findViewById(R.id.email);
        save = findViewById(R.id.nextBtn);
        fName = findViewById(R.id.firstName);
        LName = findViewById(R.id.lastName);
        editname = findViewById(R.id.editname);
        progressDialog = new ProgressDialog(this);
        progressDialog.setCanceledOnTouchOutside(false);
        locationCheckBox = findViewById(R.id.locationCheckBox);

        save.setVisibility(View.GONE);
        phone.setText(tinyDB.getObject("user", UserModel.class).getId());
        fName.setBackgroundColor(getResources().getColor(R.color.grey_3));
        LName.setBackgroundColor(getResources().getColor(R.color.grey_3));
        email.setBackgroundColor(getResources().getColor(R.color.grey_3));

        if (!openCal.getText().equals(getResources().getString(R.string.birthday_text)))
        {
            openCal.setBackgroundColor(Color.TRANSPARENT);
        }

        setOnTouchListnerToView(save);
        setAllEditable(false);

        findViewById(R.id.shareLocationText).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    findViewById(R.id.shareLocationText).setAlpha(.5f);


                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    findViewById(R.id.shareLocationText).setAlpha(1f);
                }
                return false;
            }
        });

        findViewById(R.id.shareLocationText).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (locationCheckBox.isChecked())
                {
                    locationCheckBox.setChecked(false);
                }else
                    locationCheckBox.setChecked(true);
            }
        });


        locationCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(MyProfileActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
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
                            return;
                        }
                        mFusedLocationClient.getLastLocation().addOnSuccessListener(MyProfileActivity.this, new OnSuccessListener<Location>() {
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

        editname.setImageResource(R.drawable.edit_button);
        editname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setAllEditable(editFlag);
                if (editFlag)
                    editname.setImageResource(R.drawable.edit_button_tapped);
                else
                    editname.setImageResource(R.drawable.edit_button);
                editFlag = !editFlag;

            }
        });

        findViewById(R.id.birthday_cake).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                birthDay.setVisibility(View.VISIBLE);
                getDate();
            }
        });
        openCal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //birthDay.setVisibility(View.VISIBLE);
                getDate();
            }
        });


        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(isNetwork) {

                    if (!fName.getText().toString().isEmpty() &&
                            !LName.getText().toString().isEmpty()) {
                        if (!email.getText().toString().isEmpty())
                        {
                            if (isEmailValid(email.getText().toString().trim())) {

                                progressDialog.setMessage("Saving your information");
                                progressDialog.show();
                                sendPostUserData();
                            } else {
                                Toast.makeText(MyProfileActivity.this, ""+getResources().getString(R.string.toast_signup_invalid_email), Toast.LENGTH_SHORT).show();
                            }
                        }else {

                            progressDialog.setMessage("Saving your information");
                            progressDialog.show();
                            sendPostUserData();
                        }


                    } else {
                        Toast.makeText(MyProfileActivity.this, ""+getResources().getString(R.string.toast_signup_compulsory_fields_not_filled), Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Snackbar.make(findViewById(android.R.id.content), getResources().getString(R.string.no_internet_connection_snackbar), Snackbar.LENGTH_LONG).show();

                }
            }
        });

        if (tinyDB.getObject("user", UserModel.class).getEmail() != null)
            email.setText(tinyDB.getObject("user", UserModel.class).getEmail());
        if (tinyDB.getObject("user", UserModel.class).getFirstName() != null)
            fName.setText(tinyDB.getObject("user", UserModel.class).getFirstName());
        if (tinyDB.getObject("user", UserModel.class).getLastName() != null)
            LName.setText(tinyDB.getObject("user", UserModel.class).getLastName());
        if (tinyDB.getObject("user", UserModel.class).getBirthdate() != null) {
            openCal.setText(tinyDB.getObject("user", UserModel.class).getBirthdate());
        }if(tinyDB.getObject("user", UserModel.class).getLatitude()!=null && tinyDB.getObject("user", UserModel.class).getLatitude()!=null){
            locationDisabled = true;
            locationCheckBox.setChecked(true);
        }


    }

    private void setAllEditable(boolean editFlag) {

        fName.setEnabled(editFlag);
        LName.setEnabled(editFlag);
        email.setEnabled(editFlag);
        if (openCal.getText().equals(getResources().getString(R.string.birthday_text)))
        {
            openCal.setEnabled(editFlag);
            findViewById(R.id.birthday_cake).setEnabled(editFlag);
        }
        else {
            openCal.setEnabled(false);
            findViewById(R.id.birthday_cake).setEnabled(false);
        }

        //save.setEnabled(editFlag);
        if(!locationDisabled)
        {
            locationCheckBox.setEnabled(editFlag);
            findViewById(R.id.shareLocationText).setEnabled(editFlag);
        }


            if (!editFlag) {
            if (tinyDB.getObject("user", UserModel.class).getEmail() != null)
                email.setText(tinyDB.getObject("user", UserModel.class).getEmail());
            if (tinyDB.getObject("user", UserModel.class).getFirstName() != null)
                fName.setText(tinyDB.getObject("user", UserModel.class).getFirstName());
            if (tinyDB.getObject("user", UserModel.class).getLastName() != null)
                LName.setText(tinyDB.getObject("user", UserModel.class).getLastName());
            if (tinyDB.getObject("user", UserModel.class).getBirthdate() != null) {
                openCal.setText(tinyDB.getObject("user", UserModel.class).getBirthdate());
            }if(tinyDB.getObject("user", UserModel.class).getLatitude()!=null && tinyDB.getObject("user", UserModel.class).getLatitude()!=null){
                locationDisabled = true;
                locationCheckBox.setChecked(true);
            }

            save.setVisibility(View.GONE);
            fName.setBackgroundResource(R.drawable.input_bg_grey_round);
            LName.setBackgroundResource(R.drawable.input_bg_grey_round);
            email.setBackgroundResource(R.drawable.input_bg_grey_round);
            if (openCal.getText().equals(getResources().getString(R.string.birthday_text)))
            {
                openCal.setBackgroundResource(R.drawable.input_bg_grey_round);

            }

        } else {
            save.setVisibility(View.VISIBLE);
            fName.setBackgroundResource(R.drawable.input_bg_round);
            LName.setBackgroundResource(R.drawable.input_bg_round);
            email.setBackgroundResource(R.drawable.input_bg_round);
                if (openCal.getText().equals(getResources().getString(R.string.birthday_text)))
                {
                    openCal.setBackgroundResource(R.drawable.input_bg_round);

                }




        }

    }

    private void sendPostUserData() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    URL url = new URL(getResources().getString(R.string.localhost) + "/api/" + Token.getAuthToken() + "/retailer/update/" + tinyDB.getObject("user", UserModel.class).getId());
                    Log.d(TAG, "run: " + getResources().getString(R.string.localhost) + "/api/" + Token.getAuthToken() + "/retailer/update/" + tinyDB.getObject("user", UserModel.class).getId());
                    final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setDoOutput(true);
                    conn.setDoInput(true);


                    final JSONObject data = new JSONObject();
                    data.put("firstName", fName.getText().toString());
                    data.put("name", fName.getText().toString());
                    data.put("LastName", LName.getText().toString());
                    data.put("email", email.getText().toString());
                    if (locationCheckBox.isChecked()) {
                        if (wayLatitude != 0 && wayLongitude != 0) {
                            data.put("latitude", wayLatitude);
                            data.put("longitude", wayLongitude);
                        }
                    }

                    if (!openCal.getText().toString().isEmpty() && !openCal.getText().equals(getResources().getString(R.string.birthday_text)))
                    {
                        data.put("birthdate", openCal.getText().toString());
                        openCal.setBackgroundColor(Color.TRANSPARENT);
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

                            //progressDialog.hide();
                            progressDialog.dismiss();

                            try {
                                JSONObject jsonObject = new JSONObject(response);


                                if (jsonObject.getJSONArray("msg").get(0) != null) {

                                    tinyDB.putString("userData", jsonObject.getJSONArray("msg").get(0).toString());
                                    try {

                                        JSONObject userData = new JSONObject(new TinyDB(getApplicationContext()).getString("userData"));
                                        Log.d(TAG, "processFinish1: " + userData);

                                        if (userData.has("_id")) {

                                            UserModel userModel = new UserModel();
                                            userModel.setId(userData.getString("_id"));
                                            if (userData.has("firstName") && userData.has("LastName") && userData.has("email")) {
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

                                        }

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        Toast.makeText(MyProfileActivity.this, "Error Occured", Toast.LENGTH_SHORT).show();
                                        Log.d(TAG, "onCreate: " + e.getMessage());
                                    }

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(MyProfileActivity.this, "" + getResources().getString(R.string.toast_my_profile_profile_updated), Toast.LENGTH_SHORT).show();
                                            editname.performClick();

                                        }
                                    });

                                } else {
                                  //  Toast.makeText(MyProfileActivity.this, "Error Occured", Toast.LENGTH_SHORT).show();
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
                           // Toast.makeText(MyProfileActivity.this, "" + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();

                        }
                    });
                }


            }
        });

        thread.start();
    }

    @SuppressLint("MissingPermission")
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
                    mFusedLocationClient.getLastLocation().addOnSuccessListener(MyProfileActivity.this, new OnSuccessListener<Location>() {
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
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, R.anim.slide_down);

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



    private void getDate() {
        final Calendar calendar = Calendar.getInstance();
        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int day = calendar.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog dpd =new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf=new SimpleDateFormat("dd MMMM, yyyy");
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
