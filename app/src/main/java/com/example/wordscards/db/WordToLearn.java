package com.example.wordscards.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity
public class WordToLearn implements Serializable {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    public Integer wordId;
    public String word;
    public String description;

    public WordToLearn() {
    }
}
