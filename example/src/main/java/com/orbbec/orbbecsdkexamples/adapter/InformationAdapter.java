package com.orbbec.orbbecsdkexamples.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.orbbec.orbbecsdkexamples.R;

import java.util.ArrayList;

public class InformationAdapter extends RecyclerView.Adapter<InformationAdapter.InformationView> {
    private Context mContext;
    private ArrayList<String> mMessages;


    public InformationAdapter(Context mContext) {
        this.mContext = mContext;
        this.mMessages = new ArrayList<String>();
    }

    public void newMessage(String message) {
        mMessages.add(message);
        this.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public InformationView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new InformationView(LayoutInflater.from(mContext)
                .inflate(R.layout.layout_information_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull InformationView holder, int position) {
        holder.mMessageTV.setText(mMessages.get(position));
    }

    @Override
    public int getItemCount() {
        return mMessages.size();
    }

    class InformationView extends RecyclerView.ViewHolder {

        private TextView mMessageTV;

        public InformationView(@NonNull View itemView) {
            super(itemView);
            mMessageTV = itemView.findViewById(R.id.tv_message);
        }
    }
}
