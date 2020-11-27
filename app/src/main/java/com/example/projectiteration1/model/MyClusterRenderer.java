package com.example.projectiteration1.model;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

/*** Cluster renderer class to change the markers to custom icons */
public class MyClusterRenderer extends DefaultClusterRenderer<MyClusterItem> {
    private LatLng cords;

    public MyClusterRenderer(Context context, GoogleMap map, ClusterManager<MyClusterItem> clusterManager, LatLng cords) {
        super(context, map, clusterManager);

        clusterManager.setRenderer(this);
        this.cords = cords;
    }

    @Override
    protected void onBeforeClusterItemRendered(@NonNull MyClusterItem item, @NonNull MarkerOptions markerOptions) {
        if(item.getIcon() != null) {
            markerOptions.icon(item.getIcon());
            markerOptions.snippet(item.getSnippet());
            markerOptions.title(item.getTitle());
        }
        markerOptions.visible(true);
    }

    @Override
    protected void onClusterItemRendered(@NonNull MyClusterItem clusterItem, @NonNull Marker marker) {
        super.onClusterItemRendered(clusterItem, marker);
        if(clusterItem.getPosition().equals(cords)) {
            marker.showInfoWindow();
        }
    }
}
