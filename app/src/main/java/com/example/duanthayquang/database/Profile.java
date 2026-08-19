package com.example.duanthayquang.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "profiles")
public class Profile {

    @PrimaryKey
    public long userId;

    public boolean onboarded;
    public String level;
    public String subjects;
    public String style;
    public int xp;
    public int streak;
}