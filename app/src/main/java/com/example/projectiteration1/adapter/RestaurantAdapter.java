package com.example.projectiteration1.adapter;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectiteration1.R;
import com.example.projectiteration1.model.InspectionReport;
import com.example.projectiteration1.model.Restaurant;
import com.example.projectiteration1.model.RestaurantsList;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Adapter to fit data of the restaurant into a cardview displaying restaurant
 */
public class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.ViewHolder>{
    private RestaurantsList resList;
    private OnResClickListener myListener;

    public RestaurantAdapter(){
        resList = RestaurantsList.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_all, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Restaurant res = null;
        InspectionReport report = null;
        if(resList != null){
            try{
                try{
                    res = resList.getRestaurants().get(position);
                    Log.i("Listing - Restaurant", "pos: " + position + " " + res.toString());
                }catch(Exception e){
                    Log.e("Adapter - onBind", "Error trying to access Restaurant");
                }

                try{
                    ArrayList<InspectionReport> allReports = res.getInspectionReports();
                    Collections.sort(allReports, new Comparator<InspectionReport>() {
                        @Override
                        public int compare(InspectionReport o1, InspectionReport o2) {
                            return o2.getInspectionDate().compareTo(o1.getInspectionDate());
                        }
                    });
                    report = allReports.get(0);
                    Log.i("Listing - Report", "pos: " + position + " " + report.toString());
                }catch(Exception e){
                    Log.e("Adapter - onBind", "Error trying to access Inspection");
                }

            }
            catch(Exception e){
                Log.e("Adapter - onBind", "Error trying to access Restaurant / Inspection");
            }
        }

        if(report == null){
            report = new InspectionReport();
            report.setHazardRating("LOW");
            report.setInspectionDate("11111111");
            report.setNumCritical(0);
            report.setNumNonCritical(0);
        }

        //Image
        holder.resImage.setImageResource(res.getImg());

        //Name
        String name = res.getResName();
        holder.resName.setText(name);

        //Location
        String loca = res.getAddress() + ", " + res.getCity();
        holder.resLoca.setText(loca);

        // Issues
        int critIssue = report.getNumCritical();
        int nonCritIssue = report.getNumNonCritical();

        String issues = "Critical: " + critIssue + " Non-Critical: " + nonCritIssue;

        String dateString = report.getInspectionDate();
        int year = Integer.parseInt(dateString.substring(0,4));
        int month = Integer.parseInt(dateString.substring(4,6));
        int day = Integer.parseInt(dateString.substring(6,8));

        Log.i("Dates", "Year: " + year + " Month: " + month + " Day: " + day);
        LocalDate dateInspection = LocalDate.of(year, month, day);
        LocalDate currDate = LocalDate.now();

        long daysPast = ChronoUnit.DAYS.between(dateInspection, currDate);
        Log.i("Days Past", "Days: " + daysPast);

        String textViewDate;
        if(year == 1111){
            textViewDate = "No Inspections Yet";
        }
        else if(daysPast <= 30){
            textViewDate = daysPast + " days since inspection";
        }
        else if(daysPast <= 365){
            //Month - Day
            textViewDate = "Inspected on: " + getMonth(month) + " " + day;
        }
        else{
            //Month - Year
            textViewDate = "Inspected on: " + getMonth(month) + " " + year;
        }

        String hazardRating = report.getHazardRating().toUpperCase();
        switch(hazardRating){
            case "LOW": // Low
                holder.resHazIcon.setImageResource(R.drawable.ic_checkmark);
                holder.resIssueFound.setTextColor(Color.parseColor("#4CBB17"));
                break;
            case "MODERATE": // Moderate
                holder.resHazIcon.setImageResource(R.drawable.ic_warning);
                holder.resIssueFound.setTextColor(Color.parseColor("#FF7800"));
                break;
            default: // High
                holder.resHazIcon.setImageResource(R.drawable.ic_biohazard);
                holder.resIssueFound.setTextColor(Color.parseColor("#E60000"));
                break;
        }

        //Issues
        holder.resIssueFound.setText(issues);

        //Date
        holder.resIssueDate.setText(textViewDate);
    }

    private String getMonth(int month){
        switch(month){
            case 1: return "Jan";
            case 2: return "Feb";
            case 3: return "Mar";
            case 4: return "Apr";
            case 5: return "May";
            case 6: return "Jun";
            case 7: return "Jul";
            case 8: return "Aug";
            case 9: return "Sept";
            case 10: return "Oct";
            case 11: return "Nov";
            case 12: return "Dec";
            default: return "No Inspection";
        }
    }

    @Override
    public int getItemCount() {
        /*
            TODO
            Requires Singleton class holding the Restaurant Data
         */
        return resList.getRestaurants().size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public ImageView resImage;
        public ImageView resHazIcon;
        public TextView resName;
        public TextView resLoca;
        public TextView resIssueFound;
        public TextView resIssueDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            resImage = itemView.findViewById(R.id.restaurantIcon);
            resName = itemView.findViewById(R.id.restaurantName);
            resLoca = itemView.findViewById(R.id.restaurantLocation);
            resIssueFound = itemView.findViewById(R.id.restaurantIssueFound);
            resIssueDate = itemView.findViewById(R.id.restaurantIssueDate);
            resHazIcon = itemView.findViewById(R.id.restaurantHazardIcon);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (myListener != null) {
                        int pos = getAdapterPosition();
                        if(pos >= 0 && pos < resList.getRestaurants().size()){
                            myListener.onResClick(pos);
                        }
                    }
                }
            });
        }
    }

    public interface OnResClickListener{
        void onResClick(int pos);
    }

    public void setOnResClickListener(OnResClickListener listener){
        myListener = listener;
    }
}
