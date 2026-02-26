package com.example.gamecenter.data;

import android.provider.BaseColumns;

public final class DbContract {

    private DbContract() {
    }

    public static final class UserEntry implements BaseColumns {
        public static final String TABLE_NAME = "users";
        public static final String COLUMN_USERNAME = "username";
        public static final String COLUMN_DISPLAY_NAME = "display_name";
        public static final String COLUMN_STATUS = "status";
        public static final String COLUMN_PHOTO_URI = "photo_uri";
    }

    public static final class GameEntry implements BaseColumns {
        public static final String TABLE_NAME = "games";
        public static final String COLUMN_NAME = "name";
    }

    public static final class ScoreEntry implements BaseColumns {
        public static final String TABLE_NAME = "scores";
        public static final String COLUMN_USER_ID = "user_id";
        public static final String COLUMN_GAME_ID = "game_id";
        public static final String COLUMN_SCORE = "score";
        public static final String COLUMN_TIME_MS = "time_ms";
        public static final String COLUMN_CREATED_AT = "created_at";
    }
}

