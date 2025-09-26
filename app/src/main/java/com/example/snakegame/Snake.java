package com.example.snakegame;

import android.graphics.Bitmap;

public class Snake {
    private Bitmap bitmap;
    private float x, y;
    private int size;
    private float speed = 4f; // Increased from 3–4 to 6 for faster chasing

    public Snake(Bitmap bitmap, float x, float y, int size) {
        this.bitmap = bitmap;
        this.x = x;
        this.y = y;
        this.size = size;
    }




    // find target in which direction
    public void update(float targetX, float targetY) {
        float dx = targetX - x;
        float dy = targetY - y;
        // Normalize
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        if (distance > 0) {
            x += dx / distance * speed;
            y += dy / distance * speed;
        }
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public int getSize() { return size; }

    public void draw(android.graphics.Canvas canvas) {
        canvas.drawBitmap(bitmap, x, y, null);
    }
}
