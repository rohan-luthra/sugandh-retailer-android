package com.sbw.auder.OrdersActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.github.rubensousa.gravitysnaphelper.GravitySnapHelper;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.sbw.auder.HomeScreen.MainActivity;
import com.sbw.auder.LoginAndSignUp.LoginPhoneActivity;
import com.sbw.auder.Models.DistModel;
import com.sbw.auder.Models.Token;
import com.sbw.auder.Models.UserModel;
import com.sbw.auder.PrayerSentDialog;
import com.sbw.auder.Utils.GetData;
import com.sbw.auder.Models.OrderModel;
import com.sbw.auder.RecyclerViewAdapters.OrdersRecyclerViewAdapter;
import com.sbw.auder.PlaceOrder.PlaceOrderActivity;
import com.sbw.auder.R;
import com.sbw.auder.Utils.TinyDB;
import com.sbw.auder.Models.UserModelStatic;
import com.zplesac.connectionbuddy.ConnectionBuddy;
import com.zplesac.connectionbuddy.interfaces.ConnectivityChangeListener;
import com.zplesac.connectionbuddy.models.ConnectivityEvent;
import com.zplesac.connectionbuddy.models.ConnectivityState;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class OrderActivity extends AppCompatActivity implements ConnectivityChangeListener {

    String distId;
    RecyclerView orderRecyclerView;
    DistModel distModel = new DistModel();
    static ArrayList<OrderModel> orderModelArrayList = new ArrayList<>();
    static OrdersRecyclerViewAdapter adapter;

    MediaPlayer mediaPlayer;
    ImageView distributorBg;
    Socket socket;
    ProgressBar progressBar;
    ImageView refresh;

    private FirebaseAnalytics mFirebaseAnalytics;
    boolean isNetwork = false;
    boolean dialogShowing = false;

    boolean ifComingFromFinalActivity=false;


    private static final String TAG = "OrderActivity";
    private TinyDB tinyDB;
    String PERMISSION[] = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};

    @Override
    protected void onStart() {
        super.onStart();
        try{
            ConnectionBuddy.getInstance().registerForConnectivityEvents(this, this);

        }catch (Exception e)
        {

        }
        ActivityCompat.requestPermissions(OrderActivity.this, PERMISSION, 1);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (new TinyDB(this).getString("Lang")!=null)
        {
            setLanguageForApp(new TinyDB(this).getString("Lang"), this);
        }
        setContentView(R.layout.activity_order);
        tinyDB = new TinyDB(getApplicationContext());

        ifComingFromFinalActivity=false;
        distModel = getIntent().getParcelableExtra("dist");
        distId = distModel.getId();
        distributorBg = findViewById(R.id.distributorBackgroundImage2);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
        mFirebaseAnalytics=FirebaseAnalytics.getInstance(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.tb_toolbarsearch);
        setSupportActionBar(toolbar);

        setTitle(distModel.getName());
        toolbar.setTitleTextAppearance(this, R.style.toolbarText2);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

        // add back arrow to toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // back button pressed
                logFirebaseEvent("chat_back_tapped", "", "");

                if (getIntent().getStringExtra("fromActivity") == null) {
                    finish();
                    overridePendingTransition(R.anim.exit, android.R.anim.fade_out);

                } else {
                    if (getIntent().getStringExtra("fromActivity").equalsIgnoreCase("finalOrder")) {
                        startActivity(new Intent(OrderActivity.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                        overridePendingTransition(R.anim.exit, android.R.anim.fade_out);

                    }
                }
            }
        });


        if (getIntent().getStringExtra("fromActivity") == null) {


        } else {
            if (getIntent().getStringExtra("fromActivity").equalsIgnoreCase("finalOrder")) {
                PrayerSentDialog prayerSentDialog = new PrayerSentDialog();
                prayerSentDialog.showDialog(this);
                ifComingFromFinalActivity=true;
                try{
                    MediaPlayer mp = MediaPlayer.create(this, R.raw.temple_bell);
                    mp.start();
                    mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            mp.release();
                        }
                    });
                }catch (Exception e)
                {
                    Log.d(TAG, "onCreate: "+e);
                }

            }
        }

        refresh=findViewById(R.id.refresh_button);
        refresh.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_indefinitely));
        if (!isNetwork)
        {
        }

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isNetwork) {
                    logFirebaseEvent("reciept_screen_refresh_tapped", "", "");
                    refresh.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_indefinitely));
                    progressBar.setVisibility(View.VISIBLE);
                    getData();
                }else{

                    Snackbar.make(findViewById(android.R.id.content), getResources().getString(R.string.no_internet_connection_snackbar), Snackbar.LENGTH_LONG).show();

                }
            }
        });
        mediaPlayer = new MediaPlayer();

        if (tinyDB.getString("userData") != null) {
            String userDataStr = tinyDB.getString("userData");
            Log.d(TAG, "onCreate: " + userDataStr);

            try {
                JSONObject userData = new JSONObject(userDataStr);
                Log.d(TAG, "processFinish1: " + userData);
                socket = IO.socket(getResources().getString(R.string.localhost));
                socket.connect();

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }


        } else {
            finish();
            overridePendingTransition(R.anim.exit, android.R.anim.fade_out);

        }
//        distId = getIntent().getStringExtra("dist_id");
        orderRecyclerView = findViewById(R.id.orderRecyclerView);
        orderModelArrayList = new ArrayList<>();


        if (tinyDB.getOrderData(distId) != null) {
            orderModelArrayList = tinyDB.getOrderData(distId);
            if(tinyDB.getOrderData(distId).isEmpty())
                logFirebaseEvent("chat_screen_opened_with_empty_chat", "","");
            progressBar.setVisibility(View.GONE);
            refresh.clearAnimation();
            addWebSocket();
//            addWebSocket();
        }

        adapter = new OrdersRecyclerViewAdapter(getApplicationContext(), orderModelArrayList, distModel, mediaPlayer, OrderActivity.this);
        orderRecyclerView.setAdapter(adapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());

//        new PagerSnapHelper().attachToRecyclerView(orderRecyclerView);
        orderRecyclerView.setHasFixedSize(true);
        orderRecyclerView.setNestedScrollingEnabled(false);
        orderRecyclerView.setLayoutManager(linearLayoutManager);
        if(orderModelArrayList.size()>0){
            orderRecyclerView.scrollToPosition(orderModelArrayList.size()-1);
        }else{
        }

//        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(orderRecyclerView.getContext(), linearLayoutManager.getOrientation());
//        orderRecyclerView.addItemDecoration(dividerItemDecoration);


        getData();

        if (orderModelArrayList.size()>0)
        {
            findViewById(R.id.noOrderMessageReceiptScreen).setVisibility(View.GONE);

        }
//        else{
//            findViewById(R.id.noOrderMessageReceiptScreen).setVisibility(View.VISIBLE);
//
//        }


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logFirebaseEvent("chat_place_order_tapped", "dist_name", distModel.getName());
                if (getIntent().getStringExtra("fromActivity") == null) {
                    finish();
                    overridePendingTransition(R.anim.exit, android.R.anim.fade_out);

                } else {
                    if (getIntent().getStringExtra("fromActivity").equalsIgnoreCase("finalOrder")) {
                        startActivity(new Intent(OrderActivity.this, PlaceOrderActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK)
                                .putExtra("fromActivity", "order")
                                .putExtra("dist", distModel));
                        overridePendingTransition(R.anim.enter_right, android.R.anim.fade_out);

                    }
                }
//               finish();
            }
        });

        setUpBg();

    }

    private void getData() {
        GetData getData = new GetData(new GetData.AsyncResponse() {
            @Override
            public void processFinish(String output) throws JSONException {

                try {
                    JSONObject result = new JSONObject(output);
                    if (result.getBoolean("status")) {
                        JSONArray array = result.getJSONArray("msg");
                        if(array.length()>0) {
                            orderModelArrayList.clear();
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject object = array.getJSONObject(i);

                                if (object.getString("dist_id").equals(distId)) {
                                    findViewById(R.id.noOrderMessageReceiptScreen).setVisibility(View.GONE);

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
                                    if (object.has("audioReceipt"))
                                        orderModel.setAudioRec(object.getString("audioReceipt"));
                                    if (object.has("imageReceipt"))
                                        orderModel.setImageRec(object.getString("imageReceipt"));
                                    if (object.has("videoIntroReceipt"))
                                        orderModel.setVideoIntroReceipt(object.getString("videoIntroReceipt"));
                                    if (object.has("recText")) {
                                        orderModel.setRecText(object.getString("recText"));
                                    }
                                    if (object.has("scheduledTimestamp")) {
                                        orderModel.setSchTimestamp(object.getString("scheduledTimestamp"));
                                    }
                                    if (object.has("userImageUrl")) {
                                        orderModel.setUserImageUrl(object.getString("userImageUrl"));
                                    }
                                    if (object.has("distName"))
                                        orderModel.setDistName(object.getString("distName"));


                                    Log.d(TAG, "processFinish: Order " + orderModel.getDistName());
                                    orderModelArrayList.add(orderModel);
                                }
                            }
                            tinyDB.putOrderData(distId, orderModelArrayList);
                            adapter.notifyDataSetChanged();
                            orderRecyclerView.scrollToPosition(orderModelArrayList.size()-1);
                            progressBar.setVisibility(View.GONE);
                            refresh.clearAnimation();
                            addWebSocket();
                            if (!ifComingFromFinalActivity)
                            {
                                Toast.makeText(OrderActivity.this, ""+getResources().getString(R.string.toast_receipts_screen_refreshed), Toast.LENGTH_SHORT).show();

                            }else{
                                ifComingFromFinalActivity=false;
                            }
                        }else{

                            findViewById(R.id.noOrderMessageReceiptScreen).setVisibility(View.VISIBLE);
                            logFirebaseEvent("chat_screen_opened_with_empty_chat", "","");
                            refresh.clearAnimation();

                        }

//                        addWebSocket();
                    }
                } catch (Exception e) {
                    progressBar.setVisibility(View.GONE);
                    logFirebaseEvent("chat_screen_opened_with_empty_chat", "","");
                    refresh.clearAnimation();
                    Log.d(TAG, "processFinish: " + e.getMessage());
                }
            }
        });

        getData.execute(getResources().getString(R.string.localhost) + "/api/" + tinyDB.getString(getResources().getString(R.string.authToken)) + "/order/retDist/" + FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber() + "&" + distId);
    }

    private void setUpBg() {
        distributorBg.setImageResource(R.drawable.order_activity_background);
    }





    @Override
    public void onBackPressed() {
        logFirebaseEvent("chat_back_tapped", "", "");

        mediaPlayer.reset();
        mediaPlayer.stop();

        if (getIntent().getStringExtra("fromActivity") == null) {
            finish();
            overridePendingTransition(R.anim.exit, android.R.anim.fade_out);

        } else {
            if (getIntent().getStringExtra("fromActivity").equalsIgnoreCase("finalOrder")) {
                startActivity(new Intent(OrderActivity.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                overridePendingTransition(R.anim.exit, android.R.anim.fade_out);

            }
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        mediaPlayer.reset();
        mediaPlayer.stop();
    }

    private void logFirebaseEvent(String eventName, String paramName ,String paramValue)
    {
        Bundle params = new Bundle();
        params.putString(paramName, paramValue);
        mFirebaseAnalytics.logEvent(eventName, params);
    }

    private void logFirebaseEvent(String eventName, String paramName ,int paramValue)
    {
        Bundle params = new Bundle();
        params.putInt(paramName, paramValue);
        mFirebaseAnalytics.logEvent(eventName, params);
    }

    @Override
    protected void onStop() {
        super.onStop();
        try{
            ConnectionBuddy.getInstance().unregisterFromConnectivityEvents(this);

        }catch (Exception e)
        {
            Log.d(TAG, "onStop: "+e);
        }

    }
    @Override
    public void onConnectionChange(ConnectivityEvent event) {
        try{
            // device has active internet connection
            // there is no active internet connection on this device
            isNetwork = event.getState().getValue() == ConnectivityState.CONNECTED;
            if (!isNetwork){
                Snackbar.make(findViewById(android.R.id.content), getResources().getString(R.string.no_internet_connection_snackbar), Snackbar.LENGTH_LONG).show();
            }

        }catch (Exception e)
        {
            Log.d(TAG, "onConnectionChange: "+e);
        }

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }



    private void addWebSocket() {

        for (int i = 0; i < orderModelArrayList.size(); i++) {

            OrderModel orderModel1 = orderModelArrayList.get(i);
            socket.on("order_" + orderModel1.getId(), new Emitter.Listener() {
                @Override
                public void call(final Object... args) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            try {

                                Log.d(TAG, "run: orderWebSocket" + args[0].toString());

                                JSONObject object = new JSONObject(args[0].toString());
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
                                if (object.has("audioReceipt"))
                                    orderModel.setAudioRec(object.getString("audioReceipt"));
                                if (object.has("imageReceipt"))
                                    orderModel.setImageRec(object.getString("imageReceipt"));
                                if (object.has("videoIntroReceipt"))
                                    orderModel.setVideoIntroReceipt(object.getString("videoIntroReceipt"));
                                if (object.has("recText")) {
                                    orderModel.setRecText(object.getString("recText"));
                                }
                                if (object.has("scheduledTimestamp")) {
                                    orderModel.setSchTimestamp(object.getString("scheduledTimestamp"));
                                }
                                if (object.has("userImageUrl")) {
                                    orderModel.setUserImageUrl(object.getString("userImageUrl"));
                                }
                                if (object.has("distName"))
                                    orderModel.setDistName(object.getString("distName"));


                                for (int j = 0; j < orderModelArrayList.size(); j++) {

                                    if (orderModelArrayList.get(j).getId().toString().trim().equalsIgnoreCase(orderModel.getId().toString().trim())) {
                                        Log.d(TAG, "run: Index" + orderModel + " " + j);
                                        orderModelArrayList.remove(j);
                                        orderModelArrayList.add(j, orderModel);
                                        adapter.notifyItemChanged(j);
                                        break;
                                    }
                                }


                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
                    });

                }
            });

        }

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