package com.example.damonditrichs_weight_tracking_app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class WeightAdapter extends RecyclerView.Adapter<WeightAdapter.WeightViewHolder> {

    private final List<Weight> weightList;
    private final AppDatabase db;
    private final Context context;

    // Legacy constructor (not recommended for stat updates, included for backward compatibility)
    public WeightAdapter(List<Weight> weightList, AppDatabase db) {
        this.weightList = weightList;
        this.db = db;
        this.context = null;
    }

    // Preferred constructor with context, allows updating stats in DataDisplayActivity after deletion
    public WeightAdapter(List<Weight> weightList, AppDatabase db, Context context) {
        this.weightList = weightList;
        this.db = db;
        this.context = context;
    }

    @NonNull
    @Override
    public WeightViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_weight, parent, false);
        return new WeightViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WeightViewHolder holder, int position) {
        Weight weight = weightList.get(position);
        holder.weightTextView.setText(String.valueOf(weight.weight));
        holder.dateTextView.setText(weight.date);

        holder.deleteButton.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION && adapterPosition < weightList.size()) {
                // Delete the weight from the database and remove it from the list
                db.weightDao().delete(weightList.get(adapterPosition));
                weightList.remove(adapterPosition);
                notifyItemRemoved(adapterPosition);

                // After deletion, update the statistics in DataDisplayActivity (average and change)
                // This ensures the stats are always accurate after a weight is deleted
                if (context instanceof DataDisplayActivity) {
                    ((DataDisplayActivity) context).updateWeightStats();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return weightList.size();
    }

    static class WeightViewHolder extends RecyclerView.ViewHolder {
        TextView weightTextView;
        TextView dateTextView;
        ImageButton deleteButton;

        WeightViewHolder(@NonNull View itemView) {
            super(itemView);
            weightTextView = itemView.findViewById(R.id.weightTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}