package com.holger.mashpit.tools;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;

import androidx.annotation.NonNull;

import com.holger.mashpit.R;

import java.util.List;

public class ColorSpinnerAdapter extends ArrayAdapter<Integer> implements SpinnerAdapter {

    private final List<Integer> objects;
    private final Context context;

    public ColorSpinnerAdapter(Context context, List<Integer> objects) {
        super(context, R.layout.support_simple_spinner_dropdown_item, objects);
        this.context = context;
        this.objects = objects;
    }

    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        super.getDropDownView(position, convertView, parent);

        View rowView = convertView;

        if (rowView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            rowView = inflater.inflate(R.layout.support_simple_spinner_dropdown_item, parent, false);

        }
        rowView.setBackgroundColor(objects.get(position));

        return rowView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;

        if (rowView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            rowView = inflater.inflate(R.layout.support_simple_spinner_dropdown_item, parent, false);

        }
        rowView.setBackgroundColor(objects.get(position));

        return rowView;
    }
}