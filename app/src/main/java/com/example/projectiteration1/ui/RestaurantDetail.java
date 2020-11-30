package com.example.projectiteration1.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.projectiteration1.MainActivity;
import com.example.projectiteration1.R;
import com.example.projectiteration1.adapter.InspectionAdapter;
import com.example.projectiteration1.model.InspectionReport;
import com.example.projectiteration1.model.Restaurant;
import com.example.projectiteration1.model.RestaurantsList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

/**
 * Class containing detailed issues of a specific restaurant
 */
public class RestaurantDetail extends AppCompatActivity {
    private int index;
    private boolean fromMaps;
    private Restaurant res;
    private RestaurantsList res_list;
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor sharedEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_detail);

        sharedPref = getSharedPreferences("FavRests", MODE_PRIVATE);
        sharedEditor = sharedPref.edit();

        res_list = RestaurantsList.getInstance();

        extractData();
        restaurantDetails();
        inspectionList();
        setUpButton();
    }

    private void setUpButton(){
        final ImageView btn = findViewById(R.id.favBtn);

        String trackNum = res.getTrackingNumber();
        int curr = sharedPref.getInt(trackNum, -1);
        if(curr == -1){
            btn.setImageResource(android.R.drawable.btn_star_big_off);
        }else{
            btn.setImageResource(android.R.drawable.btn_star_big_on);
        }

        btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String trackNum = res.getTrackingNumber();
                int numInspections = res.getInspectionReports().size();
                int curr = sharedPref.getInt(trackNum, -1);
                if(curr == -1){
                    // Need to change Icon to Fav
                    btn.setImageResource(android.R.drawable.btn_star_big_on);
                    Log.i("Adding To Fav", "Tracking: " + trackNum + " Inspections: " + numInspections);
                    sharedEditor.putInt(trackNum, numInspections);
                }else{
                    // Need to change Icon to Un-Fav
                    btn.setImageResource(android.R.drawable.btn_star_big_off);
                    Log.i("Removing From Fav", "Tracking: " + trackNum);
                    sharedEditor.remove(trackNum);
                }
                sharedEditor.apply();
            }
        });

    }

    //intent
    public static Intent makeLaunchIntent(Context context, int index, boolean fromMaps) {
        Intent intent=new Intent(context, RestaurantDetail.class);
        intent.putExtra(Intent.EXTRA_INDEX, index);
        intent.putExtra("FROM_MAPS", fromMaps);
        return intent;
    }

    private void extractData(){
        Intent intent=getIntent();
        index = intent.getIntExtra(Intent.EXTRA_INDEX, 0);
        fromMaps = intent.getBooleanExtra("FROM_MAPS", false);
    }

    @SuppressLint("SetTextI18n")
    private void restaurantDetails() {
        res = res_list.getRestaurants().get(index);
        //set Name of restaurant
        TextView name = findViewById(R.id.name);
        String res_name = res.getResName();
        name.setText("" + res_name);

        //set address of Restaurant
        TextView address = findViewById(R.id.address);
        String res_address = res.getAddress();
        address.setText("" + res_address);

        //set gps
        final TextView gps = findViewById(R.id.gps);
        final String res_lat = res.getLatitude();
        final String res_long = res.getLongitude();
        gps.setText(res_lat + " latitude \n" + res_long + " longitude");
        gps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!fromMaps){
                    Intent intent = MapsActivity.makeIntent(RestaurantDetail.this, res_lat, res_long);
                    //https://wajahatkarim.com/2018/04/closing-all-activities-and-launching-any-specific-activity/
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("EXIT", true);
                    startActivity(intent);
                }
                finish();
            }
        });
        ImageView img = findViewById(R.id.detailIcon);
        img.setImageResource(res.getImg());
    }

    @SuppressLint("SetTextI18n")
    private void inspectionList() {
        RecyclerView recyclerView = findViewById(R.id.inspection_list);
        recyclerView.setHasFixedSize(true);
        ArrayList<InspectionReport> report = res.getInspectionReports();
        //sort the inspection report
        Collections.sort(report, new Comparator<InspectionReport>() {
            @Override
            public int compare(InspectionReport o1, InspectionReport o2) {
                return o2.getInspectionDate().compareTo(o1.getInspectionDate());
            }
        });
        //set text if inspection report is empty
        if(report.size()==0)
        {
            TextView t = findViewById(R.id.text);
            t.setText("No Inspections Yet");
        }
        InspectionAdapter adapter = new InspectionAdapter(this, report, index);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_restaurant_details, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.menu_back:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        Log.e("Restaurant Detail - Back Button", "This should not print");
    }

}