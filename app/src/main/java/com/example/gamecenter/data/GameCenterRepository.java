package com.example.gamecenter.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

public class GameCenterRepository {

    private final GameCenterDbHelper dbHelper;

    public GameCenterRepository(Context context) {
        this.dbHelper = new GameCenterDbHelper(context.getApplicationContext());
    }

    public long getOrCreateUserId(String username) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.query(
                DbContract.UserEntry.TABLE_NAME,
                new String[]{DbContract.UserEntry._ID},
                DbContract.UserEntry.COLUMN_USERNAME + " = ?",
                new String[]{username},
                null,
                null,
                null
        );
        try {
            if (cursor.moveToFirst()) {
                return cursor.getLong(0);
            }
        } finally {
            cursor.close();
        }

        ContentValues values = new ContentValues();
        values.put(DbContract.UserEntry.COLUMN_USERNAME, username);
        values.put(DbContract.UserEntry.COLUMN_DISPLAY_NAME, username);
        values.put(DbContract.UserEntry.COLUMN_STATUS, "Activo");
        return db.insert(DbContract.UserEntry.TABLE_NAME, null, values);
    }

    public long getGameIdByName(String name) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DbContract.GameEntry.TABLE_NAME,
                new String[]{DbContract.GameEntry._ID},
                DbContract.GameEntry.COLUMN_NAME + " = ?",
                new String[]{name},
                null,
                null,
                null
        );
        try {
            if (cursor.moveToFirst()) {
                return cursor.getLong(0);
            }
        } finally {
            cursor.close();
        }
        return -1L;
    }

    public long insertScore(long userId, long gameId, int score, long timeMs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DbContract.ScoreEntry.COLUMN_USER_ID, userId);
        values.put(DbContract.ScoreEntry.COLUMN_GAME_ID, gameId);
        values.put(DbContract.ScoreEntry.COLUMN_SCORE, score);
        values.put(DbContract.ScoreEntry.COLUMN_TIME_MS, timeMs);
        values.put(DbContract.ScoreEntry.COLUMN_CREATED_AT, System.currentTimeMillis());
        return db.insert(DbContract.ScoreEntry.TABLE_NAME, null, values);
    }

    public int deleteScore(long scoreId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(
                DbContract.ScoreEntry.TABLE_NAME,
                DbContract.ScoreEntry._ID + " = ?",
                new String[]{String.valueOf(scoreId)}
        );
    }

    public int getTotalPointsForUser(long userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT SUM(" + DbContract.ScoreEntry.COLUMN_SCORE + ") FROM "
                        + DbContract.ScoreEntry.TABLE_NAME
                        + " WHERE " + DbContract.ScoreEntry.COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(userId)}
        );
        try {
            if (cursor.moveToFirst()) {
                return cursor.isNull(0) ? 0 : cursor.getInt(0);
            }
        } finally {
            cursor.close();
        }
        return 0;
    }

    public Cursor queryScores(String queryText, String operator, String orderBy) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = null;
        String[] selectionArgs = null;

        if (!TextUtils.isEmpty(queryText)) {
            boolean isNumber = queryText.matches("\\d+");
            if (isNumber) {
                String op = operator;
                if (!"<".equals(op) && !">".equals(op) && !"=".equals(op)) {
                    op = "=";
                }
                selection = DbContract.ScoreEntry.TABLE_NAME + "." + DbContract.ScoreEntry.COLUMN_SCORE + " " + op + " ?";
                selectionArgs = new String[]{queryText};
            } else {
                selection = DbContract.UserEntry.TABLE_NAME + "." + DbContract.UserEntry.COLUMN_DISPLAY_NAME + " LIKE ?";
                selectionArgs = new String[]{"%" + queryText + "%"};
            }
        }

        String sortOrder;
        if ("score".equals(orderBy)) {
            sortOrder = DbContract.ScoreEntry.TABLE_NAME + "." + DbContract.ScoreEntry.COLUMN_SCORE + " DESC";
        } else if ("time".equals(orderBy)) {
            sortOrder = DbContract.ScoreEntry.TABLE_NAME + "." + DbContract.ScoreEntry.COLUMN_TIME_MS + " ASC";
        } else {
            sortOrder = DbContract.UserEntry.TABLE_NAME + "." + DbContract.UserEntry.COLUMN_DISPLAY_NAME + " COLLATE NOCASE";
        }

        String sql = "SELECT "
                + DbContract.ScoreEntry.TABLE_NAME + "." + DbContract.ScoreEntry._ID + " AS _id, "
                + DbContract.UserEntry.TABLE_NAME + "." + DbContract.UserEntry.COLUMN_DISPLAY_NAME + " AS user_name, "
                + DbContract.GameEntry.TABLE_NAME + "." + DbContract.GameEntry.COLUMN_NAME + " AS game_name, "
                + DbContract.ScoreEntry.TABLE_NAME + "." + DbContract.ScoreEntry.COLUMN_SCORE + " AS score_value, "
                + DbContract.ScoreEntry.TABLE_NAME + "." + DbContract.ScoreEntry.COLUMN_TIME_MS + " AS time_ms, "
                + DbContract.ScoreEntry.TABLE_NAME + "." + DbContract.ScoreEntry.COLUMN_CREATED_AT + " AS created_at "
                + "FROM " + DbContract.ScoreEntry.TABLE_NAME + " "
                + "JOIN " + DbContract.UserEntry.TABLE_NAME + " ON "
                + DbContract.UserEntry.TABLE_NAME + "." + DbContract.UserEntry._ID + " = "
                + DbContract.ScoreEntry.TABLE_NAME + "." + DbContract.ScoreEntry.COLUMN_USER_ID + " "
                + "JOIN " + DbContract.GameEntry.TABLE_NAME + " ON "
                + DbContract.GameEntry.TABLE_NAME + "." + DbContract.GameEntry._ID + " = "
                + DbContract.ScoreEntry.TABLE_NAME + "." + DbContract.ScoreEntry.COLUMN_GAME_ID + " ";

        if (selection != null) {
            sql += "WHERE " + selection + " ";
        }

        sql += "ORDER BY " + sortOrder;
        return db.rawQuery(sql, selectionArgs);
    }

    public Cursor getScoreById(long scoreId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT "
                + DbContract.ScoreEntry.TABLE_NAME + "." + DbContract.ScoreEntry._ID + " AS _id, "
                + DbContract.UserEntry.TABLE_NAME + "." + DbContract.UserEntry.COLUMN_DISPLAY_NAME + " AS user_name, "
                + DbContract.GameEntry.TABLE_NAME + "." + DbContract.GameEntry.COLUMN_NAME + " AS game_name, "
                + DbContract.ScoreEntry.TABLE_NAME + "." + DbContract.ScoreEntry.COLUMN_SCORE + " AS score_value, "
                + DbContract.ScoreEntry.TABLE_NAME + "." + DbContract.ScoreEntry.COLUMN_TIME_MS + " AS time_ms, "
                + DbContract.ScoreEntry.TABLE_NAME + "." + DbContract.ScoreEntry.COLUMN_CREATED_AT + " AS created_at "
                + "FROM " + DbContract.ScoreEntry.TABLE_NAME + " "
                + "JOIN " + DbContract.UserEntry.TABLE_NAME + " ON "
                + DbContract.UserEntry.TABLE_NAME + "." + DbContract.UserEntry._ID + " = "
                + DbContract.ScoreEntry.TABLE_NAME + "." + DbContract.ScoreEntry.COLUMN_USER_ID + " "
                + "JOIN " + DbContract.GameEntry.TABLE_NAME + " ON "
                + DbContract.GameEntry.TABLE_NAME + "." + DbContract.GameEntry._ID + " = "
                + DbContract.ScoreEntry.TABLE_NAME + "." + DbContract.ScoreEntry.COLUMN_GAME_ID + " "
                + "WHERE " + DbContract.ScoreEntry.TABLE_NAME + "." + DbContract.ScoreEntry._ID + " = ?";
        return db.rawQuery(sql, new String[]{String.valueOf(scoreId)});
    }
}

