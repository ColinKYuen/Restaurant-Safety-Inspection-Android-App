package com.example.projectiteration1;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.projectiteration1.model.InspectionReport;
import com.example.projectiteration1.model.Restaurant;
import com.example.projectiteration1.model.RestaurantsList;
import com.example.projectiteration1.model.SurreyDataSet;
import com.example.projectiteration1.model.Violation;
import com.example.projectiteration1.ui.ListAllRestaurant;
import com.opencsv.CSVReader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
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

        openDownloadDialog();

        // Get singleton class of restaurants
        restaurantList = RestaurantsList.getInstance();

        // Read reports data from csv.
        try{
            readReportsData(new InputStreamReader(getResources().openRawResource(R.raw.reports_list)), true);
        }catch(Exception e) {
            Log.e("MainActivity - Read Inspects", "Error Reading the File");
            e.printStackTrace();
        }

        // Read restaurant data from csv.
        try{
            readRestaurantData(new InputStreamReader(getResources().openRawResource(R.raw.res_list)));
        }catch(Exception e){
            Log.e("MainActivity - Read Res", "Error Reading the File");
            e.printStackTrace();
        }

        // The URL for reading the JSON web file
        String resUrl = "https://data.surrey.ca/api/3/action/package_show?id=restaurants";
        String inspectionsUrl = "https://data.surrey.ca/api/3/action/package_show?id=fraser-health-restaurant-inspection-reports";
        SurreyDataSet currentData = new SurreyDataSet();        // New data reader from URLs

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(currentData.readRestaurantData(resUrl));               // Read Restaurants
        requestQueue.add(currentData.readRestaurantData(inspectionsUrl));       // Read Inspection reports

        String resFileName = "restaurants.csv";                               // Naming for the restaurants file
        String inspecFileName = "inspection_list.csv";                        // Naming for the inspections file
        //downloadData(currentData.getCSVatIndex(0), resFileName);              // Restaurants CSV
        //downloadData(currentData.getCSVatIndex(1), inspecFileName);           // Inspections CSV

        //downloadData( "https://data.surrey.ca/dataset/948e994d-74f5-41a2-b3cb-33fa6a98aa96/resource/30b38b66-649f-4507-a632-d5f6f5fe87f1/download/fraser_health_restaurant_inspection_reports.csv", inspecFileName);
        /**
            Code still requires timing of downloading handling
            Error handling
            Refactoring for more readibility
            Would not work unless download the csv files on the emulator first (btw. data downloads ~5 mins)
            Can be used after data is installed
         */

        try{
            readReportsData(new InputStreamReader(getFileInputStream(inspecFileName)), false);
        }catch(Exception e){
            Log.e("MainActivity - Read Res", "Error Reading the File");
            e.printStackTrace();
        }

        try{
            readRestaurantData(new InputStreamReader(getFileInputStream(resFileName)));
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
        //startActivity(i);
    }

    private void openDownloadDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.download_layout_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        ImageView closeView = dialog.findViewById(R.id.closeDialog);
        closeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        Button downloadButton = dialog.findViewById(R.id.downloadButton);
        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadData( "https://data.surrey.ca/dataset/948e994d-74f5-41a2-b3cb-33fa6a98aa96/resource/30b38b66-649f-4507-a632-d5f6f5fe87f1/download/fraser_health_restaurant_inspection_reports.csv", "repptr");
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private FileInputStream getFileInputStream(String nameToGetFile) {
        File downloadsFolder = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);

        File csvFile = null;
        for (File file : downloadsFolder.listFiles()) {
            if (file.getName().equals(nameToGetFile)) {
                csvFile = new File(file.getAbsolutePath());
            }
        }

        FileInputStream stream = null;
        try {
            stream = new FileInputStream(csvFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return stream;
    }

    //Followed: https://www.youtube.com/watch?v=c-SDbITS_R4
    public void downloadData(String url, String nameOfFile) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = this.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.loading_dialog, null));
        builder.setCancelable(true);

        AlertDialog dialog = builder.create();
        dialog.show();

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI |
                DownloadManager.Request.NETWORK_MOBILE);
        request.allowScanningByMediaScanner();
        request.setTitle(nameOfFile);
        request.setDestinationInExternalFilesDir(this,
               Environment.DIRECTORY_DOWNLOADS,
               nameOfFile);


        final DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        final long downloadId = manager.enqueue(request);

        final ProgressBar mProgressBar = dialog.findViewById(R.id.progressBar);

        new Thread(new Runnable() {

            @Override
            public void run() {

                boolean downloading = true;

                while (downloading) {

                    DownloadManager.Query q = new DownloadManager.Query();
                    q.setFilterById(downloadId);

                    Cursor cursor = manager.query(q);
                    cursor.moveToFirst();
                    int bytes_downloaded = cursor.getInt(cursor
                            .getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                    int bytes_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));

                    if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                        downloading = false;
                    }

                    System.out.println(bytes_downloaded);
                    System.out.println(bytes_total);

                    final int dl_progress = (int) ((bytes_downloaded * 100l) / bytes_total);

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            mProgressBar.setProgress((int) dl_progress);
                        }
                    });

                    cursor.close();
                }

            }
        }).start();
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
    private void readRestaurantData(InputStreamReader inputReader) throws IOException {
        CSVReader myReader = new CSVReader(inputReader);
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

            restaurantList.add(restaurant);
            Log.d("MainActivity - Restaurant", "Just created: " + restaurant);
        }
    }

    // Followed https://www.journaldev.com/12014/opencsv-csvreader-csvwriter-example
    private void readReportsData(InputStreamReader inputReader, boolean isInitialDataset) throws IOException {
        CSVReader myReader = new CSVReader(inputReader);
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

            // The data for hazard rating ang violations is switched places in initial data set and Surrey API data set
            if (isInitialDataset) {
                report.setHazardRating(record[5]);
                if (record[6].isEmpty()) {
                    report.setViolations(new ArrayList<Violation>());
                } else {
                    report.setViolations(getViolations(record[6]));
                }
            } else {
                report.setHazardRating(record[6]);
                if (record[5].isEmpty()) {
                    report.setViolations(new ArrayList<Violation>());
                } else {
                    report.setViolations(getViolations(record[5]));
                }
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
}