package com.example.gamecenter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Random;

public class PuzzleBobbleView extends View {

    public interface GameListener {
        void onScoreChanged(int score);

        void onGameOver(int finalScore);
    }

    private static final int ROWS = 12;
    private static final int COLS = 8;
    private static final float SPEED_PX_PER_SEC = 900f;

    private final Random random = new Random();
    private final int[][] board = new int[ROWS][COLS];

    private GameListener listener;

    private float cellSize;
    private float radius;
    private float left;
    private float top;
    private float shooterX;
    private float shooterY;

    private int score;
    private boolean gameOver;

    private float bubbleX;
    private float bubbleY;
    private float velocityX;
    private float velocityY;
    private int currentColor;
    private boolean shooting;

    private float aimX;
    private float aimY;

    private long lastFrameTime;

    private final Paint bubblePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint aimPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private int[] palette;

    public PuzzleBobbleView(Context context) {
        super(context);
        init();
    }

    public PuzzleBobbleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PuzzleBobbleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setFocusable(true);
        setFocusableInTouchMode(true);
        aimPaint.setColor(getResources().getColor(R.color.bubble_aim));
        aimPaint.setStrokeWidth(4f);
        backgroundPaint.setColor(getResources().getColor(R.color.bubble_board));
        palette = new int[]{
                getResources().getColor(R.color.bubble_red),
                getResources().getColor(R.color.bubble_blue),
                getResources().getColor(R.color.bubble_green),
                getResources().getColor(R.color.bubble_yellow),
                getResources().getColor(R.color.bubble_purple),
                getResources().getColor(R.color.bubble_cyan),
                getResources().getColor(R.color.bubble_orange)
        };
    }

    public void setGameListener(@Nullable GameListener listener) {
        this.listener = listener;
    }

    public void startNewGame() {
        score = 0;
        gameOver = false;
        shooting = false;
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                board[r][c] = 0;
            }
        }
        int filledRows = 5;
        for (int r = 0; r < filledRows; r++) {
            for (int c = 0; c < COLS; c++) {
                board[r][c] = pickRandomColor();
            }
        }
        currentColor = pickRandomColor();
        bubbleX = shooterX;
        bubbleY = shooterY;
        aimX = shooterX;
        aimY = shooterY - 200f;
        if (listener != null) {
            listener.onScoreChanged(score);
        }
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float availableWidth = w - getPaddingLeft() - getPaddingRight();
        cellSize = availableWidth / COLS;
        radius = cellSize * 0.45f;
        left = getPaddingLeft() + cellSize / 2f;
        top = getPaddingTop() + cellSize / 2f;
        shooterX = getPaddingLeft() + availableWidth / 2f;
        shooterY = h - getPaddingBottom() - radius * 1.6f;
        bubbleX = shooterX;
        bubbleY = shooterY;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect(0, 0, getWidth(), getHeight(), backgroundPaint);

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                int color = board[r][c];
                if (color == 0) {
                    continue;
                }
                bubblePaint.setColor(color);
                float cx = left + c * cellSize;
                float cy = top + r * cellSize;
                canvas.drawCircle(cx, cy, radius, bubblePaint);
            }
        }

        if (!gameOver) {
            if (!shooting) {
                canvas.drawLine(shooterX, shooterY, aimX, aimY, aimPaint);
            }
            bubblePaint.setColor(currentColor);
            float drawX = shooting ? bubbleX : shooterX;
            float drawY = shooting ? bubbleY : shooterY;
            canvas.drawCircle(drawX, drawY, radius, bubblePaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (gameOver) {
            return false;
        }
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                aimX = x;
                aimY = y;
                invalidate();
                return true;
            case MotionEvent.ACTION_UP:
                if (!shooting) {
                    launchBubble(x, y);
                }
                return true;
            default:
                return super.onTouchEvent(event);
        }
    }

    private void launchBubble(float x, float y) {
        float dx = x - shooterX;
        float dy = y - shooterY;
        if (dy >= -10f) {
            dy = -10f;
        }
        float length = (float) Math.hypot(dx, dy);
        if (length < 1f) {
            return;
        }
        velocityX = dx / length * SPEED_PX_PER_SEC;
        velocityY = dy / length * SPEED_PX_PER_SEC;
        bubbleX = shooterX;
        bubbleY = shooterY;
        shooting = true;
        lastFrameTime = SystemClock.uptimeMillis();
        postOnAnimation(frameRunnable);
    }

    private final Runnable frameRunnable = new Runnable() {
        @Override
        public void run() {
            if (!shooting || gameOver) {
                return;
            }
            long now = SystemClock.uptimeMillis();
            float dt = (now - lastFrameTime) / 1000f;
            lastFrameTime = now;
            updateBubble(dt);
            invalidate();
            if (shooting && !gameOver) {
                postOnAnimation(this);
            }
        }
    };

    private void updateBubble(float dt) {
        bubbleX += velocityX * dt;
        bubbleY += velocityY * dt;

        float leftBound = getPaddingLeft() + radius;
        float rightBound = getWidth() - getPaddingRight() - radius;
        if (bubbleX < leftBound) {
            bubbleX = leftBound;
            velocityX = -velocityX;
        } else if (bubbleX > rightBound) {
            bubbleX = rightBound;
            velocityX = -velocityX;
        }

        if (bubbleY - radius <= top - radius) {
            placeBubble();
            return;
        }

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (board[r][c] == 0) {
                    continue;
                }
                float cx = left + c * cellSize;
                float cy = top + r * cellSize;
                float distance = (float) Math.hypot(bubbleX - cx, bubbleY - cy);
                if (distance <= radius * 2f - 2f) {
                    placeBubble();
                    return;
                }
            }
        }
    }

    private void placeBubble() {
        int row = Math.round((bubbleY - top) / cellSize);
        int col = Math.round((bubbleX - left) / cellSize);
        row = clamp(row, 0, ROWS - 1);
        col = clamp(col, 0, COLS - 1);

        int[] target = findNearestEmpty(row, col);
        if (target == null) {
            gameOver = true;
            if (listener != null) {
                listener.onGameOver(score);
            }
            return;
        }

        board[target[0]][target[1]] = currentColor;
        shooting = false;
        bubbleX = shooterX;
        bubbleY = shooterY;
        resolveMatches(target[0], target[1]);
        if (isBottomReached()) {
            gameOver = true;
            if (listener != null) {
                listener.onGameOver(score);
            }
            return;
        }
        currentColor = pickRandomColor();
    }

    private boolean isBottomReached() {
        for (int c = 0; c < COLS; c++) {
            if (board[ROWS - 1][c] != 0) {
                return true;
            }
        }
        return false;
    }

    private void resolveMatches(int row, int col) {
        int color = board[row][col];
        boolean[][] visited = new boolean[ROWS][COLS];
        List<int[]> cluster = new ArrayList<>();
        collectCluster(row, col, color, visited, cluster);
        if (cluster.size() >= 3) {
            for (int[] cell : cluster) {
                board[cell[0]][cell[1]] = 0;
            }
            score += cluster.size() * 10;
            removeFloatingClusters();
            if (listener != null) {
                listener.onScoreChanged(score);
            }
        }
    }

    private void removeFloatingClusters() {
        boolean[][] connected = new boolean[ROWS][COLS];
        Deque<int[]> queue = new ArrayDeque<>();
        for (int c = 0; c < COLS; c++) {
            if (board[0][c] != 0) {
                connected[0][c] = true;
                queue.add(new int[]{0, c});
            }
        }
        while (!queue.isEmpty()) {
            int[] cell = queue.removeFirst();
            for (int[] neighbor : getNeighbors(cell[0], cell[1])) {
                int nr = neighbor[0];
                int nc = neighbor[1];
                if (nr < 0 || nr >= ROWS || nc < 0 || nc >= COLS) {
                    continue;
                }
                if (board[nr][nc] == 0 || connected[nr][nc]) {
                    continue;
                }
                connected[nr][nc] = true;
                queue.add(new int[]{nr, nc});
            }
        }

        int removed = 0;
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (board[r][c] != 0 && !connected[r][c]) {
                    board[r][c] = 0;
                    removed++;
                }
            }
        }
        if (removed > 0) {
            score += removed * 5;
            if (listener != null) {
                listener.onScoreChanged(score);
            }
        }
    }

    private void collectCluster(int row, int col, int color, boolean[][] visited, List<int[]> cluster) {
        Deque<int[]> queue = new ArrayDeque<>();
        queue.add(new int[]{row, col});
        visited[row][col] = true;
        while (!queue.isEmpty()) {
            int[] cell = queue.removeFirst();
            cluster.add(cell);
            for (int[] neighbor : getNeighbors(cell[0], cell[1])) {
                int nr = neighbor[0];
                int nc = neighbor[1];
                if (nr < 0 || nr >= ROWS || nc < 0 || nc >= COLS) {
                    continue;
                }
                if (visited[nr][nc] || board[nr][nc] != color) {
                    continue;
                }
                visited[nr][nc] = true;
                queue.add(new int[]{nr, nc});
            }
        }
    }

    private List<int[]> getNeighbors(int row, int col) {
        List<int[]> neighbors = new ArrayList<>(4);
        neighbors.add(new int[]{row - 1, col});
        neighbors.add(new int[]{row + 1, col});
        neighbors.add(new int[]{row, col - 1});
        neighbors.add(new int[]{row, col + 1});
        return neighbors;
    }

    @Nullable
    private int[] findNearestEmpty(int row, int col) {
        if (board[row][col] == 0) {
            return new int[]{row, col};
        }
        int maxRadius = Math.max(ROWS, COLS);
        for (int radius = 1; radius <= maxRadius; radius++) {
            for (int dr = -radius; dr <= radius; dr++) {
                for (int dc = -radius; dc <= radius; dc++) {
                    int nr = row + dr;
                    int nc = col + dc;
                    if (nr < 0 || nr >= ROWS || nc < 0 || nc >= COLS) {
                        continue;
                    }
                    if (board[nr][nc] == 0) {
                        return new int[]{nr, nc};
                    }
                }
            }
        }
        return null;
    }

    private int pickRandomColor() {
        return palette[random.nextInt(palette.length)];
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}

