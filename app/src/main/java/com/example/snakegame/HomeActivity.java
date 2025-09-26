package com.example.snakegame;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.app.Activity;


public class HomeActivity extends AppCompatActivity {

    private TextView info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);


        Button newGame = findViewById(R.id.btn_new_game);
        Button about   = findViewById(R.id.btn_About);
        Button hScore  = findViewById(R.id.btn_HScore);
        Button exit    = findViewById(R.id.btn_Exit);
        info = findViewById(R.id.info);

        newGame.setOnClickListener(v -> startNewGame());
        about.setOnClickListener(v -> showAbout());
        hScore.setOnClickListener(v -> showHighScore());


        // For exit button which is on the home screen

        exit.setOnClickListener(v -> {
            Activity activity = (Activity) v.getContext();
            activity.finishAffinity();   // closes all activities in the task
            System.exit(0);
        });

    }


    // Intent Passing

    private void startNewGame() {
        Intent i = new Intent(HomeActivity.this, MainActivity.class);
        i.putExtra("newGame", true);
        startActivity(i);
    }

    private void showAbout() {
        String s = "Bamboo Chase\nVersion: 1.0.0\nCreated by: Waris";
        info.setText(s);
    }

    // Fetch and display High Score
    private void showHighScore() {
        SharedPreferences prefs = getSharedPreferences("GamePrefs", MODE_PRIVATE);
        int highScore = prefs.getInt("HighScore", 0);
        info.setText("Highest Score: " + highScore);
    }
}
