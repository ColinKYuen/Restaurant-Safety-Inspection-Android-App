package com.example.projectiteration1.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.projectiteration1.R;
import com.example.projectiteration1.model.InspectionReport;
import com.example.projectiteration1.model.RestaurantsList;
import com.example.projectiteration1.model.Violation;

import java.util.ArrayList;

/**
 * Listing all the violations of a specific inspection
 */
public class DetailInspection extends AppCompatActivity {
    RestaurantsList restaurantsList;
    int inspectionIndex;
    int resIndex;
    private InspectionReport ins;
    ArrayList<Violation> mylist=new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_inspection);

        //retrieve the passed data
        Intent intent = getIntent();
        resIndex=intent.getIntExtra(Intent.EXTRA_INDEX,0);
        inspectionIndex=intent.getIntExtra("Inspection index",0);
        restaurantsList= RestaurantsList.getInstance();
        ins=restaurantsList.getRestaurants().get(resIndex).getInspectionReports().get(inspectionIndex);

        //set up date
        TextView date=findViewById(R.id.inspectionDate);
        //ask
        String dateString = restaurantsList.getRestaurants().get(resIndex).getInspectionReports().get(inspectionIndex).getInspectionDate();
        int year = Integer.parseInt(dateString.substring(0,4));
        int month = Integer.parseInt(dateString.substring(4,6));
        int day = Integer.parseInt(dateString.substring(6,8));
        dateString = getMonth(month) + " " + day + ", " + year;
        date.setText(dateString);

        //set up inspectionType
        TextView inspectiontype=findViewById(R.id.inspectionType);
        //final String inspectionType= ""+restaurantsList.getRestaurants().get(resIndex).getInspectionReports().get(inspectionIndex).getInspectionType();
        if(ins.getInspectionType().equals("Routine")){
            inspectiontype.setText(R.string.routine);
        }
        else if(ins.getInspectionType().equals("Follow-Up")){
            inspectiontype.setText(R.string.followup);
        }

        //set up number of critical issues
        TextView c=findViewById(R.id.numberofCritical);
        TextView ccc=findViewById(R.id.noci);
        //final String critical="Number of Critical Issues is "+ins.getNumCritical();
        c.setText(R.string.detailedInspectionCrit);
        ccc.setText(""+ins.getNumCritical());

        //set up number of noncritical issues
        TextView nonCri=findViewById(R.id.numberofNoncritical);
        TextView nonCCCri=findViewById(R.id.nonci);
        
        //final String noncritical="Number of Non-Critical Issues is "+ins.getNumNonCritical();
        nonCri.setText(R.string.inspectionNonCrit);
        nonCCCri.setText(""+ins.getNumNonCritical());

        //set up hazard rating
        TextView hazardLevel=findViewById(R.id.hazardLevel);
        ImageView icon=findViewById(R.id.inspectionIcon);
        //hazardLevel.setText(ins.getHazardRating());
        if(ins.getHazardRating().equals("Low")) {
            hazardLevel.setText(R.string.low);
            icon.setImageResource(R.drawable.risk_low);
            hazardLevel.setTextColor(Color.parseColor("#4CBB17"));
        }
        else if(ins.getHazardRating().equals("Moderate")){
            hazardLevel.setText(R.string.moderate);
            icon.setImageResource(R.drawable.risk_medium);
            hazardLevel.setTextColor(Color.parseColor("#FF7800"));
        }
        else{
            hazardLevel.setText(R.string.high);
            icon.setImageResource(R.drawable.risk_high);
            hazardLevel.setTextColor(Color.parseColor("#E60000"));
        }

        populateViolationList();
        populateListView();
        registerClickCallBack();
    }

    private void registerClickCallBack(){
        ListView list=findViewById(R.id.violationListView);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Violation clickedVio= mylist.get(position);
                String clickedId = violationLanguage(clickedVio.getViolationID());
                final String mess= getString(R.string.thedetailoftheviolation)+
                        clickedId;
                Toast.makeText(DetailInspection.this,mess,Toast.LENGTH_LONG).show();
            }
        });
    }

    private void populateViolationList() {
        //display all the violation under that inspection
        for(int i=0; i < ins.getViolations().size();i++){
            mylist.add(ins.getViolations().get(i));
        }

        if(mylist.isEmpty()){
            TextView epy=findViewById(R.id.noViolation);
            final String s=getString(R.string.thereisnoviolations);
            epy.setText(s);
        }
        else{
            TextView epy=findViewById(R.id.noViolation);
            final String s="";
            epy.setText(s);
        }
    }

    private void populateListView() {
        ArrayAdapter<Violation> adapter=new MyListAdapter();
        ListView list=findViewById(R.id.violationListView);
        list.setAdapter(adapter);
    }

    private class MyListAdapter extends ArrayAdapter<Violation>{

        public MyListAdapter(){
            super(DetailInspection.this,R.layout.item_view,mylist);
        }
        //@NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View itemView=convertView;
            if(itemView==null){
                itemView=getLayoutInflater().inflate(R.layout.item_view,parent,false);
            }
            Violation currentVio=mylist.get(position);
            int vioId = currentVio.getViolationID();


            //Fill the text View
            TextView severity=itemView.findViewById(R.id.severityText);
            if(currentVio.getSeriousness().equals("Critical")) {
                severity.setText(R.string.critical);
            }
            else if(currentVio.getSeriousness().equals("Not Critical")) {
                severity.setText(R.string.notcritical);
            }

            TextView description=itemView.findViewById(R.id.description);
            String vioMessage = violationLanguage(vioId);
            description.setText(vioMessage);
            ImageView icon= itemView.findViewById(R.id.severityIcon);
            ImageView food= itemView.findViewById(R.id.natureofViolation);
            food.setImageResource(R.drawable.food);

            //set up the critical icon and text color
            if(currentVio.getSeriousness().equals("Critical")) {
                icon.setImageResource(R.drawable.criticalsign);
                severity.setTextColor(Color.parseColor("#E60000"));
            }
            else {
                icon.setImageResource(R.drawable.non_critical);
                severity.setTextColor(Color.parseColor("#4CBB17"));
            }
            return itemView;
        }
    }

    public static Intent makeLaunchIntent(Context context, int index) {
        Intent intent=new Intent(context, RestaurantDetail.class);
        intent.putExtra(Intent.EXTRA_CHOOSER_TARGETS, index);
        return intent;
    }

    private void extractData(){
        Intent intent=getIntent();
        inspectionIndex=intent.getIntExtra(Intent.EXTRA_CHOOSER_TARGETS, 0);
    }

    private final String getMonth(int month){
        switch(month){
            case 1: return getString(R.string.jan);
            case 2: return getString(R.string.feb);
            case 3: return getString(R.string.mar);
            case 4: return getString(R.string.apr);
            case 5: return getString(R.string.may);
            case 6: return getString(R.string.jun);
            case 7: return getString(R.string.jul);
            case 8: return getString(R.string.aug);
            case 9: return getString(R.string.sep);
            case 10: return getString(R.string.oct);
            case 11: return getString(R.string.nov);
            case 12: return getString(R.string.dec);
            default: return getString(R.string.Noinspection);
        }
    }

    private String violationLanguage(int vioId)
    {
        int vioLanguage;
        if(vioId == 101){
            vioLanguage = R.string.v101;
        }else if (vioId == 102){
            vioLanguage = R.string.v102;
        }else if (vioId == 103){
            vioLanguage = R.string.v103;
        }else if (vioId == 104){
            vioLanguage = R.string.v104;
        }else if (vioId == 201){
            vioLanguage = R.string.v201;
        }else if (vioId == 202){
            vioLanguage = R.string.v202;
        }else if (vioId == 203){
            vioLanguage = R.string.v203;
        }else if (vioId == 204){
            vioLanguage = R.string.v204;
        }else if (vioId == 205){
            vioLanguage = R.string.v205;
        }else if (vioId == 206){
            vioLanguage = R.string.v206;
        }else if (vioId == 208){
            vioLanguage = R.string.v208;
        }else if (vioId == 209){
            vioLanguage = R.string.v209;
        }else if (vioId == 210){
            vioLanguage = R.string.v210;
        }else if (vioId == 211){
            vioLanguage = R.string.v211;
        }else if (vioId == 212){
            vioLanguage = R.string.v212;
        }else if (vioId == 301){
            vioLanguage = R.string.v301;
        }else if (vioId == 302){
            vioLanguage = R.string.v302;
        }else if (vioId == 303){
            vioLanguage = R.string.v303;
        }else if (vioId == 304){
            vioLanguage = R.string.v304;
        }else if (vioId == 305){
            vioLanguage = R.string.v305;
        }else if (vioId == 306){
            vioLanguage = R.string.v306;
        }else if (vioId == 307){
            vioLanguage = R.string.v307;
        }else if (vioId == 308){
            vioLanguage = R.string.v308;
        }else if (vioId == 309){
            vioLanguage = R.string.v309;
        }else if (vioId == 310){
            vioLanguage = R.string.v310;
        }else if (vioId == 311){
            vioLanguage = R.string.v311;
        }else if (vioId == 312){
            vioLanguage = R.string.v312;
        }else if (vioId == 313){
            vioLanguage = R.string.v313;
        }else if (vioId == 314){
            vioLanguage = R.string.v314;
        }else if (vioId == 315){
            vioLanguage = R.string.v315;
        }else if (vioId == 401){
            vioLanguage = R.string.v401;
        }else if (vioId == 402){
            vioLanguage = R.string.v402;
        }else if (vioId == 403){
            vioLanguage = R.string.v403;
        }else if (vioId == 404){
            vioLanguage = R.string.v404;
        }else if (vioId == 501){
            vioLanguage = R.string.v501;
        }else if (vioId == 502){
            vioLanguage = R.string.v502;
        }
        else
            return "ok";
        return getString(vioLanguage);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_inspection_detail, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.menu_back:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}