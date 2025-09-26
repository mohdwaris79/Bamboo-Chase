package com.example.snakegame;
import android.util.Log;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private GameView gameView;
    private TextView scoreText;
    private Button btnClose;

    private TelephonyManager telephonyManager;
    private PhoneStateListener phoneStateListener;

    private static final int REQUEST_PHONE_STATE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gameView = findViewById(R.id.game_view);
        scoreText = findViewById(R.id.score_text);
        btnClose = findViewById(R.id.btn_close);
        JoystickView joystickView = findViewById(R.id.joystickView);

        // Joystick input
        joystickView.setJoystickListener((xPercent, yPercent) -> gameView.setDirection(xPercent, yPercent));
        gameView.setScoreTextView(scoreText);

        // Initialize TelephonyManager
        telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);

        //  Setup PhoneStateListener
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String phoneNumber) {
                switch (state) {
                    case TelephonyManager.CALL_STATE_RINGING:
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                        if (gameView != null) {
                            gameView.pauseGame();   // Pause game when call comes
                        }
                        break;

                    case TelephonyManager.CALL_STATE_IDLE:
                        if (gameView != null) {
                            gameView.resumeGame();  // Resume when call ends
                        }
                        break;
                }
            }
        };

        // Check and request runtime permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_PHONE_STATE);

        } else {
            // Already granted → start listening

            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        }

        // Exit button logic
        btnClose.setOnClickListener(v -> {
            gameView.pauseGame(); // Pause immediately
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Exit Game")
                    .setMessage("Do you really want to exit?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", (dialog, which) -> {
                        gameView.stopThread(); // Stop game thread completely
                        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish(); // Finish MainActivity to clean up
                    })
                    .setNegativeButton("No", (dialog, which) -> {
                        dialog.dismiss();
                        gameView.resumeGame(); // Resume only if NO is pressed
                    })
                    .show();
        });

    }

    //  Handle permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PHONE_STATE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted → Start listening
                telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
            }
        }
    }

    // Save High Score
    public void saveHighScore(int finalScore) {
        SharedPreferences prefs = getSharedPreferences("GamePrefs", MODE_PRIVATE);
        int highScore = prefs.getInt("HighScore", 0);

        if (finalScore > highScore) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("HighScore", finalScore);
            editor.apply();
        }
    }
}
