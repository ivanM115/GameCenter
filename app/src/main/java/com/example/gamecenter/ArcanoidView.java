package com.example.gamecenter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.Random;

public class ArcanoidView extends View {

    public interface GameListener {
        void onScoreChanged(int score);

        void onLivesChanged(int lives);

        void onGameOver(int finalScore);
    }

    private static final int BRICK_ROWS = 5;
    private static final int BRICK_COLS = 8;
    private static final int INITIAL_LIVES = 3;
    private static final float BALL_SPEED = 720f;

    private final int[][] bricks = new int[BRICK_ROWS][BRICK_COLS];
    private final Random random = new Random();

    private GameListener listener;

    private float paddleWidth;
    private float paddleHeight;
    private float paddleX;
    private float paddleY;

    private float ballX;
    private float ballY;
    private float ballRadius;
    private float ballVelX;
    private float ballVelY;

    private float brickWidth;
    private float brickHeight;
    private float brickLeft;
    private float brickTop;

    private int score;
    private int lives;
    private boolean running;

    private long lastFrameTime;

    private final Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paddlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint ballPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint brickPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private int[] brickPalette;

    public ArcanoidView(Context context) {
        super(context);
        init();
    }

    public ArcanoidView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ArcanoidView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setFocusable(true);
        setFocusableInTouchMode(true);
        backgroundPaint.setColor(getResources().getColor(R.color.arcanoid_bg));
        paddlePaint.setColor(getResources().getColor(R.color.arcanoid_paddle));
        ballPaint.setColor(getResources().getColor(R.color.arcanoid_ball));
        brickPalette = new int[]{
                getResources().getColor(R.color.arcanoid_brick_1),
                getResources().getColor(R.color.arcanoid_brick_2),
                getResources().getColor(R.color.arcanoid_brick_3),
                getResources().getColor(R.color.arcanoid_brick_4)
        };
    }

    public void setGameListener(@Nullable GameListener listener) {
        this.listener = listener;
    }

    public int getLives() {
        return lives;
    }

    public void startNewGame() {
        score = 0;
        lives = INITIAL_LIVES;
        running = true;
        setupBricks();
        resetBall();
        if (listener != null) {
            listener.onScoreChanged(score);
            listener.onLivesChanged(lives);
        }
        invalidate();
        startLoop();
    }

    private void setupBricks() {
        for (int r = 0; r < BRICK_ROWS; r++) {
            for (int c = 0; c < BRICK_COLS; c++) {
                bricks[r][c] = brickPalette[(r + c) % brickPalette.length];
            }
        }
    }

    private void resetBall() {
        ballX = paddleX + paddleWidth / 2f;
        ballY = paddleY - ballRadius * 2f;
        float angle = (random.nextFloat() * 0.6f + 0.2f) * (float) Math.PI;
        ballVelX = (float) Math.cos(angle) * BALL_SPEED * (random.nextBoolean() ? 1f : -1f);
        ballVelY = -Math.abs((float) Math.sin(angle) * BALL_SPEED);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float availableWidth = w - getPaddingLeft() - getPaddingRight();
        float availableHeight = h - getPaddingTop() - getPaddingBottom();

        paddleWidth = availableWidth * 0.22f;
        paddleHeight = availableHeight * 0.03f;
        paddleX = getPaddingLeft() + (availableWidth - paddleWidth) / 2f;
        paddleY = getPaddingTop() + availableHeight * 0.9f;

        ballRadius = Math.min(availableWidth, availableHeight) * 0.018f;

        brickWidth = availableWidth / BRICK_COLS;
        brickHeight = availableHeight * 0.05f;
        brickLeft = getPaddingLeft();
        brickTop = getPaddingTop() + availableHeight * 0.08f;

        resetBall();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect(0, 0, getWidth(), getHeight(), backgroundPaint);

        for (int r = 0; r < BRICK_ROWS; r++) {
            for (int c = 0; c < BRICK_COLS; c++) {
                if (bricks[r][c] == 0) {
                    continue;
                }
                brickPaint.setColor(bricks[r][c]);
                float left = brickLeft + c * brickWidth + 4f;
                float top = brickTop + r * brickHeight + 4f;
                float right = left + brickWidth - 8f;
                float bottom = top + brickHeight - 8f;
                canvas.drawRoundRect(left, top, right, bottom, 8f, 8f, brickPaint);
            }
        }

        canvas.drawRoundRect(paddleX, paddleY, paddleX + paddleWidth, paddleY + paddleHeight, 12f, 12f, paddlePaint);
        canvas.drawCircle(ballX, ballY, ballRadius, ballPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
            float target = x - paddleWidth / 2f;
            float min = getPaddingLeft();
            float max = getWidth() - getPaddingRight() - paddleWidth;
            paddleX = clamp(target, min, max);
            invalidate();
            return true;
        }
        return super.onTouchEvent(event);
    }

    private void startLoop() {
        lastFrameTime = SystemClock.uptimeMillis();
        removeCallbacks(frameRunnable);
        postOnAnimation(frameRunnable);
    }

    private final Runnable frameRunnable = new Runnable() {
        @Override
        public void run() {
            if (!running) {
                return;
            }
            long now = SystemClock.uptimeMillis();
            float dt = (now - lastFrameTime) / 1000f;
            lastFrameTime = now;
            updateBall(dt);
            invalidate();
            if (running) {
                postOnAnimation(this);
            }
        }
    };

    private void updateBall(float dt) {
        ballX += ballVelX * dt;
        ballY += ballVelY * dt;

        float leftBound = getPaddingLeft() + ballRadius;
        float rightBound = getWidth() - getPaddingRight() - ballRadius;
        float topBound = getPaddingTop() + ballRadius;

        if (ballX < leftBound) {
            ballX = leftBound;
            ballVelX = -ballVelX;
        } else if (ballX > rightBound) {
            ballX = rightBound;
            ballVelX = -ballVelX;
        }

        if (ballY < topBound) {
            ballY = topBound;
            ballVelY = -ballVelY;
        }

        if (ballY - ballRadius > getHeight()) {
            lives -= 1;
            if (listener != null) {
                listener.onLivesChanged(lives);
            }
            if (lives <= 0) {
                running = false;
                if (listener != null) {
                    listener.onGameOver(score);
                }
            } else {
                resetBall();
            }
            return;
        }

        RectF paddleRect = new RectF(paddleX, paddleY, paddleX + paddleWidth, paddleY + paddleHeight);
        if (ballVelY > 0 && paddleRect.contains(ballX, ballY + ballRadius)) {
            ballY = paddleY - ballRadius;
            float hitPoint = (ballX - paddleX) / paddleWidth - 0.5f;
            ballVelX = BALL_SPEED * 1.1f * hitPoint;
            ballVelY = -Math.abs(ballVelY);
        }

        handleBrickCollisions();
    }

    private void handleBrickCollisions() {
        RectF ballRect = new RectF(ballX - ballRadius, ballY - ballRadius, ballX + ballRadius, ballY + ballRadius);
        for (int r = 0; r < BRICK_ROWS; r++) {
            for (int c = 0; c < BRICK_COLS; c++) {
                if (bricks[r][c] == 0) {
                    continue;
                }
                float left = brickLeft + c * brickWidth + 4f;
                float top = brickTop + r * brickHeight + 4f;
                float right = left + brickWidth - 8f;
                float bottom = top + brickHeight - 8f;
                RectF brickRect = new RectF(left, top, right, bottom);
                if (RectF.intersects(ballRect, brickRect)) {
                    bricks[r][c] = 0;
                    score += 20;
                    if (listener != null) {
                        listener.onScoreChanged(score);
                    }
                    ballVelY = -ballVelY;
                    if (areBricksCleared()) {
                        running = false;
                        if (listener != null) {
                            listener.onGameOver(score);
                        }
                    }
                    return;
                }
            }
        }
    }

    private boolean areBricksCleared() {
        for (int r = 0; r < BRICK_ROWS; r++) {
            for (int c = 0; c < BRICK_COLS; c++) {
                if (bricks[r][c] != 0) {
                    return false;
                }
            }
        }
        return true;
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}

