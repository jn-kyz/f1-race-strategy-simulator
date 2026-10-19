package services;

import models.Car;
import models.Driver;
import models.RaceEvent;
import models.Tyre;
import models.Weather;
import race.Race;
import strategy.DrivingMode;

import java.util.Random;

// Lap time (seconds) = 90 base + penalties for car, driver, tyre, weather, driving mode,
// fuel load, engine heat, low energy and a worn-out tyre.
// Under a safety car: 125 + 25% of those penalties. Only AI laps get random variation.
public class RaceEngine {
    public static final double BASE_LAP_TIME = 90.0;
    public static final double SAFETY_CAR_BASE_LAP_TIME = 125.0;
    public static final double MINIMUM_LAP_TIME = 60.0;
    private static final double SAFETY_CAR_RESOURCE_FACTOR = 0.6;
    private static final double SAFETY_CAR_WEAR_FACTOR = 0.5;
    private static final double ENERGY_BUDGET = 75.0;
    private static final double FUEL_BUDGET = 92.0;

    private final Random random;
    private final long seed;

    public RaceEngine(long seed) {
        this.seed = seed;
        this.random = new Random(seed);
    }

    public long getSeed() { return seed; }
    public Random getRandom() { return random; }

    public double calculateFuelUse(DrivingMode mode, int totalLaps, boolean safetyCarActive) {
        double use = FUEL_BUDGET / totalLaps * mode.getFuelFactor();
        return safetyCarActive ? use * SAFETY_CAR_RESOURCE_FACTOR : use;
    }

    public double calculateEnergyUse(DrivingMode mode, int totalLaps, boolean safetyCarActive) {
        double use = ENERGY_BUDGET / totalLaps * mode.getEnergyFactor();
        return safetyCarActive ? use * SAFETY_CAR_RESOURCE_FACTOR : use;
    }

    public double calculateLapTime(Driver driver, DrivingMode mode, Weather.Condition weather,
                                   boolean safetyCarActive) {
        Car car = driver.getCar();
        Tyre tyre = car.getTyre();

        double carDelta = (1.0 - car.getBasePerformance()) * 20.0;
        double driverDelta = (1.0 - driver.getSkill()) * 15.0;
        double tyreDelta = (1.0 - tyre.getPerformanceContribution(weather)) * 12.0;
        double weatherDelta = weather.getLapTimePenalty();
        double modeDelta = mode.getLapTimeChange();
        double fuelDelta = car.getFuelPercent() * 0.03;

        double heatDelta = 0.0;
        if (car.getEngineTemperature() > 105.0) {
            heatDelta = (car.getEngineTemperature() - 105.0) * 0.15;
        }
        double energyDelta = 0.0;
        if (car.getEnergyPercent() < 20.0) {
            energyDelta = (20.0 - car.getEnergyPercent()) * 0.15;
        }
        double wornOutDelta = tyre.isUnusable() ? 10.0 : 0.0;

        double deltas = carDelta + driverDelta + tyreDelta + weatherDelta + modeDelta
                + fuelDelta + heatDelta + energyDelta + wornOutDelta;

        double lapTime = safetyCarActive
                ? SAFETY_CAR_BASE_LAP_TIME + 0.25 * deltas
                : BASE_LAP_TIME + deltas;

        return Math.max(MINIMUM_LAP_TIME, lapTime);
    }

    public double simulateLap(Driver driver, DrivingMode mode, Race race, boolean addVariation) {
        if (driver.isRetired()) {
            return 0.0;
        }
        Car car = driver.getCar();
        Weather.Condition weather = race.getWeather().getCondition();
        boolean safetyCar = race.isSafetyCarActive();
        int lap = race.getCurrentLap();

        // not enough fuel for this lap, so the car stops
        double fuelUse = calculateFuelUse(mode, race.getTotalLaps(), safetyCar);
        if (car.getFuelPercent() < fuelUse) {
            driver.retire("Out of fuel on lap " + lap);
            race.recordEvent(new RaceEvent(lap, RaceEvent.EventType.RETIREMENT,
                    driver.getName() + " ran out of fuel and retired."));
            return 0.0;
        }

        double lapTime = calculateLapTime(driver, mode, weather, safetyCar);
        if (addVariation) {
            lapTime += (random.nextDouble() - 0.5) * 0.8;
        }
        driver.addLapTime(lapTime);

        if (car.getTyre().getWeatherSuitability(weather) >= 0.80) {
            driver.incrementSuitableTyreLaps();
        }

        car.consumeFuel(fuelUse);
        car.consumeEnergy(calculateEnergyUse(mode, race.getTotalLaps(), safetyCar));
        car.updateEngineTemperature(safetyCar ? 92.0 : mode.getTargetEngineTemperature());
        double wearFactor = mode.getTyreWearFactor() * (safetyCar ? SAFETY_CAR_WEAR_FACTOR : 1.0);
        car.wearTyre(wearFactor, weather);

        return lapTime;
    }
}
