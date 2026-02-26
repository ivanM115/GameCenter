package com.example.gamecenter;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gamecenter.data.GameCenterRepository;

public class ScoresActivity extends AppCompatActivity {

    private GameCenterRepository repository;
    private ScoresAdapter adapter;
    private EditText searchInput;
    private Spinner operatorSpinner;
    private Spinner orderSpinner;
    private Cursor currentCursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_scores);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.scores_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        repository = new GameCenterRepository(this);
        searchInput = findViewById(R.id.search_input);
        operatorSpinner = findViewById(R.id.spinner_operator);
        orderSpinner = findViewById(R.id.spinner_order);

        ArrayAdapter<CharSequence> operatorAdapter = ArrayAdapter.createFromResource(
                this, R.array.score_operators, android.R.layout.simple_spinner_item
        );
        operatorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        operatorSpinner.setAdapter(operatorAdapter);

        ArrayAdapter<CharSequence> orderAdapter = ArrayAdapter.createFromResource(
                this, R.array.score_orders, android.R.layout.simple_spinner_item
        );
        orderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        orderSpinner.setAdapter(orderAdapter);

        RecyclerView recyclerView = findViewById(R.id.scores_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ScoresAdapter(null, scoreId -> {
            Intent intent = new Intent(ScoresActivity.this, ScoreDetailActivity.class);
            intent.putExtra(ScoreDetailActivity.EXTRA_SCORE_ID, scoreId);
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        findViewById(R.id.button_search).setOnClickListener(v -> loadScores());

        orderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadScores();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                long scoreId = adapter.getScoreId(position);
                if (scoreId > 0) {
                    repository.deleteScore(scoreId);
                }
                loadScores();
            }
        };
        new ItemTouchHelper(callback).attachToRecyclerView(recyclerView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadScores();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (currentCursor != null) {
            currentCursor.close();
        }
    }

    private void loadScores() {
        if (currentCursor != null) {
            currentCursor.close();
        }
        String queryText = searchInput.getText().toString().trim();
        String operator = operatorSpinner.getSelectedItem().toString();
        String orderBy = mapOrder(orderSpinner.getSelectedItemPosition());
        currentCursor = repository.queryScores(queryText, operator, orderBy);
        adapter.swapCursor(currentCursor);
    }

    private String mapOrder(int position) {
        if (position == 1) {
            return "score";
        }
        if (position == 2) {
            return "time";
        }
        return "name";
    }
}

