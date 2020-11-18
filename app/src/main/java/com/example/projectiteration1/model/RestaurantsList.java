package com.example.projectiteration1.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * RestaurantList singleton class models the information
 * about a list of restaurants that can be shared between
 * activities. Data includes arrayList of Restaurant objects,
 * It supports adding new restaurant and sorting restaurants
 * in alphabetical order.
 */
public class RestaurantsList {
    private ArrayList<Restaurant> restaurants = new ArrayList<>();

    /*
        Singleton support
    */
    private RestaurantsList() {
        // To prevent from instantiating
    }
    private static RestaurantsList instance;
    public static RestaurantsList getInstance() {
        if (instance == null) {
            instance = new RestaurantsList();
        }
        return instance;
    }

    public ArrayList<Restaurant> getRestaurants() {
        return restaurants;
    }

    public void add(Restaurant restaurant) {
        restaurants.add(restaurant);
    }

    public void sortByName() {
        // Comparator for the Restaurant object
        Collections.sort(restaurants, new Comparator<Restaurant>() {
            @Override
            public int compare(Restaurant o1, Restaurant o2) {
                return o1.getResName().compareTo(o2.getResName());
            }
        });
    }

    public boolean isEmpty() {
        return restaurants.isEmpty();
    }
}
