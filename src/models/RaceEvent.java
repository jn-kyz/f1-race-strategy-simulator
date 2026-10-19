package models;

public class RaceEvent {
    public enum EventType {
        WEATHER_CHANGE, PIT_STOP, SAFETY_CAR, TYRE_CHANGE, STRATEGY_WARNING, RETIREMENT, RACE_COMPLETE
    }

    private final int lap;
    private final EventType type;
    private final String description;

    public RaceEvent(int lap, EventType type, String description) {
        if (type == null || description == null) {
            throw new IllegalArgumentException("Event type and description are required.");
        }
        this.lap = lap;
        this.type = type;
        this.description = description;
    }

    public int getLap() { return lap; }
    public EventType getType() { return type; }
    public String getDescription() { return description; }

    @Override
    public String toString() {
        return "Lap " + lap + " [" + type + "] " + description;
    }
}
