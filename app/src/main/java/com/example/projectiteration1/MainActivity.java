package com.example.projectiteration1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.example.projectiteration1.model.InspectionReport;
import com.example.projectiteration1.model.Restaurant;
import com.example.projectiteration1.model.RestaurantsList;
import com.example.projectiteration1.model.Violation;
import com.example.projectiteration1.ui.ListAllRestaurant;
import com.opencsv.CSVReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RestaurantsList restaurantList;                                 // List of restaurants
    private ArrayList<InspectionReport> reportsList = new ArrayList<>();    // List of reports. Read from csv

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get singleton class of restaurants
        restaurantList = RestaurantsList.getInstance();

        // Read reports data from csv.
        try{
            readReportsData();
        }catch(Exception e) {
            Log.e("MainActivity - Read Inspects", "Error Reading the File");
            e.printStackTrace();
        }

        // Read restaurant data from csv.
        try{
            readRestaurantData();
        }catch(Exception e){
            Log.e("MainActivity - Read Res", "Error Reading the File");
            e.printStackTrace();
        }

        // Assign reports to a restaurant
        assignInspectionReportsToRes();

        // Sort the restaurants in alphabetical order
        restaurantList.sortByName();

        // Launch into Listing all restaurants UI
        Intent i = ListAllRestaurant.makeLaunchIntent(MainActivity.this);
        startActivity(i);
    }

    private void assignInspectionReportsToRes() {
        // Inspection Reports for a single restaurant
        ArrayList<InspectionReport> oneRestaurantReports = new ArrayList<>();

        for (Restaurant restaurant : restaurantList.getRestaurants()) {
            for (InspectionReport report : reportsList) {
                // Check corresponding tracking numbers
                if (restaurant.getTrackingNumber().equals(report.getTrackingNumber())) {
                    oneRestaurantReports.add(report);
                }
            }
            // Set the reports to a restaurant
            restaurant.setInspectionReports(oneRestaurantReports);
            oneRestaurantReports = new ArrayList<>();               // Clean up space for new iteration

            // For Debugging purposes
            Log.d("MainActivity", "Assigned Reports to "
                    + restaurant.getResName() + ": "
                    + restaurant.getInspectionReports());
        }
    }

    // Followed https://www.journaldev.com/12014/opencsv-csvreader-csvwriter-example
    private void readRestaurantData() throws IOException {
        CSVReader myReader = new CSVReader(new InputStreamReader(getResources().openRawResource(R.raw.res_list)));
        List<String[]> records = myReader.readAll();
        Iterator<String[]> iterator = records.iterator();
        // Skip Header
        if(iterator.hasNext()){
            iterator.next();
        }

        while(iterator.hasNext()){
            String[] record = iterator.next();
            Restaurant restaurant = new Restaurant();
            restaurant.setTrackingNumber(record[0]);
            restaurant.setResName(record[1]);
            restaurant.setAddress(record[2]);
            restaurant.setCity(record[3]);
            restaurant.setFacType(record[4]);
            restaurant.setLatitude(record[5]);
            restaurant.setLongitude(record[6]);
            restaurant.setImg(findIcon(record[1]));

            restaurantList.add(restaurant);
            Log.d("MainActivity - Restaurant", "Just created: " + restaurant);
        }
    }

    private int findIcon(String obj){
        obj = obj.toLowerCase();
        if(obj.contains("7-eleven")){
            return R.drawable.seven_eleven;
        }
        if(obj.contains("a&w") || obj.contains("a & w")){
            return R.drawable.a_and_w;
        }
        if(obj.contains("blenz")){
            return R.drawable.blenz_coffee;
        }
        if(obj.contains("booster")){
            return R.drawable.booster_juice;
        }
        if(obj.contains("boston pizza")){
            return R.drawable.boston_pizza;
        }
        if(obj.contains("burger king")){
            return R.drawable.burger_king;
        }
        if(obj.contains("chatime")){
            return R.drawable.cha_time;
        }
        if(obj.contains("church's chicken")){
            return R.drawable.churchs_chicken;
        }
        if(obj.contains("cobs bread")){
            return R.drawable.cobs_bread;
        }
        if(obj.contains("dairy queen")){
            return R.drawable.dairy_queen;
        }
        if(obj.contains("domino's pizza")){
            return R.drawable.domino_pizza;
        }
        if(obj.contains("freshii")){
            return R.drawable.freshii;
        }
        if(obj.contains("freshslice pizza")){
            return R.drawable.freshslice_pizza;
        }
        if(obj.contains("kfc")){
            return R.drawable.kfc;
        }
        if(obj.contains("little caesars pizza")){
            return R.drawable.little_ceasar;
        }
        if(obj.contains("mcdonald")){
            return R.drawable.mcdonald;
        }
        if(obj.contains("panago")){
            return R.drawable.panago;
        }
        if(obj.contains("papa john")){
            return R.drawable.papa_johns;
        }
        if(obj.contains("pizza hut")){
            return R.drawable.pizza_hut;
        }
        if(obj.contains("save on foods")){
            return R.drawable.save_on_foods;
        }
        if(obj.contains("starbucks")){
            return R.drawable.starbucks;
        }
        if(obj.contains("subway")){
            return R.drawable.subway;
        }
        if(obj.contains("tim hortons")){
            return R.drawable.tim_hortons;
        }
        if(obj.contains("wendys")){
            return R.drawable.wendys;
        }
        if(obj.contains("white spot")){
            return R.drawable.white_spot;
        }
        if(obj.contains("t&t")){
            return R.drawable.tnt;
        }
        if(obj.contains("ihop")){
            return R.drawable.ihop;
        }
        if(obj.contains("pizza")){
            return R.drawable.pizza;
        }
        if(obj.contains("sushi")){
            return R.drawable.sushi;
        }
        if(obj.contains("chicken")){
            return R.drawable.chicken;
        }
        if(obj.contains("coffee") || obj.contains("cafe")){
            return R.drawable.coffee;
        }
        if(obj.contains("fish")){
            return R.drawable.fish;
        }
        if(obj.contains("noodles") || obj.contains("pho")){
            return R.drawable.noodles;
        }
        return R.drawable.food;
    }

    // Followed https://www.journaldev.com/12014/opencsv-csvreader-csvwriter-example
    private void readReportsData() throws IOException {
        CSVReader myReader = new CSVReader(new InputStreamReader(getResources().openRawResource(R.raw.reports_list)));
        List<String[]> records = myReader.readAll();
        Iterator<String[]> iterator = records.iterator();
        // Skip Header
        if (iterator.hasNext()) {
            iterator.next();
        }

        while (iterator.hasNext()) {
            String[] record = iterator.next();
            InspectionReport report = new InspectionReport();
            report.setTrackingNumber(record[0]);
            report.setInspectionDate(record[1]);
            report.setInspectionType(record[2]);
            report.setNumCritical(Integer.parseInt(record[3]));
            report.setNumNonCritical(Integer.parseInt(record[4]));
            report.setHazardRating(record[6]);
            if(record[5].isEmpty()){
                report.setViolations(new ArrayList<Violation>());
            }
            else{
                report.setViolations(getViolations(record[5]));
            }

            reportsList.add(report);
            Log.d("MainActivity - Reports", "Just created: " + report);
        }
    }

    private ArrayList<Violation> getViolations(String violations) {
        ArrayList<Violation> ret = new ArrayList<>();
        if(violations.isEmpty()){
            return ret;
        }

        ArrayList<String> violationList = new ArrayList<>();
        // The indexes for reading the list of all violations in a line
        int start = 0;
        int end = 0;
        // Until read all line
        while (end < violations.length()) {
            // New violation started
            if (violations.charAt(end) == '|') {
                violationList.add(violations.substring(start + 1, end));
                start = end;
            } else if (end == violations.length() - 2) {                  // Last violation in the line
                violationList.add(violations.substring(start + 1, end));
            }
            end++;
        }

        for (String singleViolation : violationList) {
            String[] attributes = singleViolation.split(",");       // Attributes of one violation
            try{
                Violation violation = new Violation(
                        Integer.parseInt(attributes[0]),                      // Violation ID
                        attributes[1],                                        // Seriousness
                        attributes[2],                                        // Description
                        attributes[3]);                                       // Reappearance
                ret.add(violation);
            }catch(Exception e){
                Log.e("Main - Get Violations", "No Viol");
            }
        }

        return ret;
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        finishAndRemoveTask();
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);
    }
}