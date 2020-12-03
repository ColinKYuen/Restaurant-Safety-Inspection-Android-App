package com.example.projectiteration1.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectiteration1.R;
import com.example.projectiteration1.model.InspectionReport;
import com.example.projectiteration1.model.Restaurant;
import com.example.projectiteration1.model.RestaurantsList;
import com.example.projectiteration1.ui.DetailInspection;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

/**
 * Adapter to fit data of the restaurant's inspections
 */
public class InspectionAdapter extends RecyclerView.Adapter<InspectionAdapter.MyViewHolder> {
    RestaurantsList res_list = RestaurantsList.getInstance();
    Context context;
    ArrayList<InspectionReport> list;
    int rest_index;
    Restaurant res = res_list.getRestaurants().get(rest_index);

    //constructor
    public InspectionAdapter(Context c, ArrayList<InspectionReport> inspectionList, int index)
    {
        context = c;
        list = inspectionList;
        rest_index = index;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.inspection_list, parent, false);
        return new MyViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
        if(list.size()==0)
        {
            InspectionReport report = new InspectionReport();
            report.setHazardRating("Low");
            report.setInspectionDate("11111111");
            report.setNumCritical(0);
            report.setNumNonCritical(0);
        }
        //hazard
        if(list.get(position).getHazardRating().equals("Low")) {
            holder.hazard.setText(R.string.low);
        }
        else if(list.get(position).getHazardRating().equals("Moderate")){
            holder.hazard.setText(R.string.moderate);
        }
        else if(list.get(position).getHazardRating().equals("High")){
            holder.hazard.setText(R.string.high);
        }
        //critical issue
        holder.critical.setText(R.string.detailedInspectionCrit /*+ ""+list.get(position).getNumCritical()*/);
        holder.CI.setText(""+list.get(position).getNumCritical());

        //non-critical issues
        holder.nonCritical.setText(R.string.inspectionNonCrit/* + list.get(position).getNumNonCritical()*/);
        holder.NCI.setText(""+list.get(position).getNumNonCritical());

        String dateString = list.get(position).getInspectionDate();
        int year = Integer.parseInt(dateString.substring(0,4));
        int month = Integer.parseInt(dateString.substring(4,6));
        int day = Integer.parseInt(dateString.substring(6,8));

        LocalDate dateInspection = LocalDate.of(year, month, day);
        LocalDate currDate = LocalDate.now();

        long daysPast = ChronoUnit.DAYS.between(dateInspection, currDate);

        String textViewDate;
        if(year == 1111){
            textViewDate = context.getResources().getString(R.string.noinspectionyet);
        }
        else if(daysPast <= 30){
            textViewDate = daysPast + " "+context.getResources().getString(R.string.dayssinceinspection);
        }
        else if(daysPast <= 365){
            //Month - Day
            textViewDate = context.getResources().getString(R.string.inspectionon) + getMonth(month) + " " + day;
        }
        else{
            //Month - Year
            textViewDate = context.getResources().getString(R.string.inspectionon) +" "+ getMonth(month) + " " + year;
        }
        holder.date.setText(textViewDate);

        //image
        if(list.get(position).getHazardRating().equals("Low")) {
            holder.risk.setImageResource(R.drawable.risk_low);
            holder.hazard.setTextColor(Color.parseColor("#4CBB17"));
        }
        else if(list.get(position).getHazardRating().equals("Moderate")){
            holder.risk.setImageResource(R.drawable.risk_medium);
            holder.hazard.setTextColor(Color.parseColor("#FF7800"));
        }
        else{
            holder.risk.setImageResource(R.drawable.risk_high);
            holder.hazard.setTextColor(Color.parseColor("#E60000"));
        }

       /* Intent i = DetailInspection.makeLaunchIntent(context, position);
        //Intent intent=new Intent(ListAllRestaurant.this,DetailInspection.class);
        i.putExtra("Insp",position);*/

    }


    private String getMonth(int month){
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
        return list.size();
    }

    public static Intent makeLaunchIntent(Context c, int index) {
        return new Intent(c, DetailInspection.class);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView hazard, date, critical, nonCritical,CI,NCI;
        ImageView risk;
        ConstraintLayout inspection_layout;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            hazard = itemView.findViewById(R.id.hazardLevel);
            date = itemView.findViewById(R.id.date);
            critical = itemView.findViewById(R.id.critical);
            nonCritical = itemView.findViewById(R.id.nonCritical);
            risk = itemView.findViewById(R.id.RiskImage);
            inspection_layout = itemView.findViewById(R.id.inspection_layout);
            CI=itemView.findViewById(R.id.CI);
            NCI=itemView.findViewById(R.id.NCI);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, DetailInspection.class);
                    intent.putExtra(Intent.EXTRA_INDEX, rest_index);
                    int position=getAdapterPosition();
                    intent.putExtra("Inspection index",position);
                    context.startActivity(intent);
                }
            });
        }
    }
}
