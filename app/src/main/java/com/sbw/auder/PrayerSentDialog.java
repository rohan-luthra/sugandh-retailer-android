package com.sbw.auder;

import android.app.Activity;
import android.app.Dialog;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.CountDownTimer;
import android.support.animation.DynamicAnimation;
import android.support.animation.SpringAnimation;
import android.support.animation.SpringForce;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Objects;

import static android.support.animation.SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY;
import static android.support.animation.SpringForce.STIFFNESS_LOW;

public class PrayerSentDialog {
    public void showDialog(Activity activity)
    {
        final Dialog dialog= new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.prayer_sent_dialog);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        final ImageView lotus = dialog.findViewById(R.id.prayer_sent_lotus);
        lotus.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.rotate_lotus));
        final TextView textView = dialog.findViewById(R.id.prayer_sent);
        final RelativeLayout linearLayout= dialog.findViewById(R.id.prayer_sent_layout);

        dialog.show();

        new CountDownTimer(1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {



            }

            @Override
            public void onFinish() {



                getHighBounceScaleX(lotus,1f,1f,DAMPING_RATIO_MEDIUM_BOUNCY, STIFFNESS_LOW);
                getHighBounceScaleY(lotus,1f,1f,DAMPING_RATIO_MEDIUM_BOUNCY, STIFFNESS_LOW);
                getHighBounceScaleX(textView,1f,1f,DAMPING_RATIO_MEDIUM_BOUNCY, STIFFNESS_LOW);
                getHighBounceScaleY(textView,1f,1f,DAMPING_RATIO_MEDIUM_BOUNCY, STIFFNESS_LOW);


            }
        }.start();

        new CountDownTimer(2000, 2000) {
            @Override
            public void onTick(long millisUntilFinished) {


                try{
                    dialog.getWindow().setWindowAnimations(R.style.fadeout);

                }catch (Exception e)
                {

                }
            }

            @Override
            public void onFinish() {
                try{
                    dialog.dismiss();

                }catch (Exception e)
                {

                }
            }
        }.start();
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
                Resources.getSystem().getDisplayMetrics());
    }

    private SpringAnimation getHighBounceScaleX(View view, float velocityDp, float finalPosition, float DAMPING, float STIFFNESS) {
        final SpringAnimation anim = new SpringAnimation(view, DynamicAnimation.SCALE_X);
        anim.setStartVelocity(getVelocity(velocityDp));
        anim.animateToFinalPosition(finalPosition);

        anim.setSpring(getSpringForce(DAMPING, STIFFNESS, finalPosition));
        return anim;
    }

    private SpringAnimation getHighBounceScaleY(View view, float velocityDp, float finalPosition, float DAMPING, float STIFFNESS) {
        final SpringAnimation anim = new SpringAnimation(view, DynamicAnimation.SCALE_Y);
        anim.setStartVelocity(getVelocity(velocityDp));
        anim.animateToFinalPosition(finalPosition);

        anim.setSpring(getSpringForce(DAMPING, STIFFNESS, finalPosition));
        return anim;
    }




}
