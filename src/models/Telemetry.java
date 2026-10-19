package models;

import java.util.Locale;

// Reads values from the car, so it never holds its own copy.
public class Telemetry {
    private final Car car;

    public Telemetry(Car car) {
        if (car == null) {
            throw new IllegalArgumentException("Telemetry needs a car.");
        }
        this.car = car;
    }

    public double getFuelPercent() { return car.getFuelPercent(); }
    public double getEnergyPercent() { return car.getEnergyPercent(); }
    public double getEngineTemperature() { return car.getEngineTemperature(); }
    public double getTyreCondition() { return car.getTyre().getCondition(); }
    public String getTyreCompound() { return car.getTyre().getCompoundName(); }

    public String format() {
        return String.format(Locale.US,
            "------------- TELEMETRY -------------%n"
          + "Tyre compound     : %s%n"
          + "Tyre condition    : %.1f%%%n"
          + "Fuel remaining    : %.1f%%%n"
          + "Energy level      : %.1f%%%n"
          + "Engine temperature: %.1f C%s%n"
          + "-------------------------------------",
            getTyreCompound(), getTyreCondition(), getFuelPercent(), getEnergyPercent(),
            getEngineTemperature(), car.isOverheating() ? "  (HOT!)" : "");
    }
}
