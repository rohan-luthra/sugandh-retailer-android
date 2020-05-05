package com.sbw.auder;

import android.app.Activity;
import android.app.Dialog;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.sbw.auder.Utils.TinyDB;

import java.util.Locale;
import java.util.Objects;

public class ChooseLanguage {



    public void showDialog(final Activity activity){

        final Dialog dialog= new Dialog(activity);

        final TinyDB tinyDB= new TinyDB(activity);
        if (tinyDB.getString("Lang")==null)
        {
            tinyDB.putString("Lang", "en");
        }
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setContentView(R.layout.choose_language);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        final TextView saveLang = dialog.findViewById(R.id.saveLang);
        final String[] choice = new String[1];


        RadioGroup languageGroup = dialog.findViewById(R.id.chooseLang);
        languageGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {

            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                // This will get the radiobutton that has changed in its check state
                RadioButton checkedRadioButton = (RadioButton)group.findViewById(checkedId);
                // This puts the value (true/false) into the variable
                boolean isChecked = checkedRadioButton.isChecked();
                // If the radiobutton that has changed in check state is now checked...
                if (isChecked)
                {
                    // Changes the textview's text to "Checked: example radiobutton text"
                    choice[0] =String.valueOf(checkedRadioButton.getText());
                }
            }

        });
        dialog.show();
        saveLang.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    saveLang.setTextColor(Color.parseColor("#80ffffff"));

                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    saveLang.setTextColor(activity.getResources().getColor(R.color.grey_11));
                }
                return false;
            }
        });
        saveLang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (choice[0]!=null)
                {
                    if (choice[0].equals("English"))
                    {
                        setLanguageForApp("en", activity);
                        tinyDB.putString("Lang", "en");
                        try{
                            dialog.dismiss();

                        }catch (Exception e)
                        {

                        }
                        activity.recreate();
                    }else if(choice[0].equals("हिंदी")){
                        setLanguageForApp("hi", activity);
                        tinyDB.putString("Lang", "hi");
                        try{
                            dialog.dismiss();

                        }catch (Exception e)
                        {

                        }
                        activity.recreate();
                    }
                }else{
                    Toast.makeText(activity, activity.getResources().getString(R.string.please_choose_language), Toast.LENGTH_SHORT).show();
                }

            }
        });


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
