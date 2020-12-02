package com.example.projectiteration1.ui;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SearchView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectiteration1.R;
import com.example.projectiteration1.adapter.FavouriteAdapter;
import com.example.projectiteration1.model.ConfigurationsList;
import com.example.projectiteration1.model.InspectionReport;
import com.example.projectiteration1.model.MyClusterItem;
import com.example.projectiteration1.model.MyClusterRenderer;
import com.example.projectiteration1.model.Restaurant;
import com.example.projectiteration1.model.RestaurantsList;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.android.clustering.ClusterManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Map;


/**
 * Map Activity to display the restaurants on a map
 * Followed Brian Fraser's video for the most part
 */
public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final int REQUEST_CODE = 101;
    public static final String USER_SEARCH_RESULT = "User search result";
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
    String query = "";
    String userInput = "";
    String input = "";
    private ArrayList<Restaurant> filteredList = new ArrayList<>();
    private ArrayList<Restaurant> favList = new ArrayList<>();

    private boolean initLaunch = true;
    private RecyclerView recyclerList;
    private FavouriteAdapter favAdapter;
    private LinearLayoutManager listLayout;
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor sharedEditor;
    public static Dialog dialog;

    public static Dialog dialogFilter;

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
        query = intent.getStringExtra("User input");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        sharedPref = getSharedPreferences("FavRests", MODE_PRIVATE);
        sharedEditor = sharedPref.edit();

        res_list = RestaurantsList.getInstance();
        // If searching has not been done that altered the resList
        if (!ConfigurationsList.getCopyOfList(this).isEmpty()) {
            res_list.getRestaurants().clear();
            res_list.getRestaurants().addAll(
                    ConfigurationsList.getCopyOfList(this));        // Copy of a list stored in a SharedPrefs
        }
        client = LocationServices.getFusedLocationProviderClient(this);
        extractData();
        getLocPermission();

        if(initLaunch){
            checkFav();
            initLaunch = false;
        }


        final SearchView searching = findViewById(R.id.map_search_bar);
        final Button allResButton = findViewById(R.id.all_res_btn);
        //setUpFilterButton();

        // If watching the full list of res
        if (searching.getQuery().toString().isEmpty()) {
            allResButton.setEnabled(false);
            allResButton.setVisibility(View.INVISIBLE);
        }

        // Clear map and add all of the restaurants
        allResButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMap.clear();                                        // Clear current map
                setUpClusterer(res_list.getRestaurants());           // Display search results
                searching.setQuery("", false);
                allResButton.setEnabled(false);
                allResButton.setVisibility(View.INVISIBLE);
                userInput = "";
            }
        });

        // Searching only when user submits search
        searching.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                input = searching.getQuery().toString();             // Search Bar

                // Results list
                filteredList = new ArrayList<>();
                if (input.isEmpty()) {
                    filteredList.addAll(res_list.getRestaurants());
                } else {
                    //filtering for res name, recent hazard level should be low and number of critical violations should be <=3
                    for (Restaurant res : res_list.getRestaurants()) {
                        if (res.getResName().toLowerCase().contains(input.toLowerCase())) {
                            filteredList.add(res);
                        }
                    }
                }

                // The search gives no results
                if (filteredList.isEmpty()) {
                    Toast.makeText(MapsActivity.this, "No results", Toast.LENGTH_LONG).show();
                }

                mMap.clear();                           // Clear current map
                if(clusterManager == null){
                    setUpClusterer(filteredList);
                }else{
                    setFilteredList(filteredList);
                }

                searching.clearFocus();

                // Enable button to return viewing all restaurants
                allResButton.setEnabled(true);
                allResButton.setVisibility(View.VISIBLE);

                // Support to pass data into intent
                userInput = searching.getQuery().toString();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });     // TextChanged

        // Delay for the list to process
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                searching.setQuery(query, true);
            }
        }, 500);
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
        Intent intent = getIntent();
        // So that UI would display all res for with working clusters
        if (intent.getIntExtra("Initial map run", 0) == 1) {
            setUpClusterer(res_list.getRestaurants());
        }

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

        setUpFilterButton();
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
    private void setUpClusterer(final ArrayList<Restaurant> restaurants) {
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
        addItems(restaurants);

        // Point the map's listeners at the listeners implemented by the cluster
        // manager.
        mMap.setOnCameraIdleListener(clusterManager);
        mMap.setOnMarkerClickListener(clusterManager);

        clusterManager.setOnClusterItemInfoWindowClickListener(new ClusterManager.OnClusterItemInfoWindowClickListener<MyClusterItem>() {
            @Override
            public void onClusterItemInfoWindowClick(MyClusterItem item) {
                for(int i = 0; i < restaurants.size();i++) {
                    final Restaurant r = restaurants.get(i);
                    int position = findIndexPosition(r.getResName());
                    final LatLng cords = new LatLng(Double.parseDouble(r.getLatitude()), Double.parseDouble(r.getLongitude()));
                    if (item.getPosition().equals(cords)) {
                        Intent intent = RestaurantDetail.makeLaunchIntent(MapsActivity.this, position, true);
                        startActivity(intent);
                    }
                }
            }
        });
    }

    private int findIndexPosition(String name) {
        // Finds the position of a restaurant in the res_list given a res name
        for (int i = 0; i < res_list.getSize(); i++) {
            if (name.equals(
                    res_list.getRestaurants().get(i).getResName())) {
                return i;
            }
        }
        return 0;
    }

    private void addItems(ArrayList<Restaurant> restaurants) {
        if(clusterManager != null){
            clusterManager.clearItems();
        }

        for (int i = 0; i < restaurants.size(); i++) {
            Restaurant r = restaurants.get(i);
            String lat = r.getLatitude();
            String lng = r.getLongitude();
            String hazard_level;
            InspectionReport report;

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
        Map<String, ?> allKeys = sharedPref.getAll();
        for (Map.Entry<String, ?> entry : allKeys.entrySet()) {
            String trackNum = entry.getKey();
            int prevInspections = Integer.parseInt(entry.getValue().toString());
            for(Restaurant res : res_list.getRestaurants()){
                if(res.getTrackingNumber().equals(trackNum)){
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

    private void setUpFilterButton(){
        dialogFilter = new Dialog(MapsActivity.this);
        dialogFilter.setContentView((R.layout.filter_dialog));
        dialogFilter.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#ffffff")));
        dialogFilter.setCancelable(false);

        ImageView filter_button = findViewById(R.id.map_filterBtn);
        filter_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogFilter.show();
                getButtonData();
            }
        });

    }

    private void getButtonData(){
        final RadioGroup radioGroup_hzd = dialogFilter.findViewById(R.id.radio_hazard);
        final RadioGroup radioGroup_critical = dialogFilter.findViewById(R.id.radio_critical);
        final EditText num_critical_filter = dialogFilter.findViewById(R.id.int_critical);
        final Switch switch_fav = dialogFilter.findViewById(R.id.favourites_filter_btn);

        final Button apply_filter = dialogFilter.findViewById(R.id.Apply_filter);
        final Button cancel_filter = dialogFilter.findViewById(R.id.Cancel_filter);
        final Button reset_filter = dialogFilter.findViewById(R.id.Reset_filter);

        Log.i("initiated", "buttons");

        //set on click listener on apply button
        apply_filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFilteredList(res_list.getRestaurants());
                dialogFilter.dismiss();
            }
        });

        //on click listener on cancel button
        cancel_filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogFilter.dismiss();
            }
        });

        //on click listener on reset button
        reset_filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                radioGroup_hzd.clearCheck();
                RadioButton haz = dialogFilter.findViewById(R.id.btn_all);
                haz.setChecked(true);
                radioGroup_critical.clearCheck();
                RadioButton crit = dialogFilter.findViewById(R.id.less_than);
                crit.setChecked(true);
                num_critical_filter.setText("");
                switch_fav.setChecked(false);

                addItems(res_list.getRestaurants());
                dialogFilter.dismiss();
                Log.i("Restaurant List", "Size: " + res_list.getSize());
            }
        });
    }

    public void setFilteredList(ArrayList<Restaurant> incRes){
        final RadioGroup radioGroup_hzd = dialogFilter.findViewById(R.id.radio_hazard);
        final RadioGroup radioGroup_critical = dialogFilter.findViewById(R.id.radio_critical);
        final EditText num_critical_filter = dialogFilter.findViewById(R.id.int_critical);
        final Switch switch_fav = dialogFilter.findViewById(R.id.favourites_filter_btn);

        int numOfCrit = 0;
        String hazardFilter = "";
        ArrayList<Restaurant> filterList = new ArrayList<>();

        boolean isFav = switch_fav.isChecked();
        if(!num_critical_filter.getText().toString().isEmpty()){
            numOfCrit = Integer.parseInt(num_critical_filter.getText().toString());
        }

        // Check Hazard Level
        switch(radioGroup_hzd.getCheckedRadioButtonId()){
            case R.id.btn_low:
                hazardFilter = "low";
                break;
            case R.id.btn_moderate:
                hazardFilter = "moderate";
                break;
            case R.id.btn_high:
                hazardFilter = "high";
                break;
            default:
                hazardFilter = null;
        }

        Log.i("Filter - Hazard", "Searching for Hazard Level of: " + hazardFilter);
        if(hazardFilter == null){
            filterList.addAll(incRes);
        }
        else{
            for(Restaurant res : incRes){
                try{
                    InspectionReport report = res.getInspectionReports().get(0);
                    Log.i("Filter - Hazard", "Name: " + res.getResName() + ", Report Hazard: " + report.getHazardRating() + ", Filter Hazard: " + hazardFilter);
                    if(report.getHazardRating().toLowerCase().equals(hazardFilter)){
                        filterList.add(res);
                    }
                }catch (Exception e){
                    Log.e("Filter - Hazard Level", "No Inspections / Error Finding. Adding to List.");
                }
            }
        }


        // Check Crits in the Current Year
        if(!num_critical_filter.getText().toString().isEmpty()){
            Log.i("Filter - Crit", "Num of Crits: " + num_critical_filter.getText().toString());
            int currYear = Calendar.getInstance().getTime().getYear() + 1900;
            Log.i("Filter - Crit", "Current Year: " + currYear);
            Iterator<Restaurant> iter = filterList.iterator();
            switch(radioGroup_critical.getCheckedRadioButtonId()){
                case R.id.less_than:
                    Log.i("Filter - Crit", "x <= " + numOfCrit);
                    while(iter.hasNext()){
                        Restaurant res = iter.next();
                        String tracking = res.getTrackingNumber();
                        int critCount = 0;
                        for(InspectionReport rep : res.getInspectionReports()){
                            int year = Integer.parseInt(rep.getInspectionDate().substring(0,4));
                            Log.i("Filter - Crit", "Name: " + res.getResName() + ", Report Year: " + year + ", Num of Crit: " + rep.getNumCritical());
                            if(year == currYear){
                                Log.i("Filter - Crit", "Adding: " + rep.getNumCritical() + ", from: " + year);
                                critCount += rep.getNumCritical();
                            }
                        }

                        if(critCount > numOfCrit){
                            iter.remove();
                            Log.i("Filter - <=", "Removing: " + tracking);
                        }
                    }
                    break;
                case R.id.greater_than:
                    Log.i("Filter - Crit", "x >= " + numOfCrit);
                    while(iter.hasNext()){
                        Restaurant res = iter.next();
                        String tracking = res.getTrackingNumber();
                        int critCount = 0;
                        for(InspectionReport rep : res.getInspectionReports()){
                            int year = Integer.parseInt(rep.getInspectionDate().substring(0,4));
                            if(year == currYear){
                                critCount += rep.getNumCritical();
                            }
                        }

                        if(critCount < numOfCrit){
                            iter.remove();
                            Log.i("Filter - >=", "Removing: " + tracking);
                        }
                    }
                    break;
                default:
            }
        }

        // Check Favourites
        if(isFav){
            Log.i("Filter - Favs", "Favs");
            Iterator<Restaurant> iter = filterList.iterator();
            while(iter.hasNext()){
                Restaurant res = iter.next();
                String tracking = res.getTrackingNumber();
                int curr = sharedPref.getInt(tracking, -1);
                if(curr == -1){
                    // Is not fav
                    iter.remove();
                    Log.i("Filter - Fav", "Removing: " + tracking);
                }
            }
        }

        //Debugging
        for(Restaurant res : filterList){
            Log.i("Filtered", res.toString());
        }

        // Throw Filter List
        addItems(filterList);
    }

    @Override
    public void onResume(){
        super.onResume();
        if(clusterManager != null){
            clusterManager.clearItems();
            if(filteredList != null && !filteredList.isEmpty()){
                addItems(filteredList);
            }
            else{
                addItems(res_list.getRestaurants());
            }
        }

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.menu_to_list:
                Intent i = ListAllRestaurant.makeLaunchIntent(MapsActivity.this);
                i.putExtra(USER_SEARCH_RESULT, userInput);
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
