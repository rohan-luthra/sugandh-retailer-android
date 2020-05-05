package com.sbw.auder.Utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import com.sbw.auder.R;

public class FullScreenImageDialogBox extends Dialog {

    Uri uri;
    Bitmap bitmap;

    public FullScreenImageDialogBox(@NonNull Context context, Bitmap bitmap) {
        super(context);
        this.bitmap = bitmap;
    }

    public FullScreenImageDialogBox(@NonNull Context context, Uri uri) {

        super(context);
        this.uri = uri;
    }

    ImageView imageView, backBtn;

    public FullScreenImageDialogBox(@NonNull Context context) {
        super(context);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.full_screen_image_dialogbox);
        imageView = findViewById(R.id.image);
        backBtn = findViewById(R.id.backBtn);

        if(uri!=null){
            imageView.setImageURI(uri);
        }else{
            imageView.setImageBitmap(bitmap);
        }

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }
}
