package models;

import java.util.Random;

public class Weather {
    public enum Condition {
        DRY       ("Dry",         0.0),
        LIGHT_RAIN("Light rain",  5.0),
        HEAVY_RAIN("Heavy rain", 11.0);

        private final String label;
        private final double lapTimePenalty;

        Condition(String label, double lapTimePenalty) {
            this.label = label;
            this.lapTimePenalty = lapTimePenalty;
        }

        public String getLabel() { return label; }
        public double getLapTimePenalty() { return lapTimePenalty; }
    }

    public static final double DEFAULT_CHANGE_PROBABILITY = 0.06;

    private Condition condition;
    private double changeProbability;

    public Weather() {
        this(Condition.DRY, DEFAULT_CHANGE_PROBABILITY);
    }

    public Weather(Condition startingCondition, double changeProbability) {
        setCondition(startingCondition);
        setChangeProbability(changeProbability);
    }

    public Condition getCondition() { return condition; }

    public void setCondition(Condition condition) {
        if (condition == null) {
            throw new IllegalArgumentException("Weather condition cannot be null.");
        }
        this.condition = condition;
    }

    public double getChangeProbability() { return changeProbability; }

    public void setChangeProbability(double probability) {
        if (probability < 0.0 || probability > 1.0) {
            throw new IllegalArgumentException("Weather change probability must be between 0 and 1.");
        }
        this.changeProbability = probability;
    }

    public boolean maybeChange(Random random) {
        if (random.nextDouble() >= changeProbability) {
            return false;
        }
        switch (condition) {
            case DRY:
                condition = Condition.LIGHT_RAIN;
                break;
            case HEAVY_RAIN:
                condition = Condition.LIGHT_RAIN;
                break;
            default:
                condition = random.nextBoolean() ? Condition.DRY : Condition.HEAVY_RAIN;
                break;
        }
        return true;
    }

    public String getDisplayText() {
        return condition.getLabel() + " (" + condition + ")";
    }

    @Override
    public String toString() {
        return "Weather: " + getDisplayText();
    }
}
