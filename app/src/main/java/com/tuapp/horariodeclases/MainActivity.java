package com.tuapp.horariodeclases;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 2500; // 2.5 segundos

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                // Iniciar la actividad principal
                Intent intent = new Intent(MainActivity.this, ListarActivity.class);
                startActivity(intent);

                // Cerrar esta actividad
                finish();
            }
        }, SPLASH_DELAY);
    }
}
