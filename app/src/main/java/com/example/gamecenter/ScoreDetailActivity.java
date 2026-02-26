package com.example.gamecenter;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

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
        } else {
            Toast.makeText(this, "Error: No se encontró la puntuación", Toast.LENGTH_SHORT).show();
            finish();
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

                // Format values
                String timeFormatted = TimeFormatter.formatMillis(timeMs);
                String dateFormatted = android.text.format.DateFormat.format("dd/MM/yyyy HH:mm", createdAt).toString();

                // Update UI
                updateUI(userName, gameName, scoreValue, timeFormatted, dateFormatted);

            } else {
                Toast.makeText(this, "Puntuación no encontrada", Toast.LENGTH_SHORT).show();
                finish();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error al cargar los datos", Toast.LENGTH_SHORT).show();
            finish();
        } finally {
            cursor.close();
        }
    }

    private void updateUI(String userName, String gameName, int scoreValue, String timeFormatted, String dateFormatted) {
        ((TextView) findViewById(R.id.detail_user)).setText(userName);
        ((TextView) findViewById(R.id.detail_game)).setText(gameName);
        ((TextView) findViewById(R.id.detail_score)).setText(String.valueOf(scoreValue));
        ((TextView) findViewById(R.id.detail_time)).setText(timeFormatted);
        ((TextView) findViewById(R.id.detail_date)).setText(dateFormatted);
    }
}

