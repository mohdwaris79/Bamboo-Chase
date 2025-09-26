package com.example.snakegame;

import android.graphics.Bitmap;
import android.graphics.Canvas;

public class Panda {
    private Bitmap bitmap;
    private float x, y;
    private int size;

    private float speed = 6f;
    private boolean isBoosted = false;
    private long boostEndTime = 0;
    private float boostMultiplier = 1f;

    public Panda(Bitmap bitmap, float x, float y, int size) {
        this.bitmap = bitmap;
        this.x = x;
        this.y = y;
        this.size = size;
    }

    public void move(float dx, float dy) {
        float actualSpeed = speed * boostMultiplier;
        x += dx * actualSpeed;
        y += dy * actualSpeed;
    }

    // Panda wrap-around logic
    public void wrapAround(int width, int height) {
        if (x < 0) x = width - size;
        if (x + size > width) x = 0;
        if (y < 0) y = height - size;
        if (y + size > height) y = 0;
    }

    // Apply booster
    public void applySpeedBoost(float multiplier, long durationMs) {
        isBoosted = true;
        boostMultiplier = multiplier;
        boostEndTime = System.currentTimeMillis() + durationMs;
    }

    // End booster
    public void updateBoostTimer() {
        if (isBoosted && System.currentTimeMillis() > boostEndTime) {
            isBoosted = false;
            boostMultiplier = 1f;
        }
    }

    // Getters
    public float getX() { return x; }
    public float getY() { return y; }
    public int getSize() { return size; }

    public void draw(Canvas canvas) {
        canvas.drawBitmap(bitmap, x, y, null);
    }
}
