package com.sbw.auder.OrdersActivity;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.sbw.auder.R;

public class FullScreenImageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);

        String path = getIntent().getStringExtra("path");

        final ImageView imageView = findViewById(R.id.imageView);
        imageView.setImageURI(Uri.parse(path));

        if(getIntent().getStringExtra("from").equals("user")){
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            Log.d("TAG", "onCreate: "+imageView.getWidth());
        }else if(getIntent().getStringExtra("from").equals("recp")){
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            Log.d("TAG", "onCreate: "+imageView.getWidth());

        }

        findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("TAG", "onCreate: "+imageView.getWidth());
              //  Toast.makeText(getApplicationContext(), ""+imageView.getWidth(), Toast.LENGTH_SHORT).show();
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
