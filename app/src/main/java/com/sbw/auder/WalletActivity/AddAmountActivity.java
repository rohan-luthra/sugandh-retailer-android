package com.sbw.auder.WalletActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.sbw.auder.LoginAndSignUp.LoginPhoneActivity;
import com.sbw.auder.Models.Token;
import com.sbw.auder.Models.UserModel;
import com.sbw.auder.Models.WalletModel;
import com.sbw.auder.MyProfileActivity;
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
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;


public class AddAmountActivity extends AppCompatActivity implements ConnectivityChangeListener {

    EditText amountToBeAdded;
    TextView amt1, amt2, amt3, amt4, amount;
    Button pay;

    TinyDB tinyDB;
    ProgressDialog progressDialog;
    private static final String TAG = "AddAmountActivity";
    private FirebaseAnalytics mFirebaseAnalytics;
    ArrayList<TextView> amts;

    boolean flag = true;
    boolean isNetwork = false;

    int currency;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (new TinyDB(this).getString("Lang")!=null)
        {
            setLanguageForApp(new TinyDB(this).getString("Lang"), this);
        }
        setContentView(R.layout.activity_add_amount);
        ConnectionBuddyConfiguration networkInspectorConfiguration = new ConnectionBuddyConfiguration.Builder(this).build();
        ConnectionBuddy.getInstance().init(networkInspectorConfiguration);
        tinyDB = new TinyDB(getApplicationContext());

        Toolbar toolbar = (Toolbar) findViewById(R.id.tb_toolbarsearch);
        setSupportActionBar(toolbar);
        // add back arrow to toolbar

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // back button pressed

                finish();
                overridePendingTransition(android.R.anim.fade_in, R.anim.slide_down);

            }
        });
        toolbar.setNavigationIcon(R.drawable.ic_cross_white);

        setTitle(getResources().getString(R.string.add_money));
        toolbar.setTitleTextAppearance(this, R.style.toolbarText2);
        mFirebaseAnalytics=FirebaseAnalytics.getInstance(this);

        amountToBeAdded = findViewById(R.id.amountToBeAdded);
        amt1 = findViewById(R.id.amount1);
        amt2 = findViewById(R.id.amount2);
        amt3 = findViewById(R.id.amount3);
        amt4 = findViewById(R.id.amount4);
        amount = findViewById(R.id.amount);
        pay = findViewById(R.id.addAmount);
        setOnTouchListnerToView(pay);
        tinyDB = new TinyDB(this);
        progressDialog = new ProgressDialog(this);

        if( tinyDB.getObject("wallet", WalletModel.class)!=null ){
            WalletModel walletModel = tinyDB.getObject("wallet", WalletModel.class);
             currency = (int) Integer.parseInt(String.valueOf(walletModel.getAmount()));
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
            formatter.setMaximumFractionDigits(0);
            amount.setText(formatter.format(currency)+"");
            //amount.setText(walletModel.getAmount()+"");
        }else{
          //  Toast.makeText(this, "Add Wallet First", Toast.LENGTH_SHORT).show();
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        }


        amts = new ArrayList<>();
        amts.add(amt1);amts.add(amt2);amts.add(amt3);amts.add(amt4);

        for(final TextView amt:amts){
            amt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    amountToBeAdded.setText(amt.getText());
                    unselectAll(amts);
                    amt.setBackground(getResources().getDrawable(R.drawable.btn_bg_yellow));

                }
            });
        }


        pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!amountToBeAdded.getText().toString().isEmpty() && !amountToBeAdded.getText().toString().equals("0")){
                    try{

                        if(isNetwork) {

                            Long incAmount = Long.parseLong(amountToBeAdded.getText().toString().trim());
                            sendAddAmountPost(incAmount);
                        }else{
                            //Snackbar.make(findViewById(android.R.id.content), "NO INTERNET CONNECTION", Snackbar.LENGTH_LONG).show();
                            Toast.makeText(AddAmountActivity.this, getResources().getString(R.string.no_internet_connection_snackbar), Toast.LENGTH_SHORT).show();
                        }

                    }catch (Exception e){

                    }
                }else{
                    Toast.makeText(AddAmountActivity.this, getResources().getString(R.string.noAmountAddedMessage), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void sendAddAmountPost(Long incAmount) throws JSONException {

        final String urlAdress = getResources().getString(R.string.localhost) + "/api/" + new TinyDB(getApplicationContext()).getString(getResources().getString(R.string.authToken)) + "/wallet/incAmount/" + FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
        progressDialog.setMessage(getResources().getString(R.string.adding_amount));
        progressDialog.show();

        final JSONObject data = new JSONObject();
        data.put("amount", incAmount);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    URL url = new URL(urlAdress);
                    Log.d(TAG, "run: " + urlAdress);
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

                    final Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                    StringBuilder sb = new StringBuilder();
                    for (int c; (c = in.read()) >= 0; )
                        sb.append((char) c);
                    final String response = sb.toString();

                    Log.i("STATUS", String.valueOf(conn.getResponseCode()));
                    Log.i("MSG123", response);

                    final JSONObject jsonObject = new JSONObject(response);

                    Log.d(TAG, "run: " + jsonObject);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            try {
                                if(jsonObject.getBoolean("status")){

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            flag = false;
                                            logFirebaseEvent("money_added", "amount", Integer.parseInt(String.valueOf(amountToBeAdded.getText())));
                                            logFirebaseEvent("money_added", "previous_wallet_balance", currency);
                                            Toast.makeText(AddAmountActivity.this, "Rs " + Integer.parseInt(String.valueOf(amountToBeAdded.getText())) + " " + getResources().getString(R.string.toast_wallet_money_added), Toast.LENGTH_SHORT).show();
                                            updateWalletData();
                                            Log.d(TAG, "run: " + flag);
                                        }
                                    });

                                }else{
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
//                                            try {
//                                                Toast.makeText(AddAmountActivity.this, jsonObject.getString("msg")+"", Toast.LENGTH_SHORT).show();
//
//                                            } catch (JSONException e) {
//                                                e.printStackTrace();
//                                            }
                                            progressDialog.dismiss();

                                        }
                                    });
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
                 //           Toast.makeText(AddAmountActivity.this, "" + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();

                        }
                    });
                }


            }
        });

        thread.start();
    }



    public void unselectAll(ArrayList<TextView> amts){

        for(TextView amt:amts){
            amt.setBackground(getResources().getDrawable(R.drawable.btn_bg_purple));
        }

    }

    private void updateWalletData() {
        GetData getData = new GetData(new GetData.AsyncResponse() {
            @Override
            public void processFinish(final String output) throws JSONException {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            JSONObject res = new JSONObject(output);
                            if (res.getBoolean("status")) {

                                JSONObject walletRes = new JSONObject(output);
                                final JSONObject walletJson = walletRes.getJSONArray("msg").getJSONObject(0);
                                currency = (int) Integer.parseInt(String.valueOf(walletJson.getLong("amount")));
                                NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
                                formatter.setMaximumFractionDigits(0);
                                amount.setText(formatter.format(currency)+"");
                                //amount.setText(walletJson.getLong("amount")+"");
                               // logFirebaseEvent("wallet_balance", "value", Integer.parseInt(amount.getText().toString()) );

                                final WalletModel walletModel = new WalletModel();
                                walletModel.setAmount(walletJson.getLong("amount"));
                                walletModel.setId(walletJson.getString("_id"));
                                walletModel.setTimestamp(walletJson.getLong("timestamp"));

                                tinyDB.putObject("wallet", walletModel);

                                progressDialog.dismiss();
//                                finish();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                       // amount.setText(walletModel.getAmount()+"");
                                        currency = (int) Integer.parseInt(String.valueOf(walletModel.getAmount()));
                                        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
                                        formatter.setMaximumFractionDigits(0);
                                        amount.setText(formatter.format(currency)+"");
                                        unselectAll(amts);
                                        amountToBeAdded.setText("0");

                                    }
                                });


                            } else {


                            }


                        } catch (JSONException e) {
                            e.printStackTrace();

                            progressDialog.dismiss();
                            finish();
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

                        }catch (Exception e){
                            e.printStackTrace();

                            progressDialog.dismiss();
                            finish();
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

                        }


                        progressDialog.dismiss();
                    }
                });
            }
        });
        getData.execute(getResources().getString(R.string.localhost) + "/api/"+new TinyDB(getApplicationContext()).getString(getResources().getString(R.string.authToken))+"/wallet/" + FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());
    }
    private void logFirebaseEvent(String eventName, String paramName , int paramValue)
    {
        Bundle params = new Bundle();
        params.putInt(paramName, paramValue);
        mFirebaseAnalytics.logEvent(eventName, params);
    }

    @Override
    public void onBackPressed() {

        finish();
        overridePendingTransition(android.R.anim.fade_in, R.anim.slide_down);
        super.onBackPressed();
    }
    private void logFirebaseEvent2(String eventName, String paramName , String paramValue)
    {
        Bundle params = new Bundle();
        params.putString(paramName, paramValue);
        mFirebaseAnalytics.logEvent(eventName, params);
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: " + flag);
        if (flag)
        {
            logFirebaseEvent2("wallet_back_tapped_without_adding_money", "", "");
        }
        super.onDestroy();
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
