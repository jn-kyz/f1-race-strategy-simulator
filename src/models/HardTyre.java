package models;

public class HardTyre extends Tyre {
    public HardTyre() {
        super("Hard", 0.92, 1.4, false);
    }

    @Override
    public double getWeatherSuitability(Weather.Condition weather) {
        switch (weather) {
            case DRY:        return 1.00;
            case LIGHT_RAIN: return 0.68;
            default:         return 0.42;
        }
    }

    @Override
    public String getDescription() {
        return "Most durable dry tyre, lower peak grip. Good for long stints.";
    }
}
