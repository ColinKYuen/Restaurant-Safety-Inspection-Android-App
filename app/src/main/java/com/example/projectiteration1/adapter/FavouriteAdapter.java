package com.example.projectiteration1.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectiteration1.R;
import com.example.projectiteration1.model.Restaurant;

import java.util.ArrayList;

public class FavouriteAdapter extends RecyclerView.Adapter<FavouriteAdapter.ViewHolder> {
    private ArrayList<Restaurant> favList;

    public FavouriteAdapter(ArrayList<Restaurant> incList){
        favList = incList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.condense_list, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Restaurant res = favList.get(position);
        Log.i("Fav - onBind", "Name: " + res.getResName());
        holder.resName.setText(res.getResName());
        holder.resIcon.setImageResource(res.getImg());
    }

    @Override
    public int getItemCount() {
        return favList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public ImageView resIcon;
        public TextView resName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            resIcon = itemView.findViewById(R.id.condenseIcon);
            resName = itemView.findViewById(R.id.condenseName);
        }
    }
}
