package com.sbw.auder.Camera2Kit;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.animation.DynamicAnimation;
import android.support.animation.SpringAnimation;
import android.support.animation.SpringForce;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.sbw.auder.PlaceOrder.PlaceOrderFinalActivity;
import com.sbw.auder.R;
import com.sbw.auder.Utils.TinyDB;

import java.util.Locale;

import static android.support.animation.SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY;
import static android.support.animation.SpringForce.STIFFNESS_MEDIUM;
import static com.sbw.auder.PlaceOrder.PlaceOrderNextActivity.getPath;

public class MainActivityCamera extends AppCompatActivity
    implements View.OnClickListener, AspectRatioFragment.AspectRatioListener {

    private static final String FRAGMENT_DIALOG = "aspect_dialog";
    private static final int PICK_IMAGE = 98;


    private static final int[] FLASH_OPTIONS = {
        CameraConstants.FLASH_AUTO,
        CameraConstants.FLASH_OFF,
        CameraConstants.FLASH_ON,
    };

    private static final int[] FLASH_ICONS = {
        R.drawable.ic_flash_auto,
        R.drawable.ic_flash_off,
        R.drawable.ic_flash_on,
    };

    private static final int[] FLASH_TITLES = {
        R.string.flash_auto,
        R.string.flash_off,
        R.string.flash_on,
    };

    private int mCurrentFlashIndex;


    private Camera2Fragment mCamera2Fragment;

    /**
     * The button of record video
     */


    /**
     * The button of take picture
     */
    private ImageView mPictureButton;
    private boolean canClick = true;
    private boolean enableMenuItems = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // setFullScreen();
        if (new TinyDB(this).getString("Lang")!=null)
        {
            setLanguageForApp(new TinyDB(this).getString("Lang"), this);
        }
        setContentView(R.layout.activity_main_camera);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setTitle("");
        toolbar.setTitleTextAppearance(this,R.style.toolbarText2);
        setSupportActionBar(toolbar);
        if (null == savedInstanceState) {
            mCamera2Fragment = Camera2Fragment.newInstance();
            getFragmentManager().beginTransaction()
                .replace(R.id.container, mCamera2Fragment)
                .commit();
        } else {
            mCamera2Fragment = (Camera2Fragment) getFragmentManager().findFragmentById(R.id.container);
        }

        mPictureButton =  findViewById(R.id.picture);
        mPictureButton.setOnClickListener(this);
        Button skipButton=findViewById(R.id.skip);

        setOnTouchListnerToView(skipButton);

        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), PlaceOrderFinalActivity.class).
                        putExtra("audioData", getIntent().getStringExtra("audioData")).
                        putExtra("distId", getIntent().getStringExtra("distId"))
                        .putExtra("dist", getIntent().getParcelableExtra("dist"))
                        .putExtra("orderType_id", getIntent().getStringExtra("orderType_id"))
                        .putExtra("orderType_fees", getIntent().getStringExtra("orderType_fees"))
                        .putExtra("OrderAudioLength", getIntent().getIntExtra("OrderAudioLength", 0)));
                overridePendingTransition(R.anim.enter_right, android.R.anim.fade_out);

            }
        });
        findViewById(R.id.uploadImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(gallery, PICK_IMAGE);

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {

            Uri imageUri = data.getData();

            startActivity(new Intent(this, PlaceOrderFinalActivity.class)
                    .putExtra("audioData", getIntent().getStringExtra("audioData"))
                    .putExtra("distId", getIntent().getStringExtra("distId"))
                    .putExtra("dist", getIntent().getParcelableExtra("dist"))
                    .putExtra("imagePath", getPath(getApplicationContext(), imageUri))
                    .putExtra("orderType_id", getIntent().getStringExtra("orderType_id"))
                    .putExtra("orderType_fees", getIntent().getStringExtra("orderType_fees"))
                    .putExtra("OrderAudioLength", getIntent().getIntExtra("OrderAudioLength", 0))
                    .putExtra("imageFrom", 0));//0-gallery, 1-camera

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {

                recreate();

            }

            //recreate();

            overridePendingTransition(R.anim.enter_right, android.R.anim.fade_out);



        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu_camera, menu);
        if (enableMenuItems)
        {
            for (int i = 0; i < menu.size(); i++)
                menu.getItem(i).setVisible(true);
            return true;

        }else{
            for (int i = 0; i < menu.size(); i++)
                menu.getItem(i).setVisible(false);
            return false;

        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.switch_flash:
                mCurrentFlashIndex = (mCurrentFlashIndex + 1) % FLASH_OPTIONS.length;
                item.setTitle(FLASH_TITLES[mCurrentFlashIndex]);
                item.setIcon(FLASH_ICONS[mCurrentFlashIndex]);
                mCamera2Fragment.setFlash(FLASH_OPTIONS[mCurrentFlashIndex]);
                return true;
            case R.id.switch_camera:
                int facing = mCamera2Fragment.getFacing();
                mCamera2Fragment.setFacing(facing == CameraConstants.FACING_FRONT ?
                    CameraConstants.FACING_BACK : CameraConstants.FACING_FRONT);

                invalidateOptionsMenu();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {


        boolean flash=mCamera2Fragment.isFlashSupported();

        menu.findItem(R.id.switch_camera)
                .setVisible(mCamera2Fragment.isFacingSupported());

        menu.findItem(R.id.switch_flash)
                .setVisible(flash)
                .setTitle(FLASH_TITLES[mCurrentFlashIndex])
                .setIcon(FLASH_ICONS[mCurrentFlashIndex]);


        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.picture: {
                getHighBounceScaleX(findViewById(R.id.picture), 6f, 1f).start();
                getHighBounceScaleY(findViewById(R.id.picture), 6f, 1f).start();
                mCamera2Fragment.takePicture();
                findViewById(R.id.picture).setEnabled(false);
                findViewById(R.id.skip).setEnabled(false);
                findViewById(R.id.uploadImage).setEnabled(false);
                enableMenuItems = false;
                invalidateOptionsMenu();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);

                        }catch (Exception e){}

                    }
                }, 1000);
                break;
            }

        }
    }

    @Override
    public void onAspectRatioSelected(@NonNull AspectRatio ratio) {
        Toast.makeText(this, ratio.toString(), Toast.LENGTH_SHORT).show();
        mCamera2Fragment.setAspectRatio(ratio);
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

    private SpringAnimation getHighBounceScaleX(View view, float velocityDp, float finalPosition) {
        final SpringAnimation anim = new SpringAnimation(view, DynamicAnimation.SCALE_X);
        anim.setStartVelocity(getVelocity(velocityDp));
        anim.animateToFinalPosition(finalPosition);

        anim.setSpring(getSpringForce(DAMPING_RATIO_MEDIUM_BOUNCY, STIFFNESS_MEDIUM, finalPosition));
        return anim;
    }

    private SpringAnimation getHighBounceScaleY(View view, float velocityDp, float finalPosition) {
        final SpringAnimation anim = new SpringAnimation(view, DynamicAnimation.SCALE_Y);
        anim.setStartVelocity(getVelocity(velocityDp));
        anim.animateToFinalPosition(finalPosition);

        anim.setSpring(getSpringForce(DAMPING_RATIO_MEDIUM_BOUNCY, STIFFNESS_MEDIUM, finalPosition));
        return anim;
    }

    @Override
    protected void onResume() {
        findViewById(R.id.picture).setEnabled(true);
        findViewById(R.id.skip).setEnabled(true);
        findViewById(R.id.uploadImage).setEnabled(true);
        canClick=true;
        enableMenuItems = true;
        invalidateOptionsMenu();
        findViewById(R.id.progressBar).setVisibility(View.GONE);

        super.onResume();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {


        int action = event.getAction();
        int keyCode = event.getKeyCode();
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (action == KeyEvent.ACTION_DOWN) {
                    try {
                        if (canClick)
                        {
                            getHighBounceScaleX(findViewById(R.id.picture), 6f, 1f).start();
                            getHighBounceScaleY(findViewById(R.id.picture), 6f, 1f).start();
                            mCamera2Fragment.takePicture();
                            findViewById(R.id.picture).setEnabled(false);
                            findViewById(R.id.skip).setEnabled(false);
                            findViewById(R.id.uploadImage).setEnabled(false);
                            canClick=false;
                        }


                    } catch (Exception e) {
                    }
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (action == KeyEvent.ACTION_DOWN) {
                    try {

                        if (canClick)
                        {
                            getHighBounceScaleX(findViewById(R.id.picture), 6f, 1f).start();
                            getHighBounceScaleY(findViewById(R.id.picture), 6f, 1f).start();
                            mCamera2Fragment.takePicture();
                            findViewById(R.id.picture).setEnabled(false);
                            findViewById(R.id.skip).setEnabled(false);
                            findViewById(R.id.uploadImage).setEnabled(false);
                            canClick= false;
                        }

                    } catch (Exception e) {
                    }
                }
                return true;
            default:
                return super.dispatchKeyEvent(event);
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
    private void setOnTouchListnerToView(final Button target)
    {
        target.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    target.setTextColor(Color.parseColor("#80ffffff"));

                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    target.setTextColor(getResources().getColor(R.color.grey_11));
                }
                return false;
            }
        });
    }


}
