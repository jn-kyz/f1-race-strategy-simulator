package race;

import exceptions.InvalidStrategyException;
import models.Driver;
import models.HardTyre;
import models.IntermediateTyre;
import models.MediumTyre;
import models.RaceEvent;
import models.SoftTyre;
import models.Tyre;
import models.WetTyre;

// Penalty is halved under the safety car. Nothing changes if the stop is not allowed.
public class PitStop {
    public static final double DEFAULT_PENALTY_SECONDS = 20.0;
    public static final double SAFETY_CAR_PENALTY_FACTOR = 0.5;

    public static final String[] TYRE_NAMES = {"Soft", "Medium", "Hard", "Intermediate", "Wet"};

    private final Driver driver;
    private final Tyre newTyre;
    private final double basePenalty;

    public PitStop(Driver driver, Tyre newTyre) {
        this(driver, newTyre, DEFAULT_PENALTY_SECONDS);
    }

    public PitStop(Driver driver, Tyre newTyre, double basePenalty) {
        if (driver == null || newTyre == null) {
            throw new IllegalArgumentException("Pit stop needs a driver and a tyre.");
        }
        if (basePenalty < 0.0) {
            throw new IllegalArgumentException("Pit stop penalty cannot be negative.");
        }
        this.driver = driver;
        this.newTyre = newTyre;
        this.basePenalty = basePenalty;
    }

    public static Tyre createTyre(int choice) throws InvalidStrategyException {
        if (choice < 1 || choice > TYRE_NAMES.length) {
            throw new InvalidStrategyException("Invalid tyre choice: " + choice + " (choose 1-5).");
        }
        return createTyre(TYRE_NAMES[choice - 1]);
    }

    public static Tyre createTyre(String compoundName) throws InvalidStrategyException {
        if (compoundName != null) {
            switch (compoundName.toLowerCase()) {
                case "soft":         return new SoftTyre();
                case "medium":       return new MediumTyre();
                case "hard":         return new HardTyre();
                case "intermediate": return new IntermediateTyre();
                case "wet":          return new WetTyre();
                default:             break;
            }
        }
        throw new InvalidStrategyException("Unknown tyre compound: " + compoundName);
    }

    public double execute(Race race) throws InvalidStrategyException {
        int lap = race.getCurrentLap();

        // check everything before changing anything
        if (driver.isRetired()) {
            throw new InvalidStrategyException(driver.getName() + " has retired and cannot pit.");
        }
        if (race.isFinished() || lap < 1) {
            throw new InvalidStrategyException("Pit stops are only possible during the race.");
        }
        if (driver.getLastPitLap() == lap) {
            throw new InvalidStrategyException("Already pitted on lap " + lap + ".");
        }

        String oldTyre = driver.getCar().getTyre().toString();
        double penalty = race.isSafetyCarActive() ? basePenalty * SAFETY_CAR_PENALTY_FACTOR : basePenalty;
        driver.getCar().installTyre(newTyre);
        driver.addTimePenalty(penalty);
        driver.incrementPitStops(lap);

        race.recordEvent(new RaceEvent(lap, RaceEvent.EventType.PIT_STOP, String.format(java.util.Locale.US,
                "%s pitted (+%.1fs%s).", driver.getName(), penalty,
                race.isSafetyCarActive() ? ", safety car discount" : "")));
        race.recordEvent(new RaceEvent(lap, RaceEvent.EventType.TYRE_CHANGE,
                driver.getName() + " changed tyres: " + oldTyre + " -> " + newTyre.getCompoundName() + " (100%)."));
        return penalty;
    }
}
