package com.sbw.auder;

import android.animation.Animator;
import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.Objects;

public class ViewReceiptImage {

    public void showDialog(Activity activity, String imagePath){

        final Dialog dialog= new Dialog(activity, android.R.style.Theme_Black_NoTitleBar_Fullscreen);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setContentView(R.layout.view_receipt_image);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        ImageView receiptImage = dialog.findViewById(R.id.receiptImage);
        final ImageView dismissDialog = dialog.findViewById(R.id.dismissDialog);
        dismissDialog.setVisibility(View.GONE);
        RelativeLayout relativeLayout = dialog.findViewById(R.id.relativeLayout);

        relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        receiptImage.setImageURI(Uri.parse(imagePath));

        receiptImage.setScaleX(0);
        receiptImage.setScaleY(0f);

        dialog.show();
        receiptImage.animate().scaleX(1f).scaleY(1f).setDuration(200).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {

                dismissDialog.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        }).start();

        dismissDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }
}
