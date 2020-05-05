package com.sbw.auder.ViewHolders;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.sbw.auder.R;


public class OrdersViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView id, dist, time_receipt, timer,audioRec, schTime, recText, timestamp_order,noImageText;

    public ImageView statusImg, imageRec, double_tick, scheduled, recieptcorner, noorder;
    public ImageView playAuido,recAudio;
    public SeekBar seekBar;

    public int mediaFileLength;
    public int realTimeLength;
    public String url,audioUri,rcptAdudioUri;

    public ProgressBar progressBar1, progressBar2;
    //    public AudioStreaming audioStreamingCustomFont, recAudioStreamingCustomFont;
    public RelativeLayout relativeLayout, userOrederLayout;



    public OrdersViewHolder(@NonNull View itemView) {
        super(itemView);
        id = itemView.findViewById(R.id.id);
//        dist = itemView.findViewById(R.id.name);
//        time = itemView.findViewById(R.id.time);
//        statusImg = itemView.findViewById(R.id.statusImg);
        playAuido =  itemView.findViewById(R.id.playAudio);
        timer = itemView.findViewById(R.id.timer);
        seekBar = itemView.findViewById(R.id.seekbar);
        audioRec = itemView.findViewById(R.id.audioRec);
        imageRec = itemView.findViewById(R.id.imageRec);

//        audioStreamingCustomFont = (AudioStreaming) itemView.findViewById(R.id.playCustomFonts);
//        recAudioStreamingCustomFont = (AudioStreaming) itemView.findViewById(R.id.recplayCustomFonts);
        statusImg= itemView.findViewById(R.id.userImage);
        recAudio = itemView.findViewById(R.id.recAudio);
        progressBar1 = itemView.findViewById(R.id.progressBar1);
        progressBar2 = itemView.findViewById(R.id.progressBar2);
        schTime= itemView.findViewById(R.id.schTime);
        relativeLayout=itemView.findViewById(R.id.recieptsLayout);
        userOrederLayout=itemView.findViewById(R.id.userOrderLayout);
        recText = itemView.findViewById(R.id.recText);
        timestamp_order=itemView.findViewById(R.id.timerstamp_order);
        double_tick=itemView.findViewById(R.id.doubletick);
        scheduled=itemView.findViewById(R.id.scheduled);
        time_receipt=itemView.findViewById(R.id.timerstamp_reciept);
        recieptcorner=itemView.findViewById(R.id.reciptcorner);
        noImageText =  itemView.findViewById(R.id.noImageText);


    }

    @Override
    public void onClick(View v) {

    }

}