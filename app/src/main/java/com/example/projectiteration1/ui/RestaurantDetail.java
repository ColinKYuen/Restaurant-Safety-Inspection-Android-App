package com.example.projectiteration1.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.projectiteration1.R;
import com.example.projectiteration1.adapter.InspectionAdapter;
import com.example.projectiteration1.model.InspectionReport;
import com.example.projectiteration1.model.Restaurant;
import com.example.projectiteration1.model.RestaurantsList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Class containing detailed issues of a specific restaurant
 */
public class RestaurantDetail extends AppCompatActivity {

    private RestaurantsList res_list;
    private Restaurant res;
    private int index;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_detail);

        res_list = RestaurantsList.getInstance();

        extractData();
        restaurantDetails();
        inspectionList();
    }

    //intent
    public static Intent makeLaunchIntent(Context context, int index) {
        Intent intent=new Intent(context, RestaurantDetail.class);
        intent.putExtra(Intent.EXTRA_INDEX, index);
        return intent;
    }

    private void extractData(){
        Intent intent=getIntent();
        index=intent.getIntExtra(Intent.EXTRA_INDEX, 0);
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
        TextView gps = findViewById(R.id.gps);
        String res_lat = res.getLatitude();
        String res_long = res.getLongitude();
        gps.setText(res_lat + " latitude \n" + res_long + " longitude");

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

}