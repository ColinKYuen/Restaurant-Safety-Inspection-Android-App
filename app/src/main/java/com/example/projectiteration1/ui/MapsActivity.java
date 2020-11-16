package com.example.projectiteration1.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TableRow;
import android.widget.Toast;

import com.example.projectiteration1.MainActivity;
import com.example.projectiteration1.R;
import com.example.projectiteration1.model.MyClusterItem;
import com.example.projectiteration1.model.Restaurant;
import com.example.projectiteration1.model.RestaurantsList;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;


/**
 * Map Activity to display the restaurants on a map
 * Followed Brian Fraser's video for the most part
 */
public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final int REQUEST_CODE = 101;
    private String TAG = "MapsActivity";
    private RestaurantsList res_list;
    private Location currentLocation;
    private GoogleMap mMap;
    private FusedLocationProviderClient client;
    private Boolean permission_granted = false;
    private ClusterManager<MyClusterItem> clusterManager;
    private String lttude = null;
    private String lgtude = null;

    public static Intent makeLaunchIntent(Context c) {
        Intent intent = new Intent(c, MapsActivity.class);
        return intent;
    }

    public static Intent makeIntent(Context c, String lat, String lng){
        Intent intent = new Intent(c, MapsActivity.class);
        intent.putExtra("Latitude :", lat);
        intent.putExtra("Longitude :", lng);
        return intent;
    }

    private void extractData(){
        Intent intent = getIntent();
        lttude = intent.getStringExtra("Latitude :");
        lgtude = intent.getStringExtra("Longitude :");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        res_list = RestaurantsList.getInstance();
        extractData();
        getLocPermission();
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
        if (res_list == null) {
            res_list = RestaurantsList.getInstance();
        }

        if(lttude != null && lgtude != null){
            LatLng lat_lng = new LatLng(Double.parseDouble(lttude), Double.parseDouble(lgtude));
            moveCamera(lat_lng, 20f);
        } else if (permission_granted) {
            getCurrentLocation();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat
                    .checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
        }

        //enable map zooming
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);

        //Enable compass
        mMap.getUiSettings().setCompassEnabled(true);


        /*// Add a marker in Sydney, Australia,
        LatLng sydney = new LatLng(-33.852, 151.211);
        mMap.addMarker(new MarkerOptions()
                .position(sydney)
                .title("Marker in Sydney"));
        // Do not need to moveCamera at this point
        //googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        // Add a marker in Vancouver, Canada,
        // and move the map's camera to the same location.
        LatLng van = new LatLng(49.246292, -123.116226);
        mMap.addMarker(new MarkerOptions()
                .position(van)
                .title("Marker in Vancouver"));
        // Do not need to moveCamera at this point
        //googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        // Set Marker Color for Sample Marker Sydney
        new MarkerOptions()
                .position(sydney)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

        // Set Marker Color for Sample Marker Vancouver
        new MarkerOptions()
                .position(van)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));

        // Set Marker Icon for Sample Marker Sydney
        new MarkerOptions()
                .position(sydney)
                .title("Sydney")
                .snippet("Population: x,xxx,xxx")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_checkmark));

        // Set Marker Icon for Sample Marker Vancouver
        new MarkerOptions()
                .position(van)
                .title("Vancouver")
                .snippet("Population: x,xxx,xxx")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.food));*/


        //mMap.moveCamera(CameraUpdateFactory.newLatLng(userLoca));
        setUpClusterer();
        Log.i("End of MapReady", "Added all Markers");
    }

    //Cluster set up
    private void setUpClusterer() {

        // Initialize the manager with the context and the map.
        // (Activity extends context, so we can pass 'this' in the constructor.)
        clusterManager = new ClusterManager<MyClusterItem>(this, mMap);

        // Add cluster items (markers) to the cluster manager.
        addItems();

        // Position the map.
        for(int i = 0; i<res_list.getRestaurants().size();i++){
            final Restaurant r = res_list.getRestaurants().get(i);
            final LatLng cords = new LatLng(Double.parseDouble(r.getLatitude()), Double.parseDouble(r.getLongitude()));
            moveCamera(cords, 15f);
        }

        clusterManager.setOnClusterItemInfoWindowClickListener(new ClusterManager.OnClusterItemInfoWindowClickListener<MyClusterItem>() {
            @Override
            public void onClusterItemInfoWindowClick(MyClusterItem item) {
                for(int i = 0; i<res_list.getRestaurants().size();i++) {
                    final Restaurant r = res_list.getRestaurants().get(i);
                    final LatLng cords = new LatLng(Double.parseDouble(r.getLatitude()), Double.parseDouble(r.getLongitude()));
                    moveCamera(cords, 15f);
                    if (item.getPosition().equals(cords)) {
                        Intent intent = RestaurantDetail.makeLaunchIntent(MapsActivity.this, i);
                        startActivity(intent);
                    }
                }
            }
        });

        // Point the map's listeners at the listeners implemented by the cluster
        // manager.
        mMap.setOnCameraIdleListener( clusterManager);
        mMap.setOnMarkerClickListener( clusterManager);
    }

    //Once the test is done, addItem() can be deleted
    private void addItems() {

        for(int i = 0; i<res_list.getRestaurants().size(); i++){
            Restaurant r = res_list.getRestaurants().get(i);
            String lat = r.getLatitude();
            String lng = r.getLongitude();
            MyClusterItem offsetItem = new MyClusterItem(Double.parseDouble(lat), Double.parseDouble(lng), r.getResName(), r.getAddress());
            clusterManager.addItem(offsetItem);
        }
    }


    private void initMap(){
        Log.d(TAG, "initialize map");
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    private void getLocPermission(){
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                permission_granted = true;
                initMap();
            }else{
                ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE);
            }
        }else{
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE);
        }
    }

    public void onRequestPermissionsResult(int reqCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permission_granted = false;
        if (reqCode == REQUEST_CODE) {
            if (grantResults.length > 0) {
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        permission_granted = false;
                        Log.d(TAG, "permission failed");
                        return;
                    }
                }
            }
            permission_granted = true;
            Log.d(TAG, "permission granted");

            //initialise the map
            initMap();
        }
    }

    private void getCurrentLocation(){
        Log.d(TAG, "getting current location");

        client = LocationServices.getFusedLocationProviderClient(this);
        try {
            if (permission_granted) {
                Task loc = client.getLastLocation();
                loc.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "location found");
                            currentLocation = (Location) task.getResult();

                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), 15f);
                        } else {
                            Log.d(TAG, "location is null");
                            Toast.makeText(MapsActivity.this, "unable to get device's current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.d(TAG, "Exception thrown" + e.getMessage());
        }
    }

    private void moveCamera(LatLng lat_lng, float zoom){
        Log.d(TAG, "moving camera to " + lat_lng.latitude + ", " + lat_lng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lat_lng, zoom));
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
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed(){
        finish();
        Log.e("All Restaurant List - Back Button", "This should not print");
    }

}
