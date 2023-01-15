package com.example.wordscards.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface CollectionsToLearnDao {
    @Query("SELECT * FROM CollectionsToLearn")
    List<CollectionsToLearn> getAll();

    @Insert
    void insertAll(CollectionsToLearn... collections);

    @Delete
    void delete(CollectionsToLearn collection);

    @Update
    void update(CollectionsToLearn collection);

    @Query("SELECT * FROM CollectionsToLearn WHERE collectionId = :id")
    CollectionsToLearn getById(int id);
}
