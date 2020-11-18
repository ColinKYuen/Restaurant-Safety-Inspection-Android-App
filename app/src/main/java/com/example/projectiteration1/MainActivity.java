package com.example.projectiteration1;

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
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    // SharedPreferences support
    public static final String FILE_NAME_VERSION = "File name version14";
    public static final String LAST_FILE_NAME_VERSION = "Last file name version14";
    public static final String LAST_MODIFIED_RES = "Last modified Res14";
    public static final String LAST_MODIFIED_FILE_DATE_RES = "Last modified file date Res14";
    private static final String LAST_MODIFIED_INSPECT = "Last modified Inspections14";
    private static final String LAST_MODIFIED_FILE_DATE_INSPECT = "Last modified file date Inspections14";
    public static final String LAST_VISITED_DATE = "Last visited Time14";
    private static final String LAST_MODIFIED_DATE = "Last checked Time14";
    public static final String WAS_NEVER_MODIFIED = "Was never modified14";

    private RestaurantsList restaurantList;                                 // List of restaurants
    private ArrayList<InspectionReport> reportsList = new ArrayList<>();    // List of reports. Read from csv
    private SurreyDataSet surreyDataSet = new SurreyDataSet("", "");                // New data reader from URLs

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get singleton class of restaurants
        restaurantList = RestaurantsList.getInstance();

        Date newDate = new Date();                             // Time right now
        Date oldDate = new Date(getLastUpdatedDate());         // Time for last time app was updated

        //final long TWENTY_HOURS = 3600 * 1000 * 20;         // seconds in hour * millisecond * #of hours
        final long TWENTY_HOURS = 1000;         // seconds in hour * millisecond * #of hours

        Log.d("Last updated: ", oldDate.toString());         // Debug
        Log.d("Current time: ", newDate.toString());         // Debug
        if (newDate.getTime() - oldDate.getTime() > TWENTY_HOURS) {
            try {
                // The URL for reading the JSON web file
                String resUrl = "https://data.surrey.ca/api/3/action/package_show?id=restaurants";
                String inspectionsUrl = "https://data.surrey.ca/api/3/action/package_show?id=fraser-health-restaurant-inspection-reports";

                RequestQueue requestQueue = Volley.newRequestQueue(this);
                requestQueue.add(surreyDataSet.findRestaurantData(resUrl));               // Find Restaurants from URL
                requestQueue.add(surreyDataSet.findRestaurantData(inspectionsUrl));       // Find Inspection reports from URL

                try {
                    final Handler handler = new Handler();

                    final Runnable download = new Runnable() {
                        @Override
                        public void run() {
                            if (!surreyDataSet.isEmpty()) {                                    // Data has not been processed yet
                                String[] names = getNamesForFiles();                           // Naming to store files
                                openDownloadDialog(surreyDataSet.getCsvURLFiles(), names);     // Option to update data
                            }
                        }
                    };
                    final int[] numberOfTries = {0};
                    final Runnable checkUpdate = new Runnable() {
                        @Override
                        public void run() {
                            // if has already read files
                            boolean knowsLastModifiedDates = !surreyDataSet.getLastModifiedInspect().equals("")
                                    && !surreyDataSet.getLastModifiedRes().equals("");
                            // If files are updated
                            boolean isUpdated = !getLastModifiedRes().equals(surreyDataSet.getLastModifiedRes())
                                    || !getLastModifiedInspect().equals(surreyDataSet.getLastModifiedInspect());
                            if (knowsLastModifiedDates) {
                                if (isUpdated) {            // Data on the server has changed
                                    download.run();
                                    if (surreyDataSet.isEmpty()) {
                                        handler.postDelayed(download, 3000); // Extra 3 sec if file were not processed yet
                                    }
                                } else {
                                    surreyDataSet.sortCsv();
                                    Toast.makeText(MainActivity.this, "Running latest version", Toast.LENGTH_SHORT).show();
                                    openDataset();
                                }
                            } else {
                                numberOfTries[0]++;
                                if (numberOfTries[0] == 2) {
                                    surreyDataSet.sortCsv();
                                    Toast.makeText(MainActivity.this,
                                            "Waiting timeout, loading saved data",
                                            Toast.LENGTH_LONG).show();
                                    openDataset();
                                }
                            }
                        }
                    };

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            checkUpdate.run();

                            // Meaning that files are processed from URL
                            boolean knowsLastModifiedDates = !surreyDataSet.getLastModifiedInspect().equals("")
                                    && !surreyDataSet.getLastModifiedRes().equals("");
                            if (!knowsLastModifiedDates) {
                                handler.postDelayed(checkUpdate, 3000);   //Extra 3 sec to check file status if not right
                            }
                        }
                    }, 4000);    // The default value of 4 sec to process URL files

                } catch(Exception e) {
                    Log.e("Getting CSV URL from web", "not enough time to process the url link");
                    Toast.makeText(MainActivity.this, "Reading timeout, loading old data", Toast.LENGTH_LONG).show();
                    openDataset();
                    finish();
                }
            } catch (Exception e) {
                Log.e("MainActivity: establishing connection with server", "Error processing server");
                Log.d("MainActivity: Reading data", "Reading initial data set...");
                Toast.makeText(MainActivity.this, "Downloading failed, loading saved data", Toast.LENGTH_LONG).show();
                openDataset();
                finish();
            }
        }       // end of if: timing of the update
        else {
            openDataset();
            finish();
        }

    }

    private void openDataset() {
        Handler handler = new Handler();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (getLastUpdatedDate() == 0) {                                    // Has never been updated
                    readingInitialDataSet();
                } else {
                    try {
                        readLastSavedDataSet();                                     // CSV with last saved updates
                    } catch (Exception e) {
                        saveFileNameVersion(-1);
                        openDataset();
                        e.printStackTrace();
                    }
                }
            }
        }, 2000);       // Default value to process the list
    }

    private long getLastUpdatedDate() {
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
        return preferences.getInt(LAST_FILE_NAME_VERSION, 1);
    }

    private void saveFileNameVersion(int num) {
        SharedPreferences preferences = getSharedPreferences(FILE_NAME_VERSION, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        int newVersion = getFileNameVersion() + num;
        editor.putInt(LAST_FILE_NAME_VERSION, newVersion);
        editor.apply();
    }

    private String[] getNamesForFiles() {
        int version = getFileNameVersion();
        if(version == 0){
            return null;
        }
        String resName = "restaurants_v" + version + ".csv";                // Naming for the restaurant file
        String inspectName = "inspection_reports_v" + version + ".csv";     // Naming for the inspections list file
        Log.i("Main - Get File Name", "Res: " + resName + " Ins: " + inspectName);
        return new String[]{resName, inspectName};
    }

    private void readLastSavedDataSet() throws Exception{
        // Naming for the files that are distinguished by versions
        String[] fileNames = getNamesForFiles();
        if(fileNames == null){
            readingInitialDataSet();
            return;
        }

        // To make sure there are no duplicates stored in between app runs
        if (restaurantList.isEmpty()) {
            try {
                readReportsData(new InputStreamReader(getFileInputStream(fileNames[1])), false);
            } catch (Exception e) {
                Log.e("MainActivity - Read Res New", "Error Reading the File");
                e.printStackTrace();
                throw new Exception("IO");
            }

            try {
                readRestaurantData(new InputStreamReader(getFileInputStream(fileNames[0])));
            } catch (Exception e) {
                Log.e("MainActivity - Read Res New", "Error Reading the File");
                e.printStackTrace();
                throw new Exception("IO");
            }

            // Assign reports to a restaurant
            assignInspectionReportsToRes();

            // Sort the restaurants in alphabetical order
            restaurantList.sortByName();
        }

        // Launch into Listing all restaurants UI
        Intent i = ListAllRestaurant.makeLaunchIntent(MainActivity.this);
        startActivity(i);
        finish();
    }

    private void openDownloadDialog(final ArrayList<String> urls, final String[] names) {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.download_layout_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        ImageView closeView = dialog.findViewById(R.id.closeDialog);
        closeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Downloading canceled", Toast.LENGTH_LONG).show();
                openDataset();
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
        builder.setCancelable(false);

        final AlertDialog dialog = builder.create();                    // Dialog for the downloading
        dialog.show();
        final ImageView cancel = dialog.findViewById(R.id.cancelDownload);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Downloading canceled", Toast.LENGTH_LONG).show();
                openDataset();
                dialog.dismiss();
            }
        });

        int count = 0;      // for documents count
        final int[] filesDownloadedCounter = {0};             // To tracks how many files was already downloaded

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
                            filesDownloadedCounter[0]++;
                            if (filesDownloadedCounter[0] == 2) {      // Files has been downloaded
                                //cancel.setEnabled(false);              // Cannot cancel if files has been downloaded already
                                cancel.setOnClickListener(null);              // Cannot cancel if files has been downloaded already
                                saveFileNameVersion(1);          // Save the version of files that has been installed
                                saveLastCheckedDate();                 // Save last time update occurred
                                saveLastModifiedInspect();             // Save last modified date for the inspections
                                saveLastModifiedRes();                 // Save last modified date for the restaurants
                            }
                            downloading = false;
                        }

                        // For the progress bar
                        final int estimateProgress = (int) ((alreadyDownloaded * 100L) / total);

                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                progressBar.setProgress(estimateProgress);        // The progress for the bar
                                if (estimateProgress == 100) {                          // If downloaded
                                    progressText.setText("2 of 2");

                                    if (filesDownloadedCounter[0] == 2) {
                                        Handler handler = new Handler();
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                dialog.dismiss();                           // Close dialog
                                                openDataset();
                                            }
                                        }, 3500);
                                    }
                                }
                            }
                        });
                        cursor.close();
                    }
                }
            }).start();
        }
    }

    private void readingInitialDataSet() {
        // To allow no duplicates
        if (restaurantList.isEmpty()) {
            // Read reports data from csv.
            try {
                readReportsData(new InputStreamReader(getResources().openRawResource(R.raw.inspection_list)), false);
            } catch (Exception exception) {
                Log.e("MainActivity - Read Inspects", "Error Reading the File");
                exception.printStackTrace();
            }

            // Read restaurant data from csv.
            try {
                readRestaurantData(new InputStreamReader(getResources().openRawResource(R.raw.res_list)));
            } catch (Exception exception) {
                Log.e("MainActivity - Read Res", "Error Reading the File");
                exception.printStackTrace();
            }

            // Assign reports to a restaurant
            assignInspectionReportsToRes();

            // Sort the restaurants in alphabetical order
            restaurantList.sortByName();
        }

        // Launch into Listing all restaurants UI
        Intent i = ListAllRestaurant.makeLaunchIntent(MainActivity.this);
        startActivity(i);
        finish();
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
            restaurant.setImg(findIcon(record[1]));

            restaurantList.add(restaurant);
            Log.d("MainActivity - Restaurant", "Just created: " + restaurant);
        }
    }

    private int findIcon(String obj) {
        obj = obj.toLowerCase();
        if (obj.contains("7-eleven")) {
            return R.drawable.seven_eleven;
        }
        if (obj.contains("a&w") || obj.contains("a & w")) {
            return R.drawable.a_and_w;
        }
        if (obj.contains("blenz")) {
            return R.drawable.blenz_coffee;
        }
        if (obj.contains("booster")) {
            return R.drawable.booster_juice;
        }
        if (obj.contains("boston pizza")) {
            return R.drawable.boston_pizza;
        }
        if (obj.contains("burger king")) {
            return R.drawable.burger_king;
        }
        if (obj.contains("chatime")) {
            return R.drawable.cha_time;
        }
        if (obj.contains("church's chicken")) {
            return R.drawable.churchs_chicken;
        }
        if (obj.contains("cobs bread")) {
            return R.drawable.cobs_bread;
        }
        if (obj.contains("dairy queen")) {
            return R.drawable.dairy_queen;
        }
        if (obj.contains("domino's pizza")) {
            return R.drawable.domino_pizza;
        }
        if (obj.contains("freshii")) {
            return R.drawable.freshii;
        }
        if (obj.contains("freshslice pizza")) {
            return R.drawable.freshslice_pizza;
        }
        if (obj.contains("kfc")) {
            return R.drawable.kfc;
        }
        if (obj.contains("little caesars pizza")) {
            return R.drawable.little_ceasar;
        }
        if (obj.contains("mcdonald")) {
            return R.drawable.mcdonald;
        }
        if (obj.contains("panago")) {
            return R.drawable.panago;
        }
        if (obj.contains("papa john")) {
            return R.drawable.papa_johns;
        }
        if (obj.contains("pizza hut")) {
            return R.drawable.pizza_hut;
        }
        if (obj.contains("save on foods")) {
            return R.drawable.save_on_foods;
        }
        if (obj.contains("starbucks")) {
            return R.drawable.starbucks;
        }
        if (obj.contains("subway")) {
            return R.drawable.subway;
        }
        if (obj.contains("tim hortons")) {
            return R.drawable.tim_hortons;
        }
        if (obj.contains("wendys")) {
            return R.drawable.wendys;
        }
        if (obj.contains("white spot")) {
            return R.drawable.white_spot;
        }
        if (obj.contains("t&t")) {
            return R.drawable.tnt;
        }
        if (obj.contains("ihop")) {
            return R.drawable.ihop;
        }
        if (obj.contains("pizza")) {
            return R.drawable.pizza;
        }
        if (obj.contains("sushi")) {
            return R.drawable.sushi;
        }
        if (obj.contains("chicken")) {
            return R.drawable.chicken;
        }
        if (obj.contains("coffee") || obj.contains("cafe")) {
            return R.drawable.coffee;
        }
        if (obj.contains("fish")) {
            return R.drawable.fish;
        }
        if (obj.contains("noodles") || obj.contains("pho")) {
            return R.drawable.noodles;
        }
        return R.drawable.food;
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
            report.setNumCritical(myIntParse(record[3]));
            report.setNumNonCritical(myIntParse(record[4]));

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
                        myIntParse(attributes[0]),                            // Violation ID
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

    private int myIntParse(String text){
        int ret;
        try{
            ret = Integer.parseInt(text);
        }catch(Exception e){
            ret = 0;
        }
        return ret;
    }

    private double myDoubleParse(String text){
        double ret;
        try{
            ret = Double.parseDouble(text);
        }catch(Exception e){
            ret = 0;
        }
        return ret;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}