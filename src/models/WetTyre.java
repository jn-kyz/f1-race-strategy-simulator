package models;

public class WetTyre extends Tyre {
    public WetTyre() {
        super("Wet", 0.80, 1.8, true);
    }

    @Override
    public double getWeatherSuitability(Weather.Condition weather) {
        switch (weather) {
            case DRY:        return 0.55;
            case LIGHT_RAIN: return 0.85;
            default:         return 1.00;
        }
    }

    @Override
    public double getWeatherWearMultiplier(Weather.Condition weather) {
        switch (weather) {
            case DRY:        return 2.5;
            case LIGHT_RAIN: return 1.3;
            default:         return 1.0;
        }
    }

    @Override
    public String getDescription() {
        return "Best in heavy rain. Very poor and fast-wearing on a dry track.";
    }
}
