package com.example.projectiteration1.model;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

/**
 * SurreyDataSet class models the information
 * about a link to surrey data sets. It consists of
 * arraylist of csv URL links, and last modified dates
 * It supports reading the file from a URL link and
 * making JsonObjectRequests
 */
public class SurreyDataSet {
    private ArrayList<String> csvURLFiles;
    private String lastModifiedRes;
    private String lastModifiedInspect;

    public SurreyDataSet(String lastModifiedRes, String lastModifiedInspect) {
        this.csvURLFiles = new ArrayList<>();
        this.lastModifiedRes = lastModifiedRes;
        this.lastModifiedInspect = lastModifiedInspect;
    }

    // Followed tutorial: https://www.youtube.com/watch?v=DpEg_UVkv6E
    public JsonObjectRequest findRestaurantData(final String URL) {
        JsonObjectRequest objectRequest =  new JsonObjectRequest(
                Request.Method.GET,
                URL,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e("MainActivity", response.toString());
                        try {
                            JSONObject result = (JSONObject) response.get("result");            // The result json object
                            JSONArray resourceArray = (JSONArray) result.get("resources");      // The array of resource json objects
                            for (int i = 0; i < resourceArray.length(); i++) {
                                JSONObject oneResource = (JSONObject) resourceArray.get(i);

                                String lastModified = oneResource.get("last_modified").toString();
                                if (result.get("title").equals("Restaurants")                   // If it is a restaurants file
                                    && !lastModified.equals("null")) {
                                    setLastModifiedRes(lastModified);
                                    Log.d("Surrey data set", "Last modified Res Date: " + getLastModifiedRes());      // For debug
                                } else if (!lastModified.equals("null")) {
                                    setLastModifiedInspect(lastModified);
                                    Log.d("Surrey data set", "Last modified Inspect Date: " + getLastModifiedInspect());      // For debug
                                }

                                // Finding the needed "CSV" file of data
                                if (oneResource.get("format").equals("CSV")) {
                                    String csvUrl = oneResource.get("url").toString();
                                    csvURLFiles.add(csvUrl);
                                    sortCsv();
                                    Log.d("Surrey data set", csvUrl);                       // For debug
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Error response", error.toString());
                    }
                }
        );

        return objectRequest;
    }

    public boolean isEmpty() {
        return csvURLFiles.isEmpty();
    }

    public ArrayList<String> getCsvURLFiles() {
        return csvURLFiles;
    }

    public void sortCsv() {
        Collections.sort(csvURLFiles);
    }

    public String getLastModifiedRes() {
        return lastModifiedRes;
    }

    public void setLastModifiedRes(String lastModifiedRes) {
        this.lastModifiedRes = lastModifiedRes;
    }

    public String getLastModifiedInspect() {
        return lastModifiedInspect;
    }

    public void setLastModifiedInspect(String lastModifiedInspect) {
        this.lastModifiedInspect = lastModifiedInspect;
    }
}
