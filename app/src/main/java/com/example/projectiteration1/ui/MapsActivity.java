package com.example.projectiteration1.ui;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectiteration1.MainActivity;
import com.example.projectiteration1.R;
import com.example.projectiteration1.adapter.FavouriteAdapter;
import com.example.projectiteration1.adapter.RestaurantAdapter;
import com.example.projectiteration1.model.InspectionReport;
import com.example.projectiteration1.model.MyClusterItem;
import com.example.projectiteration1.model.MyClusterRenderer;
import com.example.projectiteration1.model.Restaurant;
import com.example.projectiteration1.model.RestaurantsList;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.android.clustering.ClusterManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

import static android.telephony.CellLocation.requestLocationUpdate;



public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback{
    private static final int REQUEST_CODE = 101;
    private String TAG = "MapsActivity";
    private RestaurantsList res_list;
    private MyClusterItem offsetItem;
    private Location currentLocation;
    private GoogleMap mMap;
    private FusedLocationProviderClient client;
    private Boolean permission_granted = false;
    private ClusterManager<MyClusterItem> clusterManager;
    private LocationRequest locationRequest;
    private MyClusterRenderer renderer;
    private String lttude = null;
    private String lgtude = null;
    private MapView mapView;

    private boolean initLaunch = true;
    private RecyclerView recyclerList;
    private FavouriteAdapter favAdapter;
    private LinearLayoutManager listLayout;
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor sharedEditor;
    public static Dialog dialog;

    public static Intent makeLaunchIntent(Context c) {
        return new Intent(c, MapsActivity.class);
    }

    public static Intent makeIntent(Context c, String lat, String lng){
        Intent intent = new Intent(c, MapsActivity.class);
        intent.putExtra("Latitude", lat);
        intent.putExtra("Longitude", lng);
        return intent;
    }

    private void extractData(){
        Intent intent = getIntent();
        lttude = intent.getStringExtra("Latitude");
        lgtude = intent.getStringExtra("Longitude");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        sharedPref = getSharedPreferences("FavRests", MODE_PRIVATE);
        sharedEditor = sharedPref.edit();

        res_list = RestaurantsList.getInstance();
        client = LocationServices.getFusedLocationProviderClient(this);
        extractData();
        getLocPermission();

        if(initLaunch){
            checkFav();
            initLaunch = false;
        }
    }


/*
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.*/


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (res_list == null) {
            res_list = RestaurantsList.getInstance();
        }

        if (permission_granted) {
            getCurrentLocation();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat
                    .checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
        }

        if(lttude != null && lgtude != null){
            LatLng lat_lng = new LatLng(Double.parseDouble(lttude), Double.parseDouble(lgtude));
            moveCamera(lat_lng, 30f);
        }

        //enable map zooming
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);

        //Enable compass
        mMap.getUiSettings().setCompassEnabled(true);

        //mMap.moveCamera(CameraUpdateFactory.newLatLng(userLoca));
        setUpClusterer();

        //https://www.youtube.com/watch?v=5fjwDx8fOMk
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                Log.i("MOVEMENT", "USER MOVED");
                if(lttude == null || lgtude == null){
                    Log.i("MOVEMENT 2", "MOVE MAP TO CURRENT");
                    getCurrentLocation();
                }
                else{
                    Log.i("MOVEMENT 2", "MOVE MAP TO RESTAURANT");
                    fromDetails();
                }
            }
        };

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);

        Log.i("End of MapReady", "Added all Markers");
    }

    private void fromDetails(){
        LatLng cords;
        try{
            cords = new LatLng(Double.parseDouble(lttude), Double.parseDouble(lgtude));
            moveCamera(cords, 30f);
            lttude = lgtude = null;
        }
        catch (Exception e){
            getCurrentLocation();
        }
    }

    //Cluster set up
    private void setUpClusterer() {
        // Initialize the manager with the context and the map.
        // (Activity extends context, so we can pass 'this' in the constructor.)
        clusterManager = new ClusterManager<>(this, mMap);

        LatLng cord = null;
        if(lttude !=null && lgtude != null)
        {
            cord = new LatLng(Double.parseDouble(lttude), Double.parseDouble(lgtude));
        }

        renderer = new MyClusterRenderer(MapsActivity.this, mMap, clusterManager, cord);

        // Add cluster items (markers) to the cluster manager.
        addItems();

        // Point the map's listeners at the listeners implemented by the cluster
        // manager.
        mMap.setOnCameraIdleListener( clusterManager);
        mMap.setOnMarkerClickListener( clusterManager);

        clusterManager.setOnClusterItemInfoWindowClickListener(new ClusterManager.OnClusterItemInfoWindowClickListener<MyClusterItem>() {
            @Override
            public void onClusterItemInfoWindowClick(MyClusterItem item) {
                for(int i = 0; i<res_list.getRestaurants().size();i++) {
                    final Restaurant r = res_list.getRestaurants().get(i);
                    final LatLng cords = new LatLng(Double.parseDouble(r.getLatitude()), Double.parseDouble(r.getLongitude()));
                    if (item.getPosition().equals(cords)) {
                        Intent intent = RestaurantDetail.makeLaunchIntent(MapsActivity.this, i, true);
                        startActivity(intent);
                    }
                }
            }
        });
    }

    //Once the test is done, addItem() can be deleted
    private void addItems() {
        for (int i = 0; i < res_list.getRestaurants().size(); i++) {
            Restaurant r = res_list.getRestaurants().get(i);
            String lat = r.getLatitude();
            String lng = r.getLongitude();
            String hazard_level;
            InspectionReport report;
            try {
                ArrayList<InspectionReport> allReports = r.getInspectionReports();
                Collections.sort(allReports, new Comparator<InspectionReport>() {
                    @Override
                    public int compare(InspectionReport o1, InspectionReport o2) {
                        return o2.getInspectionDate().compareTo(o1.getInspectionDate());
                    }
                });
            } catch (Exception e) {
                Log.e("Maps", "Error trying to access Inspection or Sorting");
            }
            BitmapDescriptor icon_id;
            try {
                report = r.getInspectionReports().get(0);
            } catch (Exception e) {
                report = null;
            }

            boolean isFav = false;
            String trackNum = r.getTrackingNumber();
            int curr = sharedPref.getInt(trackNum, -1);
            if(curr != -1){
                isFav = true;
            }

            if(isFav){
                if (report == null || report.getHazardRating().equals("Low")) {
                    hazard_level = "Low";
                    icon_id = BitmapDescriptorFactory.fromResource(R.drawable.fav_green);
                } else if (report.getHazardRating().equals("Moderate")) {
                    hazard_level = "Moderate";
                    icon_id = BitmapDescriptorFactory.fromResource(R.drawable.fav_orange);
                } else {
                    hazard_level = "high";
                    icon_id = BitmapDescriptorFactory.fromResource(R.drawable.fav_red);
                }
            }
            else{
                if (report == null || report.getHazardRating().equals("Low")) {
                    hazard_level = "Low";
                    icon_id = BitmapDescriptorFactory.fromResource(R.drawable.green);
                } else if (report.getHazardRating().equals("Moderate")) {
                    hazard_level = "Moderate";
                    icon_id = BitmapDescriptorFactory.fromResource(R.drawable.orange);
                } else {
                    hazard_level = "high";
                    icon_id = BitmapDescriptorFactory.fromResource(R.drawable.red);
                }
            }

            offsetItem = new MyClusterItem(Double.parseDouble(lat), Double.parseDouble(lng), icon_id, r.getResName(), r.getAddress()+ "       Hazard Level : " + hazard_level);
            clusterManager.addItem(offsetItem);
        }
        clusterManager.cluster();
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
                for (int grantResult : grantResults) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
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

    private void setUpList(){
        View inflatedView = getLayoutInflater().inflate(R.layout.fav_dialog, null);
        recyclerList = inflatedView.findViewById(R.id.favRecycler);
        recyclerList.setHasFixedSize(true);
        listLayout = new LinearLayoutManager(this);
        recyclerList.setLayoutManager(listLayout);
        favAdapter = new FavouriteAdapter(res_list.getRestaurants());
        recyclerList.setAdapter(favAdapter);
    }

    private void checkFav(){
        setUpList();
        boolean hasUpdate = false;
        ArrayList<Restaurant> favList = new ArrayList<>();
        Map<String, ?> allKeys = sharedPref.getAll();
        for (Map.Entry<String, ?> entry : allKeys.entrySet()) {
            String trackNum = entry.getKey();
            int prevInspections = Integer.parseInt(entry.getValue().toString());
            for(Restaurant res : res_list.getRestaurants()){
                if(res.getTrackingNumber().equals(trackNum)){
                    res.setFav(true);
                    if(res.getInspectionReports().size() > prevInspections){ // NEW INSPECTION ADDED
                        hasUpdate = true;
                        // Update SharedPref
                        sharedEditor.putInt(trackNum, res.getInspectionReports().size());

                        // Add to Updated Fav List
                        favList.add(res);
                    }
                    break;
                }
            }
        }

        if(hasUpdate){
            Log.i("Maps - Check Update", "Has new update, will display list.");
            showDialog(MapsActivity.this, favList);
        }
        Log.i("Maps - Check Update", "No new Update");

        sharedEditor.apply();
    }

    public void showDialog(Activity activity, ArrayList favList){
        dialog = new Dialog(activity);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.fav_dialog);

        RecyclerView recyclerView = dialog.findViewById(R.id.favRecycler);
        FavouriteAdapter myAdapater = new FavouriteAdapter(favList);
        recyclerView.setAdapter(myAdapater);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        dialog.findViewById(R.id.favClose).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.setOnKeyListener(new Dialog.OnKeyListener(){
            @Override
            public boolean onKey(DialogInterface arg0, int keyCode, KeyEvent event){
                if(keyCode == KeyEvent.KEYCODE_BACK){
                    dialog.dismiss();
                }
                return true;
            }
        });

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    @Override
    public void onResume(){
        super.onResume();
        if(clusterManager != null){
            clusterManager.clearItems();
            addItems();
        }
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
        finish();

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        finish();
    }
}
