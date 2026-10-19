package models;

public class MediumTyre extends Tyre {
    public MediumTyre() {
        super("Medium", 0.96, 2.2, false);
    }

    @Override
    public double getWeatherSuitability(Weather.Condition weather) {
        switch (weather) {
            case DRY:        return 1.00;
            case LIGHT_RAIN: return 0.72;
            default:         return 0.47;
        }
    }

    @Override
    public String getDescription() {
        return "Balanced dry tyre. Good all-round choice on a dry track.";
    }
}
