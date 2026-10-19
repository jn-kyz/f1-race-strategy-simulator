package strategy;

import exceptions.InvalidStrategyException;
import models.Car;
import models.Tyre;
import models.Weather;

import java.util.ArrayList;
import java.util.List;

public class Strategy {
    private final int totalLaps;
    private final double normalFuelPerLap;
    private final double saveFuelPerLap;
    private DrivingMode currentMode = DrivingMode.NORMAL;

    public Strategy(int totalLaps, double normalFuelPerLap, double saveFuelPerLap) {
        if (totalLaps < 1 || normalFuelPerLap <= 0.0 || saveFuelPerLap <= 0.0) {
            throw new IllegalArgumentException("Strategy needs positive lap and fuel values.");
        }
        this.totalLaps = totalLaps;
        this.normalFuelPerLap = normalFuelPerLap;
        this.saveFuelPerLap = saveFuelPerLap;
    }

    public void chooseDrivingMode(DrivingMode mode) throws InvalidStrategyException {
        if (mode == null) {
            throw new InvalidStrategyException("A driving mode must be chosen.");
        }
        this.currentMode = mode;
    }

    public DrivingMode getDrivingMode() { return currentMode; }

    public double evaluateTyreSuitability(Tyre tyre, Weather.Condition weather) {
        return tyre.getWeatherSuitability(weather);
    }

    public String describeSuitability(double suitability) {
        if (suitability >= 0.95) return "excellent";
        if (suitability >= 0.80) return "good";
        if (suitability >= 0.65) return "poor";
        return "very poor";
    }

    public String recommendTyre(Weather.Condition weather, int remainingLaps) {
        switch (weather) {
            case HEAVY_RAIN: return "Wet";
            case LIGHT_RAIN: return "Intermediate";
            default:
                if (remainingLaps > 25) return "Hard";
                if (remainingLaps > 12) return "Medium";
                return "Soft";
        }
    }

    public boolean shouldPitStop(Car car, Weather.Condition weather, int currentLap, boolean safetyCarActive) {
        int remaining = totalLaps - currentLap + 1;
        if (remaining <= 2) {
            return false;
        }
        Tyre tyre = car.getTyre();
        if (evaluateTyreSuitability(tyre, weather) < 0.65) {
            return true;
        }
        if (tyre.getCondition() < 25.0) {
            return true;
        }
        return safetyCarActive && tyre.getCondition() < 60.0 && remaining > 5;
    }

    public List<String> getWarnings(Car car, Weather.Condition weather, int currentLap) {
        List<String> warnings = new ArrayList<>();
        int remaining = totalLaps - currentLap + 1;
        Tyre tyre = car.getTyre();

        if (evaluateTyreSuitability(tyre, weather) < 0.65) {
            warnings.add("The " + tyre.getCompoundName() + " tyre is unsuitable for " + weather.getLabel().toLowerCase() + ".");
        }
        if (tyre.getCondition() < 15.0) {
            warnings.add("Tyres are critically worn.");
        }
        if (car.getFuelPercent() < remaining * saveFuelPerLap) {
            warnings.add("Fuel is too low to finish even in SAVE mode.");
        } else if (car.getFuelPercent() < remaining * normalFuelPerLap) {
            warnings.add("Fuel is too low to finish in NORMAL mode. Use SAVE to stretch it.");
        }
        if (car.isEnergyDepleted()) {
            warnings.add("Energy is depleted, so the car is losing performance.");
        }
        if (car.isOverheating()) {
            warnings.add("Engine is overheating. Consider NORMAL or SAVE.");
        }
        return warnings;
    }

    public DrivingMode recommendDrivingMode(Car car, int currentLap, boolean safetyCarActive) {
        int remaining = totalLaps - currentLap + 1;
        if (car.getFuelPercent() < remaining * normalFuelPerLap || car.isOverheating()) {
            return DrivingMode.SAVE;
        }
        boolean freshTyres = car.getTyre().getCondition() > 40.0;
        boolean plentyOfFuel = car.getFuelPercent() > remaining * normalFuelPerLap * 1.25;
        if (!safetyCarActive && remaining <= 4 && freshTyres && plentyOfFuel && car.getEnergyPercent() > 20.0) {
            return DrivingMode.PUSH;
        }
        return DrivingMode.NORMAL;
    }

    public List<String> generateRecommendations(Car car, Weather.Condition weather, int currentLap,
                                                boolean safetyCarActive) {
        List<String> advice = new ArrayList<>();
        int remaining = totalLaps - currentLap + 1;
        Tyre tyre = car.getTyre();
        double suitability = evaluateTyreSuitability(tyre, weather);

        advice.add(String.format(java.util.Locale.US, "Tyre: %s is %s for %s (%.0f%% suitable), condition %.0f%%.",
                tyre.getCompoundName(), describeSuitability(suitability), weather.getLabel().toLowerCase(),
                suitability * 100, tyre.getCondition()));

        if (shouldPitStop(car, weather, currentLap, safetyCarActive)) {
            advice.add("Pit stop: worthwhile now. Suggested tyre: " + recommendTyre(weather, remaining) + ".");
            if (safetyCarActive) {
                advice.add("The safety car is out, so the stop costs only half the usual time.");
            }
        } else if (remaining <= 2) {
            advice.add("Pit stop: not worthwhile, too few laps remain to recover the lost time.");
        } else {
            advice.add("Pit stop: not needed yet, the tyres are still good enough.");
        }

        DrivingMode suggested = recommendDrivingMode(car, currentLap, safetyCarActive);
        advice.add("Driving mode: " + suggested.getLabel() + " suggested"
                + (suggested == DrivingMode.SAVE ? " (fuel or engine temperature needs protecting)."
                : suggested == DrivingMode.PUSH ? " (few laps left and resources are healthy)."
                : " (balanced use of tyres and fuel)."));

        advice.add(String.format(java.util.Locale.US, "Fuel: %.1f%% left, about %.1f%% needed for %d lap(s) in NORMAL mode.",
                car.getFuelPercent(), remaining * normalFuelPerLap, remaining));

        for (String warning : getWarnings(car, weather, currentLap)) {
            advice.add("WARNING: " + warning);
        }
        return advice;
    }
}
