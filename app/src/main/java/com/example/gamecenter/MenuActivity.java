package com.example.gamecenter;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.gamecenter.data.GameCenterRepository;

public class MenuActivity extends AppCompatActivity {

    public static final String EXTRA_USERNAME = "extra_username";
    public static final String EXTRA_USER_ID = "extra_user_id";

    private GameCenterRepository repository;
    private long userId;
    private String username;
    private TextView points;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menu);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.menu_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        repository = new GameCenterRepository(this);
        username = getIntent().getStringExtra(EXTRA_USERNAME);
        userId = getIntent().getLongExtra(EXTRA_USER_ID, -1L);
        TextView welcome = findViewById(R.id.menu_welcome);
        points = findViewById(R.id.menu_points);
        if (username != null && !username.isEmpty()) {
            welcome.setText(getString(R.string.menu_welcome_user, username));
        }

        findViewById(R.id.menu_games).setOnClickListener(v -> {
            Intent intent = new Intent(MenuActivity.this, GamesActivity.class);
            intent.putExtra(EXTRA_USERNAME, username);
            intent.putExtra(EXTRA_USER_ID, userId);
            startActivity(intent);
        });
        findViewById(R.id.menu_scores).setOnClickListener(v -> {
            Intent intent = new Intent(MenuActivity.this, ScoresActivity.class);
            intent.putExtra(EXTRA_USERNAME, username);
            intent.putExtra(EXTRA_USER_ID, userId);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (userId > 0) {
            int totalPoints = repository.getTotalPointsForUser(userId);
            points.setText(getString(R.string.menu_points_value, totalPoints));
        }
    }
}
