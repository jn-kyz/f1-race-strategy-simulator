package models;

public class IntermediateTyre extends Tyre {
    public IntermediateTyre() {
        super("Intermediate", 0.85, 2.0, true);
    }

    @Override
    public double getWeatherSuitability(Weather.Condition weather) {
        switch (weather) {
            case DRY:        return 0.70;
            case LIGHT_RAIN: return 1.00;
            default:         return 0.80;
        }
    }

    @Override
    public double getWeatherWearMultiplier(Weather.Condition weather) {
        return weather == Weather.Condition.DRY ? 1.8 : 1.0;
    }

    @Override
    public String getDescription() {
        return "Best in light rain. Overheats and wears fast on a dry track.";
    }
}
