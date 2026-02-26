package com.example.gamecenter;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.gamecenter.data.GameCenterRepository;

public class Game2Activity extends AppCompatActivity {

    private static final long GAME_DURATION_MS = 10000L;

    private GameCenterRepository repository;
    private long userId;
    private long gameId;
    private int score;
    private long startTime;
    private boolean running;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private TextView scoreView;
    private TextView timerView;
    private Button tapButton;

    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (!running) {
                return;
            }
            long elapsed = System.currentTimeMillis() - startTime;
            long remaining = Math.max(0, GAME_DURATION_MS - elapsed);
            timerView.setText(TimeFormatter.formatMillis(remaining));
            if (remaining <= 0) {
                finishGame();
            } else {
                handler.postDelayed(this, 250L);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_game2);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.game2_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        repository = new GameCenterRepository(this);
        userId = getIntent().getLongExtra(MenuActivity.EXTRA_USER_ID, -1L);
        gameId = repository.getGameIdByName("Tap Frenzy");

        scoreView = findViewById(R.id.game2_score_value);
        timerView = findViewById(R.id.game2_timer_value);
        tapButton = findViewById(R.id.game2_tap_button);

        findViewById(R.id.game2_start_button).setOnClickListener(v -> startGame());
        tapButton.setOnClickListener(v -> {
            if (!running) {
                return;
            }
            score += 1;
            scoreView.setText(String.valueOf(score));
        });
    }

    private void startGame() {
        score = 0;
        running = true;
        startTime = System.currentTimeMillis();
        scoreView.setText("0");
        tapButton.setEnabled(true);
        handler.removeCallbacks(timerRunnable);
        handler.post(timerRunnable);
    }

    private void finishGame() {
        running = false;
        tapButton.setEnabled(false);
        long elapsed = System.currentTimeMillis() - startTime;
        if (userId > 0 && gameId > 0) {
            repository.insertScore(userId, gameId, score, elapsed);
        }
        Toast.makeText(this, R.string.game2_finished, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(timerRunnable);
    }
}

