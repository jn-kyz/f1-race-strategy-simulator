import exceptions.InvalidRaceConfigurationException;
import exceptions.InvalidStrategyException;
import models.Car;
import models.Driver;
import models.HardTyre;
import models.IntermediateTyre;
import models.MediumTyre;
import models.RaceEvent;
import models.RaceResult;
import models.SoftTyre;
import models.Team;
import models.Tyre;
import models.Weather;
import models.WetTyre;
import race.PitStop;
import race.Race;
import race.RaceManager;
import race.SafetyCar;
import services.InputValidator;
import services.RaceEngine;
import services.StrategyAnalyzer;
import strategy.DrivingMode;
import strategy.Strategy;
import strategy.StrategyDecision;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Scanner;

// Optional arguments: [seed] [safetyCarLap], e.g. java -cp out Main 12345 6
public class Main {
    private static InputValidator input;
    private static long seed = System.nanoTime();
    private static int forcedSafetyCarLap = 0;
    private static int printedEvents = 0;

    public static void main(String[] args) {
        parseArguments(args);
        Scanner scanner = new Scanner(System.in);
        input = new InputValidator(scanner);
        try {
            mainMenuLoop();
        } catch (NoSuchElementException e) {
            System.out.println();
            System.out.println("Input ended. Goodbye!");
        }
    }

    private static void parseArguments(String[] args) {
        try {
            if (args.length >= 1) {
                seed = Long.parseLong(args[0]);
            }
            if (args.length >= 2) {
                forcedSafetyCarLap = Integer.parseInt(args[1]);
            }
        } catch (NumberFormatException e) {
            System.out.println("Ignoring invalid arguments. Usage: java -cp out Main [seed] [safetyCarLap]");
        }
    }

    private static void mainMenuLoop() {
        boolean running = true;
        while (running) {
            System.out.println();
            System.out.println("==============================================");
            System.out.println("          F1 RACE STRATEGY SIMULATOR");
            System.out.println("==============================================");
            System.out.println();
            System.out.println("1. Start New Race");
            System.out.println("2. View Tyre Information");
            System.out.println("3. View How to Play");
            System.out.println("4. Exit");
            System.out.println();
            int choice = input.readInt("Choose an option: ", 1, 4);
            switch (choice) {
                case 1:
                    startNewRace();
                    break;
                case 2:
                    showTyreInformation();
                    break;
                case 3:
                    showHowToPlay();
                    break;
                default:
                    running = false;
                    break;
            }
        }
        System.out.println("Thanks for playing. Goodbye!");
    }

    private static void showTyreInformation() {
        System.out.println();
        System.out.println("---------------- TYRE INFORMATION ----------------");
        List<Tyre> tyres = new ArrayList<>();
        tyres.add(new SoftTyre());
        tyres.add(new MediumTyre());
        tyres.add(new HardTyre());
        tyres.add(new IntermediateTyre());
        tyres.add(new WetTyre());
        for (Tyre tyre : tyres) {
            System.out.println(tyre.getInfo());
            System.out.println();
        }
        System.out.println("'dry/light rain/heavy rain' = how suitable the tyre is in that weather.");
    }

    private static void showHowToPlay() {
        System.out.println();
        System.out.println("---------------- HOW TO PLAY ----------------");
        System.out.println("You are the race strategist. You do not steer; you decide the plan.");
        System.out.println("Each lap, pick an action:");
        System.out.println("  1 PUSH    faster laps, but more fuel, energy, heat and tyre wear");
        System.out.println("  2 NORMAL  balanced");
        System.out.println("  3 SAVE    slower laps, but protects fuel, energy and tyres");
        System.out.println("  4 PIT     fit new tyres (costs 20 seconds, or 10 under a safety car)");
        System.out.println("  5-7       view telemetry, advice or standings (these do NOT use up a lap)");
        System.out.println("Choosing 1, 2 or 3 runs the lap. Pit stops happen before the lap, so pit first,");
        System.out.println("then choose a driving mode.");
        System.out.println("Watch the weather: dry tyres are slow in rain, rain tyres are slow on a dry track.");
        System.out.println("Fuel never refills. If it runs out you retire. Safety cars make pit stops cheaper.");
        System.out.println("Your position comes from completed laps and total time against 3 AI drivers.");
    }

    private static void startNewRace() {
        System.out.println();
        System.out.println("---------------- NEW RACE ----------------");
        String driverName = input.readNonEmptyString("Driver name: ", 25);
        String teamName = input.readNonEmptyString("Team name: ", 25);
        int laps = input.readInt("Total laps (" + Race.MIN_LAPS + "-" + Race.MAX_LAPS + "): ",
                Race.MIN_LAPS, Race.MAX_LAPS);

        Tyre startingTyre = askForTyre("Choose your starting tyre:");
        System.out.println("Weather variability: 1. Standard (rain changes are rare)   2. Advanced (more changes)");
        int variability = input.readInt("Choose 1 or 2: ", 1, 2);
        double weatherChance = (variability == 1) ? Weather.DEFAULT_CHANGE_PROBABILITY : 0.15;

        try {
            Team playerTeam = new Team(teamName, 0.97);
            Car playerCar = new Car(playerTeam.getCarPerformance(), startingTyre);
            Driver player = new Driver(driverName, 7, playerTeam, playerCar, 1.00);
            playerTeam.addDriver(player);

            Driver rivalA = createAiDriver("A. Rivera", 11, "Apex Racing", 0.98, 0.99, new MediumTyre());
            Driver rivalB = createAiDriver("K. Tanaka", 22, "Nova Motorsport", 0.965, 0.985, new HardTyre());
            Driver rivalC = createAiDriver("L. Moreau", 33, "Vertex GP", 0.95, 0.97, new SoftTyre());

            List<Driver> grid = new ArrayList<>();
            grid.add(rivalA);
            grid.add(rivalB);
            grid.add(player);
            grid.add(rivalC);

            RaceEngine engine = new RaceEngine(seed);
            Weather weather = new Weather(Weather.Condition.DRY, weatherChance);
            SafetyCar safetyCar = new SafetyCar();
            if (forcedSafetyCarLap > 0) {
                safetyCar.scheduleDeployment(forcedSafetyCarLap, 3);
            }
            Race race = new Race("Simulated Grand Prix", laps, grid, weather, safetyCar);
            Strategy strategy = new Strategy(laps,
                    engine.calculateFuelUse(DrivingMode.NORMAL, laps, false),
                    engine.calculateFuelUse(DrivingMode.SAVE, laps, false));
            RaceManager manager = new RaceManager(race, engine, player, strategy);

            runRace(race, manager, player, strategy);
        } catch (InvalidRaceConfigurationException e) {
            System.out.println("Could not create the race: " + e.getMessage());
        }

        if (!input.readYesNo("Return to the main menu?")) {
            System.out.println("Thanks for playing. Goodbye!");
            System.exit(0);
        }
    }

    private static Driver createAiDriver(String name, int number, String teamName, double carPerformance,
                                         double skill, Tyre tyre) {
        Team team = new Team(teamName, carPerformance);
        Driver driver = new Driver(name, number, team, new Car(carPerformance, tyre), skill);
        team.addDriver(driver);
        return driver;
    }

    private static Tyre askForTyre(String title) {
        while (true) {
            System.out.println(title);
            for (int i = 0; i < PitStop.TYRE_NAMES.length; i++) {
                System.out.println("  " + (i + 1) + ". " + PitStop.TYRE_NAMES[i]);
            }
            int choice = input.readInt("Tyre (1-5): ", 1, PitStop.TYRE_NAMES.length);
            try {
                return PitStop.createTyre(choice);
            } catch (InvalidStrategyException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private static void runRace(Race race, RaceManager manager, Driver player, Strategy strategy) {
        printedEvents = 0;
        manager.startRace();
        System.out.println();
        System.out.println("Lights out! " + race.getName() + " - " + race.getTotalLaps() + " laps.");

        while (!race.isFinished() && !player.isRetired()) {
            printNewEvents(race);
            printLapHeader(race, player, strategy, manager);

            boolean lapSimulated = false;
            while (!lapSimulated) {
                printActionMenu();
                int choice = input.readInt("Choose action (1-7): ", 1, 7);
                try {
                    StrategyDecision decision = StrategyDecision.fromMenuNumber(choice);
                    lapSimulated = handleDecision(decision, race, manager, player, strategy);
                } catch (InvalidStrategyException e) {
                    System.out.println("  Invalid decision: " + e.getMessage());
                }
            }
        }

        printNewEvents(race);
        if (player.isRetired()) {
            System.out.println();
            System.out.println("*** You have retired: " + player.getRetirementReason() + " ***");
            System.out.println("The remaining drivers finish the race...");
            manager.finishRaceWithoutPlayer();
        }
        printRaceSummary(race, manager, player);
    }

    private static void printLapHeader(Race race, Driver player, Strategy strategy, RaceManager manager) {
        Car car = player.getCar();
        System.out.println();
        System.out.println("================= LAP " + race.getCurrentLap() + " / " + race.getTotalLaps()
                + " =================");
        System.out.printf(Locale.US, "Driver: %s | Position: P%d | Weather: %s%s%n",
                player.getName(), player.getPosition(), race.getWeather().getDisplayText(),
                race.isSafetyCarActive() ? " | SAFETY CAR OUT" : "");
        System.out.printf(Locale.US, "Tyre: %s %.1f%% | Fuel: %.1f%% | Energy: %.1f%% | Engine: %.1f C%n",
                car.getTyre().getCompoundName(), car.getTyre().getCondition(), car.getFuelPercent(),
                car.getEnergyPercent(), car.getEngineTemperature());
        if (strategy.shouldPitStop(car, race.getWeather().getCondition(), race.getCurrentLap(),
                race.isSafetyCarActive())) {
            System.out.println("Hint: a pit stop looks worthwhile (choose 6 for the full advice).");
        }
    }

    private static void printActionMenu() {
        System.out.println("Actions:");
        System.out.println("  1. PUSH     2. NORMAL     3. SAVE     4. PIT STOP");
        System.out.println("  5. VIEW TELEMETRY   6. VIEW STRATEGY RECOMMENDATION   7. VIEW RACE STATUS");
    }

    private static boolean handleDecision(StrategyDecision decision, Race race, RaceManager manager,
                                          Driver player, Strategy strategy) throws InvalidStrategyException {
        switch (decision) {
            case PUSH:
            case NORMAL:
            case SAVE:
                DrivingMode mode = decision.getDrivingMode();
                strategy.chooseDrivingMode(mode);
                int lapNumber = race.getCurrentLap();
                int lapsBefore = player.getCompletedLaps();
                manager.playLap(strategy.getDrivingMode());
                if (player.getCompletedLaps() > lapsBefore) {
                    List<Double> times = player.getLapTimes();
                    System.out.printf(Locale.US, "Lap %d completed in %s (%s) | Position: P%d | Race time: %s%n",
                            lapNumber, RaceResult.formatTime(times.get(times.size() - 1)), mode.getLabel(),
                            player.getPosition(), RaceResult.formatTime(player.getTotalRaceTime()));
                }
                printNewEvents(race);
                return true;
            case PIT_STOP:
                performPitStop(race, manager, player);
                return false;
            case VIEW_TELEMETRY:
                System.out.println(player.getCar().getTelemetry().format());
                return false;
            case VIEW_RECOMMENDATION:
                System.out.println("---------- STRATEGY RECOMMENDATION ----------");
                for (String line : strategy.generateRecommendations(player.getCar(),
                        race.getWeather().getCondition(), race.getCurrentLap(), race.isSafetyCarActive())) {
                    System.out.println("  " + line);
                }
                System.out.println("(These are suggestions only. The decision is yours.)");
                return false;
            case VIEW_RACE_STATUS:
                System.out.print(manager.getStandingsTable());
                return false;
            default:
                throw new InvalidStrategyException("Unhandled decision: " + decision);
        }
    }

    private static void performPitStop(Race race, RaceManager manager, Driver player) {
        System.out.println("Current tyre: " + player.getCar().getTyre());
        System.out.println("Choose a replacement tyre (0 to cancel):");
        for (int i = 0; i < PitStop.TYRE_NAMES.length; i++) {
            System.out.println("  " + (i + 1) + ". " + PitStop.TYRE_NAMES[i]);
        }
        int choice = input.readInt("Tyre (0-5): ", 0, PitStop.TYRE_NAMES.length);
        if (choice == 0) {
            System.out.println("Pit stop cancelled. No time lost.");
            return;
        }
        try {
            Tyre newTyre = PitStop.createTyre(choice);
            double penalty = new PitStop(player, newTyre).execute(race);
            System.out.printf(Locale.US, "Pit stop complete: %s fitted, +%.1f seconds.%n",
                    newTyre.getCompoundName(), penalty);
            manager.checkPlayerWarnings();
            printNewEvents(race);
            System.out.println("Now choose a driving mode to run lap " + race.getCurrentLap() + ".");
        } catch (InvalidStrategyException e) {
            System.out.println("Pit stop not possible: " + e.getMessage() + " No time was added.");
        }
    }

    private static void printNewEvents(Race race) {
        List<RaceEvent> events = race.getEvents();
        while (printedEvents < events.size()) {
            System.out.println("  * " + events.get(printedEvents));
            printedEvents++;
        }
    }

    private static void printRaceSummary(Race race, RaceManager manager, Driver player) {
        List<RaceResult> classification = manager.getFinalClassification();
        RaceResult playerResult = manager.getResultFor(player);
        Car car = player.getCar();

        System.out.println();
        System.out.println("==============================================");
        System.out.println("               RACE FINISHED");
        System.out.println("==============================================");
        System.out.println("FINAL CLASSIFICATION");
        for (RaceResult result : classification) {
            System.out.println(result);
        }
        System.out.println();
        System.out.println("YOUR RACE");
        System.out.println("Total race time : " + RaceResult.formatTime(player.getTotalRaceTime())
                + " (laps " + RaceResult.formatTime(player.getLapTimeTotal())
                + " + penalties " + RaceResult.formatTime(player.getPenaltyTotal()) + ")");
        System.out.println("Completed laps  : " + player.getCompletedLaps() + " / " + race.getTotalLaps());
        System.out.printf(Locale.US, "Final tyre      : %s %.1f%%%n", car.getTyre().getCompoundName(),
                car.getTyre().getCondition());
        System.out.printf(Locale.US, "Remaining fuel  : %.1f%%   Remaining energy: %.1f%%%n",
                car.getFuelPercent(), car.getEnergyPercent());
        System.out.println("Pit stops       : " + player.getPitStops());
        System.out.println();
        System.out.println("RACE EVENT HISTORY");
        if (race.getEvents().isEmpty()) {
            System.out.println("  (no events)");
        }
        for (RaceEvent event : race.getEvents()) {
            System.out.println("  " + event);
        }
        System.out.println();
        System.out.println(new StrategyAnalyzer().generateReport(player, playerResult, race, classification.size()));
    }
}
