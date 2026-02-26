package com.example.gamecenter;

public final class TimeFormatter {

    private TimeFormatter() {
    }

    public static String formatMillis(long timeMs) {
        long totalSeconds = timeMs / 1000L;
        long minutes = totalSeconds / 60L;
        long seconds = totalSeconds % 60L;
        return String.format("%02d:%02d", minutes, seconds);
    }
}

