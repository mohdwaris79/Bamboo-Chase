package com.example.snakegame;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;

public class SoundManager {
    private SoundPool soundPool;
    private int eatFoodSound, eatBoosterSound, snakeBiteSound;

    public SoundManager(Context context) {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setAudioAttributes(audioAttributes)
                .setMaxStreams(3)
                .build();

        eatFoodSound = soundPool.load(context, R.raw.eat_food, 1);
        eatBoosterSound = soundPool.load(context, R.raw.eat_booster, 1);
        snakeBiteSound = soundPool.load(context, R.raw.snake_bite, 1);
    }

    public void playEatFood() {
        soundPool.play(eatFoodSound, 1, 1, 0, 0, 1);
    }

    public void playEatBooster() {
        soundPool.play(eatBoosterSound, 1, 1, 0, 0, 1);
    }

    public void playSnakeBite() {
        soundPool.play(snakeBiteSound, 1, 1, 0, 0, 1);
    }

    public void release() {
        soundPool.release();
        soundPool = null;
    }
}
