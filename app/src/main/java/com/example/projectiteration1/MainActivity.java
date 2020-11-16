package com.example.projectiteration1;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
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

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.example.projectiteration1.model.InspectionReport;
import com.example.projectiteration1.model.Restaurant;
import com.example.projectiteration1.model.RestaurantsList;
import com.example.projectiteration1.model.SurreyDataSet;
import com.example.projectiteration1.model.Violation;
import com.example.projectiteration1.ui.ListAllRestaurant;
import com.opencsv.CSVReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    // SharedPreferences support
    public static final String FILE_NAME_VERSION = "File name version";
    public static final String LAST_FILE_NAME_VERSION = "Last file name version";
    public static final String LAST_MODIFIED_RES = "Last modified Res";
    public static final String LAST_MODIFIED_FILE_DATE_RES = "Last modified file date Res";
    private static final String LAST_MODIFIED_INSPECT = "Last modified Inspections";
    private static final String LAST_MODIFIED_FILE_DATE_INSPECT = "Last modified file date Inspections";
    public static final String LAST_VISITED_DATE = "Last visited Time";
    private static final String LAST_MODIFIED_DATE = "Last checked Time";

    private RestaurantsList restaurantList;                                 // List of restaurants
    private ArrayList<InspectionReport> reportsList = new ArrayList<>();    // List of reports. Read from csv
    final SurreyDataSet surreyDataSet = new SurreyDataSet();                // New data reader from URLs

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get singleton class of restaurants
        restaurantList = RestaurantsList.getInstance();

        Date newDate = new Date();
        Date oldDate = new Date(getLastCheckedDate());

        final long TWENTY_HOURS = 3600 * 1000 * 20;         // seconds in hour * millisecond * #of hours

        if (newDate.getTime() - oldDate.getTime() > TWENTY_HOURS) {
            saveLastCheckedDate();
            System.out.println(newDate.toString());
            System.out.println(oldDate.toString());

            // TODO: check in with server for info
            // Consider each user action possibility
        }

        try {
            // The URL for reading the JSON web file
            String resUrl = "https://data.surrey.ca/api/3/action/package_show?id=restaurants";
            String inspectionsUrl = "https://data.surrey.ca/api/3/action/package_show?id=fraser-health-restaurant-inspection-reports";

            RequestQueue requestQueue = Volley.newRequestQueue(this);
            requestQueue.add(surreyDataSet.readRestaurantData(resUrl));               // Read Restaurants
            requestQueue.add(surreyDataSet.readRestaurantData(inspectionsUrl));       // Read Inspection reports

            try {
                final Handler handler = new Handler();
                final Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        if (!surreyDataSet.isEmpty()) {                     // Data has not been processed yet
                            String[] names = getNamesForFiles();            // Naming to store files
                            openDownloadDialog(surreyDataSet.getCsvURLFiles(), names);     // Option to update data
                        }
                    }
                };


                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        runnable.run();
                        if (surreyDataSet.isEmpty()) {
                            handler.postDelayed(runnable, 3000); // Extra 3 sec if file were not processed yet
                        }
                    }
                }, 3000);    // The default value to process CSV files

            } catch(Exception e) {
                Log.e("Getting CSV URL from web", "not enough time to process the url link");
            }
        } catch (Exception e) {
            Log.e("MainActivity: establishing connection with server", "Error processing server");
            Log.d("MainActivity: Reading data", "Reading initial data set...");

            // Read reports data from csv.
            try{
                readReportsData(new InputStreamReader(getResources().openRawResource(R.raw.reports_list)), true);
            }catch(Exception exception) {
                Log.e("MainActivity - Read Inspects", "Error Reading the File");
                exception.printStackTrace();
            }

            // Read restaurant data from csv.
            try{
                readRestaurantData(new InputStreamReader(getResources().openRawResource(R.raw.res_list)));
            }catch(Exception exception){
                Log.e("MainActivity - Read Res", "Error Reading the File");
                exception.printStackTrace();
            }
        }




        try{
           // readReportsData(new InputStreamReader(getFileInputStream(inspecFileName)), false);
        }catch(Exception e){
            Log.e("MainActivity - Read Res", "Error Reading the File");
            e.printStackTrace();
        }

        try{
            //readRestaurantData(new InputStreamReader(getFileInputStream(resFileName)));
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

    private long getLastCheckedDate() {
        SharedPreferences preferences = getSharedPreferences(LAST_MODIFIED_DATE, MODE_PRIVATE);
        return preferences.getLong(LAST_VISITED_DATE, 0);
    }

    private void saveLastCheckedDate() {
        SharedPreferences preferences = getSharedPreferences(LAST_MODIFIED_DATE, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        Date date = new Date();
        editor.putLong(LAST_VISITED_DATE, date.getTime());
        editor.apply();
    }

    private String getLastModifiedRes() {
        SharedPreferences preferences = getSharedPreferences(LAST_MODIFIED_RES, MODE_PRIVATE);
        return preferences.getString(LAST_MODIFIED_FILE_DATE_RES, "Was never modified");
    }

    private void saveLastModifiedRes() {
        SharedPreferences preferences = getSharedPreferences(LAST_MODIFIED_RES, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        String newVersion = surreyDataSet.getLastModifiedRes();
        editor.putString(LAST_MODIFIED_FILE_DATE_RES, newVersion);
        editor.apply();
    }

    private String getLastModifiedInspect() {
        SharedPreferences preferences = getSharedPreferences(LAST_MODIFIED_INSPECT, MODE_PRIVATE);
        return preferences.getString(LAST_MODIFIED_FILE_DATE_INSPECT, "Was never modified");
    }

    private void saveLastModifiedInspect() {
        SharedPreferences preferences = getSharedPreferences(LAST_MODIFIED_INSPECT, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        String newVersion = surreyDataSet.getLastModifiedInspect();
        editor.putString(LAST_MODIFIED_FILE_DATE_INSPECT, newVersion);
        editor.apply();
    }

    private int getFileNameVersion() {
        SharedPreferences preferences = getSharedPreferences(FILE_NAME_VERSION, MODE_PRIVATE);
        int version = preferences.getInt(LAST_FILE_NAME_VERSION, 1);
        return version;
    }

    private void saveFileNameVersion() {
        SharedPreferences preferences = getSharedPreferences(FILE_NAME_VERSION, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        int newVersion = getFileNameVersion() + 1;
        editor.putInt(LAST_FILE_NAME_VERSION, newVersion);
        editor.commit();
    }

    private String[] getNamesForFiles() {
        int version = getFileNameVersion();
        String resName = "restaurants_v" + version + ".csv";                // Naming for the restaurant file
        String inspectName = "inspection_reports_v" + version + ".csv";     // Naming for the inspections list file
        saveFileNameVersion();
        return new String[]{resName, inspectName};
    }

    private void openDownloadDialog(final ArrayList<String> urls, final String[] names) {
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
                downloadData(urls, names);
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

    // For using download manager, followed: https://www.youtube.com/watch?v=c-SDbITS_R4
    // To show progress, followed: https://overcoder.net/q/77443/%D0%BF%D0%BE%D0%BA%D0%B0%D0%B7%D0%B0%D1%82%D1%8C-%D0%BF%D1%80%D0%BE%D0%B3%D1%80%D0%B5%D1%81%D1%81-%D0%B2-%D0%B7%D0%B0%D0%B3%D1%80%D1%83%D0%B7%D0%BA%D0%B5-%D1%81-%D0%BF%D0%BE%D0%BC%D0%BE%D1%89%D1%8C%D1%8E-downloadmanager
    public void downloadData(ArrayList<String> downloadURLs, String[] names) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = this.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.loading_dialog, null));
        builder.setCancelable(true);

        final AlertDialog dialog = builder.create();                    // Dialog for the downloading
        dialog.show();

        int count = 0;      // for documents count
        for (String url : downloadURLs) {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI |
                    DownloadManager.Request.NETWORK_MOBILE);
            request.allowScanningByMediaScanner();
            request.setTitle(names[count]);
            request.setDestinationInExternalFilesDir(this,
                    Environment.DIRECTORY_DOWNLOADS,
                    names[count]);
            count++;

            final DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            final long downloadId = manager.enqueue(request);

            final TextView progressText = dialog.findViewById(R.id.progressText);               // Progress text to track how many files read
            final ProgressBar progressBar = dialog.findViewById(R.id.progressBar);              // Progress bar

            new Thread(new Runnable() {
                @Override
                public void run() {
                    boolean downloading = true;

                    while (downloading) {
                        DownloadManager.Query query = new DownloadManager.Query();
                        query.setFilterById(downloadId);

                        Cursor cursor = manager.query(query);
                        cursor.moveToFirst();

                        int alreadyDownloaded = cursor.getInt(cursor
                                .getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                        int total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));

                        if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                                == DownloadManager.STATUS_SUCCESSFUL) {                 // Until not downloaded
                            downloading = false;
                        }

                        // For the progress bar
                        final int estimateProgress = (int) ((alreadyDownloaded * 100l) / total);

                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                progressBar.setProgress((int) estimateProgress);        // The progress for the bar

                                if (estimateProgress == 100) {                          // If downloaded
                                    progressText.setText("2 of 2");
                                    Handler handler = new Handler();
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            dialog.dismiss();                           // Close dialog
                                        }
                                    }, 3000);
                                }
                            }
                        });
                        cursor.close();
                    }
                }
            }).start();
        }
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