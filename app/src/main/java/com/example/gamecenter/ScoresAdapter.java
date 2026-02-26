package com.example.gamecenter;

import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ScoresAdapter extends RecyclerView.Adapter<ScoresAdapter.ScoreViewHolder> {

    public interface OnScoreClickListener {
        void onScoreClick(long scoreId);
    }

    private Cursor cursor;
    private final OnScoreClickListener listener;

    public ScoresAdapter(Cursor cursor, OnScoreClickListener listener) {
        this.cursor = cursor;
        this.listener = listener;
    }

    public void swapCursor(Cursor newCursor) {
        if (cursor == newCursor) {
            return;
        }
        cursor = newCursor;
        notifyDataSetChanged();
    }

    public long getScoreId(int position) {
        if (cursor == null || !cursor.moveToPosition(position)) {
            return -1L;
        }
        int idIndex = cursor.getColumnIndexOrThrow("_id");
        return cursor.getLong(idIndex);
    }

    @NonNull
    @Override
    public ScoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_score, parent, false);
        return new ScoreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScoreViewHolder holder, int position) {
        if (cursor == null || !cursor.moveToPosition(position)) {
            return;
        }
        String userName = cursor.getString(cursor.getColumnIndexOrThrow("user_name"));
        String gameName = cursor.getString(cursor.getColumnIndexOrThrow("game_name"));
        int scoreValue = cursor.getInt(cursor.getColumnIndexOrThrow("score_value"));
        long timeMs = cursor.getLong(cursor.getColumnIndexOrThrow("time_ms"));
        long scoreId = cursor.getLong(cursor.getColumnIndexOrThrow("_id"));

        holder.userName.setText(userName);
        holder.gameName.setText(gameName);
        holder.scoreValue.setText(String.valueOf(scoreValue));
        holder.timeValue.setText(TimeFormatter.formatMillis(timeMs));

        holder.itemView.setOnClickListener(v -> listener.onScoreClick(scoreId));
    }

    @Override
    public int getItemCount() {
        return cursor == null ? 0 : cursor.getCount();
    }

    static class ScoreViewHolder extends RecyclerView.ViewHolder {
        final TextView userName;
        final TextView gameName;
        final TextView scoreValue;
        final TextView timeValue;

        ScoreViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.item_user_name);
            gameName = itemView.findViewById(R.id.item_game_name);
            scoreValue = itemView.findViewById(R.id.item_score_value);
            timeValue = itemView.findViewById(R.id.item_time_value);
        }
    }
}

