package com.example.snakegame;

import android.graphics.Bitmap;
import android.graphics.Canvas;

public class Food {
    protected float x, y;
    protected int size;
    protected int points;
    protected Bitmap bitmap;

    public Food(Bitmap bitmap, float x, float y, int size, int points) {
        this.bitmap = bitmap;
        this.x = x;
        this.y = y;
        this.size = size;
        this.points = points;
    }

    public void draw(Canvas canvas) {
        if (bitmap != null) {
            canvas.drawBitmap(bitmap, x, y, null);
        }
    }

    public int getPoints() {
        return points;
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public int getSize() { return size; }
}
