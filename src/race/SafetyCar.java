package race;

import java.util.Random;

// Checked once per lap: not before lap 3, not on the last lap, 5 laps gap between safety cars.
public class SafetyCar {
    public enum Change { NONE, DEPLOYED, ENDED }

    public static final double DEFAULT_PROBABILITY = 0.04;
    public static final int MIN_DEPLOY_LAP = 3;
    public static final int COOLDOWN_LAPS = 5;

    private boolean active = false;
    private int lapsRemaining = 0;
    private int lastEndedLap = -100;
    private double deployProbability = DEFAULT_PROBABILITY;
    private int scheduledLap = 0;
    private int scheduledDuration = 3;

    public SafetyCar() { }

    public SafetyCar(double deployProbability) {
        setDeployProbability(deployProbability);
    }

    public boolean isActive() { return active; }
    public int getLapsRemaining() { return lapsRemaining; }
    public double getDeployProbability() { return deployProbability; }

    public void setDeployProbability(double probability) {
        if (probability < 0.0 || probability > 1.0) {
            throw new IllegalArgumentException("Safety car probability must be between 0 and 1.");
        }
        this.deployProbability = probability;
    }

    public void scheduleDeployment(int lap, int durationLaps) {
        if (lap < 1 || durationLaps < 1) {
            throw new IllegalArgumentException("Lap and duration must be at least 1.");
        }
        this.scheduledLap = lap;
        this.scheduledDuration = durationLaps;
    }

    public Change update(int upcomingLap, int totalLaps, Random random) {
        if (active) {
            lapsRemaining--;
            if (lapsRemaining <= 0) {
                active = false;
                lastEndedLap = upcomingLap - 1;
                return Change.ENDED;
            }
            return Change.NONE;
        }

        boolean scheduledNow = (scheduledLap == upcomingLap);
        boolean allowedNow = upcomingLap >= MIN_DEPLOY_LAP
                && upcomingLap < totalLaps
                && (upcomingLap - lastEndedLap) > COOLDOWN_LAPS;

        if (scheduledNow || (allowedNow && random.nextDouble() < deployProbability)) {
            active = true;
            lapsRemaining = scheduledNow ? scheduledDuration : 2 + random.nextInt(3);
            return Change.DEPLOYED;
        }
        return Change.NONE;
    }
}
