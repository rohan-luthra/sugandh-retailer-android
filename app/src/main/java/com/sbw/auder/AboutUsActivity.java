package com.sbw.auder;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.location.LocationServices;
import com.sbw.auder.Utils.TinyDB;

import java.util.Locale;

public class AboutUsActivity extends AppCompatActivity {

    TextView aboutUs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (new TinyDB(this).getString("Lang")!=null)
        {
            setLanguageForApp(new TinyDB(this).getString("Lang"), this);
        }
        setContentView(R.layout.activity_about_us);
        aboutUs=findViewById(R.id.aboutUs);

        Toolbar toolbar = (Toolbar) findViewById(R.id.tb_toolbarsearch);
        setSupportActionBar(toolbar);

        setTitle(getResources().getString(R.string.aboutNandi));
        toolbar.setTitleTextAppearance(this, R.style.toolbarText2);
        // add back arrow to toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // back button pressed
                finish();
                overridePendingTransition(android.R.anim.fade_in, R.anim.slide_down);

            }
        });
        toolbar.setNavigationIcon(R.drawable.ic_cross_white);
        //setInfoText(getResources().getString(R.string.about_us));
        setHTMLaboutUsText();

    }
    private void setHTMLaboutUsText() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            aboutUs.setText(Html.fromHtml(getResources().getString(R.string.about_us), Html.FROM_HTML_MODE_LEGACY));
        } else {
            aboutUs.setText(Html.fromHtml(getResources().getString(R.string.about_us)));
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, R.anim.slide_down);
    }
    private void setInfoText(String info) {


        StringBuilder infoBuilder = new StringBuilder(info);
        SpannableStringBuilder sb = new SpannableStringBuilder(infoBuilder);

        int start = infoBuilder.indexOf("*");
        while (start >= 0) {

            int end = infoBuilder.indexOf("*", start + 1);
            if (end >= 0) {
                StyleSpan bss = new StyleSpan(android.graphics.Typeface.BOLD); // Span to make text bold
                sb.setSpan(bss, start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                sb.setSpan(new AbsoluteSizeSpan(18, true), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
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


        this.aboutUs.setText(sb);


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
}
