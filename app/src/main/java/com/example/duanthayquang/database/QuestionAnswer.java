package com.example.duanthayquang.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "question_answers")
public class QuestionAnswer {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public long userId;
    public String subject;
    public String question;
    public String steps;
    public String finalAnswer;
    public boolean starred;
    public long createdAt;
}