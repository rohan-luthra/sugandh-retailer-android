package com.sbw.auder.OrdersActivity;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.graphics.BitmapCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.dynamic.IFragmentWrapper;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.sbw.auder.Models.DistModel;
import com.sbw.auder.PlaceOrder.PlaceOrderActivity;
import com.sbw.auder.R;
import com.sbw.auder.Utils.TinyDB;
import com.smarteist.autoimageslider.DefaultSliderView;
import com.smarteist.autoimageslider.IndicatorAnimations;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderLayout;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.RandomAccess;

import kotlin.random.Random;

public class NoOrderActivity extends AppCompatActivity {

    private static final String TAG = "NoOrderActivity";
    private SliderLayout sliderLayout;
    ImageView placeOrder, placeOrder3,chooseLanguage;

    TextView info, placeOreder2;
    private FirebaseAnalytics mFirebaseAnalytics;
    TinyDB tinyDB;
    private int height=0;
    private DistModel distModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (new TinyDB(this).getString("Lang")!=null)
        {
            setLanguageForApp(new TinyDB(this).getString("Lang"), this);
        }
        setContentView(R.layout.activity_no_order);
        tinyDB = new TinyDB(getApplicationContext());

        chooseLanguage= findViewById(R.id.changeToHindi);
        info = findViewById(R.id.info);
        info.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction()== MotionEvent.ACTION_DOWN)
                {
                    logFirebaseEvent("info_scrolled", "", "");

                }
                return false;
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            info.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                @Override
                public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                    logFirebaseEvent("info_scrolled", "", "");
                }
            });
        }


        distModel = getIntent().getParcelableExtra("dist");
        Toolbar toolbar = (Toolbar) findViewById(R.id.tb_toolbarsearch2);
        setSupportActionBar(toolbar);

        mFirebaseAnalytics=FirebaseAnalytics.getInstance(this);
        TextView title=findViewById(R.id.titleofplaceorder);
        TextView loca = findViewById(R.id.location);
        title.setText(distModel.getName());
        loca.setText(distModel.getLocation());

        // add back arrow to toolbar
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // back button pressed
                onBackPressed();
            }
        });
        toolbar.setNavigationIcon(R.drawable.ic_cross_white);
        setTitle(distModel.getName());

        if(distModel.getDistInfo()!=null)
            setHTMLinfoText(distModel.getDistInfo());
        else
            setInfoText("Information coming soon...");

        sliderLayout = findViewById(R.id.imageSlider);
        sliderLayout.setIndicatorAnimation(IndicatorAnimations.SLIDE); //set indicator animation by using SliderLayout.IndicatorAnimations. :WORM or THIN_WORM or COLOR or DROP or FILL or NONE or SCALE or SCALE_DOWN or SLIDE and SWAP!!
        sliderLayout.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION);
        sliderLayout.setScrollTimeInSec(10);//set scroll delay in seconds :
        if(distModel.getImageUrls()!=null){
            setSliderViews(distModel.getImageUrls());
        }else{
            setSliderViews(null);

        }
        sliderLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logFirebaseEvent("info_images_scrolled", "", "");

            }
        });
        sliderLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    logFirebaseEvent("info_images_scrolled", "", "");

                }

                return false;
            }
        });



        placeOrder = findViewById(R.id.placeOrder);
        placeOreder2=findViewById(R.id.placeOrder2);
        placeOrder3=findViewById(R.id.placeOrder3);
        placeOreder2.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    placeOreder2.setTextColor(Color.parseColor("#80ffffff"));

                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    placeOreder2.setTextColor(getResources().getColor(R.color.grey_11));
                }
                return false;
            }
        });

        placeOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                logFirebaseEvent("info_place_order_tapped", "dist_name", distModel.getName());

                if (getIntent().getStringExtra("fromActivity") == null) {
                    finish();
                    overridePendingTransition(R.anim.enter_right, android.R.anim.fade_out);

                } else {

                    if( !getIntent().getStringExtra("fromActivity").equals("main")){
                        finish();
                        overridePendingTransition(R.anim.enter_right, android.R.anim.fade_out);

                    }else{
                        startActivity(new Intent(NoOrderActivity.this, PlaceOrderActivity.class)
                                .putExtra("dist_id", distModel.id)
                                .putExtra("dist", distModel));
                        overridePendingTransition(R.anim.enter_right, android.R.anim.fade_out);
                        finish();

                    }

                }

            }
        });
        placeOreder2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                logFirebaseEvent("info_place_order_tapped", "dist_name", distModel.getName());

                if (getIntent().getStringExtra("fromActivity") == null) {
                    finish();
                    overridePendingTransition(R.anim.enter_right, android.R.anim.fade_out);

                } else {

                    if( !getIntent().getStringExtra("fromActivity").equals("main")){
                        finish();
                        overridePendingTransition(R.anim.enter_right, android.R.anim.fade_out);

                    }else{
                        startActivity(new Intent(NoOrderActivity.this, PlaceOrderActivity.class)
                                .putExtra("dist_id", distModel.id)
                                .putExtra("dist", distModel));
                        overridePendingTransition(R.anim.enter_right, android.R.anim.fade_out);
                        finish();

                    }

                }
            }
        });
        placeOrder3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                logFirebaseEvent("info_place_order_tapped", "dist_name", distModel.getName());

                if (getIntent().getStringExtra("fromActivity") == null) {
                    finish();
                    overridePendingTransition(R.anim.enter_right, android.R.anim.fade_out);

                } else {

                    if( !getIntent().getStringExtra("fromActivity").equals("main")){
                        finish();
                        overridePendingTransition(R.anim.enter_right, android.R.anim.fade_out);

                    }else{
                        startActivity(new Intent(NoOrderActivity.this, PlaceOrderActivity.class)
                                .putExtra("dist_id", distModel.id)
                                .putExtra("dist", distModel));
                        overridePendingTransition(R.anim.enter_right, android.R.anim.fade_out);
                        finish();

                    }

                }

            }
        });
        chooseLanguage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logFirebaseEvent("read_in_hindi_tapped", "", "");
                scrollToLanguage();
            }
        });

    }

    private void setHTMLinfoText(String distInfo) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                info.setText(Html.fromHtml(distInfo, Html.FROM_HTML_MODE_LEGACY));
            } else {
                info.setText(Html.fromHtml(distInfo));
            }
        info.setMovementMethod(LinkMovementMethod.getInstance());

    }
    public void scrollToLanguage(){
        final ScrollView scrollView = findViewById(R.id.scrollInfo);
            scrollView.post(new Runnable() {
                @Override
                public void run() {
                    height= info.getLineCount();
                    //Toast.makeText(NoOrderActivity.this, ""+height, Toast.LENGTH_SHORT).show();
                    int y= info.getLayout().getLineForOffset(info.getText().toString().indexOf("----------"))+3;
                    y=info.getLayout().getLineTop(y);

                    //scrollView.smoothScrollTo(0, y);
                    ObjectAnimator.ofInt(scrollView, "scrollY",  y).setDuration(500).start();
                    //Toast.makeText(NoOrderActivity.this, ""+y, Toast.LENGTH_SHORT).show();
                }
            });

    }

    private void setSliderViews(final ArrayList<String> imageUrls) {

        final TinyDB tinyDB = new TinyDB(this);

        if (imageUrls!=null && imageUrls.size()>0)
        {


            if (tinyDB.getListString("SLIDER_"+distModel.getId())!=null && tinyDB.getListString("SLIDER_"+distModel.getId()).size()>0)
            {
                final ArrayList<String> bitmaps;
                bitmaps = tinyDB.getListString("SLIDER_"+distModel.getId());

                for (int i=0;i<bitmaps.size();i++)
                {
                    final int finalI = i;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            DefaultSliderView sliderView = new DefaultSliderView(getApplicationContext());
                            sliderView.setImageUrl(String.valueOf(bitmaps.get(finalI)));
                            sliderView.setImageScaleType(ImageView.ScaleType.CENTER_CROP);
                            //at last add this view in your layout :
                            sliderLayout.addSliderView(sliderView);
                        }
                    });
                }



            }else{


                final ArrayList<String> bitmaps2 = new ArrayList<>();


                for (final String s : imageUrls) {
                    Glide.with(this).asBitmap()
                            .load(getResources()
                                    .getString(R.string.localhost)
                                    + "/api/"+
                                    tinyDB.getString(getResources()
                                            .getString(R.string.authToken))
                                    +"/download/" +
                                    s)
                            .listener(new RequestListener<Bitmap>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                                    String filename = "SliderImage_" + distModel.getId()+ s;
                                    final String path = tinyDB.saveImage(resource, filename);
                                        tinyDB.setImagePath(filename, path);


                                    Log.d(TAG, "setSliderViews:byte array: ");

                                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                                    resource.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                                    String uriPath = String.valueOf(Uri.fromFile(new File(path)));



                                    bitmaps2.add(String.valueOf(Uri.parse(uriPath)));
                                    tinyDB.putListString("SLIDER_"+distModel.getId(), bitmaps2);
                                    return false;
                                }
                            }).submit();

                    DefaultSliderView sliderView = new DefaultSliderView(this);
                    sliderView.setImageUrl(getResources().getString(R.string.localhost) + "/api/"+ tinyDB.getString(getResources().getString(R.string.authToken))+"/download/" + s);
                    sliderView.setImageScaleType(ImageView.ScaleType.CENTER_CROP);
                    sliderLayout.addSliderView(sliderView);

                }


            }



        }else{
            DefaultSliderView sliderView = new DefaultSliderView(this);
            sliderView.setImageDrawable(R.drawable.india_gate_bg);
            sliderView.setImageScaleType(ImageView.ScaleType.CENTER_CROP);
            //at last add this view in your layout :
            sliderLayout.addSliderView(sliderView);
        }




    }

    private void checkIfNewImagesAddedOnServer() {
        if (distModel.getImageUrls()!=null)
        {
            ArrayList<String> imageUrls = distModel.getImageUrls();
            final ArrayList<String> bitmaps2= new ArrayList<>();


            for (final String s : imageUrls) {
                Glide.with(this).asBitmap()
                        .load(getResources()
                                .getString(R.string.localhost)
                                + "/api/"+
                                tinyDB.getString(getResources()
                                        .getString(R.string.authToken))
                                +"/download/" +
                                s)
                        .listener(new RequestListener<Bitmap>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {

                                String filename = "SliderImage_" + distModel.getId()+ s;
                                final String path = tinyDB.saveImage(resource, filename);
                                tinyDB.setImagePath(filename, path);

                                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                                resource.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                                String uriPath = MediaStore.Images.Media.insertImage(getApplicationContext().getContentResolver(), resource, "Title", null);

                                bitmaps2.add(String.valueOf(Uri.parse(uriPath)));
                                return false;
                            }
                        }).submit();

            }

            if (tinyDB.getListString("SLIDER_"+distModel.getId())!=bitmaps2)
            {
                tinyDB.putListString("SLIDER_"+distModel.getId(), bitmaps2);
            }
        }

    }

    private void setInfoText(String info) {


        StringBuilder infoBuilder = new StringBuilder(info);
        SpannableStringBuilder sb = new SpannableStringBuilder(infoBuilder);

        int start = infoBuilder.indexOf("*");
        while (start >= 0) {

            int end = infoBuilder.indexOf("*", start + 1);
            if (end >= 0) {
                Log.d(TAG, "setInfoText: " + start + " " + end);
                StyleSpan bss = new StyleSpan(android.graphics.Typeface.BOLD); // Span to make text bold
                sb.setSpan(bss, start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                sb.setSpan(new AbsoluteSizeSpan(22, true), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                if (start < sb.length()) {
                    sb.delete(start, start + 1);
                    infoBuilder.delete(start, start + 1);
                }
                if (end - 1 < sb.length()) {
                    sb.delete(end - 1, end);
                    infoBuilder.delete(end - 1, end);
                }
                start = infoBuilder.indexOf("*", end + 1);

            } else {
//                sb.replace(start, start+1, "");
                start = -1;
            }

        }


        this.info.setText(sb);


    }

    private void logFirebaseEvent(String eventName, String paramName, String paramValue) {
        Bundle params = new Bundle();
        params.putString(paramName, paramValue);
        mFirebaseAnalytics.logEvent(eventName, params);
    }

    @Override
    public void onBackPressed() {
        logFirebaseEvent("info_back_tapped", "", "");
        super.onBackPressed();
        overridePendingTransition(R.anim.enter_right, android.R.anim.fade_out);


    }
    public Bitmap getBitmap(String path) {
        try {
            Bitmap bitmap=null;
            File f= new File(path);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;

            bitmap = BitmapFactory.decodeStream(new FileInputStream(f), null, options);
            Log.d(TAG, "getBitmap: "+bitmap);


            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "getBitmap: "+e);

            return null;
        }}


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