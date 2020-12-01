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
import android.widget.RadioButton;
import android.widget.SearchView;
import android.widget.TextView;

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
    Dialog dialog;
    private Chip low_lvl;
    private Chip moderate_lvl;
    private Chip high_lvl;
    private Chip less_equal;
    private Chip greater_equal;
    private EditText critical_filter;

    private Button reset_filter;
    private Button cancel_filter;
    private Button apply_filter;

    private ArrayList<String> selected_data;
    private ArrayList<String> selected_chip_data;

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
        setUpFilterButton();

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

    //https://www.youtube.com/watch?v=Yu_5F1VNpjk&list=WL&index=1
    private void setUpFilterButton(){

        dialog = new Dialog(ListAllRestaurant.this);
        dialog.setContentView((R.layout.filter_dialog));
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#ffffff")));
        //dialog.setCancelable(false);


        ImageButton filter_button = (ImageButton) findViewById(R.id.filterbtn);
        filter_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
            }
        });
    }

    private void getChipData(){
        low_lvl = findViewById(R.id.chip_low);
        moderate_lvl = findViewById(R.id.chip_moderate);
        high_lvl = findViewById(R.id.chip_high);
        less_equal = findViewById(R.id.less_than_chip);
        greater_equal = findViewById(R.id.greater_than_chip);
        critical_filter = findViewById(R.id.int_critical);

        CompoundButton.OnCheckedChangeListener checkedChangeListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    selected_data.add(buttonView.getText().toString());
                }
                else
                {
                    selected_data.remove((buttonView.getText().toString()));
                }
            }
        };
        low_lvl.setOnCheckedChangeListener(checkedChangeListener);
        moderate_lvl.setOnCheckedChangeListener(checkedChangeListener);
        high_lvl.setOnCheckedChangeListener(checkedChangeListener);
        less_equal.setOnCheckedChangeListener(checkedChangeListener);
        greater_equal.setOnCheckedChangeListener(checkedChangeListener);

        apply_filter = findViewById(R.id.Apply_filter);
        apply_filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selected_chip_data = selected_data;
                dialog.dismiss();
            }
        });
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
    public void onBackPressed(){
        super.onBackPressed();
        finish();
        Log.e("All Restaurant List - Back Button", "This should not print");
    }
}