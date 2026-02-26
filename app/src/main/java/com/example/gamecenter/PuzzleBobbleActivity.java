package com.example.gamecenter;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.gamecenter.data.GameCenterRepository;

public class PuzzleBobbleActivity extends AppCompatActivity implements PuzzleBobbleView.GameListener {

    private final Handler handler = new Handler(Looper.getMainLooper());

    private GameCenterRepository repository;
    private long userId;
    private long gameId;

    private TextView scoreView;
    private TextView timeView;
    private PuzzleBobbleView bobbleView;

    private long startTime;
    private boolean running;

    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (!running) {
                return;
            }
            long elapsed = System.currentTimeMillis() - startTime;
            timeView.setText(TimeFormatter.formatMillis(elapsed));
            handler.postDelayed(this, 500L);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_puzzle_bobble);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.puzzle_bobble_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        repository = new GameCenterRepository(this);
        userId = getIntent().getLongExtra(MenuActivity.EXTRA_USER_ID, -1L);
        gameId = repository.getGameIdByName("Puzzle Bobble");

        scoreView = findViewById(R.id.puzzle_bobble_score_value);
        timeView = findViewById(R.id.puzzle_bobble_time_value);
        bobbleView = findViewById(R.id.puzzle_bobble_view);
        bobbleView.setGameListener(this);

        findViewById(R.id.puzzle_bobble_new).setOnClickListener(v -> startNewGame());

        startNewGame();
    }

    private void startNewGame() {
        running = true;
        startTime = System.currentTimeMillis();
        handler.removeCallbacks(timerRunnable);
        handler.post(timerRunnable);
        bobbleView.startNewGame();
        scoreView.setText("0");
        timeView.setText("00:00");
    }

    @Override
    public void onScoreChanged(int score) {
        scoreView.setText(String.valueOf(score));
    }

    @Override
    public void onGameOver(int finalScore) {
        running = false;
        handler.removeCallbacks(timerRunnable);
        long elapsed = System.currentTimeMillis() - startTime;
        if (userId > 0 && gameId > 0) {
            repository.insertScore(userId, gameId, finalScore, elapsed);
        }
        Toast.makeText(this, R.string.game_bobble_finished, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(timerRunnable);
    }
}

