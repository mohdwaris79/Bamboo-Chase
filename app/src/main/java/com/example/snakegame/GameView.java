package com.example.snakegame;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import java.util.Random;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {

    private GameThread thread;
    private Panda panda;
    private Snake snake;
    private Food normalFood;
    private BoosterFood boosterFood;
    private TextView scoreText;

    private int score = 0;
    private int foodsEaten = 0;
    private int foodsMissed = 0;
    private static final int TOTAL_FOODS = 15;
    private static final int BOOSTER_TRIGGER = 5;
    private static final long BOOSTER_DURATION_MS = 5000;
    private static final long NORMAL_FOOD_TIMEOUT = 10000; // 10 seconds

    private float dirX = 0f, dirY = 0f;

    private Bitmap pandaBitmap, snakeBitmap, foodBitmap, boosterBitmap;

    private final Random random = new Random();
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    // SoundPool
    private SoundPool soundPool;
    private int foodSoundId, boosterSoundId, biteSoundId;

    private boolean gameOver = false;
    private boolean gameWin = false;

    private long foodSpawnTime; // Track when normal food spawned

    // Background variables
    private Paint gradientPaint;
    private Paint starPaint;
    private float[] starX;
    private float[] starY;
    private int starCount = 70;
    private final Random starRand = new Random();
    private int frameCounter = 0;
    private GameThread gameThread;
    private volatile boolean paused = false;





    public GameView(Context context) { super(context); init(); }
    public GameView(Context context, AttributeSet attrs) { super(context, attrs); init(); }

    private void init() {
        getHolder().addCallback(this);
        setFocusable(true);
        paint.setColor(Color.WHITE);
        paint.setTextSize(42f);

        // Background paint
        gradientPaint = new Paint();
        starPaint = new Paint();
        starPaint.setColor(Color.WHITE);
        starPaint.setAlpha(180); // semi-transparent stars
        starX = new float[starCount];
        starY = new float[starCount];

        // Initialize sounds
        AudioAttributes attrs = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soundPool = new SoundPool.Builder()
                .setMaxStreams(3)
                .setAudioAttributes(attrs)
                .build();

        foodSoundId = soundPool.load(getContext(), R.raw.food_sound, 1);
        boosterSoundId = soundPool.load(getContext(), R.raw.booster_sound, 1);
        biteSoundId = soundPool.load(getContext(), R.raw.bite_sound, 1);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // Cache gradient
        gradientPaint.setShader(new LinearGradient(
            0, 0, 0, h,
            Color.parseColor("#001F4D"),
            Color.parseColor("#1E3A8A"),
            Shader.TileMode.CLAMP
        ));





        // Initialize stars
        for (int i = 0; i < starCount; i++) {
            starX[i] = starRand.nextFloat() * w;
            starY[i] = starRand.nextFloat() * h;
        }
    }

    public void setScoreTextView(TextView tv) { this.scoreText = tv; }




    //For panda and snake movement

    public void setDirection(float dxPercent, float dyPercent) {
        float magnitude = (float) Math.sqrt(dxPercent * dxPercent + dyPercent * dyPercent);
        if (magnitude > 1f) { dxPercent /= magnitude; dyPercent /= magnitude; }
        dirX = dxPercent;
        dirY = dyPercent;
    }




    //Game start Logic

    public void startGame() {
        stopThread(); // Ensure any existing thread is stopped first

        score = 0;
        foodsEaten = 0;
        foodsMissed = 0;
        boosterFood = null;
        gameOver = false;
        gameWin = false;
        paused = false;
        updateScoreUI();

        // Load and scale bitmaps
        pandaBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.panda);
        snakeBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.snake);
        foodBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.food);
        boosterBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.booster);

        pandaBitmap = Bitmap.createScaledBitmap(pandaBitmap, 100, 100, false);
        snakeBitmap = Bitmap.createScaledBitmap(snakeBitmap, 100, 100, false);
        foodBitmap = Bitmap.createScaledBitmap(foodBitmap, 60, 60, false);
        boosterBitmap = Bitmap.createScaledBitmap(boosterBitmap, 70, 70, false);

        float px = Math.max(0, getWidth() / 2f - 50);
        float py = Math.max(0, getHeight() / 2f - 50);
        panda = new Panda(pandaBitmap, px, py, 100);
        snake = new Snake(snakeBitmap, 100, 100, 100);

        spawnNormalFood();

        thread = new GameThread(getHolder(), this);
        thread.setRunning(true);
        thread.start();
    }



    // Normal Food Spawn
    private void spawnNormalFood() {
        float x = random.nextInt(Math.max(1, getWidth() - 50));
        float y = random.nextInt(Math.max(1, getHeight() - 50));
        normalFood = new Food(foodBitmap, x, y, 60, 1);
        foodSpawnTime = System.currentTimeMillis();
    }


    //Booster food spawn
    private void spawnBooster() {
        float x = random.nextInt(Math.max(1, getWidth() - 70));
        float y = random.nextInt(Math.max(1, getHeight() - 70));
        boosterFood = new BoosterFood(boosterBitmap, x, y, 70, 0, 1.8f);
    }



    // Game update logic

    public void update() {


            if (paused) return;




        if (panda == null || snake == null) return;
        if (gameOver || gameWin) return;

        panda.move(dirX, dirY);
        panda.wrapAround(getWidth(), getHeight());
        panda.updateBoostTimer();

        snake.update(panda.getX(), panda.getY());


        // Food eating logic by panda and snake

        boolean justAte = false;
        if (normalFood != null && isColliding(panda.getX(), panda.getY(), panda.getSize(),
            normalFood.getX(), normalFood.getY(), normalFood.getSize())) {
            score += normalFood.getPoints();
            foodsEaten++;
            justAte = true;

        if (soundPool != null) soundPool.play(foodSoundId, 1f, 1f, 0, 0, 1f);

        if (foodsEaten + foodsMissed < TOTAL_FOODS) spawnNormalFood();

        else normalFood = null;

        if (foodsEaten % BOOSTER_TRIGGER == 0 && boosterFood == null) spawnBooster();
            }

        if (normalFood != null && System.currentTimeMillis() - foodSpawnTime > NORMAL_FOOD_TIMEOUT) {
            foodsMissed++;
            score = Math.max(0, score - 1);
            if (foodsEaten + foodsMissed < TOTAL_FOODS) spawnNormalFood();
            else normalFood = null;
            updateScoreUI();
        }

        if (boosterFood != null && isColliding(panda.getX(), panda.getY(), panda.getSize(),
            boosterFood.getX(), boosterFood.getY(), boosterFood.getSize())) {
            score += 50;
            panda.applySpeedBoost(boosterFood.getSpeedBoost(), BOOSTER_DURATION_MS);
            boosterFood = null;
            if (soundPool != null) soundPool.play(boosterSoundId, 1f, 1f, 0, 0, 1f);
            updateScoreUI();
            }

        if (justAte) updateScoreUI();

        if (foodsEaten + foodsMissed >= TOTAL_FOODS) {
            gameWin = true;
            stopThread();

            // Save high score
            ((MainActivity) getContext()).saveHighScore(score);
        }


        if (isColliding(panda.getX(), panda.getY(), panda.getSize(), snake.getX(), snake.getY(), snake.getSize())) {
            gameOver = true;
            stopThread();

            if(soundPool != null) soundPool.play(biteSoundId, 1f, 1f, 0, 0, 1f);

            // Save high score
            ((MainActivity) getContext()).saveHighScore(score);
        }

    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (canvas == null) return;

        drawBackground(canvas);

        if (normalFood != null) normalFood.draw(canvas);
        if (boosterFood != null) boosterFood.draw(canvas);

        if (snake != null) snake.draw(canvas);
        if (panda != null) panda.draw(canvas);

        // canvas.drawText("Score: " + score + " / " + TOTAL_FOODS, 40, 60, paint);

        if (gameOver) drawCentered(canvas, "Game Over! ");
        else if (gameWin) drawCentered(canvas, "You Win! ");
        paint.setFakeBoldText(true);

    }

    private void drawBackground(Canvas canvas) {
        frameCounter++;
        canvas.drawRect(0, 0, getWidth(), getHeight(), gradientPaint);

        for (int i = 0; i < starCount; i++) {
            canvas.drawCircle(starX[i], starY[i], 3, starPaint);

            // Update star positions every 2 frames for performance
            if (frameCounter % 2 == 0) {
                starY[i] += 0.3f;
                if (starY[i] > getHeight()) starY[i] = 0;
            }
        }
    }

    private void drawCentered(Canvas canvas, String msg) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(Color.WHITE);
        p.setTextSize(56f);
        p.setFakeBoldText(true);
        float w = p.measureText(msg);
        float x = (getWidth() - w) / 2f;
        float y = getHeight() / 2f;
        canvas.drawText(msg, x, y, p);
    }

    private boolean isColliding(float x1, float y1, int size1, float x2, float y2, int size2) {
        float cx1 = x1 + size1 / 2f;
        float cy1 = y1 + size1 / 2f;
        float cx2 = x2 + size2 / 2f;
        float cy2 = y2 + size2 / 2f;
        float dx = cx1 - cx2;
        float dy = cy1 - cy2;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        return dist < (size1 / 2f + size2 / 2f);
    }


    //update in score
    private void updateScoreUI() {
        if (scoreText != null)
            scoreText.post(() -> scoreText.setText("Score: " + score));

    }



    public void stopThread() {
        if (thread != null) {
            thread.setRunning(false);
            try {
                thread.join(1000); // Wait up to 1 second
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            thread = null;
        }
    }



    // Pause game loop without killing thread


    public void pauseGame() {
        paused = true;
    }

    // Resume game loop if still alive
    public void resumeGame() {
        if (thread != null && thread.isAlive()) {
            paused = false;
        }
    }
    public boolean isPaused() {
        return paused;
    }


    // Restart a new thread only for a new game (not for resume)
    public void restartGame() {
        stopThread();
        startGame();
    }





    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        startGame();
    }

    @Override public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (thread != null) {
            thread.setRunning(false);
            try { thread.join(); } catch (InterruptedException ignored) {}
        }
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }
}
