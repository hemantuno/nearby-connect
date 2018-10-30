package com.survivingwithandroid.androidthings.nearby.adapter;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.survivingwithandroid.androidthings.nearby.R;

import java.util.List;

public class CustomAdapter extends BaseAdapter {
    Context context;
    List<ScanResult> wifiList;
    LayoutInflater inflter;

    public CustomAdapter(Context applicationContext,List<ScanResult> results) {
        this.context = applicationContext;
        this.wifiList = results;
        inflter = (LayoutInflater.from(applicationContext));
    }

    @Override
    public int getCount() {
        return wifiList.size();
    }

    @Override
    public Object getItem(int i) {
        return wifiList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = inflter.inflate(R.layout.item_wifi_list, null);
        TextView names = (TextView) view.findViewById(R.id.tvSsidName);
        names.setText(wifiList.get(i).SSID);
        return view;
    }
}