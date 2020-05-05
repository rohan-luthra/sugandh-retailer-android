package com.sbw.auder;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.LoopingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.util.Util;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.sbw.auder.HomeScreen.MainActivity;
import com.sbw.auder.LoginAndSignUp.LoginPhoneActivity;
import com.sbw.auder.Models.OrderModel;
import com.sbw.auder.Models.Token;
import com.sbw.auder.Models.UserModelStatic;
import com.sbw.auder.Utils.AudioStreaming;
import com.sbw.auder.Utils.CacheDataSourceFactory;
import com.sbw.auder.Utils.DownloadAudio;
import com.sbw.auder.Utils.TinyDB;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

public class PlayDistOrderActivity extends AppCompatActivity {

    PlayerView playerView;
    SimpleExoPlayer player;
    OrderModel orderModel = new OrderModel();

    ImageView statusImg, preview, backBtn, orderImage, playAudio;
    TextView time, infoText;
    AudioStreaming audioStreaming;
    ProgressBar progressBar;
    TinyDB tinyDB;
    Handler handler;
    Runnable audiRunnable;
    boolean runningFlag = true, audioDone = false;
    int repeatLoop = 0;

    ImageView lotus;
    TextView lotus_loading;
    Date startScreen, endScreen;

    private static final String TAG = "PlayDistOrderActivity";
    private FirebaseAnalytics mFirebaseAnalytics;
    private boolean videoPlayingForFirstTime=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_dist_order);
//        final DistModel distModel = getIntent().getParcelableExtra("dist");


        Toolbar toolbar = (Toolbar) findViewById(R.id.tb_toolbarsearch2);
        setSupportActionBar(toolbar);
        tinyDB = new TinyDB(getApplicationContext());

        lotus = findViewById(R.id.lotus);
        lotus_loading = findViewById(R.id.lotus_loading);
        lotus.setVisibility(View.VISIBLE);
        lotus_loading.setVisibility(View.VISIBLE);
        lotus.startAnimation(AnimationUtils.loadAnimation(this, R.anim.rotate_lotus));
        lotus_loading.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fadeinout));
        // add back arrow to toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // back button pressed
                if (getIntent().getStringExtra("fromActivity") != null) {
                    if (getIntent().getStringExtra("fromActivity").equalsIgnoreCase("notification")) {
                        logFirebaseEvent("came_via_notification", "", "");
                        startActivity(new Intent(PlayDistOrderActivity.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

                    } else {
                        logFirebaseEvent("came_via_app", "", "");
                        finish();
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

                    }

                } else {
                    logFirebaseEvent("came_via_app", "", "");
                    finish();
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

                }

            }
        });
        toolbar.setNavigationIcon(R.drawable.ic_cross_white);



        startScreen = Calendar.getInstance().getTime();

        audioStreaming = findViewById(R.id.playCustomFonts);
        handler = new Handler();
        infoText = findViewById(R.id.infoText);
        infoText.setText("Your prayer is loading ...");
        infoText.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fadeinout));

        playerView = findViewById(R.id.video_view);
        time = findViewById(R.id.time);
        statusImg = findViewById(R.id.currentStatus);
        preview = findViewById(R.id.preview);
        orderImage = findViewById(R.id.orderImage);
        playAudio = findViewById(R.id.playAudio);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        orderImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logFirebaseEvent("picture_tapped", "", "");
            }
        });

        playerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logFirebaseEvent("video_tapped", "", "");
            }
        });


        if (getIntent().getParcelableExtra("order") != null) {
            orderModel = getIntent().getParcelableExtra("order");
            Log.d(TAG, "onCreate: " + orderModel.getTimestamp());
            loadAll();
        } else {

            if (getIntent().getExtras() != null) {

                for (String key : getIntent().getExtras().keySet()) {
                    if (key.equalsIgnoreCase("data")) {
//                        Log.d(TAG, "onCreate:asd " + getIntent().getExtras().getString(key));
                        try {
                            OrderModel orderModel = getOrderModelfromJson(new JSONObject(getIntent().getExtras().getString(key)));
                            Log.d(TAG, "onCreate: asd" + orderModel);
                            this.orderModel = orderModel;
                            loadAll();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.d(TAG, "onCreate: asd" + e.getMessage());
                        }
                    }
                }
            }
        }
    }

    private void loadAll() {

        setTitle(orderModel.getDistName());
        String imagePath = tinyDB.getImagePath(orderModel.getId());
        if (imagePath != null && !tinyDB.isFileNotExist(imagePath)) {
            orderImage.setImageURI(Uri.parse(tinyDB.getImagePath(orderModel.getId())));
        } else {
            Glide.with(getApplicationContext()).asBitmap()
                    .load(getResources().getString(R.string.localhost) + "/api/" + tinyDB.getString(getResources().getString(R.string.authToken)) + "/download/" + orderModel.getId() + "_user.jpg").listener(new RequestListener<Bitmap>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                    return false;
                }

                @Override
                public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                    String path = tinyDB.saveImage(resource, orderModel.getId());
                    tinyDB.setImagePath(orderModel.getId(), path);
                    return false;
                }
            }).into(orderImage);
        }

        final String audioPtah = tinyDB.getAudioPath(orderModel.getId());
        if (audioPtah == null || tinyDB.isFileNotExist(audioPtah)) {
            DownloadAudio downloadAudio = new DownloadAudio(getApplicationContext());
            downloadAudio.execute(PlayDistOrderActivity.this.getResources().getString(R.string.localhost) + "/api/" + tinyDB.getString(getResources().getString(R.string.authToken)) + "/download/" + orderModel.getId() + ".3gp", orderModel.getId(), ".mp3");
            audioStreaming.withUrl(PlayDistOrderActivity.this.getResources().getString(R.string.localhost) + "/api/" + tinyDB.getString(getResources().getString(R.string.authToken)) + "/download/" + orderModel.getId() + ".3gp");
        } else {
            audioStreaming.withUrl(tinyDB.getAudioPath(orderModel.getId()));
           // Toast.makeText(PlayDistOrderActivity.this, "from saved", Toast.LENGTH_SHORT).show();
        }

        if (orderModel.getVideoIntroReceipt() != null) {
            if (Util.SDK_INT > 23) {
                initializePlayer();
//                new CountDownTimer(5000, 5000) {
//                    @Override
//                    public void onTick(long millisUntilFinished) {
//
//                    }
//
//                    @Override
//                    public void onFinish() {
//
//                        initialiseAudioPlayer();
//
//                    }
//                }.start();


                Log.d(TAG, "onCreate: Init Video");
            }
        } else {
            playerView.setVisibility(View.GONE);
            try {
                playAudio.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
                lotus.clearAnimation();
                lotus_loading.clearAnimation();
                lotus.setVisibility(View.GONE);
                lotus_loading.setVisibility(View.GONE);
                audioStreaming.toggleAudio();
                audioStreaming.mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        playAudio.setImageResource(R.drawable.exo_controls_play);
                        audioStreaming.destroy();
                        audioDone = true;
                    }
                });
                audioStreaming.mediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                    @Override
                    public void onBufferingUpdate(MediaPlayer mp, int percent) {
                        if (percent > 99) {
                            playAudio.setVisibility(View.GONE);
                            progressBar.setVisibility(View.GONE);
                            lotus.clearAnimation();
                            lotus_loading.clearAnimation();
                            lotus.setVisibility(View.GONE);
                            lotus_loading.setVisibility(View.GONE);
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        playAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    if (audioStreaming.mediaPlayer != null) {
                        audioStreaming.toggleAudio();
                        if (audioStreaming.mediaPlayer.isPlaying()) {
                            logFirebaseEvent("order_playing_play_tapped", "", "");
                            playAudio.setImageResource(R.drawable.exo_controls_pause);
                        } else {
                            logFirebaseEvent("order_playing_pause_tapped", "", "");
                            playAudio.setImageResource(R.drawable.exo_controls_play);

                        }

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });


        try {
            setLatestStatus(orderModel);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void initialiseAudioPlayer() {
        if (!audioDone) {

            try {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        infoText.clearAnimation();
                        infoText.setVisibility(View.GONE);
                    }
                });
                playAudio.setVisibility(View.GONE);
                if (runningFlag) {
                    audioStreaming.toggleAudio();
                    audioStreaming.mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            playAudio.setImageResource(R.drawable.exo_controls_play);
                            audioDone = true;
                        }
                    });
                    audioStreaming.mediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                        @Override
                        public void onBufferingUpdate(MediaPlayer mp, int percent) {
                            if (percent > 99) {
                                playAudio.setVisibility(View.GONE);
                                progressBar.setVisibility(View.GONE);
                                lotus.clearAnimation();
                                lotus_loading.clearAnimation();
                                lotus.setVisibility(View.GONE);
                                lotus_loading.setVisibility(View.GONE);
                            }
                        }
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

//                    audiRunnable = new Runnable() {
//                        @Override
//                        public void run() {
//
//                        }
//                    };
//
//                    handler.postDelayed(audiRunnable, 0000);
        }

    }

    private void initializePlayer() {
        player = ExoPlayerFactory.newSimpleInstance(new DefaultRenderersFactory(this), new DefaultTrackSelector(), new DefaultLoadControl());
        playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
        player.setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT);


        String videoPath = tinyDB.getVideoPath("VID_" + orderModel.getVideoIntroReceipt());
        Uri uri;
        if (videoPath == null || tinyDB.isFileNotExist(videoPath)) {
          //  Toast.makeText(PlayDistOrderActivity.this, "Video Downloading", Toast.LENGTH_SHORT).show();
            DownloadAudio download = new DownloadAudio(getApplicationContext());
            download.execute(getResources().getString(R.string.localhost) + "/api/" + tinyDB.getString(getResources().getString(R.string.authToken)) + "/download/" + orderModel.getVideoIntroReceipt(), "VID_" + orderModel.getVideoIntroReceipt(), ".mp4");
            uri = Uri.parse(getResources().getString(R.string.localhost) + "/api/" + tinyDB.getString(getResources().getString(R.string.authToken)) + "/download/" + orderModel.getVideoIntroReceipt());
        } else {
          //  Toast.makeText(PlayDistOrderActivity.this, "Video From saved file", Toast.LENGTH_SHORT).show();
            uri = Uri.parse(videoPath);
        }


//        MediaSource mediaSource = buildMediaSource(uri, new CacheDataSourceFactory(getApplicationContext(), 100 * 1024 * 1024, 5 * 1024 * 1024), new DefaultExtractorsFactory(), null, null););
        MediaSource mediaSource = new ExtractorMediaSource(uri,
                new CacheDataSourceFactory(getApplicationContext(), 100 * 1024 * 1024, 5 * 1024 * 1024), new DefaultExtractorsFactory(), null, null);
        LoopingMediaSource loopingMediaSource = new LoopingMediaSource(mediaSource);
        player.prepare(mediaSource);
        player.setPlayWhenReady(true);
        playerView.setPlayer(player);
        player.setVolume(0.5f);
        playerView.setShutterBackgroundColor(Color.TRANSPARENT);

        player.setVideoListener(new SimpleExoPlayer.VideoListener() {
            @Override
            public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {

            }

            @Override
            public void onRenderedFirstFrame() {
                preview.setVisibility(View.GONE);
                playAudio.setImageResource(R.drawable.exo_controls_pause);
                progressBar.setVisibility(View.GONE);
                lotus.clearAnimation();
                lotus_loading.clearAnimation();
                lotus.setVisibility(View.GONE);
                lotus_loading.setVisibility(View.GONE);
                if (videoPlayingForFirstTime)
                {
                    new CountDownTimer(5000, 5000) {
                        @Override
                        public void onTick(long millisUntilFinished) {

                        }

                        @Override
                        public void onFinish() {
                            videoPlayingForFirstTime=false;
                            initialiseAudioPlayer();
                        }
                    }.start();
                }


            }
        });



        player.addListener(new Player.EventListener() {


            @Override
            public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {

            }

            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

            }

            @Override
            public void onLoadingChanged(boolean isLoading) {

            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

                switch (playbackState) {
                    case ExoPlayer.STATE_BUFFERING:
                        break;
                    case ExoPlayer.STATE_ENDED:
          //              Toast.makeText(PlayDistOrderActivity.this, "Loop", Toast.LENGTH_SHORT).show();
                        player.seekTo(0);
                        repeatLoop++;
                        break;
                    case ExoPlayer.STATE_IDLE:
                        break;
                    case ExoPlayer.STATE_READY:
                        break;
                    default:
                        break;
                }

            }

            @Override
            public void onRepeatModeChanged(int repeatMode) {

            }

            @Override
            public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {

            }

            @Override
            public void onPositionDiscontinuity(int reason) {

            }

            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

            }

            @Override
            public void onSeekProcessed() {

            }


        });
    }


    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onResume() {
        super.onResume();
        runningFlag = true;

        if (player != null) {
            player.setPlayWhenReady(true);
        }
        if (audioStreaming.mediaPlayer != null) {
            try {
                if (audioStreaming.mediaPlayer.isPlaying()) {
                    playAudio.setImageResource(R.drawable.exo_controls_pause);
                } else {
                    playAudio.setImageResource(R.drawable.exo_controls_play);
                    playAudio.performClick();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void onPause() {
        super.onPause();
        runningFlag = false;
        playAudio.setImageResource(R.drawable.exo_controls_play);

        if (player != null) {
            player.setPlayWhenReady(false);
        }
        if (audioStreaming.mediaPlayer != null) {
            if (audioStreaming.mediaPlayer.isPlaying()) {
                try {
                    audioStreaming.toggleAudio();
                    playAudio.setImageResource(R.drawable.exo_controls_play);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                if (audiRunnable != null && handler != null) {
                    audiRunnable = new Runnable() {
                        @Override
                        public void run() {
                        }
                    };
                    handler.removeCallbacks(audiRunnable);
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        endScreen = Calendar.getInstance().getTime();
        int timeDiff = (int) (endScreen.getTime() - startScreen.getTime()) / 1000;
        logFirebaseEvent("order_playing_back_tapped", "after_time", timeDiff);

        if(player!=null) {
            int percentage = (int) ((float) player.getCurrentPosition() / player.getDuration() * 100);
            Log.d(TAG, "onDestroy: " + player.getCurrentPosition() + " " + player.getDuration());
            if (repeatLoop == 0)
                logFirebaseEvent("dist_video_percentage_played", "percentage", 100);
            else
                logFirebaseEvent("dist_video_percentage_played", "percentage", percentage);
            logFirebaseEvent("dist_video_loop", "value", repeatLoop);
        }


        if (audioStreaming.mediaPlayer != null) {
            Log.d(TAG, "onDestroy: " + audioStreaming.mediaPlayer.getCurrentPosition() + " " + audioStreaming.mediaPlayer.getDuration());
            int audioPercentage = (int) (Math.ceil((double) audioStreaming.mediaPlayer.getCurrentPosition() / (double) audioStreaming.mediaPlayer.getDuration() * 100));
            logFirebaseEvent("order_audio_percentage_player", "percentage", audioPercentage);
        } else
            logFirebaseEvent("order_audio_percentage_player", "percentage", 0);

        audioStreaming.destroy();
        super.onDestroy();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @SuppressLint("SetTextI18n")
    private void setLatestStatus(OrderModel orderModel) throws JSONException {

        statusImg.setImageResource(R.drawable.sent_icon);
        Timestamp timestamp = new Timestamp(Long.parseLong(orderModel.getTimestamp()));
        Log.d(TAG, "setLatestStatus: " + timestamp.getDate());
        Timestamp today = new Timestamp(new Date().getTime());
        if (today.getYear() == timestamp.getYear()) {
            if (((today.getMonth() - timestamp.getMonth()) + (today.getDate() - timestamp.getDate()) == 0)) {
                if (timestamp.getMinutes() < 10) {
                    time.setText(timestamp.getHours() + ":0" + timestamp.getMinutes());
                } else {
                    time.setText(timestamp.getHours() + ":" + timestamp.getMinutes());
                }
            } else if (((today.getMonth() - timestamp.getMonth()) + (today.getDate() - timestamp.getDate()) == 1)) {
                time.setText("Yesterday");
            } else {
                time.setText(timestamp.getDate() + "/" + (timestamp.getMonth() + 1) + "/" + (timestamp.getYear() % 100) + "");
            }
        } else {
            time.setText(timestamp.getDate() + "/" + (timestamp.getMonth() + 1) + "/" + (timestamp.getYear() % 100) + "");
        }

        try {
            JSONArray status = new JSONArray(orderModel.statusJsonStr);
            if (status.getJSONObject(1) != null) {
                statusImg.setImageResource(R.drawable.played_icon);
            }
        }catch (Exception e){
            e.printStackTrace();
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
        if (object.has("audioReceipt"))
            orderModel.setAudioRec(object.getString("audioReceipt"));
        if (object.has("imageReceipt"))
            orderModel.setImageRec(object.getString("imageReceipt"));
        if (object.has("videoIntroReceipt"))
            orderModel.setVideoIntroReceipt(object.getString("videoIntroReceipt"));
        Log.d(TAG, "getOrderModelfromJson: " + orderModel);
        return orderModel;
    }

    @Override
    public void onBackPressed() {
        logFirebaseEvent("order_playing_back_tapped", "", "");
        if (getIntent().getStringExtra("fromActivity") != null) {
            if (getIntent().getStringExtra("fromActivity").equalsIgnoreCase("notification")) {
                logFirebaseEvent("came_via_notification", "", "");
                startActivity(new Intent(PlayDistOrderActivity.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

            } else {
                logFirebaseEvent("came_via_app", "", "");
                super.onBackPressed();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

            }
        } else {
            logFirebaseEvent("came_via_app", "", "");

            super.onBackPressed();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        }
    }

    private void logFirebaseEvent(String eventName, String paramName, String paramValue) {
        Bundle params = new Bundle();
        params.putString(paramName, paramValue);
        mFirebaseAnalytics.logEvent(eventName, params);
    }

    private void logFirebaseEvent(String eventName, String paramName, int paramValue) {
        Bundle params = new Bundle();
        params.putInt(paramName, paramValue);
        mFirebaseAnalytics.logEvent(eventName, params);
    }

}