package com.example.wordscards.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {WordToLearn.class, CollectionsToLearn.class, WordToCollection.class}, version = 2)
public abstract class WordsDatabase extends RoomDatabase {

    public abstract WordToLearnDao wordToLearnDao();
    public abstract WordToCollectionDao wordToCollectionDao();
    public abstract CollectionsToLearnDao collectionsToLearnDao();

    private static volatile WordsDatabase INSTANCE;

    public static WordsDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (WordsDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room
                            .databaseBuilder(context.getApplicationContext(),
                                    WordsDatabase.class, "wordDatabase_db_1")
                            .addMigrations(WordsDatabase.MIGRATION_1_2)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(final SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE CollectionsToLearn (" +
                    "collectionId INTEGER PRIMARY KEY NOT NULL," +
                    "name TEXT)");
            database.execSQL("CREATE TABLE WordToCollection (" +
                    "entityId INTEGER PRIMARY KEY NOT NULL," +
                    "wordId INTEGER NOT NULL," +
                    "collectionId INTEGER NOT NULL)");
        }
    };
}
