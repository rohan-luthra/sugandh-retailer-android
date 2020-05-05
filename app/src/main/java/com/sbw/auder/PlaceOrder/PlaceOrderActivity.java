package com.sbw.auder.PlaceOrder;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.PowerManager;
import android.support.animation.DynamicAnimation;
import android.support.animation.SpringAnimation;
import android.support.animation.SpringForce;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.sbw.auder.Camera2Kit.MainActivityCamera;
import com.sbw.auder.HomeScreen.MainActivity;
import com.sbw.auder.HomeWatcher;
import com.sbw.auder.Models.DistModel;
import com.sbw.auder.Models.OrderTypeModel;
import com.sbw.auder.OrdersActivity.NoOrderActivity;
import com.sbw.auder.OrdersActivity.OrderActivity;
import com.sbw.auder.R;
import com.sbw.auder.Utils.GetData;
import com.sbw.auder.Utils.TinyDB;
import com.wooplr.spotlight.SpotlightView;
import com.wooplr.spotlight.utils.SpotlightListener;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import static android.os.Environment.DIRECTORY_MUSIC;
import static android.support.animation.SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY;
import static android.support.animation.SpringForce.STIFFNESS_LOW;
import static android.support.animation.SpringForce.STIFFNESS_MEDIUM;


public class PlaceOrderActivity extends AppCompatActivity {
    int previousOrderTypeSelected = 0, currentOrderTypeSelected = 0;
    int totalRecordingLength = 0;
    int recordingTimerCounter = 0;
    int audioStatus = 0;
    int AUDIO_STATUS_READY_TO_RECORD = 0;
    int AUDIO_STATUS_RECORDING = 1;
    int AUDIO_STATUS_RECORDING_COMPLETED = 2;
    int AUDIO_STATUS_PLAYING = 3;

    long maxRecordingSeconds, currentSeconds;

    String
            distId,
            orderTypeId,
            orderTypeAmount;
    String audioPath = "";

    String audioData;


    private static final String TAG = "PlaceOrderActivity";

    ImageView
            audioBtn,
            distBGimage,
            infoButton;
    Button
            myOrdersButton,
            reRecordButton,
            nextButton;

    MediaPlayer mediaPlayer;
    MediaRecorder mediaRecorder;

    OrderTypeModel currentOrderTypeModel;
    Timer timerRecording;

    TextView orderTypeButton_5,
            orderTypeButton_4,
            orderTypeButton_3,
            orderTypeButton_2,
            orderTypeButton_1,
            SELECT_TYPE;

    boolean areOrderTypeButtonsVisible = false;
    boolean isOrderPlaying = false;
    boolean isReRecordTapped = false;
    boolean dontDestroyAudioData = false;
    //FIREBASE STATES.
    private boolean stoppedAfterPlaying = false;
    private boolean manuallyStoppedPlaying = true;
    private boolean isBottomBarTapped = false;

    CountDownTimer timerPlaying;
    DistModel distModel = new DistModel();

    TinyDB tinyDB;
    TextView timerDisplay;


    final int REQUEST_PERMISSION_AUDIO = 99;
    private ArrayList<OrderTypeModel> orderTypeModels;
    FirebaseAnalytics mFirebaseAnalytics;
    private HomeWatcher mHomeWatcher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (new TinyDB(this).getString("Lang")!=null)
        {
            setLanguageForApp(new TinyDB(this).getString("Lang"), this);
        }
        setContentView(R.layout.activity_place_order);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.tb_toolbarsearch2);
        setSupportActionBar(toolbar);
        tinyDB = new TinyDB(this);

        infoButton = findViewById(R.id.info_button);
        myOrdersButton = findViewById(R.id.reciepts_button);
        SELECT_TYPE = findViewById(R.id.textmsg);
        TextView title = findViewById(R.id.titleofplaceorder);
        TextView location = findViewById(R.id.location);
        orderTypeButton_5 = findViewById(R.id.fBtn1);
        orderTypeButton_4 = findViewById(R.id.fBtn2);
        orderTypeButton_3 = findViewById(R.id.fBtn3);
        orderTypeButton_2 = findViewById(R.id.fBtn4);
        orderTypeButton_1 = findViewById(R.id.fBtn5);
        distBGimage = findViewById(R.id.distributorBackgroundImage);
        audioBtn = findViewById(R.id.audioBtn);
        reRecordButton = findViewById(R.id.retry);
        nextButton = findViewById(R.id.next);
        timerDisplay = findViewById(R.id.seconds);


        setOnTouchListnerToView(nextButton, R.drawable.ic_right_arrow, R.drawable.ic_right_arrow_pressed, 1);
        setOnTouchListnerToView(reRecordButton, R.drawable.ic_retry, R.drawable.ic_retry_pressed, 0);
        setOnTouchListnerToView(myOrdersButton, R.drawable.ic_my_orders_icon, R.drawable.ic_my_orders_icon_pressed, 0);
        //for firebase log
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        stoppedAfterPlaying = false;
        manuallyStoppedPlaying = true;
        currentOrderTypeSelected = 0;
        previousOrderTypeSelected = 0;
        isReRecordTapped = false;


        distModel = getIntent().getParcelableExtra("dist");
        distId = distModel.getId();
        title.setText(distModel.getName());
        location.setText(distModel.getLocation());
        Log.d(TAG, "onCreate: " + distModel);


        setUpBg();
        setOrderTypeData();

        SELECT_TYPE.setVisibility(View.VISIBLE);
        myOrdersButton.setVisibility(View.VISIBLE);

        firstShowOrderTypeButtons();


        // add back arrow to toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                backTappedFirebaseLog();
                if (audioStatus == AUDIO_STATUS_RECORDING_COMPLETED || audioStatus == AUDIO_STATUS_PLAYING)
                {
                    if (audioStatus == AUDIO_STATUS_PLAYING)
                    {
                        audioBtn.performClick();
                    }
                    AlertDialog.Builder backPressed = new AlertDialog.Builder(PlaceOrderActivity.this);
                    backPressed.setMessage(getResources().getString(R.string.alertMessageAfterRecordingBackPressed));
                    backPressed.setTitle("");
                    backPressed.setPositiveButton(getResources().getString(R.string.alertDialogGoBack), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            performFinishActivityOnTappingNavigationBack();
                        }
                    });
                    backPressed.setNegativeButton(getResources().getString(R.string.alertDialogCancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    backPressed.show();

                }else{
                    performFinishActivityOnTappingNavigationBack();

                }



            }
        });


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            startActivity(new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
        }


        if (!checkAudioPermissions())
            requestAudioPermission();
        else {

            infoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    infoTappedFirebaseLog();
                    dontDestroyAudioData = true;
                    startActivity(new Intent(PlaceOrderActivity.this, NoOrderActivity.class).putExtra("dist", distModel));
                    overridePendingTransition(R.anim.enter_right, android.R.anim.fade_out);

                }
            });
            myOrdersButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    logFirebaseEvent("chat_opened", "dist_name", distModel.getName());
                    if (currentOrderTypeSelected != 0) {
                        logFirebaseEvent("chat_opened", "when", "after_selecting_type");
                    } else if (currentOrderTypeSelected == 0 && isBottomBarTapped) {
                        logFirebaseEvent("chat_opened", "when", "after_tapping_on_bottom_bar");
                    } else if (audioStatus == AUDIO_STATUS_READY_TO_RECORD && !isReRecordTapped) {
                        logFirebaseEvent("chat_opened", "when", "after_arriving_on_screen");
                    }
                    reRecordTappedFirebaseLog();

                    dontDestroyAudioData = true;
                    startActivity(new Intent(PlaceOrderActivity.this, OrderActivity.class).putExtra("dist", distModel));
                    overridePendingTransition(R.anim.enter_right, android.R.anim.fade_out);


                }
            });


            SELECT_TYPE.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    logFirebaseEvent("isBottomBarTappedped", "", "");
                    isBottomBarTapped = true;
                    bounceOrderTypes();
                }
            });

            hideBottomBarButtons();

            mHomeWatcher = new HomeWatcher(this);

            //THIS KEEPS EYE ON WHEN THE HOME BUTTON IS BEING PRESSED.
            mHomeWatcher.setOnHomePressedListener(new HomeWatcher.OnHomePressedListener() {
                @Override
                public void onHomePressed() {
                    // do something here...
                    dontDestroyAudioData = false;

                    homePressedFirebaseLog();


                }

                @Override
                public void onHomeLongPressed() {
                    dontDestroyAudioData = false;
                }
            });
            mHomeWatcher.startWatch();



            //THIS CODE WILL EXECUTE WHEN THE USER WILL TAP ON NEXT BUTTON WHICH WILL INTENT IT TO CAMERA AND WILL CARRY THE PATH OF RECORDED MEDIA
            nextButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    nextTappedFirebaseLog();

                    if (audioData != null) {
                        if (audioData.length() > 0) {
                            handleNextTappedWhilePlaying();

                            startActivity(new Intent(PlaceOrderActivity.this, MainActivityCamera.class)
                                    .putExtra("audioData", audioPath)
                                    .putExtra("distId", distId)
                                    .putExtra("dist", distModel)
                                    .putExtra("orderType_id", orderTypeId)
                                    .putExtra("orderType_fees", orderTypeAmount)
                                    .putExtra("OrderAudioLength", totalRecordingLength).putExtra("openfront", 1));
                            overridePendingTransition(R.anim.enter_right, android.R.anim.fade_out);

                        } else {
                            //  Toast.makeText(PlaceOrderActivity.this, "Record Your Order First!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        //Toast.makeText(PlaceOrderActivity.this, "Record Your Order First!", Toast.LENGTH_SHORT).show();
                    }


                }
            });


            if (checkAudioPermissions()) {

                reRecordButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        animateButtonsOnReRecord();
                        isReRecordTapped = true;
                        if (stoppedAfterPlaying) {
                            logFirebaseEvent("re_record_tapped", "when", "after_playing");
                        } else if (audioStatus == AUDIO_STATUS_RECORDING_COMPLETED) {
                            logFirebaseEvent("re_record_tapped", "when", "without_playing");
                        }
                        myOrdersButton.setVisibility(View.VISIBLE);

                        audioStatus = AUDIO_STATUS_READY_TO_RECORD;
                        audioData = null;
                        if (timerPlaying != null) {
                            timerPlaying.onFinish();
                            timerPlaying.cancel();
                            timerPlaying = null;

                        }
                        changeButtonsImageOnReRecord();

                        currentSeconds = maxRecordingSeconds;

                        initialiseTimerDisplayText(maxRecordingSeconds);

                        if (mediaPlayer != null) {
                            try {
                                if (mediaPlayer.isPlaying()) mediaPlayer.stop();
                            } catch (Exception e) {
                                Log.d(TAG, "onClick: " + e.getMessage());
                            }
                            mediaPlayer.release();
                        }

                    }
                });

                nextButton.setEnabled(true);
                reRecordButton.setEnabled(true);


                audioBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {


                        if (maxRecordingSeconds != 0) {

                            if (audioStatus == AUDIO_STATUS_READY_TO_RECORD) {
                                isReRecordTapped = false;
                                totalRecordingLength = 0;
                                myOrdersButton.setVisibility(View.GONE);


                                new CountDownTimer(1000, 1000) {
                                    @Override
                                    public void onTick(long millisUntilFinished) {

                                    }

                                    @Override
                                    public void onFinish() {

                                        setSpotLight(getResources().getString(R.string.spotLightTextStopRecordingMidway),
                                                getResources().getString(R.string.spotLightTextStopRecordingMidwayMessage),
                                                audioBtn,
                                                "stop recording");

                                    }
                                }.start();

                                hideBottomBarButtons();

                                currentSeconds = maxRecordingSeconds + 1;

                                Bundle param = new Bundle();
                                param.putInt("type", Integer.parseInt(orderTypeModels.get(5 - currentOrderTypeSelected).getFees()));
                                mFirebaseAnalytics.logEvent("record_tapped", param);

                                try {
//                                    File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
//                                            + "/" + getApplicationContext().getResources().getString(R.string.app_name) + "/Audio");


                                    File dcimDir = new File(getApplicationContext().getExternalFilesDir(null),DIRECTORY_MUSIC);
                                    File picsDir = new File(dcimDir, "Nandi");

                                    boolean success = true;
                                    if (!picsDir.exists()) {
                                        success = picsDir.mkdirs();
                                    }
                                    if (success) {
                                        //audioPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() + "/" + getApplicationContext().getResources().getString(R.string.app_name) + "/Audio/" + UUID.randomUUID().toString() + ".3gp";
                                        audioPath = new File(picsDir, UUID.randomUUID().toString() + ".3gp").getAbsolutePath();
                                        Log.d(TAG, "onClick: audiopath" + audioPath);

                                    }

                                } catch (Exception e) {
                                    Log.d(TAG, "onClick: audiopath" + e.getMessage());
                                }

                                mediaRecorder = new MediaRecorder();
                                setUpMediaRecorder();
                                timerRecording = new Timer();
                                timerRecording.scheduleAtFixedRate(new TimerTask() {
                                    @SuppressLint("SetTextI18n")
                                    @Override
                                    public void run() {

                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {

                                                setTimerDisplayTextWhenRecording();
                                            }
                                        });


                                        if (currentSeconds == -1) {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {

                                                    if (mediaRecorder != null) {
                                                        audioStatus = AUDIO_STATUS_RECORDING;
                                                        audioBtn.performClick();


                                                    }

                                                }
                                            });
                                        }
                                    }
                                }, maxRecordingSeconds, 1000);

                                audioBtn.setImageResource(R.drawable.stop_button_bg);

                                try {
                                    audioStatus = AUDIO_STATUS_RECORDING;
                                    mediaRecorder.prepare();
                                    Log.d(TAG, "onClick: try");
                                    mediaRecorder.start();

                                    audioBtn.setImageResource(R.drawable.stop_button_bg);


                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Log.d(TAG, "onClick: catch" + e.getMessage());
                                }

                                nextButton.setEnabled(false);
                                findViewById(R.id.forFadeInOut).setVisibility(View.VISIBLE);
                                findViewById(R.id.forFadeInOut).startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fadeinout));

                            } else if (audioStatus == AUDIO_STATUS_RECORDING) {
                                findViewById(R.id.forFadeInOut).clearAnimation();
                                findViewById(R.id.forFadeInOut).setVisibility(View.GONE);

                                if (maxRecordingSeconds - currentSeconds > 0) {

                                    new SpotlightView.Builder(PlaceOrderActivity.this)
                                            .introAnimationDuration(500)
                                            .enableRevealAnimation(true)
                                            .performClick(true)
                                            .fadeinTextDuration(300)
                                            .headingTvColor(Color.parseColor("#eb273f"))
                                            .headingTvSize(18)
                                            .headingTvText(getResources().getString(R.string.spotLightTextPreviewPrayer))
                                            .target(findViewById(R.id.forTourGuide))
                                            .subHeadingTvColor(Color.parseColor("#ffffff"))
                                            .subHeadingTvSize(14)
                                            .subHeadingTvText(getResources().getString(R.string.spotLightTextPreviewPrayerMessage))
                                            .maskColor(Color.parseColor("#99000000"))
                                            .lineAnimDuration(400)
                                            .lineAndArcColor(Color.parseColor("#eb273f"))
                                            .dismissOnTouch(true)
                                            .dismissOnBackPress(true)
                                            .enableDismissAfterShown(false)
                                            .usageId("play recording")
                                            .setListener(new SpotlightListener() {
                                                @Override
                                                public void onUserClicked(String s) {
                                                    CountDownTimer ct = new CountDownTimer(500, 500) {
                                                        @Override
                                                        public void onTick(long millisUntilFinished) {

                                                        }

                                                        @Override
                                                        public void onFinish() {

                                                            setSpotLight(getResources().getString(R.string.spotLightTextMoveAhead),
                                                                    getResources().getString(R.string.spotLightTextMoveAheadMessage),
                                                                    nextButton,
                                                                    "nextButton recording");

                                                        }
                                                    };
                                                    ct.start();


                                                }
                                            })
                                            .show();


                                    if (timerRecording != null) {
                                        timerRecording.cancel();
                                        timerRecording = null;
                                    }

                                    nextButton.setEnabled(true);


                                    try {
                                        mediaRecorder.stop();
                                        mediaRecorder.release();
                                        mediaRecorder = null;
                                    } catch (IllegalStateException e) {
                                    }

                                    DisplayMetrics displayMetrics = new DisplayMetrics();
                                    getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

                                    Log.d(TAG, "onClick: height" + displayMetrics.heightPixels);

                                    float audioBtnY = audioBtn.getY();
                                    float destinationY = findViewById(R.id.bottombar_1).getY();
                                    float translationY = Math.abs((audioBtnY - destinationY) / 1.75f);
                                    timerDisplay.animate().translationY(translationY).setDuration(150).start();
                                    audioBtn.animate().scaleX(.4f).scaleY(.4f).translationY(translationY).setDuration(150).start();

                                    showBottomBarButtons();

                                    nextButton.setEnabled(true);

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            initialiseTimerDisplayText((totalRecordingLength - 1));

                                        }
                                    });

                                    audioStatus = AUDIO_STATUS_RECORDING_COMPLETED;

                                    logForRecordingStopped();


                                    Toast.makeText(PlaceOrderActivity.this, "" + getResources().getString(R.string.toast_place_order_screen_recording_complete), Toast.LENGTH_SHORT).show();

                                    audioBtn.setImageResource(R.drawable.play_button_bg);
                                    timerDisplay.setBackground(getResources().getDrawable(R.drawable.timerbarorange));


                                    byte bytes[] = new byte[0];
                                    try {
                                        File file = new File(audioPath);
                                        bytes = FileUtils.readFileToByteArray(file);
                                        audioData = Base64.encodeToString(bytes, Base64.DEFAULT);
                                        Log.d(TAG, "onClick: " + audioData.length());


                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    Toast.makeText(PlaceOrderActivity.this, "Recording cannot be less than one second !", Toast.LENGTH_SHORT).show();
                                }

                            } else if (audioStatus == AUDIO_STATUS_RECORDING_COMPLETED) {


                                mediaPlayer = new MediaPlayer();

                                try {
                                    mediaPlayer.setDataSource(audioPath);
                                    mediaPlayer.prepare();
                                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                        @Override
                                        public void onCompletion(MediaPlayer mp) {
                                            manuallyStoppedPlaying = false;
                                            audioBtn.performClick();

                                        }
                                    });
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                isOrderPlaying = true;
                                recordingTimerCounter = 0;

                                timerPlaying = new CountDownTimer(totalRecordingLength * 1000, 1000) {
                                    public void onTick(long millisUntilFinished) {
                                        initialiseTimerDisplayText(recordingTimerCounter);
                                        recordingTimerCounter++;

                                    }

                                    public void onFinish() {
                                        initialiseTimerDisplayText((totalRecordingLength - 1));

                                    }
                                };
                                timerPlaying.start();

                                mediaPlayer.start();
                                audioStatus = AUDIO_STATUS_PLAYING;

                                if (audioStatus == AUDIO_STATUS_PLAYING && stoppedAfterPlaying) {
                                    logFirebaseEvent("recording_played", "when", "after_stopping_the_playing");
                                } else {
                                    logFirebaseEvent("recording_played", "when", "after_recording");
                                }

                                setBottomBarButtons();


                                audioBtn.setImageResource(R.drawable.stop_button_bg);


                            } else if (audioStatus == AUDIO_STATUS_PLAYING) {
                                if (mediaPlayer != null) {
                                    mediaPlayer.stop();
                                    mediaPlayer.release();
                                    mediaRecorder = new MediaRecorder();
                                    setUpMediaRecorder();
                                    audioStatus = AUDIO_STATUS_RECORDING_COMPLETED;
                                    if (isOrderPlaying) {
                                        if (timerPlaying != null) {
                                            timerPlaying.onFinish();
                                            timerPlaying.cancel();
                                            timerPlaying = null;
                                        }
                                        isOrderPlaying = false;
                                    }

                                    stoppedAfterPlaying = true;
                                    if (manuallyStoppedPlaying) {
                                        manuallyStoppedPlaying = true;
                                        logFirebaseEvent("place_order_stop_playing_tapped", "", "");
                                    }

                                    setBottomBarButtons();
                                    audioBtn.setImageResource(R.drawable.play_button_bg);


                                }

                            }
                            hideOrderTypeButton();

                        }
                    }
                });


            } else {

                requestAudioPermission();
            }
        }
        setOrderTypeData();


    }

    private void performFinishActivityOnTappingNavigationBack() {
        if (getIntent().getStringExtra("fromActivity") != null) {
            if (getIntent().getStringExtra("fromActivity").equalsIgnoreCase("notification")) {
                startActivity(new Intent(PlaceOrderActivity.this, MainActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                overridePendingTransition(R.anim.exit, android.R.anim.fade_out);

            } else if (getIntent().getStringExtra("fromActivity").equalsIgnoreCase("order")) {
                startActivity(new Intent(PlaceOrderActivity.this, MainActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                overridePendingTransition(R.anim.exit, android.R.anim.fade_out);
            } else {
                finish();
                overridePendingTransition(R.anim.exit, android.R.anim.fade_out);

            }

        } else {
            finish();
            overridePendingTransition(R.anim.exit, android.R.anim.fade_out);

        }


    }

    private void showBottomBarButtons() {
        reRecordButton.setVisibility(View.VISIBLE);
        nextButton.setVisibility(View.VISIBLE);
    }

    private void firstShowOrderTypeButtons() {
        if (!areOrderTypeButtonsVisible) {
            new CountDownTimer(400, 400) {
                public void onTick(long millisUntilFinished) {
                }

                public void onFinish() {
                    animateOrderTypeButtons();
                }
            }.start();
        }
    }

    private void setBottomBarButtons() {
        nextButton.setEnabled(true);
        reRecordButton.setEnabled(true);
    }

    private void logForRecordingStopped() {
        if (totalRecordingLength - 1 != maxRecordingSeconds) {
            Bundle param = new Bundle();
            param.putInt("type", Integer.parseInt(orderTypeModels.get(5 - currentOrderTypeSelected).getFees()));
            param.putString("length", "" + (totalRecordingLength - 1));
            mFirebaseAnalytics.logEvent("recording_explicitly_stopped", param);
        }
        if (totalRecordingLength - 1 == maxRecordingSeconds) {

            logFirebaseEvent2("recording_utilized_full", "", "", "type", Integer.parseInt(orderTypeModels.get(5 - currentOrderTypeSelected).getFees()));
        }
    }

    private void hideBottomBarButtons() {
        reRecordButton.setVisibility(View.GONE);
        nextButton.setVisibility(View.GONE);

    }

    private void setTimerDisplayTextWhenRecording() {
        if (currentSeconds > 0) {
            if ((currentSeconds - 1) / 60 != 0) {
                Log.d("log1", "log1");
                if ((currentSeconds - 1) / 60 > 9) {
                    if (((currentSeconds - 1) % 60) / 10 > 0) {
                        timerDisplay.setText((currentSeconds - 1) / 60 + ":" + ((currentSeconds - 1) % 60));
                    } else {
                        timerDisplay.setText((currentSeconds - 1) / 60 + ":0" + ((currentSeconds - 1) % 60));
                    }
                    currentSeconds--;
                    totalRecordingLength++;

                } else {
                    if (((currentSeconds - 1) % 60) / 10 > 0) {
                        timerDisplay.setText("0" + (currentSeconds - 1) / 60 + ":" + ((currentSeconds - 1) % 60));
                    } else {
                        timerDisplay.setText("0" + (currentSeconds - 1) / 60 + ":0" + ((currentSeconds - 1) % 60));
                    }
                    currentSeconds--;
                    totalRecordingLength++;
                }


            } else {
                if ((currentSeconds - 1) / 10 > 0) {
                    timerDisplay.setText("00:" + (currentSeconds - 1));
                } else {
                    timerDisplay.setText("00:0" + (currentSeconds - 1));
                }
                currentSeconds--;
                totalRecordingLength++;
            }
        } else {

            currentSeconds--;
            timerDisplay.setText("-");

        }

    }

    private void changeButtonsImageOnReRecord() {
        hideBottomBarButtons();
        timerDisplay.setBackground(getResources().getDrawable(R.drawable.timer_bar));
        audioBtn.setImageResource(R.drawable.record_button_bg);
    }

    private void animateButtonsOnReRecord() {
        animateOrderTypeButtons();
        timerDisplay.animate().translationY(0f).setDuration(150).start();
        audioBtn.animate().scaleX(1f).scaleY(1f).translationY(0f).setDuration(150).start();
    }

    private void handleNextTappedWhilePlaying() {
        CountDownTimer nextintent = new CountDownTimer(1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (timerPlaying != null) {
                    timerPlaying.onFinish();
                    timerPlaying.cancel();
                    timerPlaying = null;
                }
                if (mediaPlayer != null) {
                    try {
                        if (mediaPlayer.isPlaying()) {
                            mediaPlayer.stop();
                            audioBtn.setImageResource(R.drawable.play_button_bg);
                            audioStatus = AUDIO_STATUS_RECORDING_COMPLETED;
                        }
                    } catch (IllegalStateException e) {

                    }


                }
            }

            @Override
            public void onFinish() {

            }
        }.start();


    }

    private void nextTappedFirebaseLog() {

        Bundle params = new Bundle();
        params.putInt("type", Integer.parseInt(orderTypeModels.get(5 - currentOrderTypeSelected).getFees()));
        params.putString("dist_name", distModel.getName());
        mFirebaseAnalytics.logEvent("place_order_nextButton_tapped", params);
    }

   
    private void homePressedFirebaseLog() {
        logFirebaseEvent("home_button_tapped", "where", "place_order_screen");
        logFirebaseEvent("home_button_tapped", "dist_name", distModel.getName());

        if (isReRecordTapped) {
            logFirebaseEvent2("place_order_home_tapped", "when", "after_tapping_re_record", "type", Integer.parseInt(orderTypeModels.get(5 - currentOrderTypeSelected).getFees()));
        }

        if (audioStatus == AUDIO_STATUS_RECORDING_COMPLETED && stoppedAfterPlaying) {
            logFirebaseEvent2("place_order_home_tapped", "when", "after_playing", "type", Integer.parseInt(orderTypeModels.get(5 - currentOrderTypeSelected).getFees()));
        } else if (audioStatus == AUDIO_STATUS_RECORDING) {
            logFirebaseEvent2("place_order_home_tapped", "when", "while_recording", "type", Integer.parseInt(orderTypeModels.get(5 - currentOrderTypeSelected).getFees()));
        } else if (audioStatus == AUDIO_STATUS_PLAYING) {
            logFirebaseEvent2("place_order_home_tapped", "when", "while_playing", "type", Integer.parseInt(orderTypeModels.get(5 - currentOrderTypeSelected).getFees()));
        } else if (audioStatus == AUDIO_STATUS_RECORDING_COMPLETED) {
            logFirebaseEvent2("place_order_home_tapped", "when", "recording_was_complete", "type", Integer.parseInt(orderTypeModels.get(5 - currentOrderTypeSelected).getFees()));
        } else if (audioStatus == AUDIO_STATUS_READY_TO_RECORD && areOrderTypeButtonsVisible && currentOrderTypeSelected != 0) {
            logFirebaseEvent2("place_order_home_tapped", "when", "type_selected", "type", Integer.parseInt(orderTypeModels.get(5 - currentOrderTypeSelected).getFees()));
        } else if (currentOrderTypeSelected == 0) {
            logFirebaseEvent("place_order_home_tapped", "when", "type_not_selected");
        }


    }

    private void reRecordTappedFirebaseLog() {
        if (isReRecordTapped) {
            logFirebaseEvent("chat_opened", "when", "after_tapping_on_rerecord");
        }
    }

    private void infoTappedFirebaseLog() {
        logFirebaseEvent("info_tapped", "dist_name", distModel.getName());

    }

    private void backTappedFirebaseLog() {
        if (isReRecordTapped) {
            logFirebaseEvent2("place_order_back_tapped", "when", "after_tapping_re_record", "type", Integer.parseInt(orderTypeModels.get(5 - currentOrderTypeSelected).getFees()));
        }

        if (audioStatus == AUDIO_STATUS_RECORDING_COMPLETED && stoppedAfterPlaying) {
            logFirebaseEvent2("place_order_back_tapped", "when", "after_playing", "type", Integer.parseInt(orderTypeModels.get(5 - currentOrderTypeSelected).getFees()));
        } else if (audioStatus == AUDIO_STATUS_RECORDING) {
            logFirebaseEvent2("place_order_back_tapped", "when", "while_recording", "type", Integer.parseInt(orderTypeModels.get(5 - currentOrderTypeSelected).getFees()));
        } else if (audioStatus == AUDIO_STATUS_PLAYING) {
            logFirebaseEvent2("place_order_back_tapped", "when", "while_playing", "type", Integer.parseInt(orderTypeModels.get(5 - currentOrderTypeSelected).getFees()));
        } else if (audioStatus == AUDIO_STATUS_RECORDING_COMPLETED) {
            logFirebaseEvent2("place_order_back_tapped", "when", "recording_was_complete", "type", Integer.parseInt(orderTypeModels.get(5 - currentOrderTypeSelected).getFees()));
        } else if (audioStatus == AUDIO_STATUS_READY_TO_RECORD && areOrderTypeButtonsVisible && currentOrderTypeSelected != 0) {
            logFirebaseEvent2("place_order_back_tapped", "when", "type_selected", "type", Integer.parseInt(orderTypeModels.get(5 - currentOrderTypeSelected).getFees()));
        } else if (currentOrderTypeSelected == 0) {
            logFirebaseEvent("place_order_back_tapped", "when", "type_not_selected");
        }

    }

    private void setSpotLight(String headingText, String subHeadingText, View target, String idString) {

        new SpotlightView.Builder(PlaceOrderActivity.this)
                .introAnimationDuration(500)
                .enableRevealAnimation(true)
                .performClick(true)
                .fadeinTextDuration(300)
                .headingTvColor(Color.parseColor("#eb273f"))
                .headingTvSize(18)
                .headingTvText(headingText)
                .target(target)
                .subHeadingTvColor(Color.parseColor("#ffffff"))
                .subHeadingTvSize(14)
                .subHeadingTvText(subHeadingText)
                .maskColor(Color.parseColor("#99000000"))
                .lineAnimDuration(400)
                .lineAndArcColor(Color.parseColor("#eb273f"))
                .dismissOnTouch(true)
                .dismissOnBackPress(true)
                .enableDismissAfterShown(false)
                .usageId(idString)
                .show();


    }

    private void initialiseTimerDisplayText(long timeStamp) {
        if (timeStamp / 60 != 0) {
            if (timeStamp / 60 > 9) {
                if ((timeStamp % 60) / 10 > 0) {
                    timerDisplay.setText(timeStamp / 60 + ":" + (timeStamp % 60));
                } else {
                    timerDisplay.setText(timeStamp / 60 + ":0" + (timeStamp % 60));
                }
            } else {
                if ((timeStamp % 60) / 10 > 0) {
                    timerDisplay.setText("0" + timeStamp / 60 + ":" + (timeStamp % 60));
                } else {
                    timerDisplay.setText("0" + timeStamp / 60 + ":0" + (timeStamp % 60));
                }
            }
        } else {
            if (timeStamp / 10 > 0) {
                timerDisplay.setText("00:" + timeStamp);
            } else {
                timerDisplay.setText("00:0" + timeStamp);
            }
        }

    }


    //THIS SET AND UPDATE THE DATA OF ORDERTYPES IN THE SCREEN

    private void setOrderTypeData() {


        try {

            if (tinyDB.getOrderTypeModelListObject("orderTypeList", OrderTypeModel.class) != null) {
                orderTypeModels = new ArrayList<>();
                orderTypeModels = tinyDB.getOrderTypeModelListObject("orderTypeList", OrderTypeModel.class);
                Log.d(TAG, "setOrderTypeData: " + orderTypeModels.size());


                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String[] s = new String[5];
                        String[] s1 = new String[5];
                        String[] s2 = new String[5];
                        final SpannableStringBuilder sb[] = new SpannableStringBuilder[5];
                        for (int i = 0; i < 5; i++) {
                            s[i] = orderTypeModels.get(i).getName();

                            if (s[i].contains(" ")) {
                                int j = s[i].indexOf(" ");
                                s1[i] = s[i].substring(0, j);
                                s2[i] = s[i].substring(j + 1);
                                s[i] = s1[i] + "\n" + s2[i];
                                StringBuilder infoBuilder = new StringBuilder(s[i]);
                                SpannableStringBuilder sb2 = new SpannableStringBuilder(infoBuilder);
                                sb2.setSpan(new RelativeSizeSpan(1.5f), j + 1, infoBuilder.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                                sb[i] = sb2;
                            } else {
                                StringBuilder infoBuilder = new StringBuilder(s[i]);
                                SpannableStringBuilder sb2 = new SpannableStringBuilder(infoBuilder);
                                sb2.setSpan(new RelativeSizeSpan(1f), 0, infoBuilder.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                                sb[i] = sb2;
                            }
                        }
                        orderTypeButton_1.setText(sb[0]);
                        orderTypeButton_2.setText(sb[1]);
                        orderTypeButton_3.setText(sb[2]);
                        orderTypeButton_4.setText(sb[3]);
                        orderTypeButton_5.setText(sb[4]);
                    }
                });


                if (audioStatus != 1) {

                    orderTypeButton_1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            setSpotLightForPrayerDurationAndTimerDisplay();



//
                            previousOrderTypeSelected = currentOrderTypeSelected;
                            currentOrderTypeSelected = 5;
                            logWhenTypeSelected("type_selected", "type", 6 - currentOrderTypeSelected);

                            if (previousOrderTypeSelected != 0) {
                                logWhenTypeChanged("type_changed", (6 - previousOrderTypeSelected), (6 - currentOrderTypeSelected));
                            }

                            getHighBounceScaleX(orderTypeButton_1, 6f, 1f, DAMPING_RATIO_MEDIUM_BOUNCY, STIFFNESS_MEDIUM).start();
                            getHighBounceScaleY(orderTypeButton_1, 6f, 1f, DAMPING_RATIO_MEDIUM_BOUNCY, STIFFNESS_MEDIUM).start();

                            if (audioBtn.getVisibility() != View.VISIBLE) {
                                getHighBounceScaleX(audioBtn, 6f, 1f, DAMPING_RATIO_MEDIUM_BOUNCY, STIFFNESS_MEDIUM)
                                        .start();
                                getHighBounceScaleY(audioBtn, 6f, 1f, DAMPING_RATIO_MEDIUM_BOUNCY, STIFFNESS_MEDIUM)
                                        .start();
                            } else {
                                getHighBounceScaleX(timerDisplay, 4f, 1f, DAMPING_RATIO_MEDIUM_BOUNCY, STIFFNESS_MEDIUM)
                                        .start();
                                getHighBounceScaleY(timerDisplay, 4f, 1f, DAMPING_RATIO_MEDIUM_BOUNCY, STIFFNESS_MEDIUM)
                                        .start();
                            }

                            audioBtn.setVisibility(View.VISIBLE);
                            //isBounceAllowed = false;
                            SELECT_TYPE.setVisibility(View.GONE);
                            fabcolorchange(orderTypeButton_1, orderTypeButton_3, orderTypeButton_2, orderTypeButton_4, orderTypeButton_5);

                            OrderTypeModel orderTypeModel = orderTypeModels.get(0);
                            maxRecordingSeconds = Integer.parseInt(orderTypeModel.getAudioTimeSec());
                            currentSeconds = maxRecordingSeconds;

                            initialiseTimerDisplayText(maxRecordingSeconds);

                            timerDisplay.setVisibility(View.VISIBLE);


                            Log.d(TAG, "recyclerViewListClicked: " + orderTypeModel.toString());

                            orderTypeAmount = orderTypeModel.getFees();
                            orderTypeId = orderTypeModel.getId();
                            Log.d(TAG, "processFinish: " + orderTypeAmount + " " + orderTypeId);
                        }
                    });
                    orderTypeButton_2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {


                            setSpotLightForPrayerDurationAndTimerDisplay();



                            previousOrderTypeSelected = currentOrderTypeSelected;
                            currentOrderTypeSelected = 4;
                            logWhenTypeSelected("type_selected", "type", 6 - currentOrderTypeSelected);

                            if (previousOrderTypeSelected != 0) {
                                logWhenTypeChanged("type_changed", (6 - previousOrderTypeSelected), (6 - currentOrderTypeSelected));
                            }

                            getHighBounceScaleX(orderTypeButton_2, 6f, 1f, DAMPING_RATIO_MEDIUM_BOUNCY, STIFFNESS_MEDIUM).start();
                            getHighBounceScaleY(orderTypeButton_2, 6f, 1f, DAMPING_RATIO_MEDIUM_BOUNCY, STIFFNESS_MEDIUM).start();

                            if (audioBtn.getVisibility() != View.VISIBLE) {
                                getHighBounceScaleX(audioBtn, 6f, 1f, DAMPING_RATIO_MEDIUM_BOUNCY, STIFFNESS_MEDIUM)
                                        .start();
                                getHighBounceScaleY(audioBtn, 6f, 1f, DAMPING_RATIO_MEDIUM_BOUNCY, STIFFNESS_MEDIUM)
                                        .start();
                            } else {
                                getHighBounceScaleX(timerDisplay, 4f, 1f, DAMPING_RATIO_MEDIUM_BOUNCY, STIFFNESS_MEDIUM)
                                        .start();
                                getHighBounceScaleY(timerDisplay, 4f, 1f, DAMPING_RATIO_MEDIUM_BOUNCY, STIFFNESS_MEDIUM)
                                        .start();
                            }

                            audioBtn.setVisibility(View.VISIBLE);
                            SELECT_TYPE.setVisibility(View.GONE);
                            fabcolorchange(orderTypeButton_2, orderTypeButton_3, orderTypeButton_4, orderTypeButton_1, orderTypeButton_5);

                            OrderTypeModel orderTypeModel = orderTypeModels.get(1);
                            maxRecordingSeconds = Integer.parseInt(orderTypeModel.getAudioTimeSec());
                            currentSeconds = maxRecordingSeconds;
                            initialiseTimerDisplayText(maxRecordingSeconds);
                            timerDisplay.setVisibility(View.VISIBLE);


                            Log.d(TAG, "recyclerViewListClicked: " + orderTypeModel.toString());

                            orderTypeAmount = orderTypeModel.getFees();
                            orderTypeId = orderTypeModel.getId();
                            Log.d(TAG, "processFinish: " + orderTypeAmount + " " + orderTypeId);
                        }
                    });
                    orderTypeButton_3.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            setSpotLightForPrayerDurationAndTimerDisplay();

                            previousOrderTypeSelected = currentOrderTypeSelected;
                            currentOrderTypeSelected = 3;
                            logWhenTypeSelected("type_selected", "type", 6 - currentOrderTypeSelected);

                            if (previousOrderTypeSelected != 0) {
                                logWhenTypeChanged("type_changed", (6 - previousOrderTypeSelected), (6 - currentOrderTypeSelected));
                            }

                            getHighBounceScaleX(orderTypeButton_3, 6f, 1f, DAMPING_RATIO_MEDIUM_BOUNCY, STIFFNESS_MEDIUM).start();
                            getHighBounceScaleY(orderTypeButton_3, 6f, 1f, DAMPING_RATIO_MEDIUM_BOUNCY, STIFFNESS_MEDIUM).start();

                            if (audioBtn.getVisibility() != View.VISIBLE) {
                                getHighBounceScaleX(audioBtn, 6f, 1f, DAMPING_RATIO_MEDIUM_BOUNCY, STIFFNESS_MEDIUM)
                                        .start();
                                getHighBounceScaleY(audioBtn, 6f, 1f, DAMPING_RATIO_MEDIUM_BOUNCY, STIFFNESS_MEDIUM)
                                        .start();
                            } else {
                                getHighBounceScaleX(timerDisplay, 4f, 1f, DAMPING_RATIO_MEDIUM_BOUNCY, STIFFNESS_MEDIUM)
                                        .start();
                                getHighBounceScaleY(timerDisplay, 4f, 1f, DAMPING_RATIO_MEDIUM_BOUNCY, STIFFNESS_MEDIUM)
                                        .start();
                            }

                            audioBtn.setVisibility(View.VISIBLE);
                            SELECT_TYPE.setVisibility(View.GONE);
                            fabcolorchange(orderTypeButton_3, orderTypeButton_4, orderTypeButton_2, orderTypeButton_1, orderTypeButton_5);

                            OrderTypeModel orderTypeModel = orderTypeModels.get(2);
                            maxRecordingSeconds = Integer.parseInt(orderTypeModel.getAudioTimeSec());
                            currentSeconds = maxRecordingSeconds;

                            initialiseTimerDisplayText(maxRecordingSeconds);
                            timerDisplay.setVisibility(View.VISIBLE);


                            Log.d(TAG, "recyclerViewListClicked: " + orderTypeModel.toString());

                            orderTypeAmount = orderTypeModel.getFees();
                            orderTypeId = orderTypeModel.getId();
                            Log.d(TAG, "processFinish: " + orderTypeAmount + " " + orderTypeId);

                        }
                    });
                    orderTypeButton_4.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {


                            setSpotLightForPrayerDurationAndTimerDisplay();



                            previousOrderTypeSelected = currentOrderTypeSelected;
                            currentOrderTypeSelected = 2;
                            logWhenTypeSelected("type_selected", "type", 6 - currentOrderTypeSelected);

                            if (previousOrderTypeSelected != 0) {
                                logWhenTypeChanged("type_changed", (6 - previousOrderTypeSelected), (6 - currentOrderTypeSelected));
                            }

                            getHighBounceScaleX(orderTypeButton_4, 6f, 1f, DAMPING_RATIO_MEDIUM_BOUNCY, STIFFNESS_MEDIUM).start();
                            getHighBounceScaleY(orderTypeButton_4, 6f, 1f, DAMPING_RATIO_MEDIUM_BOUNCY, STIFFNESS_MEDIUM).start();

                            if (audioBtn.getVisibility() != View.VISIBLE) {
                                getHighBounceScaleX(audioBtn, 6f, 1f, DAMPING_RATIO_MEDIUM_BOUNCY, STIFFNESS_MEDIUM)
                                        .start();
                                getHighBounceScaleY(audioBtn, 6f, 1f, DAMPING_RATIO_MEDIUM_BOUNCY, STIFFNESS_MEDIUM)
                                        .start();
                            } else {
                                getHighBounceScaleX(timerDisplay, 4f, 1f, DAMPING_RATIO_MEDIUM_BOUNCY, STIFFNESS_MEDIUM)
                                        .start();
                                getHighBounceScaleY(timerDisplay, 4f, 1f, DAMPING_RATIO_MEDIUM_BOUNCY, STIFFNESS_MEDIUM)
                                        .start();
                            }

                            audioBtn.setVisibility(View.VISIBLE);
                            SELECT_TYPE.setVisibility(View.GONE);
                            fabcolorchange(orderTypeButton_4, orderTypeButton_3, orderTypeButton_2, orderTypeButton_1, orderTypeButton_5);

                            OrderTypeModel orderTypeModel = orderTypeModels.get(3);
                            maxRecordingSeconds = Integer.parseInt(orderTypeModel.getAudioTimeSec());
                            currentSeconds = maxRecordingSeconds;

                            initialiseTimerDisplayText(maxRecordingSeconds);

                            timerDisplay.setVisibility(View.VISIBLE);


                            Log.d(TAG, "recyclerViewListClicked: " + orderTypeModel.toString());

                            orderTypeAmount = orderTypeModel.getFees();
                            orderTypeId = orderTypeModel.getId();
                            Log.d(TAG, "processFinish: " + orderTypeAmount + " " + orderTypeId);

                        }
                    });
                    orderTypeButton_5.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            setSpotLightForPrayerDurationAndTimerDisplay();

//
                            previousOrderTypeSelected = currentOrderTypeSelected;
                            currentOrderTypeSelected = 1;
                            logWhenTypeSelected("type_selected", "type", 6 - currentOrderTypeSelected);

                            if (previousOrderTypeSelected != 0) {
                                logWhenTypeChanged("type_changed", (6 - previousOrderTypeSelected), (6 - currentOrderTypeSelected));
                            }

                            getHighBounceScaleX(orderTypeButton_5, 6f, 1f, DAMPING_RATIO_MEDIUM_BOUNCY, STIFFNESS_MEDIUM).start();
                            getHighBounceScaleY(orderTypeButton_5, 6f, 1f, DAMPING_RATIO_MEDIUM_BOUNCY, STIFFNESS_MEDIUM).start();

                            if (audioBtn.getVisibility() != View.VISIBLE) {
                                getHighBounceScaleX(audioBtn, 6f, 1f, DAMPING_RATIO_MEDIUM_BOUNCY, STIFFNESS_MEDIUM)
                                        .start();
                                getHighBounceScaleY(audioBtn, 6f, 1f, DAMPING_RATIO_MEDIUM_BOUNCY, STIFFNESS_MEDIUM)
                                        .start();
                            } else {
                                getHighBounceScaleX(timerDisplay, 4f, 1f, DAMPING_RATIO_MEDIUM_BOUNCY, STIFFNESS_MEDIUM)
                                        .start();
                                getHighBounceScaleY(timerDisplay, 4f, 1f, DAMPING_RATIO_MEDIUM_BOUNCY, STIFFNESS_MEDIUM)
                                        .start();
                            }
                            audioBtn.setVisibility(View.VISIBLE);
                            SELECT_TYPE.setVisibility(View.GONE);

                            fabcolorchange(orderTypeButton_5, orderTypeButton_4, orderTypeButton_3, orderTypeButton_2, orderTypeButton_1);

                            OrderTypeModel orderTypeModel = orderTypeModels.get(4);
                            maxRecordingSeconds = Integer.parseInt(orderTypeModel.getAudioTimeSec());
                            currentSeconds = maxRecordingSeconds;

                            initialiseTimerDisplayText(maxRecordingSeconds);

                            timerDisplay.setVisibility(View.VISIBLE);

                            Log.d(TAG, "recyclerViewListClicked: " + orderTypeModel.toString());
                            orderTypeAmount = orderTypeModel.getFees();
                            orderTypeId = orderTypeModel.getId();
                            Log.d(TAG, "processFinish: " + orderTypeAmount + " " + orderTypeId);

                        }
                    });

                } else {
                }


            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        GetData getData = new GetData(new GetData.AsyncResponse() {
            @SuppressLint("NewApi")
            @Override
            public void processFinish(String output) throws JSONException {

                try {
                    JSONObject jsonObject = new JSONObject(output);
                    JSONArray data = jsonObject.getJSONArray("msg");
                    if (data.length() > 0) {

                        orderTypeModels = new ArrayList<>();


                        for (int i = 0; i < data.length(); i++) {

                            JSONObject object = data.getJSONObject(i);
                            OrderTypeModel orderTypeModel = new OrderTypeModel();
                            orderTypeModel.setName(object.getString("name"));
                            orderTypeModel.setId(object.getString("_id"));
                            orderTypeModel.setFees(object.getString("fees"));
                            orderTypeModel.setAudioTimeSec(object.getString("audio_len_sec"));

                            currentOrderTypeModel = orderTypeModel;

                            orderTypeModels.add(orderTypeModel);


                        }


                        tinyDB.putListOrderTypeModel("orderTypeList", orderTypeModels);

                    }

                } catch (Exception e) {
                    Log.d(TAG, "processFinish: " + e.getMessage());
                }
            }
        });
        getData.execute(getResources().getString(R.string.localhost) + "/api/" + new TinyDB(getApplicationContext()).getString(getResources().getString(R.string.authToken)) + "/orderTypes");
    }

    private void setSpotLightForPrayerDurationAndTimerDisplay() {
        CountDownTimer ct0 = new CountDownTimer(100, 100) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {


                new SpotlightView.Builder(PlaceOrderActivity.this)
                        .introAnimationDuration(500)
                        .enableRevealAnimation(true)
                        .performClick(true)
                        .fadeinTextDuration(300)
                        .headingTvColor(Color.parseColor("#eb273f"))
                        .headingTvSize(18)
                        .headingTvText(getResources().getString(R.string.spotLightTextPrayerDuration))
                        .target(timerDisplay)
                        .subHeadingTvColor(Color.parseColor("#ffffff"))
                        .subHeadingTvSize(14)
                        .subHeadingTvText(getResources().getString(R.string.spotLightTextPrayerDurationMessage))
                        .maskColor(Color.parseColor("#99000000"))
                        .lineAnimDuration(400)
                        .lineAndArcColor(Color.parseColor("#eb273f"))
                        .dismissOnTouch(true)
                        .dismissOnBackPress(true)
                        .enableDismissAfterShown(false)
                        .usageId("seconds limit")
                        .setListener(new SpotlightListener() {
                            @Override
                            public void onUserClicked(String s) {

                                setSpotLight(getResources().getString(R.string.spotLightTextRecordPrayer),
                                        getResources().getString(R.string.spotLightTextRecordPrayerMessage),
                                        audioBtn,
                                        "record order");
                            }
                        })
                        .show();
            }
        };
        ct0.start();
    }

    //BOUNCING THE ORDER TYPE BUTTONS
    private void bounceOrderTypes() {
        getHighBounceScaleX(orderTypeButton_1, 6f, 1f, DAMPING_RATIO_MEDIUM_BOUNCY, STIFFNESS_MEDIUM).start();
        getHighBounceScaleY(orderTypeButton_1, 6f, 1f, DAMPING_RATIO_MEDIUM_BOUNCY, STIFFNESS_MEDIUM).start();
        getHighBounceScaleX(orderTypeButton_5, 6f, 1f, DAMPING_RATIO_MEDIUM_BOUNCY, STIFFNESS_MEDIUM).start();
        getHighBounceScaleY(orderTypeButton_5, 6f, 1f, DAMPING_RATIO_MEDIUM_BOUNCY, STIFFNESS_MEDIUM).start();
        getHighBounceScaleX(orderTypeButton_4, 6f, 1f, DAMPING_RATIO_MEDIUM_BOUNCY, STIFFNESS_MEDIUM).start();
        getHighBounceScaleY(orderTypeButton_4, 6f, 1f, DAMPING_RATIO_MEDIUM_BOUNCY, STIFFNESS_MEDIUM).start();
        getHighBounceScaleX(orderTypeButton_3, 6f, 1f, DAMPING_RATIO_MEDIUM_BOUNCY, STIFFNESS_MEDIUM).start();
        getHighBounceScaleY(orderTypeButton_3, 6f, 1f, DAMPING_RATIO_MEDIUM_BOUNCY, STIFFNESS_MEDIUM).start();
        getHighBounceScaleX(orderTypeButton_2, 6f, 1f, DAMPING_RATIO_MEDIUM_BOUNCY, STIFFNESS_MEDIUM).start();
        getHighBounceScaleY(orderTypeButton_2, 6f, 1f, DAMPING_RATIO_MEDIUM_BOUNCY, STIFFNESS_MEDIUM).start();
    }

    //SETS MEDIA RECORDER
    private void setUpMediaRecorder() {


        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        mediaRecorder.setOutputFile(audioPath);
    }

    private void requestAudioPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_PERMISSION_AUDIO);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Log.d(TAG, "onRequestPermissionsResult: " + requestCode + " " + grantResults[0]);
        switch (requestCode) {
            case REQUEST_PERMISSION_AUDIO: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                    recreate();
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                    final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                    alertDialog.setMessage(getResources().getString(R.string.place_order_permission_denied));
                    alertDialog.setPositiveButton("OKAY", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            requestAudioPermission();
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
                }
            }
        }
    }

    private boolean checkAudioPermissions() {
        int write_storage_result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int read_audio_result = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        return write_storage_result == PackageManager.PERMISSION_GRANTED && read_audio_result == PackageManager.PERMISSION_GRANTED;
    }

    //WILL SET THE BACKGROUND
    private void setUpBg() {
        final ImageView Lotus = findViewById(R.id.lotus);
        Lotus.setVisibility(View.VISIBLE);
        Lotus.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_lotus));


        if (distModel.getBackgroundUrl() != null) {
            if (tinyDB.getImagePath(distModel.getId() + "_background_" + distModel.getBackgroundUrl()) != null) {
                if (tinyDB.isFileNotExist(distModel.getId() + "_background_" + distModel.getBackgroundUrl())) {
                    Lotus.clearAnimation();
                    Lotus.setVisibility(View.GONE);

                    distBGimage.setImageURI(Uri.parse(tinyDB.getImagePath(distModel.getId() + "_background_" + distModel.getBackgroundUrl())));
                } else {
                    Glide.with(getApplicationContext())
                            .asBitmap()
                            .load(getResources().getString(R.string.localhost) + "/api/" + new TinyDB(getApplicationContext()).getString(getResources().getString(R.string.authToken)) + "/download/" + distModel.getBackgroundUrl())
                            .listener(new RequestListener<Bitmap>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                                    ;
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(final Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {


                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Lotus.clearAnimation();
                                            Lotus.setVisibility(View.GONE);
//                                            textLotus.clearAnimation();
//                                            textLotus.setVisibility(View.GONE);


                                            Log.d(TAG, "run: bg downloaded");
                                            distBGimage.setImageBitmap(resource);
                                            final String path = tinyDB.saveImage(resource, distModel.getId() + "_background_" + distModel.getBackgroundUrl());
                                            tinyDB.setImagePath(distModel.getId() + "_background_" + distModel.getBackgroundUrl(), path);
                                        }
                                    });

                                    return false;
                                }
                            }).submit();
                }
            } else {

                Log.d(TAG, "setDistributorBg: " + distModel.getBackgroundUrl());
                Glide.with(getApplicationContext())
                        .asBitmap()
                        .load(getResources().getString(R.string.localhost) + "/api/" + new TinyDB(getApplicationContext()).getString(getResources().getString(R.string.authToken)) + "/download/" + distModel.getBackgroundUrl())
                        .listener(new RequestListener<Bitmap>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(final Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {


                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Lotus.clearAnimation();
                                        Lotus.setVisibility(View.GONE);

                                        Log.d(TAG, "run: bg downloaded");
                                        distBGimage.setImageBitmap(resource);
                                        final String path = tinyDB.saveImage(resource, distModel.getId() + "_background_" + distModel.getBackgroundUrl());
                                        tinyDB.setImagePath(distModel.getId() + "_background_" + distModel.getBackgroundUrl(), path);
                                    }
                                });

                                return false;
                            }
                        }).submit();
            }
        }
    }


    @Override
    protected void onDestroy() {
        areOrderTypeButtonsVisible = false;
        super.onDestroy();

    }

    //THAT SPRING ANIMATION OF SHOWING ORDERTYPES.
    private void animateOrderTypeButtons() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (orderTypeModels != null) {
                    if (!orderTypeModels.isEmpty()) {
                        areOrderTypeButtonsVisible = true;
                        DisplayMetrics displayMetrics = new DisplayMetrics();
                        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                        float width = displayMetrics.widthPixels;
                        orderTypeButton_5.setVisibility(View.VISIBLE);
                        orderTypeButton_4.setVisibility(View.VISIBLE);
                        orderTypeButton_3.setVisibility(View.VISIBLE);
                        orderTypeButton_2.setVisibility(View.VISIBLE);
                        orderTypeButton_1.setVisibility(View.VISIBLE);
                        orderTypeButton_5.animate().scaleX(1f).scaleY(1f);
                        orderTypeButton_4.animate().scaleX(1f).scaleY(1f);
                        orderTypeButton_3.animate().scaleX(1f).scaleY(1f);
                        orderTypeButton_2.animate().scaleX(1f).scaleY(1f);
                        orderTypeButton_1.animate().scaleX(1f).scaleY(1f);

                        float customWidth = (width / 2.0f) - (0.1041f * width);

                        float velocityDp = 0f;

                        getHighBounceAnimX(orderTypeButton_5, velocityDp, customWidth).start();
                        getHighBounceAnimX(orderTypeButton_4, velocityDp, customWidth * 0.70f).start();
                        getHighBounceAnimY(orderTypeButton_4, velocityDp, -customWidth * 0.70f).start();
                        getHighBounceAnimY(orderTypeButton_3, velocityDp, -customWidth).start();
                        getHighBounceAnimX(orderTypeButton_2, velocityDp, -customWidth * 0.70f).start();
                        getHighBounceAnimY(orderTypeButton_2, velocityDp, -customWidth * 0.70f).start();
                        getHighBounceAnimX(orderTypeButton_1, velocityDp, -customWidth).start();


                        CountDownTimer ct0 = new CountDownTimer(500, 500) {
                            @Override
                            public void onTick(long millisUntilFinished) {

                            }

                            @Override
                            public void onFinish() {

                                setSpotLight(getResources().getString(R.string.spotLightTextSelectType),
                                        getResources().getString(R.string.spotLightTextSelectTypeMessage),
                                        orderTypeButton_4,
                                        "select order type");

                            }
                        };
                        ct0.start();


                    }
                }

            }
        });
    }


    private void hideOrderTypeButton() {
        setordertypedisable();
        areOrderTypeButtonsVisible = false;
        orderTypeButton_5.animate().translationY(0).translationX(0);
        orderTypeButton_4.animate().translationY(0).translationX(0);
        orderTypeButton_3.animate().translationY(0).translationX(0);
        orderTypeButton_2.animate().translationY(0).translationX(0);
        orderTypeButton_1.animate().translationY(0).translationX(0);
        orderTypeButton_5.animate().scaleX(0f).scaleY(0f);
        orderTypeButton_4.animate().scaleX(0f).scaleY(0f);
        orderTypeButton_3.animate().scaleX(0f).scaleY(0f);
        orderTypeButton_2.animate().scaleX(0f).scaleY(0f);
        orderTypeButton_1.animate().scaleX(0f).scaleY(0f);

         new CountDownTimer(300, 300) {
            public void onTick(long millisUntilFinished) {

            }

            public void onFinish() {
                reRecordButton.setEnabled(true);
            }
        }.start();


    }

    //CHANGING COLOR OF BUTTON TO PURPLE ON TAPPING
    public void fabcolorchange(TextView t1, TextView t2, TextView t3, TextView t4, TextView t5) {
        isBottomBarTapped = false;
        t1.setBackground(getResources().getDrawable(R.drawable.order_type_button));
        t1.setTextColor(getResources().getColor(R.color.white));
        t2.setBackground(getResources().getDrawable(R.drawable.order_type_button_pressed));
        t2.setTextColor(getResources().getColor(R.color.white));
        t3.setBackground(getResources().getDrawable(R.drawable.order_type_button_pressed));
        t3.setTextColor(getResources().getColor(R.color.white));
        t4.setBackground(getResources().getDrawable(R.drawable.order_type_button_pressed));
        t4.setTextColor(getResources().getColor(R.color.white));
        t5.setBackground(getResources().getDrawable(R.drawable.order_type_button_pressed));
        t5.setTextColor(getResources().getColor(R.color.white));
    }

    @Override
    public void onBackPressed() {
        backTappedFirebaseLog();

        try {
            if (mediaPlayer != null) {
                try {
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.stop();
                    }
                } catch (Exception e) {

                }

            }
            if (mediaRecorder != null) {


                mediaRecorder.release();
                if (timerRecording != null) {
                    timerRecording.cancel();
                    timerRecording = null;
                }
            }

            if (audioStatus == AUDIO_STATUS_RECORDING_COMPLETED || audioStatus == AUDIO_STATUS_PLAYING)
            {
                if (audioStatus == AUDIO_STATUS_PLAYING)
                {
                    audioBtn.performClick();
                }
                AlertDialog.Builder backPressed = new AlertDialog.Builder(PlaceOrderActivity.this);
                backPressed.setMessage(getResources().getString(R.string.alertMessageAfterRecordingBackPressed));
                backPressed.setTitle("");
                backPressed.setPositiveButton(getResources().getString(R.string.alertDialogGoBack), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        performFinishActivityOnTappingBackButton();
                    }
                });
                backPressed.setNegativeButton(getResources().getString(R.string.alertDialogCancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                backPressed.show();

            }else{
                performFinishActivityOnTappingBackButton();

            }



        } catch (Exception e) {
            e.printStackTrace();
            super.onBackPressed();
        }

    }

    private void performFinishActivityOnTappingBackButton() {
        if (getIntent().getStringExtra("fromActivity") != null) {
            if (getIntent().getStringExtra("fromActivity").equalsIgnoreCase("notification")) {
                startActivity(new Intent(PlaceOrderActivity.this, MainActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                overridePendingTransition(R.anim.exit, android.R.anim.fade_out);

            } else if (getIntent().getStringExtra("fromActivity").equalsIgnoreCase("order")) {
                startActivity(new Intent(PlaceOrderActivity.this, MainActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                overridePendingTransition(R.anim.exit, android.R.anim.fade_out);


            } else {
                super.onBackPressed();
                overridePendingTransition(R.anim.exit, android.R.anim.fade_out);

            }

        } else {
            super.onBackPressed();
            overridePendingTransition(R.anim.exit, android.R.anim.fade_out);

        }
    }

    public void setordertypedisable() {
        orderTypeButton_5.setEnabled(false);
        orderTypeButton_4.setEnabled(false);
        orderTypeButton_3.setEnabled(false);
        orderTypeButton_2.setEnabled(false);
        orderTypeButton_1.setEnabled(false);
    }

    public void setordertypeenable() {
        orderTypeButton_5.setEnabled(true);
        orderTypeButton_4.setEnabled(true);
        orderTypeButton_3.setEnabled(true);
        orderTypeButton_2.setEnabled(true);
        orderTypeButton_1.setEnabled(true);
    }

    private SpringForce getSpringForce(float dampingRatio, float stiffness, float finalPosition) {
        SpringForce force = new SpringForce();
        force.setDampingRatio(dampingRatio).setStiffness(stiffness);
        force.setFinalPosition(finalPosition);
        return force;
    }

    private float getVelocity(float velocityDp) {
        //Get Velocity in pixels per second from dp per second
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, velocityDp,
                getResources().getDisplayMetrics());
    }

    private SpringAnimation getHighBounceAnimX(TextView view, float velocityDp, float finalPosition) {
        final SpringAnimation anim = new SpringAnimation(view, DynamicAnimation.TRANSLATION_X);
        anim.setStartVelocity(getVelocity(velocityDp));
        anim.animateToFinalPosition(finalPosition);

        anim.setSpring(getSpringForce(DAMPING_RATIO_MEDIUM_BOUNCY, STIFFNESS_LOW, finalPosition));
        anim.addEndListener(new DynamicAnimation.OnAnimationEndListener() {
            @Override
            public void onAnimationEnd(DynamicAnimation dynamicAnimation, boolean b, float v, float v1) {
                setordertypeenable();
            }
        });
        return anim;
    }

    private SpringAnimation getHighBounceAnimY(TextView view, float velocityDp, float finalPosition) {
        final SpringAnimation anim = new SpringAnimation(view, DynamicAnimation.TRANSLATION_Y);
        anim.setStartVelocity(getVelocity(velocityDp));
        anim.animateToFinalPosition(finalPosition);
        anim.setSpring(getSpringForce(DAMPING_RATIO_MEDIUM_BOUNCY, STIFFNESS_LOW, finalPosition));
        return anim;
    }


    @Override
    protected void onPause() {

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        boolean isScreenOn = powerManager.isScreenOn();

        if (!isScreenOn) {

            dontDestroyAudioData = false;

        }
        try {
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) {
                    audioBtn.performClick();
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "onPause: " + e);
        }
        if (mHomeWatcher!=null)
        {
            mHomeWatcher.stopWatch();
        }

        super.onPause();

    }

    @Override
    protected void onResume() {

        if (!dontDestroyAudioData) {
            if (audioStatus == AUDIO_STATUS_RECORDING) {
                audioStatus = AUDIO_STATUS_READY_TO_RECORD;
                if (timerRecording != null) {
                    timerRecording.cancel();
                    timerRecording = null;
                }
                if (mediaRecorder != null) {
                    mediaRecorder.release();
                }
                reRecordButton.setEnabled(true);
                findViewById(R.id.forFadeInOut).clearAnimation();
                findViewById(R.id.forFadeInOut).setVisibility(View.GONE);
                reRecordButton.performClick();

            }
        }

        if (mHomeWatcher!=null)
        {
            mHomeWatcher.startWatch();
        }

        super.onResume();
    }


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {


        int action = event.getAction();
        int keyCode = event.getKeyCode();
        switch (keyCode) {

            case KeyEvent.KEYCODE_POWER:
                if (action == KeyEvent.ACTION_DOWN) {
                    dontDestroyAudioData = false;
                    return super.dispatchKeyEvent(event);

                }
            default:
                return super.dispatchKeyEvent(event);
        }

    }

    private SpringAnimation getHighBounceScaleX(View view, float velocityDp, float finalPosition, float DAMPING, float STIFFNESS) {
        final SpringAnimation anim = new SpringAnimation(view, DynamicAnimation.SCALE_X);
        anim.setStartVelocity(getVelocity(velocityDp));
        anim.animateToFinalPosition(finalPosition);

        anim.setSpring(getSpringForce(DAMPING, STIFFNESS, finalPosition));
        return anim;
    }

    private SpringAnimation getHighBounceScaleY(View view, float velocityDp, float finalPosition, float DAMPING, float STIFFNESS) {
        final SpringAnimation anim = new SpringAnimation(view, DynamicAnimation.SCALE_Y);
        anim.setStartVelocity(getVelocity(velocityDp));
        anim.animateToFinalPosition(finalPosition);

        anim.setSpring(getSpringForce(DAMPING, STIFFNESS, finalPosition));
        return anim;
    }

    private void logFirebaseEvent(String eventName, String paramName, String paramValue) {
        Bundle params = new Bundle();
        params.putString(paramName, paramValue);
        mFirebaseAnalytics.logEvent(eventName, params);
    }

    private void logWhenTypeChanged(String eventName, int indexFrom, int indexTo) {
        Bundle params = new Bundle();
        params.putInt("from", Integer.parseInt(orderTypeModels.get(indexFrom - 1).getFees()));
        params.putInt("to", Integer.parseInt(orderTypeModels.get(indexTo - 1).getFees()));
        mFirebaseAnalytics.logEvent(eventName, params);
    }

    private void logWhenTypeSelected(String eventName, String orderType, int index) {
        Bundle params = new Bundle();
        params.putInt(orderType, Integer.parseInt(orderTypeModels.get(index - 1).getFees()));
        mFirebaseAnalytics.logEvent(eventName, params);
    }

    private void logFirebaseEvent2(String eventName, String paramName, String paramValue, String paramName2, int paramValue2) {
        Bundle params = new Bundle();
        params.putString(paramName, paramValue);
        params.putInt(paramName2, paramValue2);
        mFirebaseAnalytics.logEvent(eventName, params);
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
    private void setOnTouchListnerToView(final Button target, final int drawable, final int drawablePressed, final int side)
    {
        target.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    target.setTextColor(Color.parseColor("#80ffffff"));
                    if (side==1)
                    {
                        target.setCompoundDrawablesWithIntrinsicBounds(0, 0, drawablePressed, 0);

                    }else{
                        target.setCompoundDrawablesWithIntrinsicBounds(drawablePressed, 0, 0, 0);

                    }


                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    target.setTextColor(getResources().getColor(R.color.white));
                    if (side==1)
                    {
                        target.setCompoundDrawablesWithIntrinsicBounds(0, 0, drawable, 0);

                    }else{
                        target.setCompoundDrawablesWithIntrinsicBounds(drawable, 0, 0, 0);

                    }

                }
                return false;
            }
        });
    }

}

