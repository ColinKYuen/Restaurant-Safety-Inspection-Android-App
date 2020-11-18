package com.example.projectiteration1.model;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;



public class MyClusterItem implements ClusterItem {
    private LatLng position;
    private String title;
    private String snippet;
    private BitmapDescriptor icon;

    public MyClusterItem(double lat, double lng, BitmapDescriptor icon, String title, String snippet) {
        position = new LatLng(lat, lng);
        this.title = title;
        this.icon = icon;
        this.snippet = snippet;
    }

    public BitmapDescriptor getIcon() {return icon;}

    @Override
    public LatLng getPosition() {
        return position;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getSnippet() {
        return snippet;
    }
}