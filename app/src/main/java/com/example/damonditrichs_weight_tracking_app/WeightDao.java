package com.example.damonditrichs_weight_tracking_app;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface WeightDao {
    @Query("SELECT * FROM Weight")
    List<Weight> getAllWeights();

    @Insert
    void insert(Weight weight);

    @Update
    void update(Weight weight);

    @Delete
    void delete(Weight weight);
}
