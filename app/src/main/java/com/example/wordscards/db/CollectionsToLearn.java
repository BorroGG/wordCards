package com.example.wordscards.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity
public class CollectionsToLearn implements Serializable {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    public Integer collectionId;
    public String name;

    public CollectionsToLearn() {
    }
}
