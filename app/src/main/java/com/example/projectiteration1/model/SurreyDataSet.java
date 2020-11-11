package com.example.projectiteration1.model;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SurreyDataSet {
    // Followed tutorial: https://www.youtube.com/watch?v=DpEg_UVkv6E
    public JsonObjectRequest readRestaurantData(final String URL) {
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
                                // Finding the needed "CSV" file of data
                                if (oneResource.get("format").equals("CSV")) {
                                    String csvUrl = oneResource.get("url").toString();
                                    //System.out.println(csvUrl);
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


}
