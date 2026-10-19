package models;

import java.util.Locale;

public class RaceResult {
    private final String driverName;
    private final String teamName;
    private final int completedLaps;
    private final double totalTime;
    private final int pitStops;
    private final int finalPosition;
    private final boolean retired;
    private final String statusText;

    public RaceResult(String driverName, String teamName, int completedLaps, double totalTime,
                      int pitStops, int finalPosition, boolean retired, String statusText) {
        this.driverName = driverName;
        this.teamName = teamName;
        this.completedLaps = completedLaps;
        this.totalTime = totalTime;
        this.pitStops = pitStops;
        this.finalPosition = finalPosition;
        this.retired = retired;
        this.statusText = statusText;
    }

    public String getDriverName() { return driverName; }
    public String getTeamName() { return teamName; }
    public int getCompletedLaps() { return completedLaps; }
    public double getTotalTime() { return totalTime; }
    public int getPitStops() { return pitStops; }
    public int getFinalPosition() { return finalPosition; }
    public boolean isRetired() { return retired; }
    public String getStatusText() { return statusText; }

    public static String formatTime(double seconds) {
        long totalMillis = Math.round(seconds * 1000.0);
        long millis = totalMillis % 1000;
        long totalSeconds = totalMillis / 1000;
        long secs = totalSeconds % 60;
        long minutes = (totalSeconds / 60) % 60;
        long hours = totalSeconds / 3600;
        if (hours > 0) {
            return String.format(Locale.US, "%d:%02d:%02d.%03d", hours, minutes, secs, millis);
        }
        return String.format(Locale.US, "%d:%02d.%03d", minutes, secs, millis);
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "P%-2d %-16s %-18s laps %-3d time %-11s stops %d  %s",
                finalPosition, driverName, teamName, completedLaps, formatTime(totalTime),
                pitStops, statusText);
    }
}
