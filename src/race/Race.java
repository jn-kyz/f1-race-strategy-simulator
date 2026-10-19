package race;

import exceptions.InvalidRaceConfigurationException;
import models.Driver;
import models.RaceEvent;
import models.Weather;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Race {
    public enum Status { NOT_STARTED, IN_PROGRESS, FINISHED }

    public static final int MIN_LAPS = 3;
    public static final int MAX_LAPS = 70;

    private final String name;
    private final int totalLaps;
    private int currentLap = 0;
    private final Weather weather;
    private final SafetyCar safetyCar;
    private final ArrayList<Driver> drivers = new ArrayList<>();
    private final ArrayList<RaceEvent> events = new ArrayList<>();
    private Status status = Status.NOT_STARTED;

    public Race(String name, int totalLaps, List<Driver> drivers, Weather weather, SafetyCar safetyCar)
            throws InvalidRaceConfigurationException {
        if (name == null || name.isBlank()) {
            throw new InvalidRaceConfigurationException("Race name cannot be empty.");
        }
        if (totalLaps < MIN_LAPS || totalLaps > MAX_LAPS) {
            throw new InvalidRaceConfigurationException(
                    "Lap count must be between " + MIN_LAPS + " and " + MAX_LAPS + " (got " + totalLaps + ").");
        }
        if (drivers == null || drivers.isEmpty()) {
            throw new InvalidRaceConfigurationException("A race needs at least one driver.");
        }
        if (weather == null || safetyCar == null) {
            throw new InvalidRaceConfigurationException("Weather and safety car must be provided.");
        }
        for (Driver d : drivers) {
            if (d == null || this.drivers.contains(d)) {
                throw new InvalidRaceConfigurationException("Driver list contains a null or duplicate driver.");
            }
            this.drivers.add(d);
        }
        this.name = name.trim();
        this.totalLaps = totalLaps;
        this.weather = weather;
        this.safetyCar = safetyCar;
    }

    public void start() {
        if (status != Status.NOT_STARTED) {
            throw new IllegalStateException("The race has already started.");
        }
        status = Status.IN_PROGRESS;
        currentLap = 1;
    }

    public boolean advanceToNextLap() {
        if (status != Status.IN_PROGRESS) {
            throw new IllegalStateException("The race is not in progress.");
        }
        if (currentLap >= totalLaps) {
            finish();
            return false;
        }
        currentLap++;
        return true;
    }

    public void finish() {
        if (status == Status.FINISHED) {
            return;
        }
        status = Status.FINISHED;
        recordEvent(new RaceEvent(currentLap, RaceEvent.EventType.RACE_COMPLETE, "The race is complete."));
    }

    public boolean isFinished() { return status == Status.FINISHED; }

    public void recordEvent(RaceEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null.");
        }
        events.add(event);
    }

    public List<RaceEvent> getEvents() {
        return Collections.unmodifiableList(events);
    }

    public int countEvents(RaceEvent.EventType type) {
        int count = 0;
        for (RaceEvent e : events) {
            if (e.getType() == type) {
                count++;
            }
        }
        return count;
    }

    public String getName() { return name; }
    public int getTotalLaps() { return totalLaps; }
    public int getCurrentLap() { return currentLap; }
    public int getRemainingLaps() { return totalLaps - currentLap + 1; }
    public Weather getWeather() { return weather; }
    public SafetyCar getSafetyCar() { return safetyCar; }
    public boolean isSafetyCarActive() { return safetyCar.isActive(); }
    public Status getStatus() { return status; }
    public List<Driver> getDrivers() { return Collections.unmodifiableList(drivers); }

    public String getRaceInfo() {
        return String.format("%s | Lap %d/%d | %s | Safety car: %s | Status: %s",
                name, currentLap, totalLaps, weather.getDisplayText(),
                safetyCar.isActive() ? "OUT (" + safetyCar.getLapsRemaining() + " lap(s) left)" : "no",
                status);
    }
}
