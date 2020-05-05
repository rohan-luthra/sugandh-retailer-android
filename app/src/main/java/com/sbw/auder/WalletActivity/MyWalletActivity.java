package com.sbw.auder.WalletActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.sbw.auder.Models.UserModel;
import com.sbw.auder.Models.WalletModel;
import com.sbw.auder.R;
import com.sbw.auder.Utils.GetData;
import com.sbw.auder.Utils.TinyDB;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

public class MyWalletActivity extends AppCompatActivity {

    ProgressDialog progressDialog;
    RelativeLayout noWalletLay, walletLay;
    private static final String TAG = "MyWalletActivity";

    WalletModel walletModel;

    boolean flag = true;
    TinyDB tinyDB;
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (new TinyDB(this).getString("Lang")!=null)
        {
            setLanguageForApp(new TinyDB(this).getString("Lang"), this);
        }
        setContentView(R.layout.activity_my_wallet);
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
        setTitle("My Wallet");
        mFirebaseAnalytics=FirebaseAnalytics.getInstance(this);
        flag=true;
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading");
        progressDialog.show();
        tinyDB = new TinyDB(this);
        walletModel = new WalletModel();
        walletLay = findViewById(R.id.walletLayout);
        noWalletLay = findViewById(R.id.noWalletLayout);

        try {

            if (tinyDB.getObject("wallet", WalletModel.class) != null) {

                walletModel = tinyDB.getObject("wallet", WalletModel.class);
                walletLay.setVisibility(View.VISIBLE);
                TextView amount = findViewById(R.id.amount);
                amount.setText(walletModel.getAmount() + "");
                progressDialog.dismiss();

            }
        }catch (Exception e){
            Log.d(TAG, "onCreate: " + e.getLocalizedMessage());
        }

        getWalletData();





    }

    private void getWalletData() {
        GetData getData = new GetData(new GetData.AsyncResponse() {
            @Override
            public void processFinish(final String output) throws JSONException {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        Toast.makeText(MyWalletActivity.this, ""+ output, Toast.LENGTH_SHORT).show();

                        try {
                            JSONObject res = new JSONObject(output);
                            if (res.getBoolean("status")) {

                                walletLay.setVisibility(View.VISIBLE);
                                TextView amount = findViewById(R.id.amount);
                                Button addAmount = findViewById(R.id.addAmount);


                                JSONObject walletRes = new JSONObject(output);
                                final JSONObject walletJson = walletRes.getJSONArray("msg").getJSONObject(0);
                                amount.setText(walletJson.getLong("amount")+"");

                                walletModel.setAmount(walletJson.getLong("amount"));
                                walletModel.setId(walletJson.getString("_id"));
                                walletModel.setTimestamp(walletJson.getLong("timestamp"));

                                tinyDB.putObject("wallet", walletModel);

                                addAmount.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        flag = false;
                                        startActivity(new Intent(MyWalletActivity.this, AddAmountActivity.class)
                                                .putExtra("walletJson", walletJson.toString()));
                                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

                                    }
                                });


                            } else {

                                noWalletLay.setVisibility(View.VISIBLE);
                                Button addWallet = findViewById(R.id.addWallet);
                                addWallet.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        addWalletSendPost();
                                    }
                                });
                                addWallet.performClick();
                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }catch (Exception e){
                           // Toast.makeText(MyWalletActivity.this, "Couldn't Load New Data", Toast.LENGTH_SHORT).show();
                        }


                        progressDialog.dismiss();
                    }
                });
            }
        });
        getData.execute(getResources().getString(R.string.localhost) + "/api/"+ new TinyDB(getApplicationContext()).getString(getResources().getString(R.string.authToken))+"/wallet/" + tinyDB.getObject("user", UserModel.class).getId());
    }

    private void addWalletSendPost() {

        final String urlAdress = getResources().getString(R.string.localhost)+"/api/"+new TinyDB(getApplicationContext()).getString(getResources().getString(R.string.authToken))+"/newWallet/ret/"+tinyDB.getObject("user", UserModel.class).getId();
        final JSONObject data = new JSONObject();
        progressDialog.setMessage(getResources().getString(R.string.creating_wallet_for_you));
        progressDialog.show();
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
                           //               Toast.makeText(MyWalletActivity.this, "Wallet Created", Toast.LENGTH_SHORT).show();
                                            progressDialog.dismiss();
                                            noWalletLay.setVisibility(View.GONE);
                                            walletLay.setVisibility(View.GONE);
//                                            getWalletData();
                                            recreate();

                                        }
                                    });

                                }else{
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                               //f             Toast.makeText(MyWalletActivity.this, "", Toast.LENGTH_SHORT).show();
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
                          //  Toast.makeText(MyWalletActivity.this, "" + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();

                        }
                    });
                }


            }
        });

        thread.start();
    }

    private void logFirebaseEvent(String eventName, String paramName, String paramValue) {
        Bundle params = new Bundle();
        params.putString(paramName, paramValue);
        mFirebaseAnalytics.logEvent(eventName, params);
    }

    @Override
    public void onBackPressed() {

        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, R.anim.slide_down);

    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        getWalletData();
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
}

