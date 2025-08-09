package com.example.damonditrichs_weight_tracking_app;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {User.class, Weight.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserDao userDao();
    public abstract WeightDao weightDao();
}
