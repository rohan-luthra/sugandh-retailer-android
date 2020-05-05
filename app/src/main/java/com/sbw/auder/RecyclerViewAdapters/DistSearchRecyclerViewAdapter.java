package com.sbw.auder.RecyclerViewAdapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.sbw.auder.Models.DistModel;
import com.sbw.auder.Models.Token;
import com.sbw.auder.OrdersActivity.NoOrderActivity;
import com.sbw.auder.PlaceOrder.PlaceOrderActivity;
import com.sbw.auder.Utils.TinyDB;
import com.sbw.auder.ViewHolders.DistViewHolder;
import com.sbw.auder.Models.OrderModel;
import com.sbw.auder.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;


public class DistSearchRecyclerViewAdapter extends RecyclerView.Adapter<DistViewHolder> {

    Context context;
    ArrayList<DistModel> distModelArrayList;
    String userId;
    TinyDB tinyDB;
    private long mLastClickTime = 0;
    Activity activity;

    private FirebaseAnalytics mFirebaseAnalytics;
    private static final String TAG = "DistSearchRecyclerViewA";

    public DistSearchRecyclerViewAdapter(Context context, ArrayList<DistModel> distModelArrayList, String userId, Activity activity) {
        this.context = context;
        this.distModelArrayList = distModelArrayList;
        this.userId = userId;
        tinyDB = new TinyDB(context);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
        this.activity = activity;

    }

    @NonNull
    @Override
    public DistViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.cardview_dist_search, viewGroup, false);
        return new DistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final DistViewHolder holder, final int i) {

        final DistModel distModel = distModelArrayList.get(i);
        Log.d(TAG, "onBindViewHolder: " + distModel.toString());
//        holder.id.setText(distModel.id);
        holder.name.setText(distModel.name);
        holder.location.setText(distModel.location);
        holder.time.setVisibility(View.GONE);
        if (distModel.getProfileUrl() != null) {

            if (tinyDB.getImagePath("profile_" + distModel.getId()+"_"+distModel.getProfileUrl()) != null) {

                try {

                    if (!tinyDB.getImagePath("profile_" + distModel.getId() + "_" + distModel.getProfileUrl()).isEmpty())
                        holder.distImage.setImageURI(Uri.parse(tinyDB.getImagePath("profile_" + distModel.getId() + "_" + distModel.getProfileUrl())));
                    else {
                        Glide.with(context).asBitmap().load(context.getResources().getString(R.string.localhost) + "/api/"+ new TinyDB(context).getString(context.getResources().getString(R.string.authToken))+"/download/" + distModel.getProfileUrl()).listener(new RequestListener<Bitmap>() {

                            @SuppressLint("ResourceType")
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                                holder.distImage.setImageResource(context.getResources().getColor(R.color.red_dark));
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                resource.compress(Bitmap.CompressFormat.JPEG, 100, baos);

                                String path = tinyDB.saveImageWithoutExt(resource, "profile_" + distModel.getId()+"_"+distModel.getProfileUrl());
                                tinyDB.setImagePath("profile_" + distModel.getId()+"_"+distModel.getProfileUrl(), path);

                                return false;
                            }
                        }).into(holder.distImage);

                    }
                }catch (Exception e){
                    Log.d(TAG, "onBindViewHolder: Error" + e.getLocalizedMessage());
                    Glide.with(context).asBitmap().load(context.getResources().getString(R.string.localhost) + "/api/"+new TinyDB(context).getString(context.getResources().getString(R.string.authToken))+"/download/" + distModel.getProfileUrl()).listener(new RequestListener<Bitmap>() {

                        @SuppressLint("ResourceType")
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                            holder.distImage.setImageResource(context.getResources().getColor(R.color.red_dark));
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            resource.compress(Bitmap.CompressFormat.JPEG, 100, baos);

                            String path = tinyDB.saveImageWithoutExt(resource, "profile_" + distModel.getId()+"_"+distModel.getProfileUrl());
                            tinyDB.setImagePath("profile_" + distModel.getId()+"_"+distModel.getProfileUrl(), path);

                            return false;
                        }
                    }).into(holder.distImage);
                }
            }
            else {

                Glide.with(context).asBitmap().load(context.getResources().getString(R.string.localhost) + "/api/"+new TinyDB(context).getString(context.getResources().getString(R.string.authToken))+"/download/" + distModel.getProfileUrl()).listener(new RequestListener<Bitmap>() {

                    @SuppressLint("ResourceType")
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                holder.distImage.setImageResource(context.getResources().getColor(R.color.red_dark));

                            }
                        });
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        resource.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        String path = tinyDB.saveImageWithoutExt(resource, "profile_" + distModel.getId() + "_" + distModel.getProfileUrl());
                        tinyDB.setImagePath("profile_" + distModel.getId() + "_" + distModel.getProfileUrl(), path);
                        return false;
                    }
                }).into(holder.distImage);
            }
        } else {
            holder.distImage.setImageResource(R.drawable.ic_user_white);
        }

//        getData(holder, distModel);
        if (distModel.getLatestOrder() != null) {
            try {
                setLatestStatus(holder, distModel.getLatestOrder(), distModel, i);
                holder.noorder.setVisibility(View.GONE);
                holder.statusImg.setVisibility(View.VISIBLE);
                holder.time.setVisibility(View.VISIBLE);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            holder.time.setText("");
           // holder.statusImg.setImageResource(R.drawable.no_order_yet_icon);
            holder.statusImg.setVisibility(View.GONE);
            holder.noorder.setVisibility(View.VISIBLE);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                        return;
                    }

                    logFirebaseEvent("dist_selected", "dist_name", distModel.getName());
                    logFirebaseEvent("dist_selected", "is_it_repeat", 0);
                    logFirebaseEvent("dist_selected", "order_from_top", i+1);

                    mLastClickTime = SystemClock.elapsedRealtime();

                    Intent intent = new Intent(context, NoOrderActivity.class);
                    intent.putExtra("dist", distModel);
                    intent.putExtra("fromActivity", "main");

                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                    activity.overridePendingTransition(R.anim.enter_right, android.R.anim.fade_out);


                }
            });
        }


    }

    private void setLatestStatus(DistViewHolder holder, OrderModel orderModel, final DistModel distModel, final int i) throws JSONException {

        Log.d(TAG, "setLatestStatus: " + orderModel);

        JSONArray jsonArray = new JSONArray(orderModel.getStatusJsonStr());
        JSONObject object = jsonArray.getJSONObject((jsonArray.length() - 1));

//        Timestamp timestamp = new Timestamp(Long.parseLong(orderModel.timestamp));
//        holder.time.setText(timestamp.toLocaleString());

        Iterator<String> iter = object.keys();
        while (iter.hasNext()) {
            String key = iter.next();
            if (key.equals("seen")) {

                Timestamp timestamp = new Timestamp(object.getLong("seen"));
                Timestamp today = new Timestamp(new Date().getTime());

                if(today.getYear()==timestamp.getYear()){
                    if( ((today.getMonth()-timestamp.getMonth()) + (today.getDate()-timestamp.getDate()) == 0)){
                        if(timestamp.getMinutes()<10){
                            holder.time.setText(timestamp.getHours() + ":0" + timestamp.getMinutes());

                        }else {
                            holder.time.setText(timestamp.getHours() + ":" + timestamp.getMinutes());
                        }
                    }else if (  ((today.getMonth()-timestamp.getMonth()) + (today.getDate()-timestamp.getDate()) == 1)){
                        holder.time.setText("Yesterday");
                    }else{
                        holder.time.setText(timestamp.getDate()+"/"+(timestamp.getMonth()+1)+"/"+(timestamp.getYear()%100) +"");
                    }
                }else{

                    holder.time.setText(timestamp.getDate()+"/"+(timestamp.getMonth()+1)+"/"+(timestamp.getYear()%100) +"");

                }
                holder.statusImg.setImageResource(R.drawable.played_icon);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {



                        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                            return;
                        }

                        logFirebaseEvent("dist_selected", "dist_name", distModel.getName());
                        logFirebaseEvent("dist_selected", "is_it_repeat", 1);
                        logFirebaseEvent("dist_selected", "order_from_top", i+1);

                        mLastClickTime = SystemClock.elapsedRealtime();

                        Intent intent = new Intent(context, PlaceOrderActivity.class);
                        intent.putExtra("dist", distModel);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                        context.startActivity(intent);
                        activity.overridePendingTransition(R.anim.enter_right, android.R.anim.fade_out);


                    }
                });

            } else if (key.equals("sent")) {

                Timestamp timestamp = new Timestamp(object.getLong("sent"));
                Timestamp today = new Timestamp(new Date().getTime());

                if(today.getYear()==timestamp.getYear()){
                    if( ((today.getMonth()-timestamp.getMonth()) + (today.getDate()-timestamp.getDate()) == 0)){
                        if(timestamp.getMinutes()<10){
                            holder.time.setText(timestamp.getHours() + ":0" + timestamp.getMinutes());

                        }else {
                            holder.time.setText(timestamp.getHours() + ":" + timestamp.getMinutes());
                        }
                    }else if (  ((today.getMonth()-timestamp.getMonth()) + (today.getDate()-timestamp.getDate()) == 1)){
                        holder.time.setText("Yesterday");
                    }else {
                        holder.time.setText(timestamp.getDate()+"/"+(timestamp.getMonth()+1)+"/"+(timestamp.getYear()%100) +"");

                    }
                }else{

                    holder.time.setText(timestamp.getDate()+"/"+(timestamp.getMonth()+1)+"/"+(timestamp.getYear()%100) +"");

                }


                holder.statusImg.setImageResource(R.drawable.sent_icon);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                            return;
                        }
                        mLastClickTime = SystemClock.elapsedRealtime();
                        logFirebaseEvent("dist_selected", "dist_name", distModel.getName());
                        logFirebaseEvent("dist_selected", "is_it_repeat", 1);
                        logFirebaseEvent("dist_selected", "order_from_top", i+1);

                        Intent intent = new Intent(context, PlaceOrderActivity.class);
                        intent.putExtra("dist", distModel);

                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                        activity.overridePendingTransition(R.anim.enter_right, android.R.anim.fade_out);

                    }
                });
            }
        }


    }


    @Override
    public int getItemCount() {
        return distModelArrayList.size();
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


    private void logFirebaseEvent2(String eventName, String paramName ,String paramValue, String paramName2 ,int paramValue2 )
    {
        Bundle params = new Bundle();
        params.putString(paramName, paramValue);
        params.putInt(paramName2, paramValue2);
        mFirebaseAnalytics.logEvent(eventName, params);
    }


}
