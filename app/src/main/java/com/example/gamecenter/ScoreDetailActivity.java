package com.example.gamecenter;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.gamecenter.data.GameCenterRepository;

public class ScoreDetailActivity extends AppCompatActivity {

    public static final String EXTRA_SCORE_ID = "extra_score_id";

    private GameCenterRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_score_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.score_detail_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        repository = new GameCenterRepository(this);
        long scoreId = getIntent().getLongExtra(EXTRA_SCORE_ID, -1L);
        if (scoreId > 0) {
            bindScore(scoreId);
        }
    }

    private void bindScore(long scoreId) {
        Cursor cursor = repository.getScoreById(scoreId);
        try {
            if (cursor.moveToFirst()) {
                String userName = cursor.getString(cursor.getColumnIndexOrThrow("user_name"));
                String gameName = cursor.getString(cursor.getColumnIndexOrThrow("game_name"));
                int scoreValue = cursor.getInt(cursor.getColumnIndexOrThrow("score_value"));
                long timeMs = cursor.getLong(cursor.getColumnIndexOrThrow("time_ms"));
                long createdAt = cursor.getLong(cursor.getColumnIndexOrThrow("created_at"));

                ((TextView) findViewById(R.id.detail_user)).setText(userName);
                ((TextView) findViewById(R.id.detail_game)).setText(gameName);
                ((TextView) findViewById(R.id.detail_score)).setText(String.valueOf(scoreValue));
                ((TextView) findViewById(R.id.detail_time)).setText(TimeFormatter.formatMillis(timeMs));
                ((TextView) findViewById(R.id.detail_date)).setText(android.text.format.DateFormat.format("yyyy-MM-dd HH:mm", createdAt));
            }
        } finally {
            cursor.close();
        }
    }
}

