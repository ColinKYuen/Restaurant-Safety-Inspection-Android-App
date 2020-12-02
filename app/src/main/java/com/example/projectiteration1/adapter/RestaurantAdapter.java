package com.example.projectiteration1.adapter;

import android.content.Context;
import android.content.SharedPreferences;
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
    Context context;

    public RestaurantAdapter(Context C){
        resList = RestaurantsList.getInstance();
        context=C;
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

        if(res.getFav()){
            holder.resFav.setVisibility(View.VISIBLE);
        }
        else{
            holder.resFav.setVisibility(View.INVISIBLE);
        }

        //Name
        String name = res.getResName();
        holder.resName.setText(name);

        //Location
        String loca = res.getAddress() + ", " + res.getCity();
        holder.resLoca.setText(loca);

        // Issues
        int critIssue = report.getNumCritical();
        int nonCritIssue = report.getNumNonCritical();

        String issues = context.getResources().getString(R.string.detailedInspectionCrit) + critIssue
                +"  "+ context.getResources().getString(R.string.detailedInspectionNonCrit) + nonCritIssue;

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
            textViewDate= context.getResources().getString(R.string.noinspectionyet);
        }
        else if(daysPast <= 30){
            textViewDate = daysPast + context.getResources().getString(R.string.dayssinceinspection);
        }
        else if(daysPast <= 365){
            //Month - Day
            textViewDate = context.getResources().getString(R.string.inspectionon) +" "+ getMonth(month) + " " + day;
        }
        else{
            //Month - Year
            textViewDate = context.getResources().getString(R.string.inspectionon) +" " + getMonth(month) + " " + year;
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

    private final String getMonth(int month){
        switch(month){
            case 1: return context.getResources().getString(R.string.jan);
            case 2: return context.getResources().getString(R.string.feb);
            case 3: return context.getResources().getString(R.string.mar);
            case 4: return context.getResources().getString(R.string.apr);
            case 5: return context.getResources().getString(R.string.may);
            case 6: return context.getResources().getString(R.string.jun);
            case 7: return context.getResources().getString(R.string.jul);
            case 8: return context.getResources().getString(R.string.aug);
            case 9: return context.getResources().getString(R.string.sep);
            case 10: return context.getResources().getString(R.string.oct);
            case 11: return context.getResources().getString(R.string.nov);
            case 12: return context.getResources().getString(R.string.dec);
            default: return context.getResources().getString(R.string.noinspectionyet);
        }
    }

    @Override
    public int getItemCount() {
        return resList.getRestaurants().size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public ImageView resImage;
        public ImageView resHazIcon;
        public ImageView resFav;
        public TextView resName;
        public TextView resLoca;
        public TextView resIssueFound;
        public TextView resIssueDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            resImage = itemView.findViewById(R.id.condenseIcon);
            resName = itemView.findViewById(R.id.condenseName);
            resLoca = itemView.findViewById(R.id.restaurantLocation);
            resIssueFound = itemView.findViewById(R.id.restaurantIssueFound);
            resIssueDate = itemView.findViewById(R.id.restaurantIssueDate);
            resHazIcon = itemView.findViewById(R.id.restaurantHazardIcon);
            resFav = itemView.findViewById(R.id.listFavIcon);

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
