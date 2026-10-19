package models;

import java.util.ArrayList;
import java.util.List;

public class Driver {
    private final String name;
    private final int number;
    private final Team team;
    private final Car car;
    private final double skill;

    private double lapTimeTotal = 0.0;
    private double penaltyTotal = 0.0;
    private int position;
    private int completedLaps = 0;
    private int pitStops = 0;
    private int lastPitLap = 0;
    private int suitableTyreLaps = 0;
    private boolean retired = false;
    private String retirementReason = "";
    private final ArrayList<Double> lapTimes = new ArrayList<>();

    public Driver(String name, int number, Team team, Car car, double skill) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Driver name cannot be empty.");
        }
        if (number < 1 || number > 99) {
            throw new IllegalArgumentException("Driver number must be between 1 and 99.");
        }
        if (team == null || car == null) {
            throw new IllegalArgumentException("A driver needs a team and a car.");
        }
        if (skill < 0.90 || skill > 1.00) {
            throw new IllegalArgumentException("Driver skill must be between 0.90 and 1.00.");
        }
        this.name = name.trim();
        this.number = number;
        this.team = team;
        this.car = car;
        this.skill = skill;
        this.position = 1;
    }

    public void addLapTime(double seconds) {
        if (seconds <= 0.0) {
            throw new IllegalArgumentException("Lap time must be positive.");
        }
        lapTimes.add(seconds);
        lapTimeTotal += seconds;
        completedLaps++;
    }

    public void addTimePenalty(double seconds) {
        if (seconds < 0.0) {
            throw new IllegalArgumentException("Penalty cannot be negative.");
        }
        penaltyTotal += seconds;
    }

    public void incrementPitStops(int lap) {
        pitStops++;
        lastPitLap = lap;
    }

    public void incrementSuitableTyreLaps() { suitableTyreLaps++; }

    public void retire(String reason) {
        retired = true;
        retirementReason = (reason == null) ? "Retired" : reason;
    }

    public String getName() { return name; }
    public int getNumber() { return number; }
    public Team getTeam() { return team; }
    public Car getCar() { return car; }
    public double getSkill() { return skill; }

    public double getTotalRaceTime() { return lapTimeTotal + penaltyTotal; }
    public double getLapTimeTotal() { return lapTimeTotal; }
    public double getPenaltyTotal() { return penaltyTotal; }

    public int getPosition() { return position; }

    public void setPosition(int position) {
        if (position < 1) {
            throw new IllegalArgumentException("Position must be 1 or higher.");
        }
        this.position = position;
    }

    public int getCompletedLaps() { return completedLaps; }
    public int getPitStops() { return pitStops; }
    public int getLastPitLap() { return lastPitLap; }
    public int getSuitableTyreLaps() { return suitableTyreLaps; }
    public boolean isRetired() { return retired; }
    public String getRetirementReason() { return retirementReason; }

    public List<Double> getLapTimes() { return new ArrayList<>(lapTimes); }

    public double getAverageLapTime() {
        return completedLaps == 0 ? 0.0 : lapTimeTotal / completedLaps;
    }

    @Override
    public String toString() {
        return String.format(java.util.Locale.US, "#%d %s (%s) - P%d, %d laps, %s",
                number, name, team.getName(), position, completedLaps,
                RaceResult.formatTime(getTotalRaceTime()));
    }
}
