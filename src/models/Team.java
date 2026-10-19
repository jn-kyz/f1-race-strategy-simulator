package models;

import java.util.ArrayList;
import java.util.List;

public class Team {
    private final String name;
    private final double carPerformance;
    private final ArrayList<Driver> drivers = new ArrayList<>();

    public Team(String name, double carPerformance) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Team name cannot be empty.");
        }
        if (carPerformance < 0.80 || carPerformance > 1.00) {
            throw new IllegalArgumentException("Car performance must be between 0.80 and 1.00.");
        }
        this.name = name.trim();
        this.carPerformance = carPerformance;
    }

    public boolean addDriver(Driver driver) {
        if (driver == null) {
            throw new IllegalArgumentException("Driver cannot be null.");
        }
        for (Driver existing : drivers) {
            if (existing == driver
                    || existing.getNumber() == driver.getNumber()
                    || existing.getName().equalsIgnoreCase(driver.getName())) {
                return false;
            }
        }
        drivers.add(driver);
        return true;
    }

    public List<Driver> getDrivers() {
        return new ArrayList<>(drivers);
    }

    public String getName() { return name; }
    public double getCarPerformance() { return carPerformance; }

    public String getInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("Team ").append(name)
          .append(String.format(java.util.Locale.US, " (car performance %.2f)", carPerformance))
          .append(System.lineSeparator());
        for (Driver d : drivers) {
            sb.append("  - #").append(d.getNumber()).append(" ").append(d.getName())
              .append(System.lineSeparator());
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return name;
    }
}
