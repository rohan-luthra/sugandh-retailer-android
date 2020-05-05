package com.sbw.auder.Utils;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.sbw.auder.R;

public class UploadGalleryImageDialogBox extends Dialog {

    String path;
    ImageView imageView,back;
    TextView next;

    public UploadGalleryImageDialogBox(Activity activity, String path) {
        super(activity, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        this.path = path;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.upload_from_gallery_dialogbox);

        imageView = findViewById(R.id.image);
        next = findViewById(R.id.next);
        back = findViewById(R.id.back);

        Bitmap bitmap = BitmapFactory.decodeFile(path);
        imageView.setImageBitmap(bitmap);


//        Bitmap bitmap = new Bitmap()
    }
}
