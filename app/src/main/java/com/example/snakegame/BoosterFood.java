package com.example.snakegame;

import android.graphics.Bitmap;

public class BoosterFood extends Food {

    private float speedBoost;

    public BoosterFood(Bitmap bitmap, float x, float y, int size, int points, float speedBoost) {
        super(bitmap, x, y, size, points);
        this.speedBoost = speedBoost;
    }

    public float getSpeedBoost() {
        return speedBoost;
    }
}

