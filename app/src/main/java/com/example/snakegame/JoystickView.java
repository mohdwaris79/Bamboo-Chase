package com.example.snakegame;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class JoystickView extends View {
    private Paint basePaint, hatPaint;
    private float centerX, centerY, baseRadius, hatRadius;
    private float touchX, touchY;
    private JoystickListener joystickListener;

    public interface JoystickListener {
        void onJoystickMoved(float xPercent, float yPercent);
    }

    public JoystickView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        basePaint = new Paint();
        basePaint.setColor(Color.GRAY);
        basePaint.setStyle(Paint.Style.FILL);

        hatPaint = new Paint();
        hatPaint.setColor(Color.DKGRAY);
        hatPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        centerX = w / 2f;
        centerY = h / 2f;
        baseRadius = Math.min(w, h) / 3f;
        hatRadius = Math.min(w, h) / 6f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Draw base
        canvas.drawCircle(centerX, centerY, baseRadius, basePaint);
        // Draw hat
        canvas.drawCircle(touchX == 0 ? centerX : touchX, touchY == 0 ? centerY : touchY, hatRadius, hatPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float displacement = (float) Math.sqrt(Math.pow(event.getX() - centerX, 2) + Math.pow(event.getY() - centerY, 2));

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                if (displacement < baseRadius) {
                    touchX = event.getX();
                    touchY = event.getY();
                } else {
                    float ratio = baseRadius / displacement;
                    touchX = centerX + (event.getX() - centerX) * ratio;
                    touchY = centerY + (event.getY() - centerY) * ratio;
                }

                if (joystickListener != null) {
                    float xPercent = (touchX - centerX) / baseRadius;
                    float yPercent = (touchY - centerY) / baseRadius;
                    joystickListener.onJoystickMoved(xPercent, yPercent);
                }
                invalidate();
                break;

            case MotionEvent.ACTION_UP:
                touchX = centerX;
                touchY = centerY;
                if (joystickListener != null) {
                    joystickListener.onJoystickMoved(0, 0);
                }
                invalidate();
                break;
        }
        return true;
    }

    public void setJoystickListener(JoystickListener listener) {
        this.joystickListener = listener;
    }
}
