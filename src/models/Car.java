package models;

// Fuel and energy are percentages kept between 0 and 100.
public class Car {
    public static final double MIN_ENGINE_TEMPERATURE = 60.0;
    public static final double MAX_ENGINE_TEMPERATURE = 130.0;
    public static final double OVERHEAT_TEMPERATURE = 115.0;
    public static final double START_ENGINE_TEMPERATURE = 90.0;

    private double fuelPercent = 100.0;
    private double energyPercent = 100.0;
    private double engineTemperature = START_ENGINE_TEMPERATURE;
    private final double basePerformance;
    private Tyre tyre;
    private final Telemetry telemetry;

    public Car(double basePerformance, Tyre startingTyre) {
        if (basePerformance < 0.80 || basePerformance > 1.00) {
            throw new IllegalArgumentException("Base performance must be between 0.80 and 1.00.");
        }
        if (startingTyre == null) {
            throw new IllegalArgumentException("A car needs a starting tyre.");
        }
        this.basePerformance = basePerformance;
        this.tyre = startingTyre;
        this.telemetry = new Telemetry(this);
    }

    public void consumeFuel(double percent) {
        if (percent < 0.0) {
            throw new IllegalArgumentException("Fuel use cannot be negative.");
        }
        fuelPercent = Math.max(0.0, fuelPercent - percent);
    }

    public void consumeEnergy(double percent) {
        if (percent < 0.0) {
            throw new IllegalArgumentException("Energy use cannot be negative.");
        }
        energyPercent = Math.max(0.0, energyPercent - percent);
    }

    public void updateEngineTemperature(double targetTemperature) {
        double next = engineTemperature + (targetTemperature - engineTemperature) * 0.5;
        engineTemperature = Math.min(MAX_ENGINE_TEMPERATURE, Math.max(MIN_ENGINE_TEMPERATURE, next));
    }

    public void wearTyre(double wearFactor, Weather.Condition weather) {
        tyre.applyWear(wearFactor, weather);
    }

    public void installTyre(Tyre newTyre) {
        if (newTyre == null) {
            throw new IllegalArgumentException("Cannot install a null tyre.");
        }
        this.tyre = newTyre;
    }

    public boolean isOutOfFuel() { return fuelPercent <= 0.0; }
    public boolean isEnergyDepleted() { return energyPercent <= 0.0; }
    public boolean isOverheating() { return engineTemperature >= OVERHEAT_TEMPERATURE; }

    public String getConditionSummary() {
        return String.format(java.util.Locale.US, "Tyre %s | Fuel %.1f%% | Energy %.1f%% | Engine %.1f C",
                tyre, fuelPercent, energyPercent, engineTemperature);
    }

    public double getFuelPercent() { return fuelPercent; }
    public double getEnergyPercent() { return energyPercent; }
    public double getEngineTemperature() { return engineTemperature; }
    public double getBasePerformance() { return basePerformance; }
    public Tyre getTyre() { return tyre; }
    public Telemetry getTelemetry() { return telemetry; }
}
