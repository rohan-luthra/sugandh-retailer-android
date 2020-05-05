package com.sbw.auder.PlaceOrder;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Point;
import android.hardware.Camera;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.support.animation.DynamicAnimation;
import android.support.animation.SpringAnimation;
import android.support.animation.SpringForce;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.util.TypedValue;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.camerakit.CameraKitView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.sbw.auder.CameraPreview.CameraPreview;
import com.sbw.auder.HomeScreen.MainActivity;
import com.sbw.auder.Models.DistModel;
import com.sbw.auder.R;
import com.sbw.auder.Utils.TinyDB;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static android.support.animation.SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY;
import static android.support.animation.SpringForce.STIFFNESS_MEDIUM;


@SuppressWarnings("ALL")
public class PlaceOrderNextActivity extends AppCompatActivity {
    private static final int PICK_IMAGE = 98;
    private static final int REQUEST_STORAGE_PERMISSION = 100;

    ImageView skip,
            bottombar,
            uploadImage,
            btnCapture,
            switchCamera;

    DistModel distModel;

    SurfaceView surfaceView;
    String PERMISSION[] = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};


    TinyDB tinyDB;
    static int FLAG_CAMERA = 2;

    private static final String TAG = "PlaceOrderNextActivity";

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private int CAMERA_FACE = 0;

    private String cameraId;

    private Camera mCamera;
    private CameraPreview mCameraPreview;
    private Size imageDimension;
    private ImageReader imageReader;

    //Save to FILE
    private File file;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private boolean mFlashSupported;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;

    FrameLayout frameLayout;
    int flag = 0;
    private FirebaseAnalytics mFirebaseAnalytics;
    private boolean testing = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_order_next);

        uploadImage = findViewById(R.id.uploadImage);
        skip = findViewById(R.id.skip);
        btnCapture = findViewById(R.id.btnCapture);
        switchCamera = findViewById(R.id.switchCamera);
        frameLayout = findViewById(R.id.cameraPreview);
        bottombar = findViewById(R.id.bottombar);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        getRotation(this);

//        if (testing) {
//
//
//            tourGuide = TourGuide.init(this);
//            tourGuide.setTechnique(TourGuide.Technique.CLICK);
//            userGuide("Here is where you can take a picture to send with your order. You can also select an image from your gallery.",
//                    0,
//                    findViewById(R.id.fortourguide));
//
//
//        }

        tinyDB = new TinyDB(getApplicationContext());

        distModel = getIntent().getParcelableExtra("dist");


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            startActivity(new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            FLAG_CAMERA = 1;
            if (tinyDB == null) {
                tinyDB.putInt("FLAG_CAMERA", 2);
            }
            //tinyDB.getInt("FLAG_CAMERA");
            setCamera();
            btnCapture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    if (testing) {
//                        tourGuide.cleanUp();
//                    }
                    mCamera.takePicture(myShutterCallback, myPictureCallback_RAW, mPicture);


                    btnCapture.setEnabled(false);
                    uploadImage.setEnabled(false);
                    skip.setEnabled(false);
                    shutterCam();
                    // takePicture();
                    //  getHighBounceScaleX(btnCapture, 0.2f, 1.2f).start();
                    // getHighBounceScaleY(btnCapture, 0.2f, 1.2f).start();
                    getHighBounceScaleX(btnCapture, 6f, 1f).start();
                    getHighBounceScaleY(btnCapture, 6f, 1f).start();

                    //mCamera.release();


                }
            });

            switchCamera.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    switchCamera();
                }
            });
        }


        uploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logFirebaseEvent("gallery_tapped", "", "");

//                new ImagePicker.Builder(PlaceOrderNextActivity.this).mode(ImagePicker.Mode.GALLERY).compressLevel(ImagePicker.ComperesLevel.MEDIUM).directory(Environment.getExternalStorageDirectory() + "/Recipts").extension(ImagePicker.Extension.JPG).scale(600, 600).allowMultipleImages(false).enableDebuggingMode(true).allowOnlineImages(true).build();
                Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(gallery, PICK_IMAGE);

            }
        });


        Log.d(TAG, "onCreate: " + getIntent().getStringExtra("orderTypeFees"));

        if (getIntent().getStringExtra("audioData") == null) {
          //  Toast.makeText(this, "Error Occured ", Toast.LENGTH_SHORT).show();
            finish();
            overridePendingTransition(R.anim.exit, android.R.anim.fade_out);

        }


        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                logFirebaseEvent("skip_tapped", "", "");
                startActivity(new Intent(PlaceOrderNextActivity.this, PlaceOrderFinalActivity.class).
                        putExtra("audioData", getIntent().getStringExtra("audioData")).
                        putExtra("distId", getIntent().getStringExtra("distId"))
                        .putExtra("dist", distModel)
                        .putExtra("orderType_id", getIntent().getStringExtra("orderType_id"))
                        .putExtra("orderType_fees", getIntent().getStringExtra("orderType_fees"))
                        .putExtra("OrderAudioLength", getIntent().getIntExtra("OrderAudioLength", 0)));
                overridePendingTransition(R.anim.enter_right, android.R.anim.fade_out);

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {

            Uri imageUri = data.getData();
            File file = new File(imageUri.getPath().toString());

            // logFirebaseEvent("gallery_image_used", "original_gallery_image_size",""+file.length());
//            Log.d(TAG, "onActivityResult: " + getPath(getApplicationContext(), imageUri));
            startActivity(new Intent(PlaceOrderNextActivity.this, PlaceOrderFinalActivity.class)
                    .putExtra("audioData", getIntent().getStringExtra("audioData"))
                    .putExtra("distId", getIntent().getStringExtra("distId"))
                    .putExtra("dist", distModel)
                    .putExtra("imagePath", getPath(getApplicationContext(), imageUri))
                    .putExtra("orderType_id", getIntent().getStringExtra("orderType_id"))
                    .putExtra("orderType_fees", getIntent().getStringExtra("orderType_fees"))
                    .putExtra("OrderAudioLength", getIntent().getIntExtra("OrderAudioLength", 0))
                    .putExtra("imageFrom", 0));//0-gallery, 1-camera
            overridePendingTransition(R.anim.enter_right, android.R.anim.fade_out);



        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

       try{
           if (requestCode == REQUEST_CAMERA_PERMISSION) {
               if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                   //btnCapture.setVisibility(View.GONE);
                   FLAG_CAMERA = 2;
                   setCamera();
                   btnCapture.setOnClickListener(new View.OnClickListener() {
                       @Override
                       public void onClick(View view) {

                           mCamera.takePicture(myShutterCallback, myPictureCallback_RAW, mPicture);
                           btnCapture.setEnabled(false);
                           uploadImage.setEnabled(false);
                           skip.setEnabled(false);
                           shutterCam();


                           getHighBounceScaleX(btnCapture, 6f, 1f).start();
                           getHighBounceScaleY(btnCapture, 6f, 1f).start();



                       }
                   });
                   switchCamera.setOnClickListener(new View.OnClickListener() {
                       @Override
                       public void onClick(View v) {
                           switchCamera();
                       }
                   });
               }else{
                   Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                   final AlertDialog.Builder alertDialog= new AlertDialog.Builder(this);
                   alertDialog.setMessage(getResources().getString(R.string.place_order_camera_permission_denied));
                   alertDialog.setPositiveButton("OKAY", new DialogInterface.OnClickListener() {
                       @Override
                       public void onClick(DialogInterface dialog, int which) {
                           ActivityCompat.requestPermissions(PlaceOrderNextActivity.this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                           dialog.dismiss();
                       }
                   });
                   alertDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                       @Override
                       public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                           if(i == KeyEvent.KEYCODE_BACK) {
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

       }catch (Exception e){

       }

    }


    @Override
    protected void onPause() {
        if (mCamera != null) {
            mCamera.release();

        }

        //stopBackgroundThread();
        super.onPause();
    }


    public static String getPath(Context context, Uri uri) {
        String result = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(uri, proj, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int column_index = cursor.getColumnIndexOrThrow(proj[0]);
                result = cursor.getString(column_index);
            }
            cursor.close();
        }
        if (result == null) {
            result = "Not found";
        }
        return result;
    }

    Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            final File pictureFile = getOutputMediaFile();
            //  logFirebaseEvent("picture_clicked", "original_image_size", ""+pictureFile.length());
            if (pictureFile == null) {
                return;
            }
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                      //  Toast.makeText(PlaceOrderNextActivity.this, "Clicked" + pictureFile.getAbsoluteFile(), Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "run: " + pictureFile.getAbsolutePath());
                        startActivity(new Intent(PlaceOrderNextActivity.this, PlaceOrderFinalActivity.class)
                                .putExtra("audioData", getIntent().getStringExtra("audioData"))
                                .putExtra("distId", getIntent().getStringExtra("distId"))
                                .putExtra("dist", distModel)
                                .putExtra("imagePath", pictureFile.getAbsolutePath())
                                .putExtra("orderType_id", getIntent().getStringExtra("orderType_id"))
                                .putExtra("orderType_fees", getIntent().getStringExtra("orderType_fees"))
                                .putExtra("OrderAudioLength", getIntent().getIntExtra("OrderAudioLength", 0))
                                .putExtra("imageFrom", FLAG_CAMERA));//1-camera, 0-gallery
                        overridePendingTransition(R.anim.enter_right, android.R.anim.fade_out);

                    }
                });


            } catch (FileNotFoundException e) {

            } catch (IOException e) {
            }
        }
    };

    private static File getOutputMediaFile() {
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "MyCameraApp");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        File dcimDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        File picsDir = new File(dcimDir, "Nandi");
        picsDir.mkdirs(); //make if not exist
        File file = new File(picsDir, UUID.randomUUID().toString() + ".jpg");
        final String filePath = file.getAbsolutePath();



        return file;
    }

    int numberOfCamera;
    int camId;

    public void switchCamera() {


        numberOfCamera = Camera.getNumberOfCameras();
        if (FLAG_CAMERA == 1) {

            logFirebaseEvent("camera_switched", "to", 2);

            try {

                mCamera.release();

                FLAG_CAMERA = 2;
                tinyDB.putInt("FLAG_CAMERA", 2);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                    setCamera();
                } else {
                    recreate();
                }


            } catch (RuntimeException e) {
                Log.d(TAG, "switchCamera: " + e.getMessage());
           //     Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else if (FLAG_CAMERA == 2) {
            logFirebaseEvent("camera_switched", "to", 1);
            try {


                mCamera.release();
                FLAG_CAMERA = 1;
                tinyDB.putInt("FLAG_CAMERA", 1);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                    setCamera();
                } else {
                    recreate();
                }


            } catch (RuntimeException e) {
                Log.d(TAG, "switchCamera: " + e.getMessage());
           //     Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        }
    }

    public void setAutoFocus(Camera mCamera) {
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
        Camera.Size optimalSize = getOptimalPreviewSize(sizes, getResources().getDisplayMetrics().widthPixels, getResources().getDisplayMetrics().heightPixels);
        parameters.setPreviewSize(optimalSize.width, optimalSize.height);
        mCamera.setParameters(parameters);

    }

    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.05;
        double targetRatio = (double) 16 / 9;

        if (sizes == null) return null;

        Camera.Size optimalSize = null;

        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {


        int action = event.getAction();
        int keyCode = event.getKeyCode();
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (action == KeyEvent.ACTION_DOWN) {
                    try {
                        if (mCamera != null) {
                            shutterCam();
                            btnCapture.setEnabled(false);
                            uploadImage.setEnabled(false);
                            skip.setEnabled(false);
                            mCamera.takePicture(myShutterCallback, myPictureCallback_RAW, mPicture);
                        } else{}
                      //      Toast.makeText(this, "Camera returning null", Toast.LENGTH_SHORT).show();

                    } catch (Exception e) {
                        //Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (action == KeyEvent.ACTION_DOWN) {
                    try {
                        if (mCamera != null) {
                            shutterCam();
                            btnCapture.setEnabled(false);
                            uploadImage.setEnabled(false);
                            skip.setEnabled(false);
                            mCamera.takePicture(myShutterCallback, myPictureCallback_RAW, mPicture);
                        } else{}
                           // Toast.makeText(this, "Camera returning null", Toast.LENGTH_SHORT).show();

                    } catch (Exception e) {
                    }
                }
                return true;
            default:
                return super.dispatchKeyEvent(event);
        }

    }




    public void setCamera() {
        if (tinyDB.getInt("FLAG_CAMERA") == 1) {
            try {
                RelativeLayout.LayoutParams layoutPreviewParams =
                        (RelativeLayout.LayoutParams) frameLayout.getLayoutParams();

                frameLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        logFirebaseEvent("tried_zoom_or_focus", "", "");
                    }
                });

                int camId = Camera.CameraInfo.CAMERA_FACING_BACK;
                mCamera = Camera.open(camId);
                mCamera.setDisplayOrientation(90);
                Camera.Parameters parameters = mCamera.getParameters();
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                mCamera.setParameters(parameters);

                FLAG_CAMERA = 1;
                mCameraPreview = null;
                mCameraPreview = new CameraPreview(this, mCamera);
                frameLayout.addView(mCameraPreview);
            } catch (Exception e) {
              //  Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            try {

                int camId = Camera.CameraInfo.CAMERA_FACING_FRONT;
                mCamera = Camera.open(camId);
                FLAG_CAMERA = 2;

                mCameraPreview = null;
                mCameraPreview = new CameraPreview(this, mCamera);
                Point displayDim = getDisplayWH();
                Point layoutPreviewDim = calcCamPrevDimensions(displayDim,
                        mCameraPreview.getOptimalPreviewSize(mCameraPreview.mSupportedPreviewSizes,
                                displayDim.x, displayDim.y));

                if (layoutPreviewDim != null) {
                    RelativeLayout.LayoutParams layoutPreviewParams =
                            (RelativeLayout.LayoutParams) frameLayout.getLayoutParams();
                    layoutPreviewParams.width = displayDim.x;
                    layoutPreviewParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                    frameLayout.setLayoutParams(layoutPreviewParams);
                    frameLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            logFirebaseEvent("tried_zoom_or_focus", "", "");
                        }
                    });
                }


                frameLayout.addView(mCameraPreview);
            } catch (Exception e) {
             //   Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
    private Point getDisplayWH() {

        Display display = this.getWindowManager().getDefaultDisplay();
        Point displayWH = new Point();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            display.getSize(displayWH);
            return displayWH;
        }
        displayWH.set(display.getWidth(), display.getHeight());
        return displayWH;
    }

    private Point calcCamPrevDimensions(Point disDim, Camera.Size camDim) {

        Point displayDim = disDim;
        Camera.Size cameraDim = camDim;

        double widthRatio = (double) displayDim.x / cameraDim.width;
        double heightRatio = (double) displayDim.y / cameraDim.height;

        // use ">" to zoom preview full screen
        if (widthRatio < heightRatio) {
            Point calcDimensions = new Point();
            calcDimensions.x = displayDim.x;
            calcDimensions.y = (displayDim.x * cameraDim.height) / cameraDim.width;
            return calcDimensions;
        }
        // use "<" to zoom preview full screen
        if (widthRatio > heightRatio) {
            Point calcDimensions = new Point();
            calcDimensions.x = (displayDim.y * cameraDim.width) / cameraDim.height;
            calcDimensions.y = displayDim.y;
            return calcDimensions;
        }
        return null;
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

    private SpringAnimation getHighBounceScaleX(ImageView view, float velocityDp, float finalPosition) {
        final SpringAnimation anim = new SpringAnimation(view, DynamicAnimation.SCALE_X);
        anim.setStartVelocity(getVelocity(velocityDp));
        anim.animateToFinalPosition(finalPosition);

        anim.setSpring(getSpringForce(DAMPING_RATIO_MEDIUM_BOUNCY, STIFFNESS_MEDIUM, finalPosition));
        return anim;
    }

    private SpringAnimation getHighBounceScaleY(ImageView view, float velocityDp, float finalPosition) {
        final SpringAnimation anim = new SpringAnimation(view, DynamicAnimation.SCALE_Y);
        anim.setStartVelocity(getVelocity(velocityDp));
        anim.animateToFinalPosition(finalPosition);

        anim.setSpring(getSpringForce(DAMPING_RATIO_MEDIUM_BOUNCY, STIFFNESS_MEDIUM, finalPosition));
        return anim;
    }

    public void shutterCam() {
        CountDownTimer shutter = new CountDownTimer(300, 300) {
            @Override
            public void onTick(long millisUntilFinished) {
                frameLayout.setVisibility(View.GONE);
                switchCamera.setVisibility(View.GONE);
            }

            @Override
            public void onFinish() {

                frameLayout.setVisibility(View.VISIBLE);
                disableImage();


            }
        };
        shutter.start();
    }

    @Override
    protected void onResume() {
        switchCamera.setVisibility(View.VISIBLE);
        btnCapture.setEnabled(true);
        uploadImage.setEnabled(true);
        skip.setEnabled(true);
        enableImage();


        if (mCamera != null) {
            mCamera.release();
            setCamera();

        }
//        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
//        } else {
//            if (mCamera!=null)
//            {
//                recreate();
//            }
//        }
        super.onResume();
    }

    public void disableImage() {
        btnCapture.setImageResource(R.drawable.camera_button_pressed);
        uploadImage.setImageResource(R.drawable.gallery_button_pressed);
        skip.setImageResource(R.drawable.skip_button_pressed);
    }

    public void enableImage() {
        btnCapture.setImageResource(R.drawable.camera_button_bg);
        uploadImage.setImageResource(R.drawable.gallery_button_bg);
        skip.setImageResource(R.drawable.skip_button_bg);
    }
    public void getRotation(Context context) {


        final int rotation = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getOrientation();

        switch (rotation) {
            case Surface.ROTATION_0:
                Log.d(TAG, "getRotation: \"portrait\"");
                break;
            case Surface.ROTATION_90:
                Log.d(TAG, "getRotation: \"landscape\"");
                break;
            case Surface.ROTATION_180:
                Log.d(TAG, "getRotation: \"reverse portrait\"");
                break;
            default:
                Log.d(TAG, "getRotation: \"reverse landscape\"");
        }
    }

    Camera.ShutterCallback myShutterCallback = new Camera.ShutterCallback() {
        @Override
        public void onShutter() {
        }
    };

    Camera.PictureCallback myPictureCallback_RAW = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] arg0, Camera arg1) {
        }
    };

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

    @Override
    public void onBackPressed() {
        tinyDB.putInt("FLAG_CAMERA", 2);
        logFirebaseEvent("back_tapped", "", "");
        super.onBackPressed();
        overridePendingTransition(R.anim.exit, android.R.anim.fade_out);

    }

}
