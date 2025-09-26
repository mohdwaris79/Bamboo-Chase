package com.example.snakegame;

import android.graphics.Canvas;
import android.view.SurfaceHolder;
public class GameThread extends Thread {
    private final SurfaceHolder surfaceHolder;
    private final GameView gameView;
    private boolean running;
    private final int targetFPS = 60;

    public GameThread(SurfaceHolder holder, GameView gameView) {
        this.surfaceHolder = holder;
        this.gameView = gameView;
    }

    public void setRunning(boolean running) {
        this.running = running;
        if (!running) {
            interrupt(); // Wake thread if sleeping
        }
    }

    public boolean isRunning() {
        return running;
    }

    @Override
    public void run() {
        long startTime;
        long timeMillis;
        long waitTime;
        long targetTime = 1000 / targetFPS;

        while (running) {
            if (gameView.isPaused()) {
                try {
                    sleep(100);
                } catch (InterruptedException e) {
                    // Thread interrupted while sleeping – check running again
                }
                continue;
            }

            startTime = System.nanoTime();

            Canvas canvas = null;
            try {
                canvas = surfaceHolder.lockCanvas();
                synchronized (surfaceHolder) {
                    gameView.update();
                    gameView.draw(canvas);
                }
            } finally {
                if (canvas != null) {
                    surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }

            timeMillis = (System.nanoTime() - startTime) / 1_000_000;
            waitTime = targetTime - timeMillis;
            if (waitTime > 0) {
                try {
                    sleep(waitTime);
                } catch (InterruptedException e) {
                    // Interrupted during sleep
                }
            }
        }
    }
}
