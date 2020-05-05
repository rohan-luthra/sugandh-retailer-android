package com.sbw.auder.ViewHolders;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.sbw.auder.R;

public class DistViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView id, name, time, location;
    public ImageView statusImg, noorder;
    public ImageView distImage;
    private long mLastClickTime = System.currentTimeMillis();
    private static final long CLICK_TIME_INTERVAL = 300;

    public DistViewHolder(@NonNull View itemView) {
        super(itemView);
//        id = itemView.findViewById(R.id.id);
        time = itemView.findViewById(R.id.time);
        statusImg = itemView.findViewById(R.id.statusImg);
        name = itemView.findViewById(R.id.name);
        location = itemView.findViewById(R.id.location);
        distImage = itemView.findViewById(R.id.distImage);
        noorder = itemView.findViewById(R.id.noordericon);

    }

    @Override
    public void onClick(View v) {

    }
}