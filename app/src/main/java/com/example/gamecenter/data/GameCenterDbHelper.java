package com.example.gamecenter.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class GameCenterDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "game_center.db";
    private static final int DATABASE_VERSION = 3;

    public GameCenterDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + DbContract.UserEntry.TABLE_NAME + " ("
                + DbContract.UserEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DbContract.UserEntry.COLUMN_USERNAME + " TEXT UNIQUE NOT NULL, "
                + DbContract.UserEntry.COLUMN_DISPLAY_NAME + " TEXT, "
                + DbContract.UserEntry.COLUMN_STATUS + " TEXT, "
                + DbContract.UserEntry.COLUMN_PHOTO_URI + " TEXT"
                + ")");

        db.execSQL("CREATE TABLE " + DbContract.GameEntry.TABLE_NAME + " ("
                + DbContract.GameEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DbContract.GameEntry.COLUMN_NAME + " TEXT UNIQUE NOT NULL"
                + ")");

        db.execSQL("CREATE TABLE " + DbContract.ScoreEntry.TABLE_NAME + " ("
                + DbContract.ScoreEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DbContract.ScoreEntry.COLUMN_USER_ID + " INTEGER NOT NULL, "
                + DbContract.ScoreEntry.COLUMN_GAME_ID + " INTEGER NOT NULL, "
                + DbContract.ScoreEntry.COLUMN_SCORE + " INTEGER NOT NULL, "
                + DbContract.ScoreEntry.COLUMN_TIME_MS + " INTEGER NOT NULL, "
                + DbContract.ScoreEntry.COLUMN_CREATED_AT + " INTEGER NOT NULL, "
                + "FOREIGN KEY(" + DbContract.ScoreEntry.COLUMN_USER_ID + ") REFERENCES "
                + DbContract.UserEntry.TABLE_NAME + "(" + DbContract.UserEntry._ID + "), "
                + "FOREIGN KEY(" + DbContract.ScoreEntry.COLUMN_GAME_ID + ") REFERENCES "
                + DbContract.GameEntry.TABLE_NAME + "(" + DbContract.GameEntry._ID + ")"
                + ")");

        db.execSQL("INSERT INTO " + DbContract.GameEntry.TABLE_NAME + " (" + DbContract.GameEntry.COLUMN_NAME + ") VALUES ('2048')");
        db.execSQL("INSERT INTO " + DbContract.GameEntry.TABLE_NAME + " (" + DbContract.GameEntry.COLUMN_NAME + ") VALUES ('Puzzle Bobble')");
        db.execSQL("INSERT INTO " + DbContract.GameEntry.TABLE_NAME + " (" + DbContract.GameEntry.COLUMN_NAME + ") VALUES ('Arcanoid')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DbContract.ScoreEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DbContract.GameEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DbContract.UserEntry.TABLE_NAME);
        onCreate(db);
    }
}
