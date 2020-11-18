package com.example.projectiteration1.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectiteration1.R;
import com.example.projectiteration1.adapter.RestaurantAdapter;
import com.example.projectiteration1.model.*;

import java.util.Map;

/**
 * UI Logic for listing all Restaurants in CardView via RecyclerView
 */
public class ListAllRestaurant extends AppCompatActivity {
    private RecyclerView recyclerList;
    private RestaurantAdapter resAdapter;
    private RecyclerView.LayoutManager listLayout;
    private RestaurantsList resList;

    public static Intent makeLaunchIntent(Context c) {
        return new Intent(c, ListAllRestaurant.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_restaurants);
        resList = RestaurantsList.getInstance();

        setUpList();
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