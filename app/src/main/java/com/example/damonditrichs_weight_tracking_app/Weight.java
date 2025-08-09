package com.example.damonditrichs_weight_tracking_app;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Weight {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public float weight;
    public String date;
}
