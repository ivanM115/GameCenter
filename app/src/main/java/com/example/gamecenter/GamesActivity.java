package com.example.gamecenter;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class GamesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_games);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.games_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        String username = getIntent().getStringExtra(MenuActivity.EXTRA_USERNAME);
        long userId = getIntent().getLongExtra(MenuActivity.EXTRA_USER_ID, -1L);

        findViewById(R.id.card_2048).setOnClickListener(v -> {
            Intent intent = new Intent(GamesActivity.this, Game2048Activity.class);
            intent.putExtra(MenuActivity.EXTRA_USERNAME, username);
            intent.putExtra(MenuActivity.EXTRA_USER_ID, userId);
            startActivity(intent);
        });
        findViewById(R.id.card_bobble).setOnClickListener(v -> {
            Intent intent = new Intent(GamesActivity.this, PuzzleBobbleActivity.class);
            intent.putExtra(MenuActivity.EXTRA_USERNAME, username);
            intent.putExtra(MenuActivity.EXTRA_USER_ID, userId);
            startActivity(intent);
        });
        findViewById(R.id.card_arcanoid).setOnClickListener(v -> {
            Intent intent = new Intent(GamesActivity.this, ArcanoidActivity.class);
            intent.putExtra(MenuActivity.EXTRA_USERNAME, username);
            intent.putExtra(MenuActivity.EXTRA_USER_ID, userId);
            startActivity(intent);
        });
    }
}
