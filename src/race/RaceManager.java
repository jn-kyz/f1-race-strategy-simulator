package race;

import models.Car;
import models.Driver;
import models.RaceEvent;
import models.RaceResult;
import models.Tyre;
import models.Weather;
import exceptions.InvalidStrategyException;
import services.RaceEngine;
import strategy.DrivingMode;
import strategy.Strategy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class RaceManager {
    private final Race race;
    private final RaceEngine engine;
    private final Driver player;
    private final Strategy playerStrategy;
    private final Strategy advisor;
    private Set<String> lastWarnings = new HashSet<>();

    public RaceManager(Race race, RaceEngine engine, Driver player, Strategy playerStrategy) {
        if (race == null || engine == null || player == null || playerStrategy == null) {
            throw new IllegalArgumentException("RaceManager needs a race, engine, player and strategy.");
        }
        this.race = race;
        this.engine = engine;
        this.player = player;
        this.playerStrategy = playerStrategy;
        int laps = race.getTotalLaps();
        this.advisor = new Strategy(laps,
                engine.calculateFuelUse(DrivingMode.NORMAL, laps, false),
                engine.calculateFuelUse(DrivingMode.SAVE, laps, false));
    }

    public void startRace() {
        race.start();
        updatePositions();
        checkPlayerWarnings();
    }

    public void playLap(DrivingMode playerMode) {
        if (race.getStatus() != Race.Status.IN_PROGRESS) {
            throw new IllegalStateException("The race is not in progress.");
        }
        for (Driver driver : race.getDrivers()) {
            if (driver.isRetired()) {
                continue;
            }
            if (driver == player) {
                engine.simulateLap(driver, playerMode, race, false);
            } else {
                considerAiPitStop(driver);
                engine.simulateLap(driver, chooseAiMode(driver), race, true);
            }
        }
        updatePositions();

        if (!anyDriverRunning()) {
            race.finish();
            return;
        }
        if (race.advanceToNextLap()) {
            updateConditions(race.getCurrentLap());
            checkPlayerWarnings();
        }
    }

    public void finishRaceWithoutPlayer() {
        while (!race.isFinished()) {
            playLap(DrivingMode.NORMAL);
        }
    }

    private boolean anyDriverRunning() {
        for (Driver d : race.getDrivers()) {
            if (!d.isRetired()) {
                return true;
            }
        }
        return false;
    }

    private void considerAiPitStop(Driver ai) {
        Car car = ai.getCar();
        Weather.Condition weather = race.getWeather().getCondition();
        if (ai.getLastPitLap() == race.getCurrentLap()) {
            return;
        }
        if (advisor.shouldPitStop(car, weather, race.getCurrentLap(), race.isSafetyCarActive())) {
            try {
                Tyre tyre = PitStop.createTyre(advisor.recommendTyre(weather, race.getRemainingLaps()));
                new PitStop(ai, tyre).execute(race);
            } catch (InvalidStrategyException e) {
            }
        }
    }

    private DrivingMode chooseAiMode(Driver ai) {
        Car car = ai.getCar();
        int remaining = race.getRemainingLaps();
        int total = race.getTotalLaps();
        double normalUse = engine.calculateFuelUse(DrivingMode.NORMAL, total, false);
        double pushUse = engine.calculateFuelUse(DrivingMode.PUSH, total, false);
        if (car.getFuelPercent() < remaining * normalUse) {
            return DrivingMode.SAVE;
        }
        if (remaining <= 3 && car.getFuelPercent() > remaining * pushUse * 1.1
                && car.getTyre().getCondition() > 40.0) {
            return DrivingMode.PUSH;
        }
        return DrivingMode.NORMAL;
    }

    private void updateConditions(int lap) {
        Weather weather = race.getWeather();
        Weather.Condition before = weather.getCondition();
        if (weather.maybeChange(engine.getRandom())) {
            race.recordEvent(new RaceEvent(lap, RaceEvent.EventType.WEATHER_CHANGE,
                    "Weather changed from " + before + " to " + weather.getCondition() + "."));
        }
        SafetyCar.Change change = race.getSafetyCar().update(lap, race.getTotalLaps(), engine.getRandom());
        if (change == SafetyCar.Change.DEPLOYED) {
            race.recordEvent(new RaceEvent(lap, RaceEvent.EventType.SAFETY_CAR,
                    "Safety car deployed for " + race.getSafetyCar().getLapsRemaining()
                    + " lap(s). Lap times are slower and pit stops are cheaper."));
        } else if (change == SafetyCar.Change.ENDED) {
            race.recordEvent(new RaceEvent(lap, RaceEvent.EventType.SAFETY_CAR,
                    "Safety car in. Racing resumes."));
        }
    }

    public void checkPlayerWarnings() {
        if (player.isRetired() || race.isFinished()) {
            return;
        }
        List<String> warnings = playerStrategy.getWarnings(
                player.getCar(), race.getWeather().getCondition(), race.getCurrentLap());
        for (String warning : warnings) {
            if (!lastWarnings.contains(warning)) {
                race.recordEvent(new RaceEvent(race.getCurrentLap(),
                        RaceEvent.EventType.STRATEGY_WARNING, warning));
            }
        }
        lastWarnings = new HashSet<>(warnings);
    }

    // running drivers first, then more laps, then lower total time
    public List<Driver> getOrderedDrivers() {
        List<Driver> ordered = new ArrayList<>(race.getDrivers());
        ordered.sort((a, b) -> {
            if (a.isRetired() != b.isRetired()) {
                return a.isRetired() ? 1 : -1;
            }
            if (a.getCompletedLaps() != b.getCompletedLaps()) {
                return Integer.compare(b.getCompletedLaps(), a.getCompletedLaps());
            }
            return Double.compare(a.getTotalRaceTime(), b.getTotalRaceTime());
        });
        return ordered;
    }

    public void updatePositions() {
        int position = 1;
        for (Driver d : getOrderedDrivers()) {
            d.setPosition(position++);
        }
    }

    public List<RaceResult> getFinalClassification() {
        List<RaceResult> results = new ArrayList<>();
        int position = 1;
        for (Driver d : getOrderedDrivers()) {
            results.add(toResult(d, position++));
        }
        return results;
    }

    public RaceResult getResultFor(Driver driver) {
        int position = 1;
        for (Driver d : getOrderedDrivers()) {
            if (d == driver) {
                return toResult(d, position);
            }
            position++;
        }
        throw new IllegalArgumentException("Driver is not part of this race.");
    }

    private RaceResult toResult(Driver d, int position) {
        String status = d.isRetired() ? "DNF (" + d.getRetirementReason() + ")" : "Finished";
        return new RaceResult(d.getName(), d.getTeam().getName(), d.getCompletedLaps(),
                d.getTotalRaceTime(), d.getPitStops(), position, d.isRetired(), status);
    }

    public String getStandingsTable() {
        StringBuilder sb = new StringBuilder();
        sb.append(race.getRaceInfo()).append(System.lineSeparator());
        List<Driver> ordered = getOrderedDrivers();
        Driver leader = ordered.get(0);
        for (Driver d : ordered) {
            String gap;
            if (d.isRetired()) {
                gap = "DNF";
            } else if (d == leader) {
                gap = "Leader";
            } else if (d.getCompletedLaps() == leader.getCompletedLaps()) {
                gap = String.format(Locale.US, "+%.3fs", d.getTotalRaceTime() - leader.getTotalRaceTime());
            } else {
                gap = "-";
            }
            sb.append(String.format(Locale.US, "P%-2d %-16s %-18s laps %-3d %-11s stops %d  %s%s%n",
                    d.getPosition(), d.getName(), d.getTeam().getName(), d.getCompletedLaps(),
                    RaceResult.formatTime(d.getTotalRaceTime()), d.getPitStops(), gap,
                    d == player ? "  <-- you" : ""));
        }
        return sb.toString();
    }

    public boolean isPlayerRetired() { return player.isRetired(); }
}
