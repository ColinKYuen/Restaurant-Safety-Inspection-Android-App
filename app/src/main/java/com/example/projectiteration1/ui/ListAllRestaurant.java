package com.example.projectiteration1.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectiteration1.R;
import com.example.projectiteration1.adapter.RestaurantAdapter;
import com.example.projectiteration1.model.ConfigurationsList;
import com.example.projectiteration1.model.InspectionReport;
import com.example.projectiteration1.model.Restaurant;
import com.example.projectiteration1.model.RestaurantsList;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;

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

    private SharedPreferences sharedPref;

    public static Dialog dialogFilter;

    public static Intent makeLaunchIntent(Context c) {
        return new Intent(c, ListAllRestaurant.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_restaurants);
        resList = RestaurantsList.getInstance();

        sharedPref = getSharedPreferences("FavRests", MODE_PRIVATE);

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
        resAdapter = new RestaurantAdapter(this);
        recyclerList.setAdapter(resAdapter);
        resAdapter.setOnResClickListener(new RestaurantAdapter.OnResClickListener() {
            @Override
            public void onResClick(String tracking) {
                int pos = 0;
                for(int i = 0; i < resList.getSize(); i++){
                    Restaurant res = resList.getRestaurants().get(i);
                    if(res.getTrackingNumber().equals(tracking)){
                        pos = i;
                        break;
                    }
                }
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

        ImageView filter_button = findViewById(R.id.filterbtn);
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

        //set on click listener on apply button
        apply_filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFilteredList();

                // Support for changing search query when filter is applied
                SearchView searching = findViewById(R.id.searchBar);
                String submitMe = searching.getQuery().toString().isEmpty() ?
                        " " : searching.getQuery().toString();
                searching.setQuery(submitMe, true);
                searching.setQuery(submitMe + " ", false);
                searching.setQuery(submitMe, true);
                if (searching.getQuery().toString().trim().isEmpty()) {
                    searching.setQuery("", false);
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
                radioGroup_hzd.clearCheck();
                RadioButton haz = dialogFilter.findViewById(R.id.btn_all);
                haz.setChecked(true);
                radioGroup_critical.clearCheck();
                RadioButton crit = dialogFilter.findViewById(R.id.less_than);
                crit.setChecked(true);

                num_critical_filter.setText("");

                switch_fav.setChecked(false);
                resAdapter.clearFilter();

                // Support for changing search query when filter is applied
                SearchView searching = findViewById(R.id.searchBar);
                String submitMe = searching.getQuery().toString().isEmpty() ?
                            " " : searching.getQuery().toString();
                searching.setQuery(submitMe, true);
                searching.setQuery(submitMe + " ", false);
                searching.setQuery(submitMe, true);
                if (searching.getQuery().toString().trim().isEmpty()) {
                    searching.setQuery("", false);
                }
                dialogFilter.dismiss();
            }
        });
    }

    public void setFilteredList(){
        final RadioGroup radioGroup_hzd = dialogFilter.findViewById(R.id.radio_hazard);
        final RadioGroup radioGroup_critical = dialogFilter.findViewById(R.id.radio_critical);
        final EditText num_critical_filter = dialogFilter.findViewById(R.id.int_critical);
        final Switch switch_fav = dialogFilter.findViewById(R.id.favourites_filter_btn);

        int numOfCrit = -1;
        String hazardFilter = "";
        ArrayList<Restaurant> filterList = new ArrayList<>();
        resAdapter.clearFilter();

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

        for(Restaurant res : resList.getRestaurants()){
            filterList.add(res.clone());
        }

        if(hazardFilter != null){
            Iterator<Restaurant> iter = filterList.iterator();
            while(iter.hasNext()){
                Restaurant res = iter.next();
                try{
                    InspectionReport report = res.getInspectionReports().get(0);
                    if(!report.getHazardRating().toLowerCase().equals(hazardFilter)){
                        iter.remove();
                    }
                }catch (Exception e){
                    iter.remove();
                }
            }
        }

        // Check Crits in the Current Year
        if(numOfCrit > 0){
            int currYear = Calendar.getInstance().getTime().getYear() + 1900;
            Iterator<Restaurant> iter = filterList.iterator();
            switch(radioGroup_critical.getCheckedRadioButtonId()){
                case R.id.less_than:
                    while(iter.hasNext()){
                        Restaurant res = iter.next();
                        int critCount = 0;
                        for(InspectionReport rep : res.getInspectionReports()){
                            int year = Integer.parseInt(rep.getInspectionDate().substring(0,4));
                            if(year == currYear){
                                critCount += rep.getNumCritical();
                            }
                        }

                        if(critCount > numOfCrit){
                            iter.remove();
                        }
                    }
                    break;
                case R.id.greater_than:
                    while(iter.hasNext()){
                        Restaurant res = iter.next();
                        int critCount = 0;
                        for(InspectionReport rep : res.getInspectionReports()){
                            int year = Integer.parseInt(rep.getInspectionDate().substring(0,4));
                            if(year == currYear){
                                critCount += rep.getNumCritical();
                            }
                        }

                        if(critCount < numOfCrit){
                            iter.remove();
                        }
                    }
                    break;
                default:
            }
        }

        // Check Favourites
        if(isFav){
            Iterator<Restaurant> iter = filterList.iterator();
            while(iter.hasNext()){
                Restaurant res = iter.next();
                String tracking = res.getTrackingNumber();
                int curr = sharedPref.getInt(tracking, -1);
                if(curr == -1){
                    // Is not fav
                    iter.remove();
                }
                else{
                    res.setFav(true);
                }
            }
        }

        // Throw Filter List
        resAdapter.setSearch(filterList);
    }



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
    public void onResume(){
        super.onResume();
        resAdapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        finish();
    }
}