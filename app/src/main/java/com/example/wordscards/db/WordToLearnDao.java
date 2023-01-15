package com.example.wordscards.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface WordToLearnDao {
    @Query("SELECT * FROM WordToLearn")
    List<WordToLearn> getAll();

    @Insert
    void insertAll(WordToLearn... words);

    @Delete
    void delete(WordToLearn word);

    @Update
    void update(WordToLearn word);

    @Query("SELECT * FROM WordToLearn WHERE wordId = :id")
    WordToLearn getById(int id);

}
