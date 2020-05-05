package com.sbw.auder.PlaceOrder;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.sbw.auder.Camera2Kit.CameraConstants;
import com.sbw.auder.LoginAndSignUp.LoginPhoneActivity;
import com.sbw.auder.Models.DistModel;
import com.sbw.auder.Models.Token;
import com.sbw.auder.Models.UserModel;
import com.sbw.auder.Models.WalletModel;
import com.sbw.auder.OrdersActivity.OrderActivity;
import com.sbw.auder.R;
import com.sbw.auder.Utils.GetData;
import com.sbw.auder.Utils.TinyDB;
import com.sbw.auder.WalletActivity.AddAmountActivity;
import com.wooplr.spotlight.SpotlightView;
import com.wooplr.spotlight.utils.SpotlightListener;
import com.zplesac.connectionbuddy.ConnectionBuddy;
import com.zplesac.connectionbuddy.ConnectionBuddyConfiguration;
import com.zplesac.connectionbuddy.interfaces.ConnectivityChangeListener;
import com.zplesac.connectionbuddy.models.ConnectivityEvent;
import com.zplesac.connectionbuddy.models.ConnectivityState;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;


public class PlaceOrderFinalActivity extends AppCompatActivity implements ConnectivityChangeListener {

    private static final String TAG = "PlaceOrderFinalActivity";
    private static final int CLICKED_IMAGE_WIDTH = 720;

    ImageView previewImage,
            playPreviewAudio,
            sendButton,
            lotus;

    TextView
            previewMessage;

    ProgressDialog progressDialog;

    TextView placeOrder;

    private String audioData;
    private MediaPlayer mediaPlayer;
    int audioStatus = 0;
    int AUDIO_STATUS_PLAY = 0;
    int AUDIO_STATUS_STOP = 1;

    private Bitmap img;

    DistModel distModel;
    TinyDB tinyDB;

    private FirebaseAnalytics mFirebaseAnalytics;

    //for firebase analytics
    boolean previewNotPlayed = false;

    boolean isNetwork = false;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (new TinyDB(this).getString("Lang")!=null)
        {
            setLanguageForApp(new TinyDB(this).getString("Lang"), this);
        }
        setContentView(R.layout.activity_place_order_final);
        ConnectionBuddyConfiguration networkInspectorConfiguration = new ConnectionBuddyConfiguration.Builder(this).build();
        ConnectionBuddy.getInstance().init(networkInspectorConfiguration);

        checkUser();

        playPreviewAudio = findViewById(R.id.audioBtn);
        lotus = findViewById(R.id.lotus);
        sendButton = findViewById(R.id.send_button);
        Toolbar toolbar = (Toolbar) findViewById(R.id.tb_toolbarsearch);
        previewMessage = findViewById(R.id.preview_message);
        previewImage = findViewById(R.id.preview);
        placeOrder = findViewById(R.id.placeOrder);

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
                overridePendingTransition(R.anim.exit, android.R.anim.fade_out);

            }
        });
        setTitle("Place Order");
        distModel = getIntent().getParcelableExtra("dist");


        lotus.setVisibility(View.VISIBLE);
        lotus.startAnimation(AnimationUtils.loadAnimation(this, R.anim.rotate_lotus));

        setSpotLight();

        previewNotPlayed = true;
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        previewMessage.setVisibility(View.VISIBLE);
        previewImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logFirebaseEvent("preview_image_tapped", "", "");
            }
        });

        progressDialog = new ProgressDialog(this);
        progressDialog.setCanceledOnTouchOutside(false);

        tinyDB = new TinyDB(this);


        if (!getIntent().getStringExtra("orderType_fees").equals("0")) {
            placeOrder.setText(getResources().getString(R.string.send_order_with_amount)+" Rs. " + getIntent().getStringExtra("orderType_fees") + "/- "+getResources().getString(R.string.offering));
        } else {
            placeOrder.setText(getResources().getString(R.string.send_my_prayer));
        }

        if (getIntent().getStringExtra("imagePath") != null) {
            lotus.clearAnimation();
            lotus.setVisibility(View.GONE);


            new Thread(new Runnable() {
                @Override
                public void run() {

                    Bitmap temp = BitmapFactory.decodeFile(getIntent().getStringExtra("imagePath"));

                    ByteArrayOutputStream stream = new ByteArrayOutputStream();

                    temp.compress(Bitmap.CompressFormat.JPEG, 100, stream);

                    byte[] imageDataBytes = stream.toByteArray();
                    final Bitmap finalTemp = BitmapFactory.decodeByteArray(imageDataBytes, 0, imageDataBytes.length);
                    Log.d(TAG, "run: " + temp.getByteCount());

                }
            }).start();


            setPreviewImage();

        } else {

            if (distModel.getSkippedImage() != null) {
                loadDefaultImage(distModel);
            }

        }

        Log.d(TAG, "onCreate: " + getIntent().getStringExtra("distId"));


        setRecordedAudioPreview();


        placeOrder.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    placeOrder.setTextColor(Color.parseColor("#80ffffff"));
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    placeOrder.setTextColor(getResources().getColor(R.color.white));
                }
                return false;
            }
        });


        placeOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendOrderToDistributor();

            }
        });
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendOrderToDistributor();
            }
        });


    }

    private void sendOrderToDistributor() {
        logFirebaseEvent("pay_and_send_tapped", "", "");
        placeOrder.setTextColor(Color.parseColor("#ffffff"));

        progressDialog.setMessage(getResources().getString(R.string.creating_order));
        progressDialog.show();
        final JSONObject finalData = new JSONObject();
        try {
            final String[] imageData = {"0"};
            final JSONObject data = new JSONObject();
            data.put("ret_id", tinyDB.getObject("user", UserModel.class).getId());
            data.put("dist_id", getIntent().getStringExtra("distId"));
            data.put("orderType_id", getIntent().getStringExtra("orderType_id"));
            data.put("orderType_fees", getIntent().getStringExtra("orderType_fees"));
            data.put("orderAudio", audioData);
            data.put("OrderAudioLength", getIntent().getIntExtra("OrderAudioLength", 0));

            Log.d(TAG, "onClick: " + getIntent().getStringExtra("orderTpye_id"));
            if (getIntent().getStringExtra("imagePath") != null) {
                Log.d(TAG, "onCreate: " + getIntent().getStringExtra("imagePath"));

                int x = img.getWidth();
                int y = img.getHeight();
                float aspectRatio = (float) y / x;
                x = (int) (CLICKED_IMAGE_WIDTH);
                y = (int) ((float) aspectRatio * x);
                img = Bitmap.createScaledBitmap(img, x, y, true);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                img.compress(Bitmap.CompressFormat.JPEG, 25, stream);

                byte[] imageDataBytes = stream.toByteArray();
                imageData[0] = Base64.encodeToString(imageDataBytes, Base64.DEFAULT);

                data.put("orderImage", imageData[0]);
                Log.d(TAG, "onCreate: Done");
                finalData.put("data", data);
                try {

                    if (tinyDB.getObject("wallet", WalletModel.class) == null) {
                        addWallet();
                        placeOrder.setEnabled(true);
                        try{
                            progressDialog.dismiss();

                        }catch (Exception e)
                        {

                        }

                    } else {
                        WalletModel walletModel = tinyDB.getObject("wallet", WalletModel.class);
                        long walletamount = walletModel.getAmount();
                        if (Long.parseLong(getIntent().getStringExtra("orderType_fees")) <= walletamount) {
                            sendPost(getResources().getString(R.string.localhost) + "/api/" + new TinyDB(getApplicationContext()).getString(getResources().getString(R.string.authToken)) + "/placeOrder", finalData);
                        } else {
                            startActivity(new Intent(PlaceOrderFinalActivity.this, AddAmountActivity.class));
                            overridePendingTransition(R.anim.enter_right, android.R.anim.fade_out);
                            placeOrder.setEnabled(true);
                            progressDialog.dismiss();

                        }
                    }

                } catch (Exception e) {
                    addWallet();
                    placeOrder.setEnabled(true);
                    progressDialog.dismiss();
                }
            } else {
                finalData.put("data", data);
                imageData[0] = "0";
                data.put("orderImage", imageData[0]);
                finalData.put("data", data);

                try {

                    if (tinyDB.getObject("wallet", WalletModel.class) == null) {
                        addWallet();
                        placeOrder.setEnabled(true);
                        progressDialog.dismiss();
                    } else {
                        WalletModel walletModel = tinyDB.getObject("wallet", WalletModel.class);
                        long walletamount = walletModel.getAmount();
                        if (Long.parseLong(getIntent().getStringExtra("orderType_fees")) <= walletamount) {
                            sendPost(getResources().getString(R.string.localhost) + "/api/" + new TinyDB(getApplicationContext()).getString(getResources().getString(R.string.authToken)) + "/placeOrder", finalData);
                        } else {
                            startActivity(new Intent(PlaceOrderFinalActivity.this, AddAmountActivity.class));
                            overridePendingTransition(R.anim.enter_right, android.R.anim.fade_out);

                            placeOrder.setEnabled(true);
                            progressDialog.dismiss();

                        }
                    }

                } catch (Exception e) {
                    addWallet();
                    placeOrder.setEnabled(true);
                    progressDialog.dismiss();
                }


            }


        } catch (JSONException e) {
            progressDialog.hide();
            e.printStackTrace();
            Log.d(TAG, "onClick: " + e.getMessage());
        }
    }

    private void setRecordedAudioPreview() {
        if (getIntent().getStringExtra("audioData") != null) {

            File file = new File(getIntent().getStringExtra("audioData"));
            try {
                byte[] bytes = FileUtils.readFileToByteArray(file);
                audioData = Base64.encodeToString(bytes, Base64.DEFAULT);
                playPreviewAudio.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (audioStatus == AUDIO_STATUS_PLAY) {

                            if (mediaPlayer == null) {
                                mediaPlayer = new MediaPlayer();
                                try {
                                    mediaPlayer.setDataSource(getIntent().getStringExtra("audioData"));
                                    mediaPlayer.prepare();
                                    mediaPlayer.setLooping(false);


                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            }
                            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                @Override
                                public void onCompletion(MediaPlayer mp) {
                                    mediaPlayer.stop();
                                    mediaPlayer.release();
                                    mediaPlayer = null;
                                    audioStatus = AUDIO_STATUS_PLAY;
                                    playPreviewAudio.setImageResource(R.drawable.preview_play_button);
                                    previewMessage.setVisibility(View.VISIBLE);
                                }
                            });

                            audioStatus = AUDIO_STATUS_STOP;
                            mediaPlayer.start();
                            previewMessage.setVisibility(View.GONE);
                            previewNotPlayed = false;
                            logFirebaseEvent("preview_play_tapped", "", "");
                            playPreviewAudio.setImageResource(R.drawable.preview_pause_button);
                        } else if (audioStatus == AUDIO_STATUS_STOP) {

                            if (mediaPlayer != null) {
                                mediaPlayer.pause();
                                logFirebaseEvent("preview_stop_tapped", "", "");
                                //mediaPlayer.release();
                                audioStatus = AUDIO_STATUS_PLAY;
                                previewMessage.setVisibility(View.VISIBLE);
                                playPreviewAudio.setImageResource(R.drawable.preview_play_button);
                            }

                        }


                    }
                });
                previewMessage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        playPreviewAudio.performClick();
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "onCreate: " + e.getMessage());
            }


        } else {
            finish();
            overridePendingTransition(R.anim.exit, android.R.anim.fade_out);

        }

    }

    private void setPreviewImage() {
        if (getIntent().getIntExtra("cameraFacing", 2) == CameraConstants.FACING_BACK) {

            img = BitmapFactory.decodeFile(getIntent().getStringExtra("imagePath"));

            ExifInterface exif = null;
            try {
                exif = new ExifInterface(getIntent().getStringExtra("imagePath"));
                int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_UNDEFINED);
                Matrix matrix = new Matrix();
                if (orientation == 6) {
                    matrix.postRotate(90);
                }
                else if (orientation == 3) {
                    matrix.postRotate(180);
                }
                else if (orientation == 8) {
                    matrix.postRotate(270);
                }
                img = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true); // rotating bitmap
                //img = rotateBitmap(img, 90);
                if (img.getWidth()>img.getHeight())
                {
                    img = rotateBitmap(img, 90);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
//            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
//                    ExifInterface.ORIENTATION_UNDEFINED);
//            if (orientation > 0) {
//                img = rotateBitmap(img, 90);
//
//            }
            logFirebaseEvent2("camera_used", "which_one", 1);
            Glide.with(this).asBitmap().load(img).into(previewImage);


        } else if (getIntent().getIntExtra("cameraFacing", 2) == CameraConstants.FACING_FRONT) {
            img = BitmapFactory.decodeFile(getIntent().getStringExtra("imagePath"));
            ExifInterface exif = null;
            try {
                exif = new ExifInterface(getIntent().getStringExtra("imagePath"));

                int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_UNDEFINED);
                Matrix matrix = new Matrix();
                if (orientation == 6) {
                    matrix.postRotate(90);
                }
                else if (orientation == 3) {
                    matrix.postRotate(180);
                }
                else if (orientation == 8) {
                    matrix.postRotate(270);
                }
                img = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true); // rotating bitmap
                //img= rotateBitmap(img, 270);
                if (img.getWidth()>img.getHeight())
                {
                    img = rotateBitmap(img, 270);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
//            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
//                    ExifInterface.ORIENTATION_UNDEFINED);

//            if (orientation > 0) {
//                img = rotateBitmap(img, 270);
//
//            }
            logFirebaseEvent2("camera_used", "which_one", 2);
            Glide.with(this).asBitmap().load(img).into(previewImage);


        } else {

            img = BitmapFactory.decodeFile(getIntent().getStringExtra("imagePath"));
            ExifInterface exif = null;
            try {
                exif = new ExifInterface(getIntent().getStringExtra("imagePath"));
                int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_UNDEFINED);
                Matrix matrix = new Matrix();
                if (orientation == 6) {
                    matrix.postRotate(90);
                }
                else if (orientation == 3) {
                    matrix.postRotate(180);
                }
                else if (orientation == 8) {
                    matrix.postRotate(270);
                }
                img = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true); // rotating bitmap


            } catch (IOException e) {
                e.printStackTrace();
            }
            assert exif != null;


            //Toast.makeText(this, ""+orientation, Toast.LENGTH_SHORT).show();

                //img = rotateBitmap(img, orientation);


                //previewImage.setImageBitmap(img);
            Glide.with(getApplicationContext()).asBitmap().load(img).into(previewImage);

        }
    }

    private void setSpotLight() {
        new CountDownTimer(1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

                new SpotlightView.Builder(PlaceOrderFinalActivity.this)
                        .introAnimationDuration(500)
                        .enableRevealAnimation(true)
                        .performClick(true)
                        .fadeinTextDuration(300)
                        .headingTvColor(Color.parseColor("#eb273f"))
                        .headingTvSize(18)
                        .headingTvText(getResources().getString(R.string.spotLightTextPreview))
                        .target(playPreviewAudio)
                        .subHeadingTvColor(Color.parseColor("#ffffff"))
                        .subHeadingTvSize(14)
                        .subHeadingTvText(getResources().getString(R.string.spotLightTextPreviewMessage))
                        .maskColor(Color.parseColor("#99000000"))
                        .lineAnimDuration(400)
                        .lineAndArcColor(Color.parseColor("#eb273f"))
                        .dismissOnTouch(true)
                        .dismissOnBackPress(true)
                        .enableDismissAfterShown(false)
                        .usageId("preview picture play audio")
                        .setListener(new SpotlightListener() {
                            @Override
                            public void onUserClicked(String s) {
                                new SpotlightView.Builder(PlaceOrderFinalActivity.this)
                                        .introAnimationDuration(500)
                                        .enableRevealAnimation(true)
                                        .performClick(true)
                                        .fadeinTextDuration(300)
                                        .headingTvColor(Color.parseColor("#eb273f"))
                                        .headingTvSize(18)
                                        .headingTvText(getResources().getString(R.string.spotLightTextReadyToSend))
                                        .target(sendButton)
                                        .subHeadingTvColor(Color.parseColor("#ffffff"))
                                        .subHeadingTvSize(14)
                                        .subHeadingTvText(getResources().getString(R.string.spotLightTextReadyToSendMessage))
                                        .maskColor(Color.parseColor("#99000000"))
                                        .lineAnimDuration(400)
                                        .lineAndArcColor(Color.parseColor("#eb273f"))
                                        .dismissOnTouch(true)
                                        .dismissOnBackPress(true)
                                        .enableDismissAfterShown(false)
                                        .usageId("preview picture send order")
                                        .show();
                            }
                        })
                        .show();
            }

            @Override
            public void onFinish() {

            }
        }.start();

    }

    private void checkUser() {

        TinyDB tinyDB = new TinyDB(getApplicationContext());
        if (tinyDB.getString(getResources().getString(R.string.authToken)) != null && !tinyDB.getString(getResources().getString(R.string.authToken)).isEmpty()) {
            Token.setAuthToken(tinyDB.getString(getResources().getString(R.string.authToken)));
            Log.d(TAG, "checkUser: " + new TinyDB(getApplicationContext()).getString(getResources().getString(R.string.authToken)));
        } else {
            Toast.makeText(this, "You have been signed out. Please log in again.", Toast.LENGTH_SHORT).show();

            startActivity(new Intent(PlaceOrderFinalActivity.this, LoginPhoneActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        }
        if (tinyDB.getString("userData") == null) {
            Toast.makeText(this, "You have been signed out. Please log in again.", Toast.LENGTH_SHORT).show();

            startActivity(new Intent(PlaceOrderFinalActivity.this, LoginPhoneActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        } else if (tinyDB.getString("userData").isEmpty()) {
            Toast.makeText(this, "You have been signed out. Please log in again.", Toast.LENGTH_SHORT).show();

            startActivity(new Intent(PlaceOrderFinalActivity.this, LoginPhoneActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        } else {
            try {

                JSONObject userData = new JSONObject(new TinyDB(getApplicationContext()).getString("userData"));
                Log.d(TAG, "checkUser: " + userData);

                if (userData.has("_id")) {
                    tinyDB.getObject("user", UserModel.class).setId(userData.getString("_id"));
                    if (userData.has("firstName")) {
                        tinyDB.getObject("user", UserModel.class).setFirstName(userData.getString("firstName"));
                    }
                    if (userData.has("LastName")) {
                        tinyDB.getObject("user", UserModel.class).setLastName(userData.getString("LastName"));
                    }
                    if (userData.has("birthdate")) {
                        tinyDB.getObject("user", UserModel.class).setBirthdate(userData.getString("birthdate"));
                    }
                    if (userData.has("email")) {
                        tinyDB.getObject("user", UserModel.class).setEmail(userData.getString("email"));
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "You have been signed out. Please log in again.", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(PlaceOrderFinalActivity.this, LoginPhoneActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

            }
        }
    }

    private void loadDefaultImage(final DistModel distModel) {

        final TinyDB tinyDB = new TinyDB(getApplicationContext());
        if (distModel.getSkippedImage() != null) {

            if (tinyDB.getImagePath("SKIPPED_" + distModel.getSkippedImage()) != null && !tinyDB.isFileNotExist(tinyDB.getImagePath("SKIPPED_" + distModel.getSkippedImage()))) {
                lotus.clearAnimation();
                lotus.setVisibility(View.GONE);

                Glide.with(getApplicationContext())
                        .asBitmap()
                        .load(tinyDB.getImagePath("SKIPPED_" + distModel.getSkippedImage()))
                        .into(previewImage);

            } else {
                Glide.with(getApplicationContext())
                        .asBitmap()
                        .load(getResources().getString(R.string.localhost) + "/api/" + new TinyDB(getApplicationContext()).getString(getResources().getString(R.string.authToken)) + "/download/" + distModel.getSkippedImage())
                        .listener(new RequestListener<Bitmap>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                                // preview.setImageResource(R.drawable.india_gate_bg);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {

                                lotus.clearAnimation();
                                lotus.setVisibility(View.GONE);

                                final String path = tinyDB.saveImage(resource, "SKIPPED_" + distModel.getSkippedImage());
                                tinyDB.setImagePath("SKIPPED_" + distModel.getSkippedImage(), path);
                                return false;
                            }
                        }).into(previewImage);

            }

        }

    }

    public void sendPost(final String urlAdress, final JSONObject data) {

        if (isNetwork) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressDialog.setMessage(getResources().getString(R.string.sending_order));

                }
            });

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


                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                progressDialog.hide();
                                progressDialog.dismiss();
                                placeOrder.setEnabled(false);
                                sendButton.setEnabled(false);

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressDialog.dismiss();

                                        try {

                                            final JSONObject jsonObject = new JSONObject(response);

                                            Log.d(TAG, "run: " + jsonObject);
                                            if (jsonObject.getString("_id") != null) {

                                                Intent intent = new Intent(PlaceOrderFinalActivity.this, OrderActivity.class).putExtra("dist", distModel);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                intent.putExtra("fromActivity", "finalOrder");
                                                startActivity(intent);
                                                overridePendingTransition(R.anim.enter_right, android.R.anim.fade_out);

                                            } else {

                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();

                                        }
                                    }
                                });


                            }
                        });


                        conn.disconnect();
                    } catch (final Exception e) {
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                placeOrder.setEnabled(true);
                                progressDialog.dismiss();

                            }
                        });
                    }


                }
            });

            thread.start();
        } else {
            progressDialog.dismiss();
            Snackbar.make(findViewById(android.R.id.content), getResources().getString(R.string.no_internet_connection_snackbar), Snackbar.LENGTH_LONG).show();

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (audioStatus == AUDIO_STATUS_STOP) {
            playPreviewAudio.performClick();

        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (audioStatus == AUDIO_STATUS_STOP) {
            playPreviewAudio.performClick();

        }
        try{
            tinyDB.deleteImage(getIntent().getStringExtra("imagePath"));
        }catch (Exception e)
        {
        }
    }

    private Bitmap rotateBitmap(Bitmap bitmap, int rotationAngleDegree) {

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int newW = w, newH = h;
        if (rotationAngleDegree == 90 || rotationAngleDegree == 270) {
            newW = h;
            newH = w;
        }
        Bitmap rotatedBitmap = Bitmap.createBitmap(newW, newH, bitmap.getConfig());
        Canvas canvas = new Canvas(rotatedBitmap);

        Rect rect = new Rect(0, 0, newW, newH);
        Matrix matrix = new Matrix();
        float px = rect.exactCenterX();
        float py = rect.exactCenterY();
        matrix.postTranslate(-bitmap.getWidth() / 2, -bitmap.getHeight() / 2);
        matrix.postRotate(rotationAngleDegree);
        matrix.postTranslate(px, py);
        canvas.drawBitmap(bitmap, matrix, new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG | Paint.FILTER_BITMAP_FLAG));
        matrix.reset();

        return rotatedBitmap;
    }


    private void addWallet() {

        final String urlAdress = getResources().getString(R.string.localhost) + "/api/" + new TinyDB(getApplicationContext()).getString(getResources().getString(R.string.authToken)) + "/newWallet/ret/" + FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
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


    }

    private void updateWalletData(final boolean openAmount) {


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
                        if (openAmount) {
                            startActivity(new Intent(PlaceOrderFinalActivity.this, AddAmountActivity.class));
                            overridePendingTransition(R.anim.enter_right, android.R.anim.fade_out);
                        }


                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                    tinyDB.remove("wallet");
                }
            }
        });
        getData.execute(getResources().getString(R.string.localhost) + "/api/" + new TinyDB(getApplicationContext()).getString(getResources().getString(R.string.authToken)) + "/wallet/" + FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());
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
    public void onBackPressed() {
        if (previewNotPlayed) {
            logFirebaseEvent("preview_back_tapped", "when", "without_playing_preview");
        } else {

            logFirebaseEvent("preview_back_tapped", "when", "after_playing_preview");
        }
        super.onBackPressed();
        overridePendingTransition(R.anim.exit, android.R.anim.fade_out);

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

