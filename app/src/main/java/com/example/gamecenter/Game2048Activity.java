package com.example.gamecenter;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.gamecenter.data.GameCenterRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Game2048Activity extends AppCompatActivity {

    private static final int SIZE = 4;
    private static final int SWIPE_THRESHOLD = 50;
    private static final long GAME_DURATION_MS = 60 * 1000; // 1 minuto en milisegundos

    private final Random random = new Random();
    private final Handler handler = new Handler(Looper.getMainLooper());

    private final int[][] board = new int[SIZE][SIZE];
    private final int[][] prevBoard = new int[SIZE][SIZE];
    private int score;
    private int prevScore;
    private long startTime;
    private long timeRemaining; // Tiempo restante en milisegundos
    private long prevTimeRemaining;

    private TextView scoreView;
    private TextView timeView;
    private SquareGridLayout gridLayout;
    private TextView[][] cells;
    private GestureDetector gestureDetector;

    private GameCenterRepository repository;
    private long userId;
    private long gameId;

    private boolean gameOver;
    private boolean swipeHandled;
    private boolean hasMovements;

    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (gameOver) {
                return;
            }

            long elapsed = System.currentTimeMillis() - startTime;
            timeRemaining = GAME_DURATION_MS - elapsed;

            if (timeRemaining <= 0) {
                timeRemaining = 0;
                timeView.setText("00:00");
                finishGameByTimeout();
                return;
            }

            timeView.setText(TimeFormatter.formatMillis(timeRemaining));
            handler.postDelayed(this, 100L); // Actualizar más frecuentemente para mejor precisión
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_game_2048);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.game2048_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        repository = new GameCenterRepository(this);
        userId = getIntent().getLongExtra(MenuActivity.EXTRA_USER_ID, -1L);
        gameId = repository.getGameIdByName("2048");

        scoreView = findViewById(R.id.game2048_score_value);
        timeView = findViewById(R.id.game2048_time_value);
        gridLayout = findViewById(R.id.game2048_grid);

        setupGrid();
        setupGestures();

        findViewById(R.id.game2048_new).setOnClickListener(v -> resetGame());
        findViewById(R.id.game2048_undo).setOnClickListener(v -> undoMove());

        resetGame();
    }

    private void setupGrid() {
        gridLayout.setColumnCount(SIZE);
        gridLayout.setRowCount(SIZE);
        cells = new TextView[SIZE][SIZE];
        int padding = getResources().getDimensionPixelSize(R.dimen.tile_padding);
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                TextView cell = new TextView(this);
                cell.setTextSize(18f);
                cell.setTextColor(Color.BLACK);
                cell.setPadding(padding, padding, padding, padding);
                cell.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = 0;
                params.height = 0;
                params.columnSpec = GridLayout.spec(col, 1f);
                params.rowSpec = GridLayout.spec(row, 1f);
                params.setMargins(6, 6, 6, 6);
                cell.setLayoutParams(params);
                gridLayout.addView(cell);
                cells[row][col] = cell;
            }
        }
    }

    private void setupGestures() {
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                if (swipeHandled || e1 == null || e2 == null) {
                    return false;
                }
                float diffX = e2.getX() - e1.getX();
                float diffY = e2.getY() - e1.getY();
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD) {
                        swipeHandled = true;
                        move(diffX > 0 ? Direction.RIGHT : Direction.LEFT);
                        return true;
                    }
                } else {
                    if (Math.abs(diffY) > SWIPE_THRESHOLD) {
                        swipeHandled = true;
                        move(diffY > 0 ? Direction.DOWN : Direction.UP);
                        return true;
                    }
                }
                return false;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (swipeHandled || e1 == null || e2 == null) {
                    return false;
                }
                float diffX = e2.getX() - e1.getX();
                float diffY = e2.getY() - e1.getY();
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD) {
                        swipeHandled = true;
                        move(diffX > 0 ? Direction.RIGHT : Direction.LEFT);
                        return true;
                    }
                } else {
                    if (Math.abs(diffY) > SWIPE_THRESHOLD) {
                        swipeHandled = true;
                        move(diffY > 0 ? Direction.DOWN : Direction.UP);
                        return true;
                    }
                }
                return false;
            }
        });

        gridLayout.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                swipeHandled = false;
            }
            return gestureDetector.onTouchEvent(event);
        });
    }

    private void resetGame() {
        score = 0;
        gameOver = false;
        swipeHandled = false;
        hasMovements = false;
        timeRemaining = GAME_DURATION_MS; // Inicializar con 1 minuto

        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                board[row][col] = 0;
                prevBoard[row][col] = 0;
            }
        }
        addRandomTile();
        addRandomTile();

        // Guardar el estado inicial como estado previo
        saveUndoState();
        hasMovements = false; // Resetear después de guardar el estado inicial

        startTime = System.currentTimeMillis();
        handler.removeCallbacks(timerRunnable);
        handler.post(timerRunnable);
        updateUi();
    }

    private void saveUndoState() {
        for (int row = 0; row < SIZE; row++) {
            System.arraycopy(board[row], 0, prevBoard[row], 0, SIZE);
        }
        prevScore = score;
        prevTimeRemaining = timeRemaining;
    }

    private void undoMove() {
        // Verificar si hay movimientos que deshacer
        if (!hasMovements) {
            Toast.makeText(this, "No hay movimientos que deshacer", Toast.LENGTH_SHORT).show();
            return;
        }

        // Restaurar el tablero, puntuación y tiempo restante
        for (int row = 0; row < SIZE; row++) {
            System.arraycopy(prevBoard[row], 0, board[row], 0, SIZE);
        }
        score = prevScore;
        timeRemaining = prevTimeRemaining;

        // Recalcular el startTime basado en el tiempo restante
        startTime = System.currentTimeMillis() - (GAME_DURATION_MS - timeRemaining);

        hasMovements = false; // Después de deshacer, no se puede deshacer de nuevo hasta el próximo movimiento
        updateUi();
    }

    private void move(Direction direction) {
        if (gameOver) {
            return;
        }
        int[][] snapshot = new int[SIZE][SIZE];
        for (int row = 0; row < SIZE; row++) {
            System.arraycopy(board[row], 0, snapshot[row], 0, SIZE);
        }
        int snapshotScore = score;
        long snapshotTimeRemaining = timeRemaining;

        boolean moved = false;
        for (int i = 0; i < SIZE; i++) {
            int[] line = extractLine(i, direction);
            LineResult result = mergeLine(line);
            if (result.moved) {
                moved = true;
            }
            score += result.scoreGained;
            writeLine(i, direction, result.line);
        }

        if (moved) {
            hasMovements = true; // Marcar que se ha realizado un movimiento
            for (int row = 0; row < SIZE; row++) {
                System.arraycopy(snapshot[row], 0, prevBoard[row], 0, SIZE);
            }
            prevScore = snapshotScore;
            prevTimeRemaining = snapshotTimeRemaining;

            addRandomTile();
            updateUi();
            if (isGameOver()) {
                finishGame();
            }
        }
    }

    private int[] extractLine(int index, Direction direction) {
        int[] line = new int[SIZE];
        for (int i = 0; i < SIZE; i++) {
            switch (direction) {
                case LEFT:
                    line[i] = board[index][i];
                    break;
                case RIGHT:
                    line[i] = board[index][SIZE - 1 - i];
                    break;
                case UP:
                    line[i] = board[i][index];
                    break;
                case DOWN:
                    line[i] = board[SIZE - 1 - i][index];
                    break;
            }
        }
        return line;
    }

    private void writeLine(int index, Direction direction, int[] line) {
        for (int i = 0; i < SIZE; i++) {
            switch (direction) {
                case LEFT:
                    board[index][i] = line[i];
                    break;
                case RIGHT:
                    board[index][SIZE - 1 - i] = line[i];
                    break;
                case UP:
                    board[i][index] = line[i];
                    break;
                case DOWN:
                    board[SIZE - 1 - i][index] = line[i];
                    break;
            }
        }
    }

    private LineResult mergeLine(int[] line) {
        List<Integer> tiles = new ArrayList<>();
        for (int value : line) {
            if (value != 0) {
                tiles.add(value);
            }
        }

        List<Integer> merged = new ArrayList<>();
        int scoreGained = 0;
        boolean moved = false;
        int i = 0;
        while (i < tiles.size()) {
            if (i + 1 < tiles.size() && tiles.get(i).equals(tiles.get(i + 1))) {
                int newValue = tiles.get(i) * 2;
                merged.add(newValue);
                scoreGained += newValue;
                i += 2;
                moved = true;
            } else {
                merged.add(tiles.get(i));
                i += 1;
            }
        }

        while (merged.size() < SIZE) {
            merged.add(0);
        }

        int[] result = new int[SIZE];
        for (int j = 0; j < SIZE; j++) {
            result[j] = merged.get(j);
            if (result[j] != line[j]) {
                moved = true;
            }
        }

        return new LineResult(result, scoreGained, moved);
    }

    private void addRandomTile() {
        List<int[]> empty = new ArrayList<>();
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (board[row][col] == 0) {
                    empty.add(new int[]{row, col});
                }
            }
        }
        if (empty.isEmpty()) {
            return;
        }
        int[] spot = empty.get(random.nextInt(empty.size()));
        board[spot[0]][spot[1]] = random.nextInt(10) == 0 ? 4 : 2;
    }

    private void updateUi() {
        scoreView.setText(String.valueOf(score));
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                int value = board[row][col];
                TextView cell = cells[row][col];
                cell.setText(value == 0 ? "" : String.valueOf(value));
                cell.setTextColor(getTileTextColor(value));
                cell.setBackground(getTileBackground(value));
            }
        }
    }

    private int getTileTextColor(int value) {
        if (value <= 4) {
            return Color.parseColor("#776E65");
        }
        return Color.parseColor("#F9F6F2");
    }

    private android.graphics.drawable.Drawable getTileBackground(int value) {
        int color = getTileColor(value);
        float radius = getResources().getDimension(R.dimen.tile_radius);
        android.graphics.drawable.GradientDrawable drawable = new android.graphics.drawable.GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(radius);
        return drawable;
    }

    private int getTileColor(int value) {
        switch (value) {
            case 0:
                return Color.parseColor("#CDC1B4");
            case 2:
                return Color.parseColor("#EEE4DA");
            case 4:
                return Color.parseColor("#EDE0C8");
            case 8:
                return Color.parseColor("#F2B179");
            case 16:
                return Color.parseColor("#F59563");
            case 32:
                return Color.parseColor("#F67C5F");
            case 64:
                return Color.parseColor("#F65E3B");
            case 128:
                return Color.parseColor("#EDCF72");
            case 256:
                return Color.parseColor("#EDCC61");
            case 512:
                return Color.parseColor("#EDC850");
            case 1024:
                return Color.parseColor("#EDC53F");
            case 2048:
                return Color.parseColor("#EDC22E");
            default:
                return Color.parseColor("#3C3A32");
        }
    }

    private boolean isGameOver() {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (board[row][col] == 0) {
                    return false;
                }
                if (row < SIZE - 1 && board[row][col] == board[row + 1][col]) {
                    return false;
                }
                if (col < SIZE - 1 && board[row][col] == board[row][col + 1]) {
                    return false;
                }
            }
        }
        return true;
    }

    private void finishGame() {
        if (gameOver) {
            return;
        }
        gameOver = true;
        handler.removeCallbacks(timerRunnable);
        long timeUsed = GAME_DURATION_MS - timeRemaining;
        int finalScore = score;
        if (userId > 0 && gameId > 0) {
            repository.insertScore(userId, gameId, finalScore, timeUsed);
        }
        Toast.makeText(this, R.string.game2048_finished, Toast.LENGTH_SHORT).show();
    }

    private void finishGameByTimeout() {
        if (gameOver) {
            return;
        }
        gameOver = true;
        handler.removeCallbacks(timerRunnable);
        int finalScore = score;
        if (userId > 0 && gameId > 0) {
            repository.insertScore(userId, gameId, finalScore, GAME_DURATION_MS);
        }
        Toast.makeText(this, "¡Tiempo agotado! Puntuación final: " + finalScore, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(timerRunnable);
    }

    private enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    private static class LineResult {
        final int[] line;
        final int scoreGained;
        final boolean moved;

        LineResult(int[] line, int scoreGained, boolean moved) {
            this.line = line;
            this.scoreGained = scoreGained;
            this.moved = moved;
        }
    }
}
