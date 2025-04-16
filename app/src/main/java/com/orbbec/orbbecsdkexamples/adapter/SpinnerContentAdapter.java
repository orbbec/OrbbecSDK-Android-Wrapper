package com.orbbec.orbbecsdkexamples.adapter;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.orbbec.orbbecsdkexamples.R;


public class SpinnerContentAdapter<Stinging> extends ArrayAdapter<String> {
    public SpinnerContentAdapter(@NonNull Context context, int resource) {
        super(context, resource);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View v = super.getDropDownView(position, convertView, parent);
        TextView textView= v.findViewById(R.id.tv_dropdown_item);
        textView.setOnHoverListener(new View.OnHoverListener() {
            @Override
            public boolean onHover(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_HOVER_ENTER:
                        textView.setHovered(true);
                        break;
                    case MotionEvent.ACTION_HOVER_EXIT:
                        textView.setHovered(false);
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
        return v;
    }
}
