package com.example.wordscards.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity
public class WordToCollection implements Serializable {
    @PrimaryKey(autoGenerate = true)
    @NonNull
    public Integer entityId;
    @NonNull
    public Integer wordId;
    @NonNull
    public Integer collectionId;

    public WordToCollection() {
    }
}
