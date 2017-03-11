package com.vaigunth.pulldowntorefresh;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.vaigunth.cardprinter.PrinterRecyclerAdapter;
import com.vaigunth.cardprinter.PrinterRecyclerView;

import java.util.List;

/**
 * Created by Vaigunth on 30-Oct-16.
 */

public class MyRecyclerAdapter extends PrinterRecyclerAdapter {
    MyRecyclerAdapter(List mDataList, PrinterRecyclerView recyclerView) {
        super(mDataList, recyclerView);
    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card, viewGroup, false);
        MyViewHolder viewHolder = new MyViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
        initCardAnimation(viewHolder, i, R.layout.card_front, R.layout.card_back);
        MyViewHolder myViewHolder = (MyViewHolder) viewHolder;
        /*myViewHolder.imageView = (ImageView) ((MyViewHolder) viewHolder).itemView.findViewById(R.id.img_view);
        myViewHolder.imageView.setImageResource(R.drawable.b);*/
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        //ImageView imageView;
        MyViewHolder(final View itemView) {
            super(itemView);
        }
    }
}
