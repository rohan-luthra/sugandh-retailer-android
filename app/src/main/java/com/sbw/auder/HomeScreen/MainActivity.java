package com.sbw.auder.HomeScreen;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.sbw.auder.AboutUsActivity;
import com.sbw.auder.ChooseLanguage;
import com.sbw.auder.LoginAndSignUp.LoginPhoneActivity;
import com.sbw.auder.LoginAndSignUp.UserDetailsActivity;
import com.sbw.auder.Models.DistModel;
import com.sbw.auder.Models.OrderModel;
import com.sbw.auder.Models.OrderTypeModel;
import com.sbw.auder.Models.Token;
import com.sbw.auder.Models.UserModel;
import com.sbw.auder.Models.WalletModel;
import com.sbw.auder.MyProfileActivity;
import com.sbw.auder.R;
import com.sbw.auder.RecyclerViewAdapters.DistSearchRecyclerViewAdapter;
import com.sbw.auder.Utils.GetData;
import com.sbw.auder.Utils.TinyDB;
import com.sbw.auder.WalletActivity.AddAmountActivity;
import com.zplesac.connectionbuddy.ConnectionBuddy;
import com.zplesac.connectionbuddy.ConnectionBuddyConfiguration;
import com.zplesac.connectionbuddy.interfaces.ConnectivityChangeListener;
import com.zplesac.connectionbuddy.models.ConnectivityEvent;
import com.zplesac.connectionbuddy.models.ConnectivityState;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;


public class MainActivity extends AppCompatActivity implements ConnectivityChangeListener {

    private static final String TAG = "MainActivity";


    String userId;
    Socket socket;

    RecyclerView allDistRecyclerView;
    static ArrayList<DistModel> distModelArrayList = new ArrayList<>();
    TinyDB tinyDB;
    String userDataStr;

    ProgressDialog progressDialog;
    boolean isNetwork = false;

    private FirebaseAnalytics mFirebaseAnalytics;

    static private DistSearchRecyclerViewAdapter distRecyclerViewAdapter;

    private SearchView searchView;

    ProgressBar progressBar;

    SwipeRefreshLayout pullToRefresh;

    String PERMISSION[] = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
    Button permissionDeniedBtn;

    RelativeLayout parent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (new TinyDB(this).getString("Lang")!=null)
        {
            setLanguageForApp(new TinyDB(this).getString("Lang"), this);
        }
        setContentView(R.layout.activity_main);
        tinyDB = new TinyDB(getApplicationContext());
        progressDialog = new ProgressDialog(this);
        ConnectionBuddyConfiguration networkInspectorConfiguration = new ConnectionBuddyConfiguration.Builder(this).build();
        ConnectionBuddy.getInstance().init(networkInspectorConfiguration);
        parent = findViewById(R.id.parent);
        progressBar = findViewById(R.id.progressBar);
        pullToRefresh = (SwipeRefreshLayout) findViewById(R.id.pullToRefresh);
        pullToRefresh.setColorSchemeColors(getResources().getColor(R.color.purple_11));

        Toolbar toolbar = (Toolbar) findViewById(R.id.tb_toolbarsearch);
        setSupportActionBar(toolbar);
        setTitle(getResources().getString(R.string.app_name));
        toolbar.setTitleTextAppearance(this, R.style.toolbarText);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        permissionDeniedBtn = findViewById(R.id.permissionDeniedBtn);

        getOrderTypeData();
        checkIfUserRegistered();


        if (!permissionGranted()) {
            ActivityCompat.requestPermissions(MainActivity.this, PERMISSION, 1);
        } else {
            try {
                socket = IO.socket(getResources().getString(R.string.localhost));
                socket.connect();
                socket.emit("retJoin", userId);
                allDistRecyclerView = findViewById(R.id.allDistRecyclerView);

                getAllDistData();

                updateWalletData(false);

                pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

                    @Override
                    public void onRefresh() {
                        if (tinyDB.getDistModelListObject("distList", DistModel.class) != null) {
                            ArrayList<DistModel> distModelArrayList = tinyDB.getDistModelListObject("distList", DistModel.class);
                            for (int i = 0; i < distModelArrayList.size(); i++) {
                                //tinyDB.remove(distModelArrayList.get(i).getId());
                                tinyDB.remove("SLIDER_"+distModelArrayList.get(i).getId());
                            }
                        }
                        getRefreshedList();

                    }
                });

            } catch (URISyntaxException e) {
                e.printStackTrace();
            }

        }

    }

    private void getRefreshedList() {
        logFirebaseEvent("dist_list_refreshed", "", "");
        if (isNetwork) {

            GetData getData = new GetData(new GetData.AsyncResponse() {
                @Override
                public void processFinish(final String output) throws JSONException {
                    Toast.makeText(MainActivity.this, "" + getResources().getString(R.string.toast_dist_list_refreshed), Toast.LENGTH_SHORT).show();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "run: " + output);
                            init(output);
                        }
                    });
                }
            });

            if (Token.getAuthToken() != null) {
                Log.d(TAG, "onRefresh: " + getResources().getString(R.string.localhost) + "/api/" + Token.getAuthToken() + "/dists/" + userId);
                getData.execute(getResources().getString(R.string.localhost) + "/api/" + Token.getAuthToken() + "/dists/" + userId);
            } else {
                Token.setAuthToken(new TinyDB(getApplicationContext()).getString(getResources().getString(R.string.authToken)));
                Log.d(TAG, "onRefresh: " + getResources().getString(R.string.localhost) + "/api/" + Token.getAuthToken() + "/dists/" + userId);
                getData.execute(getResources().getString(R.string.localhost) + "/api/" + Token.getAuthToken() + "/dists/" + userId);
            }
        } else {
            Snackbar.make(findViewById(android.R.id.content), getResources().getString(R.string.no_internet_connection_snackbar), Snackbar.LENGTH_LONG).show();
            pullToRefresh.setRefreshing(false);

        }
    }

    private void checkIfUserRegistered() {
        try {

            if (tinyDB.getString("userData") != null) {
                userDataStr = tinyDB.getString("userData");
                Log.d(TAG, "onCreate: " + userDataStr);
                JSONObject userData = new JSONObject(userDataStr);
                Log.d(TAG, "processFinish1: " + userData);
                if (userData.has("_id")) {

                    UserModel userModel = new UserModel();
                    userModel.setId(userData.getString("_id"));
                    tinyDB.putObject("user", userModel);
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
                        userId = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
                    } else {
                        Toast.makeText(this, "Please Complete your Profile!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(MainActivity.this, UserDetailsActivity.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                .putExtra("userData", userData.toString()));
                        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.fade_out);
                    }

                }
            } else {
                startActivity(new Intent(MainActivity.this, LoginPhoneActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);

            }

        } catch (Exception e) {
            startActivity(new Intent(MainActivity.this, LoginPhoneActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
            overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);


        }

    }

    private void getOrderTypeData() {

        GetData getData = new GetData(new GetData.AsyncResponse() {
            @SuppressLint("NewApi")
            @Override
            public void processFinish(String output) throws JSONException {

                try {
                    JSONObject jsonObject = new JSONObject(output);
                    JSONArray data = jsonObject.getJSONArray("msg");
                    if (data.length() > 0) {

                        ArrayList<OrderTypeModel> orderTypeModels = new ArrayList<>();

                        for (int i = 0; i < data.length(); i++) {

                            JSONObject object = data.getJSONObject(i);
                            OrderTypeModel orderTypeModel = new OrderTypeModel();
                            orderTypeModel.setName(object.getString("name"));
                            orderTypeModel.setId(object.getString("_id"));
                            orderTypeModel.setFees(object.getString("fees"));
                            orderTypeModel.setAudioTimeSec(object.getString("audio_len_sec"));

                            orderTypeModels.add(orderTypeModel);
                        }

                        new TinyDB(getApplicationContext()).putListOrderTypeModel("orderTypeList", orderTypeModels);

                    }

                } catch (Exception e) {
                    Log.d(TAG, "processFinish: " + e.getMessage());
                }
            }
        });
        getData.execute(getResources().getString(R.string.localhost) + "/api/" + Token.getAuthToken() + "/orderTypes");

    }

    private boolean permissionGranted() {

        for (String permission : PERMISSION) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), permission) == PackageManager.PERMISSION_GRANTED) {
            } else {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        Log.d(TAG, "onClose: ");

        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem myActionMenuItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) myActionMenuItem.getActionView();
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setQueryHint(getResources().getString(R.string.search_hint_mannual));
        ImageView searchCloseImage = searchView.findViewById(android.support.v7.appcompat.R.id.search_close_btn);
        searchCloseImage.setImageResource(R.drawable.ic_cross_white);
        intSearch();
        try {
            if (tinyDB.getObject("wallet", WalletModel.class) != null) {
                WalletModel walletModel = tinyDB.getObject("wallet", WalletModel.class);
                int currency = (int) Integer.parseInt(String.valueOf(walletModel.getAmount()));
                NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
                formatter.setMaximumFractionDigits(0);
                String ss =getResources().getString(R.string.walletText)+" : " + formatter.format(currency);
                SpannableString s = new SpannableString(getResources().getString(R.string.walletText)+" : " + formatter.format(currency));
                s.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getApplicationContext(), R.color.green_10)), ss.indexOf(":")+1, s.length(), 0);
                s.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), ss.indexOf(":")+1, s.length(), 0);
                menu.findItem(R.id.wallet).setTitle(s);


            } else {
                menu.findItem(R.id.wallet).setTitle(getResources().getString(R.string.addWallet));
            }
        } catch (Exception e) {

            menu.findItem(R.id.wallet).setTitle(getResources().getString(R.string.addWallet));
        }

        return true;
    }

    String prevSearchString = "", currSearchString = "";

    private void intSearch() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                logFirebaseEvent("searched", "what", s);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {

                s = s.toLowerCase();
                prevSearchString = currSearchString;
                currSearchString = s;
                logForSearchElements();


                Log.d(TAG, "onQueryTextChange: " + s);
                ArrayList<DistModel> searchedDistModels = new ArrayList<>();
                distRecyclerViewAdapter = new DistSearchRecyclerViewAdapter(getApplicationContext(), searchedDistModels, userId, MainActivity.this);
                allDistRecyclerView.setAdapter(distRecyclerViewAdapter);
                distRecyclerViewAdapter.notifyDataSetChanged();

                for (DistModel distModel : distModelArrayList) {

                    if (distModel.getDistInfo() != null) {

                        if (distModel.getId().toLowerCase().contains(s) || distModel.getName().toLowerCase().contains(s) || distModel.getLocation().toLowerCase().contains(s) || distModel.getDistInfo().toLowerCase().contains(s)) {
                            searchedDistModels.add(distModel);
                            distRecyclerViewAdapter.notifyDataSetChanged();
                        }

                    } else {
                        if (distModel.getId().toLowerCase().contains(s) || distModel.getName().toLowerCase().contains(s) || distModel.getLocation().toLowerCase().contains(s)) {
                            searchedDistModels.add(distModel);
                            distRecyclerViewAdapter.notifyDataSetChanged();
                        }

                    }


                }
                if (searchedDistModels.isEmpty()) {
                    findViewById(R.id.noResultFound).setVisibility(View.VISIBLE);

                } else {
                    findViewById(R.id.noResultFound).setVisibility(View.GONE);
                }
                return true;
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                Log.d(TAG, "onClose: ");
                if (!currSearchString.equals(""))
                    logFirebaseEvent("searched", "what", currSearchString);
                distRecyclerViewAdapter = new DistSearchRecyclerViewAdapter(getApplicationContext(), distModelArrayList, userId, MainActivity.this);
                allDistRecyclerView.setAdapter(distRecyclerViewAdapter);
                distRecyclerViewAdapter.notifyDataSetChanged();
                findViewById(R.id.noResultFound).setVisibility(View.GONE);
                return false;
            }
        });
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logFirebaseEvent("search_tapped", "", "");
            }
        });

    }

    private void logForSearchElements() {
        if (currSearchString.indexOf(prevSearchString) == 0) {
            Log.d(TAG, "onQueryTextChange: " + currSearchString.indexOf(prevSearchString));
        } else {
            logFirebaseEvent("searched", "what", prevSearchString);
        }
    }


    @SuppressLint("StaticFieldLeak")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //logFirebaseEvent("home_menu_tapped", "", "");
        final TinyDB tinyDB = new TinyDB(getApplicationContext());
        int id = item.getItemId();
        switch (id) {
            case R.id.signOut:
                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                alertDialog.setMessage(getResources().getString(R.string.dist_list_signout_alert));
                alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {


                        progressDialog.setMessage("Signing Out");
                        progressDialog.show();
                        logFirebaseEvent("signed_out", "", "");
                        if (tinyDB.getDistModelListObject("distList", DistModel.class) != null) {
                            ArrayList<DistModel> distModelArrayList = tinyDB.getDistModelListObject("distList", DistModel.class);
                            for (int i = 0; i < distModelArrayList.size(); i++) {
                                tinyDB.remove(distModelArrayList.get(i).getId());
                                //tinyDB.remove("SLIDER_"+distModelArrayList.get(i).getId());
                            }
                        }
                        //tinyDB.remove("distList");
                        tinyDB.remove("wallet");
                        tinyDB.remove("userData");
                        tinyDB.remove("user");
                        tinyDB.remove(getResources().getString(R.string.authToken));
                        //tinyDB.clear();
                        startActivity(new Intent(MainActivity.this, LoginPhoneActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                        overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);


                    }
                });
                alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                alertDialog.show();

                logFirebaseEvent2("home_menu_tapped", "selected_option", 4);


                return true;
            case R.id.wallet:
                logFirebaseEvent2("home_menu_tapped", "selected_option", 2);

                try {
                    if (tinyDB.getObject("wallet", WalletModel.class) != null) {
                        startActivity(new Intent(MainActivity.this, AddAmountActivity.class));
                        overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
                    } else
                        addWallet();
                } catch (Exception e) {
                    addWallet();
                }

                return true;
            case R.id.myProfile:
                logFirebaseEvent2("home_menu_tapped", "selected_option", 1);
                startActivity(new Intent(MainActivity.this, MyProfileActivity.class));
                overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);

                return true;
            case R.id.aboutUs:
                logFirebaseEvent2("home_menu_tapped", "selected_option", 3);

                startActivity(new Intent(MainActivity.this, AboutUsActivity.class));
                overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);

                return true;
            case R.id.chooseLang:

                ChooseLanguage chooseLanguage = new ChooseLanguage();
                chooseLanguage.showDialog(this);


                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void addWallet() {

        if (isNetwork) {

            final String urlAdress = getResources().getString(R.string.localhost) + "/api/" + Token.getAuthToken() + "/newWallet/ret/" + tinyDB.getObject("user", UserModel.class).getId();
            final JSONObject data = new JSONObject();
            progressDialog.setMessage("Creating Wallet for you");
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
                                    if (jsonObject.getBoolean("status")) {

                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                //wallet created.
                                                progressDialog.dismiss();
                                                updateWalletData(true);
                                            }
                                        });

                                    } else {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
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
                                progressDialog.dismiss();

                            }
                        });
                    }


                }
            });

            thread.start();
        } else {
            Snackbar.make(findViewById(android.R.id.content), "NO INTERNET CONNECTION", Snackbar.LENGTH_LONG).show();

        }


    }

    private void getAllDistData() {

        if (tinyDB.getDistModelListObject("distList", DistModel.class) != null) {

            Log.d(TAG, "processFinish: From saved");

            distModelArrayList = tinyDB.getDistModelListObject("distList", DistModel.class);
            distRecyclerViewAdapter = new DistSearchRecyclerViewAdapter(getApplicationContext(), distModelArrayList, userId, MainActivity.this);
            allDistRecyclerView.setAdapter(distRecyclerViewAdapter);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
            final LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(this, R.anim.layout_animation_fall_down);
            allDistRecyclerView.setLayoutManager(linearLayoutManager);
            allDistRecyclerView.setLayoutAnimation(controller);
            progressBar.setVisibility(View.GONE);

            for (int i = 0; i < distModelArrayList.size(); i++) {

                final DistModel distModel = distModelArrayList.get(i);
                final int finalI = i;

                socket.on("newOrder" + distModel.getId() + "_" + tinyDB.getObject("user", UserModel.class).getId(), new Emitter.Listener() {
                    @Override
                    public void call(final Object... args) {
                        Log.d(TAG, "call: " + args[0].toString());

                        try {
                            OrderModel orderModel = getOrderModelfromJson(new JSONObject(args[0].toString()));
                            Log.d(TAG, "call: " + orderModel);


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                try {

                                    for (int j = 0; j < distModelArrayList.size(); j++) {


                                        if (distModelArrayList.get(j).getId().equals(distModel.getId())) {

                                            OrderModel orderModel = getOrderModelfromJson(new JSONObject(args[0].toString()));
                                            distModel.setLatestOrder(orderModel);
                                            distModelArrayList.remove(j);
                                            distRecyclerViewAdapter.notifyDataSetChanged();
                                            if (distModel.getPriority() == 0) {
                                                distModelArrayList.add(j, distModel);
                                                distRecyclerViewAdapter.notifyDataSetChanged();
                                            } else {
                                                for (int k = 0; k < distModelArrayList.size(); k++) {
                                                    if (distModelArrayList.get(k).getPriority() != 0) {
                                                        distModelArrayList.add(k, distModel);
                                                        break;
                                                    } else if (k == distModelArrayList.size() - 1) {
                                                        distModelArrayList.add(k, distModel);
                                                    }

                                                }
                                                distRecyclerViewAdapter.notifyItemChanged(j);
                                            }
                                        }
                                    }


                                    tinyDB.putListDistModel("distList", distModelArrayList);


                                } catch (Exception e) {
                                    Log.d(TAG, "run: " + e.getMessage());
                                }
                            }
                        });
                    }
                });


                socket.on("updatedOrder" + distModel.getId() + "_" + tinyDB.getObject("user", UserModel.class).getId(), new Emitter.Listener() {
                    @Override
                    public void call(final Object... args) {
                        Log.d(TAG, "call: " + args[0].toString());

                        try {
                            OrderModel orderModel = getOrderModelfromJson(new JSONObject(args[0].toString()));
                            if (distModel.getLatestOrder() != null)
                                Log.d(TAG, "call: updatedOrder " + orderModel.getId() + " " + distModel.getLatestOrder().getId());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                try {

                                    for (int j = 0; j < distModelArrayList.size(); j++) {

                                        if (distModelArrayList.get(j).getId().equals(distModel.getId())) {

                                            OrderModel orderModel = getOrderModelfromJson(new JSONObject(args[0].toString()));
                                            if (orderModel.getId().equalsIgnoreCase(distModel.getLatestOrder().getId())) {
//                                            Log.d(TAG, "run: " + distModel.id +" " + j);
                                                distModel.setLatestOrder(orderModel);
                                                distModelArrayList.remove(j);
                                                distRecyclerViewAdapter.notifyDataSetChanged();
                                                if (distModel.getPriority() == 0) {
                                                    distModelArrayList.add(j, distModel);
                                                    distRecyclerViewAdapter.notifyDataSetChanged();
                                                } else {
                                                    for (int k = 0; k < distModelArrayList.size(); k++) {
                                                        if (distModelArrayList.get(k).getPriority() != 0) {
                                                            distModelArrayList.add(k, distModel);
                                                            break;
                                                        } else if (k == distModelArrayList.size() - 1) {
                                                            distModelArrayList.add(k, distModel);
                                                        }

                                                    }
                                                    distRecyclerViewAdapter.notifyItemChanged(j);
                                                }
                                            }
                                        }
                                    }

                                    tinyDB.putListDistModel("distList", distModelArrayList);


                                } catch (Exception e) {
                                    Log.d(TAG, "run: " + e.getMessage());
                                }
                            }
                        });


                        socket.on("updateDist" + distModel.getId(), new Emitter.Listener() {
                            @Override
                            public void call(Object... args) {

                                Log.d(TAG, "call: " + args[0]);

                                JSONObject dist = null;
                                try {
                                    dist = new JSONObject(args[0].toString());
                                    final DistModel distModel = new DistModel();
                                    distModel.setId(dist.getString("_id"));
                                    distModel.setName(dist.getString("name"));
                                    distModel.setTimestamp(dist.getString("timestamp"));
                                    distModel.setLocation(dist.getString("location"));
                                    if (dist.has("profileImageUrl")) {
                                        distModel.setProfileUrl(dist.getString("profileImageUrl"));
                                    }
                                    if (dist.has("backgroundImageUrl")) {
                                        distModel.setBackgroundUrl(dist.getString("backgroundImageUrl"));
                                    }
                                    if (dist.has("distInfo")) {
                                        distModel.setDistInfo(dist.getString("distInfo"));
                                    }
                                    if (dist.has("latestOrder")) {
                                        distModel.setLatestOrder(getOrderModelfromJson(dist.getJSONObject("latestOrder")));
                                    }
                                    if (dist.has("imageUrls")) {
                                        JSONArray imageUrlsJsonArray = dist.getJSONArray("imageUrls");
                                        if (imageUrlsJsonArray.length() > 0) {
                                            ArrayList<String> tempStrings = new ArrayList<>();
                                            for (int k = 0; k < imageUrlsJsonArray.length(); k++) {
                                                tempStrings.add(imageUrlsJsonArray.getString(k));
                                            }
                                            distModel.setImageUrls(tempStrings);

                                        }
                                    }

                                    Log.d(TAG, "call: " + distModel);

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {

                                            for (int j = 0; j < distModelArrayList.size(); j++) {
                                                if (distModelArrayList.get(j).getId().equals(distModel.getId())) {
//                                            Log.d(TAG, "run: " + distModel.id +" " + j);
                                                    distModel.setLatestOrder(distModelArrayList.get(j).getLatestOrder());
                                                    distModelArrayList.remove(j);
                                                    distRecyclerViewAdapter.notifyDataSetChanged();
                                                    distModelArrayList.add(j, distModel);
                                                    distRecyclerViewAdapter.notifyItemChanged(j);
                                                }
                                            }

                                            tinyDB.putListDistModel("distList", distModelArrayList);

                                        }
                                    });


                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }


                            }
                        });

                    }
                });

            }


        }


        GetData getData = new GetData(new GetData.AsyncResponse() {
            @Override
            public void processFinish(final String output) throws JSONException {

                Log.d(TAG, "processFinish: downloading");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "run: " + output);
                        init(output);
                    }
                });
            }
        });

        if (Token.getAuthToken() != null) {
            Log.d(TAG, "onRefresh: " + getResources().getString(R.string.localhost) + "/api/" + Token.getAuthToken() + "/dists/" + userId);
            getData.execute(getResources().getString(R.string.localhost) + "/api/" + Token.getAuthToken() + "/dists/" + userId);
        } else {
            Token.setAuthToken(new TinyDB(getApplicationContext()).getString(getResources().getString(R.string.authToken)));
            Log.d(TAG, "onRefresh: " + getResources().getString(R.string.localhost) + "/api/" + Token.getAuthToken() + "/dists/" + userId);
            getData.execute(getResources().getString(R.string.localhost) + "/api/" + Token.getAuthToken() + "/dists/" + userId);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    public void init(String output) {


        try {
            JSONObject object = new JSONObject(output);
            JSONArray distData = object.getJSONArray("msg");

            distModelArrayList.clear();
            Log.d(TAG, "init: distArrayList" + distModelArrayList);

            if (distRecyclerViewAdapter == null) {
                distRecyclerViewAdapter = new DistSearchRecyclerViewAdapter(getApplicationContext(), distModelArrayList, userId, MainActivity.this);
                allDistRecyclerView.setAdapter(distRecyclerViewAdapter);
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
                final LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(this, R.anim.layout_animation_fall_down);
                allDistRecyclerView.setLayoutManager(linearLayoutManager);
                allDistRecyclerView.setLayoutAnimation(controller);
                progressBar.setVisibility(View.GONE);
            }
            progressBar.setVisibility(View.GONE);
            Log.d(TAG, "init: distArrayList" + distModelArrayList);

            if (distData.length() > 0) for (int i = 0; i < distData.length(); i++) {

                JSONObject dist = distData.getJSONObject(i);
                Log.d(TAG, "onCreate: " + dist);
                DistModel distModel = new DistModel();
                distModel.setId(dist.getString("_id"));
                distModel.setName(dist.getString("name"));
                distModel.setTimestamp(dist.getString("timestamp"));
                distModel.setLocation(dist.getString("location"));
                if (dist.has("profileImageUrl")) {
                    distModel.setProfileUrl(dist.getString("profileImageUrl"));
                }

                if (dist.has("backgroundImageUrl")) {
                    distModel.setBackgroundUrl(dist.getString("backgroundImageUrl"));
                }
                if (dist.has("distInfo")) {
                    distModel.setDistInfo(dist.getString("distInfo"));
                }
                if (dist.has("latestOrder")) {
                    distModel.setLatestOrder(getOrderModelfromJson(dist.getJSONObject("latestOrder")));
                }
                if (dist.has("skippedImg")) {
                    distModel.setSkippedImage(dist.getString("skippedImg"));
                }
                if (dist.has("imageUrls")) {
                    JSONArray imageUrlsJsonArray = dist.getJSONArray("imageUrls");
                    if (imageUrlsJsonArray.length() > 0) {
                        ArrayList<String> tempStrings = new ArrayList<>();
                        for (int k = 0; k < imageUrlsJsonArray.length(); k++) {
                            tempStrings.add(imageUrlsJsonArray.getString(k));
                        }
                        Log.d(TAG, "init: " + tempStrings);
                        distModel.setImageUrls(tempStrings);

                    }
                }
                if (dist.has("priority")) {
                    distModel.setPriority(dist.getInt("priority"));
                }
                Log.d(TAG, "init:dist " + distModel);

                distModelArrayList.add(distModel);
                Log.d(TAG, "init: distArrayList" + distModelArrayList.size());


            }
            Log.d(TAG, "init: distArrayList" + distModelArrayList.size());
            distRecyclerViewAdapter.notifyDataSetChanged();
            if (pullToRefresh != null) {
                pullToRefresh.setRefreshing(false);
            }
            tinyDB.putListDistModel("distList", distModelArrayList);


            for (int i = 0; i < distModelArrayList.size(); i++) {

                final DistModel distModel = distModelArrayList.get(i);
                final int finalI = i;
                socket.on("updatedOrder" + distModel.getId() + "_" + tinyDB.getObject("user", UserModel.class).getId(), new Emitter.Listener() {
                    @Override
                    public void call(final Object... args) {
                        Log.d(TAG, "call: " + args[0].toString());

                        try {
                            OrderModel orderModel = getOrderModelfromJson(new JSONObject(args[0].toString()));
                            Log.d(TAG, "call: " + orderModel);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                try {

                                    for (int j = 0; j < distModelArrayList.size(); j++) {
                                        if (distModelArrayList.get(j).getId().equals(distModel.getId())) {

                                            distModel.setLatestOrder(getOrderModelfromJson(new JSONObject(args[0].toString())));
                                            distModelArrayList.remove(j);
                                            distRecyclerViewAdapter.notifyDataSetChanged();
                                            distModelArrayList.add(j, distModel);
                                            distRecyclerViewAdapter.notifyItemChanged(j);
                                        }
                                    }

                                    tinyDB.putListDistModel("distList", distModelArrayList);


                                } catch (Exception e) {
                                    Log.d(TAG, "run: " + e.getMessage());
                                }
                            }
                        });


                    }
                });

                socket.on("updateDist" + distModel.getId(), new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {

                        JSONObject dist = null;
                        try {
                            dist = new JSONObject(args[0].toString());
                            final DistModel distModel = new DistModel();
                            distModel.setId(dist.getString("_id"));
                            distModel.setName(dist.getString("name"));
                            distModel.setTimestamp(dist.getString("timestamp"));
                            distModel.setLocation(dist.getString("location"));
                            if (dist.has("profileImageUrl")) {
                                distModel.setProfileUrl(dist.getString("profileImageUrl"));
                            }
                            if (dist.has("backgroundImageUrl")) {
                                distModel.setBackgroundUrl(dist.getString("backgroundImageUrl"));
                            }
                            if (dist.has("distInfo")) {
                                distModel.setDistInfo(dist.getString("distInfo"));
                            }
                            if (dist.has("latestOrder")) {
                                distModel.setLatestOrder(getOrderModelfromJson(dist.getJSONObject("latestOrder")));
                            }
                            if (dist.has("skippedImg")) {
                                distModel.setSkippedImage(dist.getString("skippedImg"));
                            }
                            if (dist.has("imageUrls")) {
                                JSONArray imageUrlsJsonArray = dist.getJSONArray("imageUrls");
                                if (imageUrlsJsonArray.length() > 0) {
                                    ArrayList<String> tempStrings = new ArrayList<>();
                                    for (int k = 0; k < imageUrlsJsonArray.length(); k++) {
                                        tempStrings.add(imageUrlsJsonArray.getString(k));
                                    }
                                    Log.d(TAG, "init: " + tempStrings);
                                    distModel.setImageUrls(tempStrings);

                                }
                            }

                            if (dist.has("priority")) {
                                distModel.setPriority(dist.getInt("priority"));
                            }


                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    for (int j = 0; j < distModelArrayList.size(); j++) {
                                        if (distModelArrayList.get(j).getId().equals(distModel.getId())) {
                                            distModel.setLatestOrder(distModelArrayList.get(j).getLatestOrder());
                                            distModelArrayList.remove(j);
                                            distRecyclerViewAdapter.notifyDataSetChanged();
                                            distModelArrayList.add(j, distModel);
                                            distRecyclerViewAdapter.notifyItemChanged(j);
                                        }
                                    }

                                    tinyDB.putListDistModel("distList", distModelArrayList);

                                }
                            });


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                });


            }


        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "init: " + e.getMessage());
            if (pullToRefresh != null) {
                pullToRefresh.setRefreshing(false);
            }
        }


    }

    private OrderModel getOrderModelfromJson(JSONObject object) throws JSONException {
        OrderModel orderModel = new OrderModel();
        orderModel.setId(object.getString("_id"));
        orderModel.setTimestamp(object.getString("timestamp"));
        orderModel.setRet_id(object.getString("ret_id"));
        orderModel.setDist_id(object.getString("dist_id"));
        orderModel.setDistUser_id(object.getString("distUser_id"));
        if (object.has("status")) {
            orderModel.setStatus(object.getJSONArray("status"));
            orderModel.setStatusJsonStr(object.getJSONArray("status").toString());
        }
        if (object.has("audioReceipt")) orderModel.setAudioRec(object.getString("audioReceipt"));
        if (object.has("imageReceipt")) orderModel.setImageRec(object.getString("imageReceipt"));
        if (object.has("videoIntroReceipt"))
            orderModel.setVideoIntroReceipt(object.getString("videoIntroReceipt"));
        Log.d(TAG, "getOrderModelfromJson: " + orderModel);
        return orderModel;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {


        try{
            if (!searchView.isIconified()) {
                searchView.onActionViewCollapsed();
            } else {
                logFirebaseEvent("back_tapped", "", "");
                super.onBackPressed();
            }
        }catch (Exception e)
        {
            super.onBackPressed();

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    recreate();

                } else {
                    progressBar.setVisibility(View.GONE);

                    final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                    alertDialog.setMessage(getResources().getString(R.string.home_screen_storage_permission_denied));
                    alertDialog.setPositiveButton("OKAY", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MainActivity.this, PERMISSION, 1);
                            dialog.dismiss();
                        }
                    });
                    alertDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                        @Override
                        public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                            if (i == KeyEvent.KEYCODE_BACK) {
                                finish();
                                overridePendingTransition(R.anim.exit, android.R.anim.fade_out);
                            }
                            return false;
                        }
                    });
                    alertDialog.setCancelable(false);
                    alertDialog.show();
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void updateWalletData(final boolean openAmount) {

        socket.on("wallet_" + tinyDB.getObject("user", UserModel.class).getId(), new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                try {
                    String output = args[0].toString();
                    JSONObject walletJson = new JSONObject(output);

                    WalletModel walletModel = new WalletModel();
                    walletModel.setAmount(walletJson.getLong("amount"));
                    walletModel.setId(walletJson.getString("_id"));
                    walletModel.setTimestamp(walletJson.getLong("timestamp"));
                    Log.d(TAG, "call: wallet updated" + walletModel.getAmount());

                    tinyDB.putObject("wallet", walletModel);
                    invalidateOptionsMenu();


                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();

                }


            }
        });


        GetData getData = new GetData(new GetData.AsyncResponse() {
            @Override
            public void processFinish(final String output) throws JSONException {
                try {
                    JSONObject res = new JSONObject(output);
                    if (res.getBoolean("status")) {

                        JSONObject walletRes = new JSONObject(output);
                        final JSONObject walletJson = walletRes.getJSONArray("msg").getJSONObject(0);

                        WalletModel walletModel = new WalletModel();
                        walletModel.setAmount(walletJson.getLong("amount"));
                        walletModel.setId(walletJson.getString("_id"));
                        walletModel.setTimestamp(walletJson.getLong("timestamp"));

                        tinyDB.putObject("wallet", walletModel);
                        invalidateOptionsMenu();
                        if (openAmount) {
                            startActivity(new Intent(MainActivity.this, AddAmountActivity.class));
                            overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
                        }


                    } else {


                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        getData.execute(getResources().getString(R.string.localhost) + "/api/" + Token.getAuthToken() + "/wallet/" + tinyDB.getObject("user", UserModel.class).getId());
    }


    private void logFirebaseEvent(String eventName, String paramName, String paramValue) {

        Bundle params = new Bundle();
        params.putString(paramName, paramValue);
        mFirebaseAnalytics.logEvent(eventName, params);
    }

    private void logFirebaseEvent2(String eventName, String paramName, int paramValue) {

        Bundle params = new Bundle();
        params.putInt(paramName, paramValue);
        mFirebaseAnalytics.logEvent(eventName, params);
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (searchView != null) {
            if (!searchView.getQuery().toString().isEmpty())
                logFirebaseEvent("searched", "what", currSearchString);
        }

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
