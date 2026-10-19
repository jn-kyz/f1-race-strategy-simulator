package models;

public class SoftTyre extends Tyre {
    public SoftTyre() {
        super("Soft", 1.00, 3.2, false);
    }

    @Override
    public double getWeatherSuitability(Weather.Condition weather) {
        switch (weather) {
            case DRY:        return 1.00;
            case LIGHT_RAIN: return 0.70;
            default:         return 0.45;
        }
    }

    @Override
    public String getDescription() {
        return "Fastest dry tyre, wears quickly. Best for short stints on a dry track.";
    }
}
