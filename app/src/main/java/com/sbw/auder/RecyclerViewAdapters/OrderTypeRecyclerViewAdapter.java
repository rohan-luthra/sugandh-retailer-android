package com.sbw.auder.RecyclerViewAdapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sbw.auder.Models.OrderTypeModel;
import com.sbw.auder.ViewHolders.OrderTypeViewHolder;
import com.sbw.auder.R;
import com.sbw.auder.RecyclerViewClickListener;

import java.util.ArrayList;

public class OrderTypeRecyclerViewAdapter extends RecyclerView.Adapter<OrderTypeViewHolder> {


    Context context;
    ArrayList<OrderTypeModel> orderModelArrayList;
    RecyclerViewClickListener itemListener;

    int selected = -1;


    public OrderTypeRecyclerViewAdapter(Context context, ArrayList<OrderTypeModel> orderModelArrayList, RecyclerViewClickListener itemListener) {
        this.context = context;
        this.orderModelArrayList = orderModelArrayList;
        this.itemListener = itemListener;
    }


    @NonNull
    @Override
    public OrderTypeViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.cardview_ordertype, viewGroup, false);
        return new OrderTypeViewHolder(view, itemListener);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderTypeViewHolder orderTypeViewHolder, int i) {


        orderTypeViewHolder.orderType.setText(orderModelArrayList.get(i).getName());
        if(i != selected){
            orderTypeViewHolder.orderType.setBackgroundColor(context.getResources().getColor(R.color.grey_3));
            orderTypeViewHolder.orderType.setTextColor(context.getResources().getColor(R.color.grey_10));

        }else{

            orderTypeViewHolder.orderType.setBackgroundColor(context.getResources().getColor(R.color.blue_0));
            orderTypeViewHolder.orderType.setTextColor(context.getResources().getColor(R.color.white));
        }


    }

    @Override
    public int getItemCount() {
        return orderModelArrayList.size();
    }

    public void setSelected(int selected){

        this.selected = selected;


    }

}
