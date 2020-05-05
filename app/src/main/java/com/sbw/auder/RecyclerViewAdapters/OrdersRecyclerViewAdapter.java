package com.sbw.auder.RecyclerViewAdapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.sbw.auder.Models.DistModel;
import com.sbw.auder.Models.OrderModel;
import com.sbw.auder.PlayDistOrderActivity;
import com.sbw.auder.R;
import com.sbw.auder.Utils.DownloadAudio;
import com.sbw.auder.Utils.TinyDB;
import com.sbw.auder.ViewHolders.OrdersViewHolder;
import com.sbw.auder.ViewReceiptImage;
import com.wooplr.spotlight.SpotlightView;
import com.wooplr.spotlight.utils.SpotlightListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

import static com.bumptech.glide.load.engine.DiskCacheStrategy.ALL;


public class OrdersRecyclerViewAdapter extends RecyclerView.Adapter<OrdersViewHolder> {

    Context context;
    private ArrayList<OrderModel> orderModelArrayList;
    private DistModel distModel;
    private TinyDB tinyDB;
    private static int playing = -1;
    private MediaPlayer mediaPlayer;
    private String audioPtah, recptAudioPath;
    FloatingActionButton fab;
    Activity activity;

    private FirebaseAnalytics mFirebaseAnalytics;

    private static final String TAG = "OrdersRecyclerViewAdapt";

    public OrdersRecyclerViewAdapter(Context context, ArrayList<OrderModel> orderModelArrayList, DistModel distModel, MediaPlayer mediaPlayer, Activity activity) {
        this.context = context;
        this.orderModelArrayList = orderModelArrayList;
        this.distModel = distModel;
        tinyDB = new TinyDB(context);
        this.mediaPlayer = mediaPlayer;
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
        this.activity = activity;
    }

    @NonNull
    @Override
    public OrdersViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.cardview_order, viewGroup, false);
        return new OrdersViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final OrdersViewHolder ordersViewHolder, final int i) {

        final OrderModel orderModel = orderModelArrayList.get(i);
        ordersViewHolder.url = orderModel.getId();
        ordersViewHolder.id.setText(orderModel.id);
        Log.d(TAG, "onBindViewHolder: " + orderModel);


        ordersViewHolder.recAudio.setImageResource(R.drawable.receipt_play_button);
        ordersViewHolder.playAuido.setImageResource(R.drawable.order_play_button);

        ordersViewHolder.imageRec.setVisibility(View.GONE);
        ordersViewHolder.progressBar2.setVisibility(View.GONE);
        ordersViewHolder.recAudio.setVisibility(View.GONE);
        ordersViewHolder.timestamp_order.setVisibility(View.VISIBLE);
        ordersViewHolder.statusImg.setVisibility(View.GONE);
        ordersViewHolder.noImageText.setVisibility(View.GONE);
        ordersViewHolder.schTime.setVisibility(View.GONE);
        ordersViewHolder.time_receipt.setVisibility(View.GONE);
        ordersViewHolder.recText.setVisibility(View.GONE);
        ordersViewHolder.recAudio.setVisibility(View.GONE);
        if (orderModelArrayList.size() == 1) {
            new CountDownTimer(1000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                    new SpotlightView.Builder(activity)
                            .introAnimationDuration(500)
                            .enableRevealAnimation(true)
                            .performClick(true)
                            .fadeinTextDuration(300)
                            .headingTvColor(Color.parseColor("#eb273f"))
                            .headingTvSize(18)
                            .headingTvText(activity.getResources().getString(R.string.spotLightTextLiveExperience))
                            .target(ordersViewHolder.playAuido)
                            .subHeadingTvColor(Color.parseColor("#ffffff"))
                            .subHeadingTvSize(14)
                            .subHeadingTvText(activity.getResources().getString(R.string.spotLightTextLiveExperienceMessage))
                            .maskColor(Color.parseColor("#99000000"))
                            .lineAnimDuration(400)
                            .lineAndArcColor(Color.parseColor("#eb273f"))
                            .dismissOnTouch(true)
                            .dismissOnBackPress(true)
                            .enableDismissAfterShown(false)
                            .usageId("receipt screen play audio")
                            .setListener(new SpotlightListener() {
                                @Override
                                public void onUserClicked(String s) {
                                    if (ordersViewHolder.recAudio.getVisibility()== View.VISIBLE)
                                    {
                                        new SpotlightView.Builder(activity)
                                                .introAnimationDuration(500)
                                                .enableRevealAnimation(true)
                                                .performClick(false)
                                                .fadeinTextDuration(300)
                                                .headingTvColor(Color.parseColor("#eb273f"))
                                                .headingTvSize(18)
                                                .headingTvText(activity.getResources().getString(R.string.spotLightTextReceipt))
                                                .target(ordersViewHolder.recAudio)
                                                .subHeadingTvColor(Color.parseColor("#ffffff"))
                                                .subHeadingTvSize(14)
                                                .subHeadingTvText(activity.getResources().getString(R.string.spotLightTextReceiptMessage))
                                                .maskColor(Color.parseColor("#99000000"))
                                                .lineAnimDuration(400)
                                                .lineAndArcColor(Color.parseColor("#eb273f"))
                                                .dismissOnTouch(true)
                                                .dismissOnBackPress(true)
                                                .enableDismissAfterShown(false)
                                                .usageId("receipt screen receipt audio")
                                                .show();

                                    }

                                }
                            })
                            .show();

                }
            }.start();

        }
        setUpOrderTimestamp(ordersViewHolder, orderModel, distModel, i);
        setUpOrderAudio(ordersViewHolder, orderModel, distModel, i);
        if (orderModel.statusJsonStr.contains("seen")) {
            setUpRecImage(ordersViewHolder, orderModel, distModel, i);
            setUpRecAudio(ordersViewHolder, orderModel, distModel, i);
        }
        setStatus(orderModel, ordersViewHolder);
        if (orderModel.getRecText() != null) {
            ordersViewHolder.recText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    logFirebaseEvent("receipt_text_tapped", "", "");
                }
            });

            String recptText = orderModel.getRecText();

            recptText=modifiedReceiptTextByAmount(recptText);


            ordersViewHolder.recText.setText(recptText);

            ordersViewHolder.recText.setVisibility(View.VISIBLE);

            ordersViewHolder.recieptcorner.setVisibility(View.VISIBLE);

        } else {
            ordersViewHolder.recText.setVisibility(View.GONE);
        }
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
            }
        });
        setUpOrderImage(ordersViewHolder, orderModel, distModel, i);
    }

    private String modifiedReceiptTextByAmount(String recptText) {


        if (recptText.contains("{") && recptText.contains("}"))
        {
            int indexOfOpenCurly = recptText.indexOf("{");
            int indexOfCloseCurly = recptText.indexOf("}");
            if (recptText.contains(" 0")) {
                recptText = recptText.replace(recptText.substring(indexOfOpenCurly + 1, indexOfCloseCurly ), "");

            }

            recptText = recptText.replace(recptText.substring(indexOfOpenCurly, indexOfOpenCurly + 1), "");
            indexOfCloseCurly = recptText.indexOf("}");
            recptText = recptText.replace(recptText.substring(indexOfCloseCurly, indexOfCloseCurly + 1), "");



        }

        return recptText;
    }


    private void setUpOrderAudio(final OrdersViewHolder ordersViewHolder, final OrderModel orderModel, DistModel distModel, final int i) {
        audioPtah = tinyDB.getAudioPath(orderModel.getId());
        if (audioPtah == null || tinyDB.isFileNotExist(audioPtah)) {
            DownloadAudio downloadAudio = new DownloadAudio(context);
            downloadAudio.execute(context.getResources().getString(R.string.localhost) + "/api/" + new TinyDB(context).getString(context.getResources().getString(R.string.authToken)) + "/download/" + orderModel.getId() + ".3gp", orderModel.getId(), ".mp3");
            //ordersViewHolder.audioStreamingCustomFont.withUrl(context.getResources().getString(R.string.localhost) + "/api/"+new TinyDB(context).getString(context.getResources().getString(R.string.authToken))+"/download/" + orderModel.getId() + ".3gp");
            ordersViewHolder.audioUri = context.getResources().getString(R.string.localhost) + "/api/" + new TinyDB(context).getString(context.getResources().getString(R.string.authToken)) + "/download/" + orderModel.getId() + ".3gp";
        } else {
            // ordersViewHolder.audioStreamingCustomFont.withUrl(tinyDB.getAudioPath(orderModel.getId()));
            ordersViewHolder.audioUri = audioPtah;
//            Toast.makeText(context,"from saved",Toast.LENGTH_SHORT).show();
        }
        ordersViewHolder.playAuido.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                audioPtah = tinyDB.getAudioPath(orderModel.getId());
                if (!orderModel.getStatusJsonStr().contains("seen")) {
                    try {
                        //ordersViewHolder.audioStreamingCustomFont.toggleAudio();


                        if (playing == i && mediaPlayer.isPlaying()) {
                            mediaPlayer.stop();
                            ordersViewHolder.playAuido.setImageResource(R.drawable.order_play_button_bg);
                            playing = -1;
                        } else if (audioPtah == null || tinyDB.isFileNotExist(audioPtah)) {
                            if (playing != -1) {
                                notifyItemChanged(playing);
                            }
//                            Toast.makeText(context,"Audio Downloading",Toast.LENGTH_SHORT).show();
                            logFirebaseEvent("unplayed_order_played", "", "");
                            mediaPlayer.reset();
                            mediaPlayer.setDataSource(ordersViewHolder.audioUri);
                            mediaPlayer.prepareAsync();
                            playing = i;
                        } else {

                            if (playing != -1) {
                                notifyItemChanged(playing);
                            }
//                           Toast.makeText(context,"Audio from saved file",Toast.LENGTH_SHORT).show();
                            logFirebaseEvent("unplayed_order_played", "", "");
                            mediaPlayer.reset();
                            mediaPlayer.setDataSource(audioPtah);
                            mediaPlayer.prepare();
                            mediaPlayer.start();
                            ordersViewHolder.playAuido.setImageResource(R.drawable.order_pause_button);
                            playing = i;

                        }
                        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp) {

                                mediaPlayer.stop();

                                playing = -1;
                                ordersViewHolder.playAuido.setImageResource(R.drawable.order_play_button_bg);
                            }
                        });


                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    logFirebaseEvent("played_order_played", "", "");
                    context.startActivity(new Intent(context, PlayDistOrderActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).putExtra("order", orderModel));
                    activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);


                }

            }
        });
    }

    private void setUpOrderImage(final OrdersViewHolder ordersViewHolder, final OrderModel orderModel, final DistModel distModel, final int i) {


        if (orderModel.userImageUrl != null && !orderModel.userImageUrl.isEmpty()) {

            ordersViewHolder.noImageText.setVisibility(View.GONE);
            try {
                final String imagePath = tinyDB.getImagePath(orderModel.getId());
                ordersViewHolder.statusImg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (tinyDB.getImagePath(orderModel.id) != null) {

                            logFirebaseEvent("order_image_tapped", "", "");

                            showImageDialog(tinyDB.getImagePath(orderModel.id));


                        }
                    }
                });

                if (imagePath != null && !tinyDB.isFileNotExist(imagePath)) {
                    Log.i("image path", tinyDB.getImagePath(orderModel.getId()));
                    ordersViewHolder.statusImg.setVisibility(View.VISIBLE);

                    Glide.with(context).clear(ordersViewHolder.statusImg);
                    Glide.with(context)
                            .asBitmap()
                            .thumbnail(0.1f)
                            .diskCacheStrategy(ALL)
                            .load(tinyDB.getImagePath(orderModel.getId()))
                            .into(ordersViewHolder.statusImg);

                    ordersViewHolder.progressBar1.setVisibility(View.GONE);
                    ordersViewHolder.statusImg.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showImageDialog(imagePath);


                        }
                    });
                } else {
//                Toast.makeText(context,"glide",Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onBindViewHolder: " + orderModel.userImageUrl);
                    Glide.with(context).clear(ordersViewHolder.statusImg);

                    try {
                        ordersViewHolder.statusImg.setVisibility(View.VISIBLE);

                        if (tinyDB.getImagePath(orderModel.getId()) != null && !tinyDB.isFileNotExist(tinyDB.getImagePath(orderModel.getId()))) {
                            Glide.with(context)
                                    .asBitmap()
                                    .diskCacheStrategy(ALL)
                                    .load(tinyDB.getImagePath(tinyDB.getImagePath(orderModel.getId())))
                                    .into(ordersViewHolder.imageRec);
                            ordersViewHolder.imageRec.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    logFirebaseEvent("order_image_tapped", "", "");

                                    showImageDialog(tinyDB.getImagePath(orderModel.getId()));


                                }
                            });
                            ordersViewHolder.progressBar2.setVisibility(View.GONE);
                        } else {

                            Glide.with(context).asBitmap().load(context.getResources().getString(R.string.localhost) + "/api/" + new TinyDB(context).getString(context.getResources().getString(R.string.authToken)) + "/download/" + orderModel.getId() + "_user.jpg").listener(new RequestListener<Bitmap>() {

                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {


                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                                    final String path = tinyDB.saveImage(resource, orderModel.getId());
                                    tinyDB.setImagePath(orderModel.getId(), path);
                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            setUpOrderImage(ordersViewHolder, orderModel, distModel, i);
                                        }
                                    });
                                    return false;
                                }
                            }).submit();
                        }
                    } catch (Exception e) {
                        Log.d(TAG, "onBindViewHolder: Error" + e.getLocalizedMessage());
                    }

                }

            } catch (Exception e) {
                ordersViewHolder.noImageText.setVisibility(View.VISIBLE);
                ordersViewHolder.noImageText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        logFirebaseEvent("order_text_tapped", "", "");
                    }
                });
                ordersViewHolder.statusImg.setVisibility(View.GONE);
            }


        } else if (distModel.getSkippedImage() != null) {

            setSkippedDistImage(distModel, orderModel, ordersViewHolder, i);


        } else {
            ordersViewHolder.noImageText.setVisibility(View.VISIBLE);
            ordersViewHolder.noImageText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    logFirebaseEvent("order_text_tapped", "", "");
                }
            });
            ordersViewHolder.statusImg.setVisibility(View.GONE);

        }
    }

    private void setSkippedDistImage(final DistModel distModel, final OrderModel orderModel, final OrdersViewHolder ordersViewHolder, final int i) {
        Log.d(TAG, "setUpOrderImage: skipped image");
        ordersViewHolder.noImageText.setVisibility(View.GONE);
        try {
            final String imagePath = tinyDB.getImagePath("SKIPPED_" + distModel.getSkippedImage());
            ordersViewHolder.statusImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (tinyDB.getImagePath("SKIPPED_" + distModel.getSkippedImage()) != null) {
                        logFirebaseEvent("order_image_tapped", "", "");
                        showImageDialog(tinyDB.getImagePath(orderModel.id));

                    }
                }
            });

            if (imagePath != null && !tinyDB.isFileNotExist(imagePath)) {

                Log.i("image path", tinyDB.getImagePath("SKIPPED_" + distModel.getSkippedImage()));
                ordersViewHolder.statusImg.setVisibility(View.VISIBLE);
                Glide.with(context).clear(ordersViewHolder.statusImg);
                Glide.with(context)
                        .load(tinyDB.getImagePath("SKIPPED_" + distModel.getSkippedImage()))
                        .diskCacheStrategy(ALL)
                        .into(ordersViewHolder.statusImg);

                ordersViewHolder.progressBar1.setVisibility(View.GONE);
                ordersViewHolder.statusImg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showImageDialog(imagePath);


                    }
                });
            } else {
                Glide.with(context).clear(ordersViewHolder.statusImg);
                try {
                    ordersViewHolder.statusImg.setVisibility(View.VISIBLE);

                    if (tinyDB.getImagePath("SKIPPED_" + distModel.getSkippedImage()) != null && !tinyDB.isFileNotExist(tinyDB.getImagePath("SKIPPED_" + distModel.getSkippedImage()))) {
                        Glide.with(context)
                                .asBitmap()
                                .diskCacheStrategy(ALL)
                                .load(tinyDB.getImagePath("SKIPPED_" + distModel.getSkippedImage()))
                                .into(ordersViewHolder.imageRec);
                        ordersViewHolder.imageRec.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                logFirebaseEvent("order_image_tapped", "", "");

                                showImageDialog(tinyDB.getImagePath(distModel.getSkippedImage()));

                            }
                        });
                        ordersViewHolder.progressBar2.setVisibility(View.GONE);
                    } else {

                        Glide.with(context).asBitmap().load(context.getResources().getString(R.string.localhost) + "/api/" + new TinyDB(context).getString(context.getResources().getString(R.string.authToken)) + "/download/" + distModel.getSkippedImage()).listener(new RequestListener<Bitmap>() {

                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {

                                        ordersViewHolder.progressBar1.setVisibility(View.GONE);
                                        ordersViewHolder.statusImg.setVisibility(View.GONE);
                                    }
                                });
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                                final String path = tinyDB.saveImage(resource, "SKIPPED_" + distModel.getSkippedImage());
                                tinyDB.setImagePath("SKIPPED_" + distModel.getSkippedImage(), path);
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        setUpOrderImage(ordersViewHolder, orderModel, distModel, i);
                                    }
                                });
                                return false;
                            }
                        }).submit();
                    }
                } catch (Exception e) {
                    Log.d(TAG, "onBindViewHolder: Error" + e.getLocalizedMessage());
                }

            }

        } catch (Exception e) {
            ordersViewHolder.noImageText.setVisibility(View.VISIBLE);
            ordersViewHolder.noImageText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    logFirebaseEvent("order_text_tapped", "", "");
                }
            });
            ordersViewHolder.statusImg.setVisibility(View.GONE);
        }
    }

    private void setUpOrderTimestamp(OrdersViewHolder ordersViewHolder, OrderModel orderModel, DistModel distModel, int i) {
        Timestamp timestamp = new Timestamp(Long.parseLong(orderModel.getTimestamp()));
        Timestamp today = new Timestamp(new Date().getTime());

        if (orderModel.getStatusJsonStr().contains("seen")) {
            ordersViewHolder.schTime.setVisibility(View.GONE);
        } else {
            ordersViewHolder.schTime.setVisibility(View.VISIBLE);
            ordersViewHolder.schTime.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    logFirebaseEvent("scheduled_chat_tapped", "", "");
                }
            });
            try {
                Timestamp schTimestamp = new Timestamp(Long.parseLong(orderModel.getSchTimestamp()));
                ordersViewHolder.schTime.setText(activity.getResources().getString(R.string.willBePlayedAt)+" " + schTimestamp.getHours() + ":" + ((schTimestamp.getMinutes() < 10) ? "0" : "") + schTimestamp.getMinutes() + " on "
                        + schTimestamp.getDate() + "/" + (schTimestamp.getMonth() + 1) + "/" + schTimestamp.getYear() % 100);
                ordersViewHolder.recieptcorner.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                Timestamp schTimestamp = new Timestamp(Long.parseLong(orderModel.getTimestamp()));
                ordersViewHolder.schTime.setText(activity.getResources().getString(R.string.willBePlayedAt)+" " + schTimestamp.getHours() + ":" + ((schTimestamp.getMinutes() < 10) ? "0" : "") + schTimestamp.getMinutes() + " on "
                        + schTimestamp.getDate() + "/" + (schTimestamp.getMonth() + 1) + "/" + schTimestamp.getYear() % 100);
                ordersViewHolder.recieptcorner.setVisibility(View.VISIBLE);
                e.printStackTrace();
            }

        }

        if (today.getYear() == timestamp.getYear()) {
            if (((today.getMonth() - timestamp.getMonth()) + (today.getDate() - timestamp.getDate()) == 0)) {
                if (timestamp.getMinutes() < 10) {
                    ordersViewHolder.timestamp_order.setText(timestamp.getHours() + ":0" + timestamp.getMinutes());

                } else {
                    ordersViewHolder.timestamp_order.setText(timestamp.getHours() + ":" + timestamp.getMinutes());
                }
            } else if (((today.getMonth() - timestamp.getMonth()) + (today.getDate() - timestamp.getDate()) == 1)) {
                ordersViewHolder.timestamp_order.setText("Yesterday");
            } else {
                ordersViewHolder.timestamp_order.setText(timestamp.getDate() + "/" + (timestamp.getMonth() + 1) + "/" + (timestamp.getYear() % 100) + "");
            }
        } else {

            ordersViewHolder.timestamp_order.setText(timestamp.getDate() + "/" + (timestamp.getMonth() + 1) + "/" + (timestamp.getYear() % 100) + "");

        }


    }

    private void setUpRecAudio(final OrdersViewHolder ordersViewHolder, final OrderModel orderModel, final DistModel distModel, final int i) {
        if (orderModel.getAudioRec() != null) {
            recptAudioPath = tinyDB.getAudioPath("RCPT_" + orderModel.getAudioRec());

            if (recptAudioPath == null || tinyDB.isFileNotExist(recptAudioPath)) {

                DownloadAudio downloadAudio = new DownloadAudio(context);
                downloadAudio.execute(context.getResources().getString(R.string.localhost) + "/api/" + new TinyDB(context).getString(context.getResources().getString(R.string.authToken)) + "/download/" + orderModel.getAudioRec(), "RCPT_" + orderModel.getAudioRec(), ".mp3");
                ordersViewHolder.rcptAdudioUri = context.getResources().getString(R.string.localhost) + "/api/" + new TinyDB(context).getString(context.getResources().getString(R.string.authToken)) + "/download/" + orderModel.getAudioRec();
            } else {
                ordersViewHolder.rcptAdudioUri = recptAudioPath;
            }
            Log.d(TAG, "onBindViewHolder: " + context.getResources().getString(R.string.localhost) + "/api/" + new TinyDB(context).getString(context.getResources().getString(R.string.authToken)) + "/download/" + orderModel.getAudioRec());
            ordersViewHolder.recAudio.setVisibility(View.VISIBLE);

            ordersViewHolder.recAudio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    try {
                        //ordersViewHolder.recAudioStreamingCustomFont.toggleAudio();

                        final String[] imagePath = {null};
                        if (tinyDB.getImagePath("RECPT_" + orderModel.getImageRec())!=null)
                        {
                            imagePath[0] = tinyDB.getImagePath("RECPT_" + orderModel.getImageRec());

                        }else{
                            Glide.with(context).asBitmap().load(context.getResources().getString(R.string.localhost) + "/api/" + new TinyDB(context).getString(context.getResources().getString(R.string.authToken)) + "/download/" + orderModel.getImageRec()).listener(new RequestListener<Bitmap>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {


                                    return false;

                                }

                                @Override
                                public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                                    final String path = tinyDB.saveImage(resource, "IMG_RECPT_" + orderModel.getImageRec());
                                    tinyDB.setImagePath("RECPT_" + orderModel.getImageRec(), path);

                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            setUpOrderImage(ordersViewHolder, orderModel, distModel, i);
                                        }
                                    });
                                    imagePath[0] =tinyDB.getImagePath("RECPT_" + orderModel.getImageRec());

                                    return false;
                                }
                            }).submit();
                        }
                        recptAudioPath = tinyDB.getAudioPath("RCPT_" + orderModel.getAudioRec());
                        if (playing == i && mediaPlayer.isPlaying()) {
                            mediaPlayer.stop();
                            ordersViewHolder.recAudio.setImageResource(R.drawable.reciept_play_button_bg);
                            playing = -1;
                        } else if (recptAudioPath == null || tinyDB.isFileNotExist(recptAudioPath)) {
//                            Toast.makeText(context,"Audio Downloading",Toast.LENGTH_SHORT).show();
                            if (playing != -1) {
                                notifyItemChanged(playing);
                            }


                            // notifyItemChanged(playing);
                            logFirebaseEvent("receipt_played", "", "");
                            mediaPlayer.reset();
                            Toast.makeText(context, activity.getResources().getString(R.string.pleasewait_loading), Toast.LENGTH_SHORT).show();
                            mediaPlayer.setDataSource(ordersViewHolder.rcptAdudioUri);
                            mediaPlayer.prepareAsync();

                            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                @Override
                                public void onPrepared(MediaPlayer mp) {
                                    mediaPlayer.start();
                                    ordersViewHolder.recAudio.setImageResource(R.drawable.receipts_stop_button);
                                }
                            });
                            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                @Override
                                public void onCompletion(MediaPlayer mp) {
                                    mediaPlayer.release();
                                    ordersViewHolder.recAudio.setImageResource(R.drawable.reciept_play_button_bg);

                                }
                            });
                            playing = i;
                            showImageDialog(imagePath[0]);

                        } else {
                            if (playing != -1) {
                                notifyItemChanged(playing);
                            }
                            showImageDialog(imagePath[0]);

//                            Toast.makeText(context,"Audio From Saved file",Toast.LENGTH_SHORT).show();
                            logFirebaseEvent("receipt_played", "", "");

                            mediaPlayer.reset();
                            mediaPlayer.setDataSource(recptAudioPath);
                            mediaPlayer.prepare();
                            mediaPlayer.start();
                            ordersViewHolder.recAudio.setImageResource(R.drawable.receipts_stop_button);
                            playing = i;
                        }

                        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp) {


                                mediaPlayer.stop();
                                playing = -1;
                                ordersViewHolder.recAudio.setImageResource(R.drawable.reciept_play_button_bg);
                            }
                        });

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }

            });

        }
    }

    private void showImageDialog(String imagePath) {

        if (imagePath!=null)
        {
            ViewReceiptImage viewReceiptImage = new ViewReceiptImage();
            viewReceiptImage.showDialog(activity, imagePath);
        }
    }

    private void setUpRecImage(final OrdersViewHolder ordersViewHolder, final OrderModel orderModel, final DistModel distModel, final int i) {
        if (orderModel.getImageRec() != null) {
            ordersViewHolder.imageRec.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (tinyDB.getImagePath(("RECPT_" + orderModel.getImageRec())) != null) {

                        logFirebaseEvent("receipt_tapped", "", "");

                        showImageDialog(orderModel.getImageRec());

                    }
                }
            });


            ordersViewHolder.relativeLayout.setVisibility(View.VISIBLE);
            ordersViewHolder.recieptcorner.setVisibility(View.VISIBLE);
            ordersViewHolder.imageRec.setVisibility(View.VISIBLE);
            ordersViewHolder.progressBar2.setVisibility(View.VISIBLE);
            final String rcptImagePath = tinyDB.getImagePath("RECPT_" + orderModel.getImageRec());
            if (rcptImagePath != null && !tinyDB.isFileNotExist(rcptImagePath)) {
//                ordersViewHolder.imageRec.setImageURI(Uri.parse(tinyDB.getImagePath("RECPT_"+orderModel.getImageRec())));
                Glide.with(context)
                        .asBitmap()
                        .thumbnail(0.1f)
                        .diskCacheStrategy(ALL)
                        .load(tinyDB.getImagePath("RECPT_" + orderModel.getImageRec()))
                        .into(ordersViewHolder.imageRec);
                ordersViewHolder.imageRec.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        logFirebaseEvent("receipt_tapped", "", "");
                        showImageDialog(rcptImagePath);


                    }
                });
                ordersViewHolder.progressBar2.setVisibility(View.GONE);
            } else {

                Glide.with(context).asBitmap().load(context.getResources().getString(R.string.localhost) + "/api/" + new TinyDB(context).getString(context.getResources().getString(R.string.authToken)) + "/download/" + orderModel.getImageRec()).listener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {

                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ordersViewHolder.progressBar1.setVisibility(View.GONE);
                                ordersViewHolder.imageRec.setVisibility(View.GONE);
                            }
                        });
                        return false;

                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                        final String path = tinyDB.saveImage(resource, "IMG_RECPT_" + orderModel.getImageRec());
                        tinyDB.setImagePath("RECPT_" + orderModel.getImageRec(), path);
                        ordersViewHolder.imageRec.setClipToOutline(true);
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ordersViewHolder.progressBar2.setVisibility(View.GONE);
                                setUpRecImage(ordersViewHolder, orderModel, distModel, i);
                            }
                        });

                        return false;
                    }
                }).submit();
            }

        } else {
            // ordersViewHolder.noImageText.setVisibility(View.VISIBLE);
            ordersViewHolder.imageRec.setVisibility(View.GONE);
        }

    }

    private void setStatus(OrderModel orderModel, final OrdersViewHolder ordersViewHolder) {

        try {


            if (orderModel.statusJsonStr.contains("seen")) {


                ordersViewHolder.double_tick.setVisibility(View.VISIBLE);
                ordersViewHolder.scheduled.setVisibility(View.GONE);
                ordersViewHolder.time_receipt.setVisibility(View.VISIBLE);


                JSONArray jsonArray = new JSONArray(orderModel.getStatusJsonStr());
                JSONObject object = jsonArray.getJSONObject(1);
                Timestamp timestamp = new Timestamp(Long.parseLong(object.getString("seen")));
                Timestamp today = new Timestamp(new Date().getTime());

                if (today.getYear() == timestamp.getYear()) {
                    if (((today.getMonth() - timestamp.getMonth()) + (today.getDate() - timestamp.getDate()) == 0)) {
                        if (timestamp.getMinutes() < 10) {
                            ordersViewHolder.time_receipt.setText(timestamp.getHours() + ":0" + timestamp.getMinutes());

                        } else {
                            ordersViewHolder.time_receipt.setText(timestamp.getHours() + ":" + timestamp.getMinutes());
                        }
                    } else if (((today.getMonth() - timestamp.getMonth()) + (today.getDate() - timestamp.getDate()) == 1)) {
                        ordersViewHolder.time_receipt.setText("Yesterday");
                    } else {
                        ordersViewHolder.time_receipt.setText(timestamp.getDate() + "/" + (timestamp.getMonth() + 1) + "/" + (timestamp.getYear() % 100) + "");
                    }
                } else {

                    ordersViewHolder.time_receipt.setText(timestamp.getDate() + "/" + (timestamp.getMonth() + 1) + "/" + (timestamp.getYear() % 100) + "");

                }

            } else {
                ordersViewHolder.scheduled.setVisibility(View.VISIBLE);
                ordersViewHolder.double_tick.setVisibility(View.GONE);
                ordersViewHolder.time_receipt.setVisibility(View.GONE);


            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(TAG, "setStatus: " + e.getLocalizedMessage());
        }


    }


    @Override
    public int getItemCount() {
        if (orderModelArrayList == null)
            return 0;
        return orderModelArrayList.size();
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        if (mediaPlayer != null) {
            mediaPlayer.reset();
            mediaPlayer.stop();
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