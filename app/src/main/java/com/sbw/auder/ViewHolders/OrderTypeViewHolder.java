package com.sbw.auder.ViewHolders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import com.sbw.auder.R;
import com.sbw.auder.RecyclerViewClickListener;

public class OrderTypeViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public Button orderType;
    RecyclerViewClickListener itemListener;



    public OrderTypeViewHolder(View itemView, RecyclerViewClickListener itemListener) {
        super(itemView);
        orderType = itemView.findViewById(R.id.orderType);
        this.itemListener = itemListener;
        orderType.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        itemListener.recyclerViewListClicked( v, getAdapterPosition());

    }

}
