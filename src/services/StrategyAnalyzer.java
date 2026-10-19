package services;

import models.Driver;
import models.RaceEvent;
import models.RaceResult;
import race.Race;

import java.util.Locale;

// Rating out of 100: position 40, completion 20, pit stops 15, suitable tyres 15, resources 10.
// A DNF is capped at 40.
public class StrategyAnalyzer {
    private static final int DNF_RATING_CAP = 40;

    public int calculateRating(Driver driver, RaceResult result, Race race, int fieldSize) {
        int rating = (int) Math.round(positionScore(result, fieldSize) + completionScore(driver, race)
                + pitScore(driver) + tyreScore(driver) + resourceScore(driver, result));
        if (result.isRetired()) {
            rating = Math.min(rating, DNF_RATING_CAP);
        }
        return Math.max(0, Math.min(100, rating));
    }

    private double positionScore(RaceResult result, int fieldSize) {
        if (result.isRetired()) {
            return 0.0;
        }
        if (fieldSize <= 1) {
            return 40.0;
        }
        return 40.0 * (fieldSize - result.getFinalPosition()) / (fieldSize - 1);
    }

    private double completionScore(Driver driver, Race race) {
        return 20.0 * driver.getCompletedLaps() / race.getTotalLaps();
    }

    private double pitScore(Driver driver) {
        switch (driver.getPitStops()) {
            case 0:
            case 1:  return 15.0;
            case 2:  return 10.0;
            case 3:  return 5.0;
            default: return 0.0;
        }
    }

    private double tyreScore(Driver driver) {
        if (driver.getCompletedLaps() == 0) {
            return 0.0;
        }
        return 15.0 * driver.getSuitableTyreLaps() / driver.getCompletedLaps();
    }

    private double resourceScore(Driver driver, RaceResult result) {
        double score = 0.0;
        if (!result.isRetired() && driver.getCar().getFuelPercent() > 0.0) {
            score += 5.0;
        }
        if (!driver.getCar().isEnergyDepleted()) {
            score += 5.0;
        }
        return score;
    }

    private int countIncidents(Race race) {
        return race.countEvents(RaceEvent.EventType.SAFETY_CAR)
             + race.countEvents(RaceEvent.EventType.WEATHER_CHANGE)
             + race.countEvents(RaceEvent.EventType.RETIREMENT)
             + race.countEvents(RaceEvent.EventType.STRATEGY_WARNING);
    }

    public String generateReport(Driver driver, RaceResult result, Race race, int fieldSize) {
        double position = positionScore(result, fieldSize);
        double completion = completionScore(driver, race);
        double pit = pitScore(driver);
        double tyres = tyreScore(driver);
        double resources = resourceScore(driver, result);
        int rating = calculateRating(driver, result, race, fieldSize);
        String nl = System.lineSeparator();

        StringBuilder sb = new StringBuilder();
        sb.append("=========== STRATEGY ANALYSIS ===========").append(nl);
        sb.append("Final position      : ").append(result.isRetired() ? "DNF" : "P" + result.getFinalPosition())
          .append(" of ").append(fieldSize).append(nl);
        sb.append("Total race time     : ").append(RaceResult.formatTime(result.getTotalTime())).append(nl);
        sb.append("Pit stops           : ").append(result.getPitStops()).append(nl);
        sb.append("Average lap time    : ").append(driver.getCompletedLaps() == 0 ? "n/a"
                : RaceResult.formatTime(driver.getAverageLapTime())).append(nl);
        sb.append("Completed laps      : ").append(result.getCompletedLaps()).append(" / ")
          .append(race.getTotalLaps()).append(nl);
        sb.append("Recorded incidents  : ").append(countIncidents(race))
          .append(" (weather changes, safety cars, warnings, retirements)").append(nl);
        sb.append(String.format(Locale.US, "STRATEGY RATING     : %d / 100%n", rating));
        sb.append("How the rating was made:").append(nl);
        sb.append(String.format(Locale.US, "  Finishing position %4.1f / 40%n", position));
        sb.append(String.format(Locale.US, "  Race completion    %4.1f / 20%n", completion));
        sb.append(String.format(Locale.US, "  Pit-stop count     %4.1f / 15%n", pit));
        sb.append(String.format(Locale.US, "  Suitable tyres     %4.1f / 15  (%d of %d laps on a suitable tyre)%n",
                tyres, driver.getSuitableTyreLaps(), driver.getCompletedLaps()));
        sb.append(String.format(Locale.US, "  Resources          %4.1f / 10%n", resources));
        if (result.isRetired()) {
            sb.append("  DNF cap: the rating is limited to ").append(DNF_RATING_CAP).append(nl);
        }
        sb.append("Summary: ").append(summary(driver, result, rating)).append(nl);
        sb.append("Note: this rating only describes this simulated race; other strategies may suit other conditions.");
        return sb.toString();
    }

    private String summary(Driver driver, RaceResult result, int rating) {
        if (result.isRetired()) {
            return "The race ended early (" + driver.getRetirementReason()
                    + "), so most of the score was lost. Watch the fuel recommendation next time.";
        }
        if (rating >= 85) {
            return "Excellent: good result, sensible tyres and well-managed resources.";
        }
        if (rating >= 65) {
            return "Solid: a decent result, with some room to improve tyre timing or resource use.";
        }
        if (rating >= 40) {
            return "Mixed: check how many laps were run on unsuitable or worn tyres and how many stops were made.";
        }
        return "Difficult race: review the tyre choices against the weather and the pit-stop timing.";
    }
}
