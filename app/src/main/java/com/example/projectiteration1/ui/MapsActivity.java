package com.example.projectiteration1.ui;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.projectiteration1.R;
import com.example.projectiteration1.model.Restaurant;
import com.example.projectiteration1.model.RestaurantsList;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Map Activity to display the restaurants on a map
 * Followed Brian Fraser's video for the most part
 */
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private RestaurantsList res_list;
    private GoogleMap mMap;

    public static Intent makeLaunchIntent(Context c) {
        return new Intent(c, MapsActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        res_list = RestaurantsList.getInstance();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if(res_list == null){
            res_list = RestaurantsList.getInstance();
        }

        for(Restaurant r : res_list.getRestaurants()){
            String latitude = r.getLatitude();
            String longitude = r.getLongitude();
            LatLng cords = new LatLng(Double.parseDouble(latitude), Double.parseDouble((longitude)));
            mMap.addMarker(new MarkerOptions().position(cords).title(r.getResName()));
            Log.i("Maps - Adding Markers", "Added Marker: " + r.getAddress() + " Lat: " + latitude + " Long: " + longitude);
        }

        //TODO: Get User Location
        LatLng userLoca = new LatLng(1,1);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(userLoca));

        Log.i("End of MapReady", "Added all Markers");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_maps_view, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.menu_to_list:
                Intent i = ListAllRestaurant.makeLaunchIntent(MapsActivity.this);
                startActivity(i);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);
        Log.e("All Restaurant List - Back Button", "This should not print");
    }
}