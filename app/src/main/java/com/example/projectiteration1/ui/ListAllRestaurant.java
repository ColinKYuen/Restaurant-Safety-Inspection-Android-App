package com.example.projectiteration1.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.input.InputManager;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.HttpAuthHandler;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectiteration1.MainActivity;
import com.example.projectiteration1.R;
import com.example.projectiteration1.adapter.RestaurantAdapter;
import com.example.projectiteration1.model.*;
import com.google.android.material.chip.Chip;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

/**
 * UI Logic for listing all Restaurants in CardView via RecyclerView
 */
public class ListAllRestaurant extends AppCompatActivity {
    private RecyclerView recyclerList;
    private RestaurantAdapter resAdapter;
    private RecyclerView.LayoutManager listLayout;
    private RestaurantsList resList;
    public static final String USER_SEARCH_RESULT = "User search result";
    String query = "";
    String input = "";

    RadioGroup radioGroup_hzd;
    RadioButton radioButton_hzd;
    RadioGroup radioGroup_critical;
    RadioButton radioButton_critical;
    RadioButton radioButton_fav;
    String hazard_filter = null;
    String critical_filter = null;
    Boolean isFav = false;
    Boolean inFavList = false;
    EditText num_critical_filter;
    public static Dialog dialogFilter;
    private ArrayList<Restaurant> filteredList = new ArrayList<>();
    private ArrayList<Restaurant> tempList = new ArrayList<>();
    private ArrayList<Restaurant> favList = new ArrayList<>();
    private ArrayList<Restaurant> tempList2 = new ArrayList<>();

    public static Intent makeLaunchIntent(Context c) {
        return new Intent(c, ListAllRestaurant.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_restaurants);
        resList = RestaurantsList.getInstance();

        // If searching has not been done that altered the resList
        if (!ConfigurationsList.getCopyOfList(this).isEmpty()) {
            resList.getRestaurants().clear();
            resList.getRestaurants().addAll(
                    ConfigurationsList.getCopyOfList(this));        // Copy of a list stored in a SharedPrefs
        }

        setUpList();

        final SearchView searching = findViewById(R.id.searchBar);
        searching.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                resAdapter.getFilter().filter(newText);
                query = searching.getQuery().toString();
                return false;
            }
        });     // TextChanged

        Intent intent = getIntent();
        input = intent.getStringExtra(USER_SEARCH_RESULT);
        // Give the time for query to process
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                searching.setQuery(input, true);
            }
        }, 500);

        setUpFilterButton();
    }

    /*  Set up RecyclerView
        https://developer.android.com/guide/topics/ui/layout/recyclerview
    */

    private void setUpList(){
        recyclerList = findViewById(R.id.allResList);
        recyclerList.setHasFixedSize(true);
        listLayout = new LinearLayoutManager(this);
        recyclerList.setLayoutManager(listLayout);
        resAdapter = new RestaurantAdapter();
        recyclerList.setAdapter(resAdapter);
        resAdapter.setOnResClickListener(new RestaurantAdapter.OnResClickListener() {
            @Override
            public void onResClick(int pos) {
                Log.i("Main - Res Click", "@Pos: " + pos);
                Intent i = RestaurantDetail.makeLaunchIntent(ListAllRestaurant.this, pos, false);
                startActivity(i);
            }
        });
    }

    private void setUpFilterButton(){

        dialogFilter = new Dialog(ListAllRestaurant.this);
        dialogFilter.setContentView((R.layout.filter_dialog));
        dialogFilter.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#ffffff")));
        dialogFilter.setCancelable(false);


        ImageView filter_button = (ImageView) findViewById(R.id.filterbtn);
        filter_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogFilter.show();
                getButtonData();
            }
        });

    }

    private void getButtonData(){

        radioGroup_hzd = (RadioGroup) dialogFilter.findViewById(R.id.radio_hazard);
        radioGroup_critical = (RadioGroup) dialogFilter.findViewById(R.id.radio_critical);

        final Button apply_filter = (Button) dialogFilter.findViewById(R.id.Apply_filter);
        final Button cancel_filter = (Button) dialogFilter.findViewById(R.id.Cancel_filter);
        final Button reset_filter = (Button) dialogFilter.findViewById(R.id.Reset_filter);
        num_critical_filter = (EditText) dialogFilter.findViewById(R.id.int_critical);
        Log.i("initiated", "buttons");

        //set on click listener on apply button
        apply_filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(radioGroup_hzd.isEnabled()) {
                    int radioId_hzd = radioGroup_hzd.getCheckedRadioButtonId();
                    radioButton_hzd = (RadioButton) dialogFilter.findViewById(radioId_hzd);
                    //hazard_filter = radioButton_hzd.getText().toString();
                }
                if(radioGroup_critical.isEnabled()) {
                    int radioId_critical = radioGroup_critical.getCheckedRadioButtonId();
                    radioButton_critical = (RadioButton) dialogFilter.findViewById(radioId_critical);
                    //critical_filter = radioButton_critical.getText().toString();
                    /*if(num_critical_filter.getText().equals(null))
                    {
                        radioButton_critical.setChecked(false);
                    }*/
                }
                radioButton_fav = (RadioButton) dialogFilter.findViewById(R.id.favourites_filter_btn);
                if(radioButton_fav.isEnabled())
                {
                    isFav = true;
                }
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
                radioButton_hzd.setChecked(false);
                radioButton_critical.setChecked(false);
                radioButton_fav.setChecked(false);
            }
        });
    }

    /*public void setFilteredList(){
        for (Restaurant res : resList.getRestaurants()) {
            ArrayList<InspectionReport> report = res.getInspectionReports();
            //sort the inspection report
            Collections.sort(report, new Comparator<InspectionReport>() {
                @Override
                public int compare(InspectionReport o1, InspectionReport o2) {
                    return o2.getInspectionDate().compareTo(o1.getInspectionDate());
                }
            });
            if(!hazard_filter.equals(null) && (!critical_filter.equals(null)) && isFav){
                for(Restaurant r: favList)
                {
                    if(res.equals(r)) {
                        inFavList = true;
                        break;
                    }
                }
                if(inFavList){
                    if(report.get(0).getHazardRating().equals(hazard_filter)){
                        if(critical_filter.equals(">=")){
                            int num = 2;
                            if(report.get(0).getNumCritical() >= num){
                                tempList.add(res);
                            }
                        }
                        else {
                            int num = 2;
                            if (report.get(0).getNumCritical() <= num) {
                                tempList.add(res);
                            }
                        }
                    }
                }
            }
            else if(!hazard_filter.equals(null) && !critical_filter.equals(null) && !isFav){
                if(report.get(0).getHazardRating().equals(hazard_filter)){
                    if(critical_filter.equals(">=")){
                        int num = 2;
                        if(report.get(0).getNumCritical() >= num){
                            tempList.add(res);
                        }
                    }
                    else {
                        int num = 2;
                        if (report.get(0).getNumCritical() <= num) {
                            tempList.add(res);
                        }
                    }
                }
            }
            else if(!hazard_filter.equals(null) && critical_filter.equals(null) && isFav){
                for(Restaurant r: favList)
                {
                    if(res.equals(r)) {
                        inFavList = true;
                        break;
                    }
                }
                if(inFavList){
                    if(report.get(0).getHazardRating().equals(hazard_filter)){
                        tempList.add(res);
                    }
                }
            }
            else if(hazard_filter.equals(null) && !critical_filter.equals(null) && isFav){
                for(Restaurant r: favList)
                {
                    if(res.equals(r)) {
                        inFavList = true;
                        break;
                    }
                }
                if(inFavList){
                    if(critical_filter.equals(">=")){
                        int num = 2;
                        if(report.get(0).getNumCritical() >= num){
                            tempList.add(res);
                        }
                    }
                    else {
                        int num = 2;
                        if (report.get(0).getNumCritical() <= num) {
                            tempList.add(res);
                        }
                    }
                }
            }
            else if(hazard_filter.equals(null) && !critical_filter.equals(null) && !isFav){
                if(critical_filter.equals(">=")){
                    int num = 2;
                    if(report.get(0).getNumCritical() >= num){
                        tempList.add(res);
                    }
                }
                else {
                    int num = 2;
                    if (report.get(0).getNumCritical() <= num) {
                        tempList.add(res);
                    }
                }
            }
            else if(!hazard_filter.equals(null) && critical_filter.equals(null) && !isFav){
                if(report.get(0).getHazardRating().equals(hazard_filter)){
                    tempList.add(res);
                }
            }
            else if(hazard_filter.equals(null) && critical_filter.equals(null) && isFav){
                for(Restaurant r: favList)
                {
                    if(res.equals(r)) {
                        inFavList = true;
                        break;
                    }
                }
                tempList.add(res);
            }
            else if(hazard_filter.equals(null) && critical_filter.equals(null) && !isFav){
                //do nothing go to next step
            }
            if (res.getResName().toLowerCase().contains(input.toLowerCase())){
                tempList2.add(res);
            }

            if(tempList.size() == 0)
            {
                filteredList = tempList2;
            }
            else
            {
                for(int i=0;i<tempList.size();i++){
                    for(int j=0;i<tempList2.size();j++)
                    {
                        if(tempList.get(i).equals(tempList2.get(j))){
                            filteredList.add(tempList.get(i));
                        }
                    }
                }
            }
        }
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_list_restaurants, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.menu_to_maps:
                Intent i = MapsActivity.makeLaunchIntent(ListAllRestaurant.this);
                i.putExtra("User input", query);
                // Putting value to display all maps if query is empty
                if (query.isEmpty()) {
                    i.putExtra("Initial map run", 1);
                }
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
        Log.e("All Restaurant List - Back Button", "This should not print");
    }
}