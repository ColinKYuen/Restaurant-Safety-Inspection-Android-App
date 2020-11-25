package com.example.projectiteration1.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class ConfigurationsList {
    private static final String COPY_RES_LIST = "Copy of the restaurant list3";

    public static ArrayList<Restaurant> getCopyOfList(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String json = preferences.getString(COPY_RES_LIST, "");

        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<Restaurant>>() {}.getType();
        ArrayList<Restaurant> newList = gson.fromJson(json, type);
        return newList;
    }

    public static void saveCopyOfList(Context context, ArrayList<Restaurant> list) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();

        Gson gson = new Gson();
        String json = gson.toJson(list);
        editor.putString(COPY_RES_LIST, json);
        editor.apply();
    }
}
