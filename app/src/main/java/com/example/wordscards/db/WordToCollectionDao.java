package com.example.wordscards.db;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

@Dao
public interface WordToCollectionDao {
    @Query("SELECT * FROM WordToLearn w " +
            "JOIN WordToCollection wc ON w.wordId = wc.wordId " +
            "WHERE wc.collectionId = :collectionId")
    List<WordToLearn> getWordToLearnByCollection(int collectionId);

    @Query("SELECT collectionId AS collectionId, COUNT(collectionId) AS countWords " +
            "FROM WordToCollection " +
            "GROUP BY collectionId")
    List<CollectionsWordsCount> getCountWordsInCollections();
}
