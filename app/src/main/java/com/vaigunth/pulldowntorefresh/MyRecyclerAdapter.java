package com.vaigunth.pulldowntorefresh;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.vaigunth.cardprinter.PrinterRecyclerAdapter;
import com.vaigunth.cardprinter.PrinterRecyclerView;

import java.util.List;

/**
 * Created by Vaigunth on 30-Oct-16.
 */

class MyRecyclerAdapter extends PrinterRecyclerAdapter {
    MyRecyclerAdapter(List<Object> mDataList, PrinterRecyclerView recyclerView) {
        super(mDataList, recyclerView);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card, viewGroup, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
        initCardAnimation(viewHolder, i, R.layout.card_front, R.layout.card_back);
        //Use getCardFrontLayout() and getCardBackLayout() to manipulate the views
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        MyViewHolder(final View itemView) {
            super(itemView);
        }
    }
}
