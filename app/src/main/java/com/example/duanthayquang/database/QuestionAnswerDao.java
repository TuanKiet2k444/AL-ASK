package com.example.duanthayquang.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface QuestionAnswerDao {

    @Insert
    long insert(QuestionAnswer qa);

    @Query("SELECT * FROM question_answers WHERE userId = :userId ORDER BY createdAt DESC")
    List<QuestionAnswer> getAllByUserId(long userId);

    @Query("SELECT * FROM question_answers WHERE userId = :userId ORDER BY createdAt DESC LIMIT 1")
    QuestionAnswer getLatestByUserId(long userId);

    @Query("SELECT * FROM question_answers WHERE id = :id LIMIT 1")
    QuestionAnswer getById(long id);

    @Query("UPDATE question_answers SET starred = NOT starred WHERE id = :id")
    void toggleStar(long id);

    @Query("SELECT COUNT(*) FROM question_answers WHERE userId = :userId AND subject = :subject")
    int countBySubject(long userId, String subject);
}