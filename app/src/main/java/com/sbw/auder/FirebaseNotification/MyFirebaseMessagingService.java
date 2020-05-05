package com.sbw.auder.FirebaseNotification;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.sbw.auder.HomeScreen.MainActivity;
import com.sbw.auder.Models.DistModel;
import com.sbw.auder.Models.OrderModel;
import com.sbw.auder.PlaceOrder.PlaceOrderActivity;
import com.sbw.auder.PlayDistOrderActivity;
import com.sbw.auder.R;
import com.sbw.auder.SplashScreenActivity;
import com.sbw.auder.Utils.TinyDB;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private NotificationManager notifManager;

    private static final String TAG = "MyFirebaseMessagingServ";
    private final static String CHANNEL_ID = "notifications";
    private final static int NOTIFICATION_ID = 001;
    static int NOTIFY_ID = 0; // ID of notification

    String[] month = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d(TAG, "onMessageReceived: " + remoteMessage.getData());
        if (remoteMessage.getData() != null && !remoteMessage.getData().isEmpty()) {
            createNotification(remoteMessage);
        } else if (remoteMessage.getNotification() != null) {
            createDefaultNotification(remoteMessage);

        }

    }

    private void createDefaultNotification(RemoteMessage remoteMessage) {

        NOTIFY_ID++;
        String id = CHANNEL_ID; // default_channel_id

        String title = remoteMessage.getNotification().getTitle();
        String text = remoteMessage.getNotification().getBody();

        Intent intent = null;
        PendingIntent pendingIntent;
        final NotificationCompat.Builder builder;
        if (notifManager == null) {
            notifManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = notifManager.getNotificationChannel(id);
            if (mChannel == null) {
                mChannel = new NotificationChannel(id, title, importance);
                mChannel.enableVibration(true);
                mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                notifManager.createNotificationChannel(mChannel);
            }
            builder = new NotificationCompat.Builder(getApplicationContext(), id);
            intent = new Intent(getApplicationContext(), SplashScreenActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT);
            builder.setContentTitle(title)                            // required
                    .setSmallIcon(R.drawable.nandi_app_icon)   // required
                    .setContentText(text) // required
                    .setDefaults(Notification.DEFAULT_ALL).setAutoCancel(true).setContentIntent(pendingIntent).setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            builder.setStyle(new NotificationCompat.BigTextStyle().bigText(text));

            builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.nandi_icon_orange));
            Notification notification = builder.build();
            notifManager.notify(NOTIFY_ID, notification);

        } else {
            builder = new NotificationCompat.Builder(getApplicationContext(), id);

            intent = new Intent(getApplicationContext(), SplashScreenActivity.class);

            //            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT);
            builder.setContentTitle(title)                            // required
                    .setSmallIcon(R.drawable.nandi_app_icon)   // required
                    .setContentText(text) // required
                    .setDefaults(Notification.DEFAULT_ALL).setAutoCancel(true).setContentIntent(pendingIntent).setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400}).setPriority(Notification.PRIORITY_HIGH);
            builder.setStyle(new NotificationCompat.BigTextStyle().bigText(text));

            builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.nandi_icon_orange));
            Notification notification = builder.build();
            notifManager.notify(NOTIFY_ID, notification);


        }


    }

    @SuppressLint("CheckResult")
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotification(RemoteMessage remoteMessage) {


        NOTIFY_ID++;
        String id = CHANNEL_ID; // default_channel_id

        String title = remoteMessage.getData().get("title");
        String text = remoteMessage.getData().get("body");
        String imageUrl = remoteMessage.getData().get("icon");

        if (remoteMessage.getData().containsKey("schTimestamp")) {
            Long timestamp = Long.parseLong(remoteMessage.getData().get("schTimestamp"));
            Log.d(TAG, "createNotification: " + remoteMessage.getData().get("schTimestamp"));

            Date date = new Date(timestamp);
            text = text + "" + date.getHours() + ":" + ((date.getMinutes() < 10) ? "0" : "") + date.getMinutes() + " on " + date.getDate()
                    + "/" + (date.getMonth() + 1) + "/" + date.getYear() % 100;
        }

        Intent intent = null;
        PendingIntent pendingIntent;
        final NotificationCompat.Builder builder;
        if (notifManager == null) {
            notifManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = notifManager.getNotificationChannel(id);
            if (mChannel == null) {
                mChannel = new NotificationChannel(id, title, importance);
                mChannel.enableVibration(true);
                mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                notifManager.createNotificationChannel(mChannel);
            }
            builder = new NotificationCompat.Builder(getApplicationContext(), id);
            if(FirebaseAuth.getInstance().getCurrentUser()!=null) {

                if (remoteMessage.getData().containsKey("distData")) {

                    try {
                        DistModel distModel = getDistFromJson(new JSONObject(remoteMessage.getData().get("distData")));
                        ArrayList<DistModel> distModelArrayList = new ArrayList<>();
                        if (new TinyDB(this).getDistModelListObject("distList", DistModel.class)!=null)
                        {
                            distModelArrayList=new TinyDB(this).getDistModelListObject("distList", DistModel.class);
                            for (int i=0;i<distModelArrayList.size();i++)
                            {
                                if (distModelArrayList.get(i).getId().equals(distModel.getId()))
                                {

                                    intent = new Intent(getApplicationContext(), PlaceOrderActivity.class)
                                            .putExtra("dist", distModelArrayList.get(i))
                                            .putExtra("fromActivity", "notification");
                                }
                            }
                        }else {
                            intent = new Intent(getApplicationContext(), MainActivity.class)
                                    .putExtra("fromActivity", "notification");
                        }



                    } catch (JSONException e) {
                        e.printStackTrace();
                        intent = new Intent(getApplicationContext(), SplashScreenActivity.class);
                    }

                } else if (remoteMessage.getData().containsKey("orderData")) {

                    try {
                        Log.d(TAG, "createNotification: Order Notification");
                        OrderModel orderModel1 = getOrderModelfromJson(new JSONObject(remoteMessage.getData().get("orderData")));
                        intent = new Intent(getApplicationContext(), PlayDistOrderActivity.class)
                                .putExtra("order", orderModel1)
                                .putExtra("fromActivity", "notification");

                    } catch (JSONException e) {
                        e.printStackTrace();
                        intent = new Intent(getApplicationContext(), SplashScreenActivity.class);

                    }


                } else {
                    intent = new Intent(getApplicationContext(), SplashScreenActivity.class);
                }
            }else{
                intent = new Intent(getApplicationContext(), SplashScreenActivity.class);

            }
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT);
            builder.setContentTitle(title)                            // required
                    .setSmallIcon(R.drawable.nandi_app_icon)   // required
                    .setContentText(text) // required
                    .setDefaults(Notification.DEFAULT_ALL).setAutoCancel(true).setContentIntent(pendingIntent).setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            builder.setStyle(new NotificationCompat.BigTextStyle().bigText(text));

            if (imageUrl != null) {
                Glide.with(getApplicationContext()).asBitmap().load(imageUrl).listener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.nandi_icon_orange));
                        Notification notification = builder.build();
                        notifManager.notify(NOTIFY_ID, notification);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                        builder.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(resource));
                        Notification notification = builder.build();
                        notifManager.notify(NOTIFY_ID, notification);

                        return false;
                    }
                }).submit();
            } else {
                builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.nandi_icon_orange));
                Notification notification = builder.build();
                notifManager.notify(NOTIFY_ID, notification);
            }
        } else {
            builder = new NotificationCompat.Builder(getApplicationContext(), id);

            if(FirebaseAuth.getInstance().getCurrentUser()!=null) {

                if (remoteMessage.getData().containsKey("distData")) {

                    try {
                        DistModel distModel = getDistFromJson(new JSONObject(remoteMessage.getData().get("distData")));
                        intent = new Intent(getApplicationContext(), PlaceOrderActivity.class)
                                .putExtra("dist", distModel)
                                .putExtra("fromActivity", "notification");

                    } catch (JSONException e) {
                        e.printStackTrace();
                        intent = new Intent(getApplicationContext(), SplashScreenActivity.class);
                    }

                } else if (remoteMessage.getData().containsKey("orderData")) {

                    try {
                        Log.d(TAG, "createNotification: Order Notification");
                        OrderModel orderModel1 = getOrderModelfromJson(new JSONObject(remoteMessage.getData().get("orderData")));
                        intent = new Intent(getApplicationContext(), PlayDistOrderActivity.class)
                                .putExtra("order", orderModel1)
                                .putExtra("fromActivity", "notification");

                    } catch (JSONException e) {
                        e.printStackTrace();
                        intent = new Intent(getApplicationContext(), SplashScreenActivity.class);

                    }


                } else {
                    intent = new Intent(getApplicationContext(), SplashScreenActivity.class);
                }
            }else{
                intent = new Intent(getApplicationContext(), SplashScreenActivity.class);

            }
            //            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT);
            builder.setContentTitle(title)                            // required
                    .setSmallIcon(R.drawable.nandi_app_icon)   // required
                    .setContentText(text) // required
                    .setDefaults(Notification.DEFAULT_ALL).setAutoCancel(true).setContentIntent(pendingIntent).setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400}).setPriority(Notification.PRIORITY_HIGH);
            builder.setStyle(new NotificationCompat.BigTextStyle().bigText(text));
            if (imageUrl != null) {
                Glide.with(getApplicationContext()).asBitmap().load(imageUrl).listener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                        Log.d(TAG, "onLoadFailed: ");
                        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.nandi_icon_orange));
                        Notification notification = builder.build();
                        notifManager.notify(NOTIFY_ID, notification);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                        builder.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(resource));
                        Log.d(TAG, "onResourceReady: Loaded");
                        Notification notification = builder.build();
                        notifManager.notify(NOTIFY_ID, notification);

                        return false;
                    }
                }).submit();
            } else {
                builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.nandi_icon_orange));
                Notification notification = builder.build();
                notifManager.notify(NOTIFY_ID, notification);
            }


        }
    }


    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        Log.d(TAG, "onNewToken: " + s);
        Log.d(TAG, "onNewToken: " + FirebaseInstanceId.getInstance().getId());
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


        Log.d(TAG, "getOrderModelfromJson: " + orderModel);
        return orderModel;
    }

    private DistModel getDistFromJson(JSONObject dist) {

        DistModel distModel;

        try {
            distModel = new DistModel();
            distModel.setId(dist.getString("_id"));
            distModel.setName(dist.getString("name"));
            distModel.setTimestamp(dist.getString("timestamp"));
            distModel.setLocation(dist.getString("location"));
            if (dist.has("profileImageUrl")) {
                distModel.setProfileUrl(dist.getString("profileImageUrl"));
            }
//                                    if (dist.has("introVideoUrl")) {
//                                        distModel.setIntoVideoUrl(dist.getString("introVideoUrl"));
//                                    }
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
                    Log.d(TAG, "init: " + tempStrings);
                    distModel.setImageUrls(tempStrings);

                }
            }

            if (dist.has("priority")) {
                distModel.setPriority(dist.getInt("priority"));
            }
            return distModel;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}