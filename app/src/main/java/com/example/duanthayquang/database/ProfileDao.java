package com.example.duanthayquang.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface ProfileDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insert(Profile profile);

    @Update
    void update(Profile profile);

    @Query("SELECT * FROM profiles WHERE userId = :userId LIMIT 1")
    Profile getByUserId(long userId);
}