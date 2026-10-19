package models;

// Parent of all tyres. Condition is 100 when new and drops to 0 as the tyre wears.
public abstract class Tyre {
    private static final double HEAVILY_WORN_BELOW = 30.0;

    private final String compoundName;
    private final double gripLevel;
    private final double wearRate;
    private final boolean wetWeatherTyre;
    private double condition = 100.0;

    protected Tyre(String compoundName, double gripLevel, double wearRate, boolean wetWeatherTyre) {
        if (compoundName == null || compoundName.isBlank()) {
            throw new IllegalArgumentException("Tyre compound name cannot be empty.");
        }
        if (gripLevel <= 0.0 || gripLevel > 1.0) {
            throw new IllegalArgumentException("Grip level must be above 0 and at most 1.");
        }
        if (wearRate <= 0.0) {
            throw new IllegalArgumentException("Wear rate must be positive.");
        }
        this.compoundName = compoundName;
        this.gripLevel = gripLevel;
        this.wearRate = wearRate;
        this.wetWeatherTyre = wetWeatherTyre;
    }

    public abstract double getWeatherSuitability(Weather.Condition weather);

    public abstract String getDescription();

    public double getWeatherWearMultiplier(Weather.Condition weather) {
        return 1.0;
    }

    public double getPerformanceContribution(Weather.Condition weather) {
        double conditionFactor = 0.75 + 0.25 * (condition / 100.0);
        if (condition < 20.0) {
            conditionFactor -= (20.0 - condition) / 20.0 * 0.15;
        }
        return gripLevel * getWeatherSuitability(weather) * conditionFactor;
    }

    public void applyWear(double wearFactor, Weather.Condition weather) {
        if (wearFactor < 0.0) {
            throw new IllegalArgumentException("Wear factor cannot be negative.");
        }
        double wear = wearRate * wearFactor * getWeatherWearMultiplier(weather);
        condition = Math.max(0.0, condition - wear);
    }

    public boolean isHeavilyWorn() { return condition < HEAVILY_WORN_BELOW; }

    public boolean isUnusable() { return condition <= 0.0; }

    public String getCompoundName() { return compoundName; }
    public double getGripLevel() { return gripLevel; }
    public double getWearRate() { return wearRate; }
    public boolean isWetWeatherTyre() { return wetWeatherTyre; }
    public double getCondition() { return condition; }

    public String getInfo() {
        return String.format(java.util.Locale.US,
            "%-12s grip %.2f | wear %.1f%%/lap | dry %.0f%% | light rain %.0f%% | heavy rain %.0f%%%n"
          + "             %s",
            compoundName, gripLevel, wearRate,
            getWeatherSuitability(Weather.Condition.DRY) * 100,
            getWeatherSuitability(Weather.Condition.LIGHT_RAIN) * 100,
            getWeatherSuitability(Weather.Condition.HEAVY_RAIN) * 100,
            getDescription());
    }

    @Override
    public String toString() {
        return String.format(java.util.Locale.US, "%s (%.1f%%)", compoundName, condition);
    }
}
