# F1 Race Strategy Simulator

A Java console application where you play a Formula 1 **race strategist**. You do not drive the car;
you decide driving modes and pit stops while the simulation calculates lap times, tyre wear, fuel,
energy, engine temperature, weather and safety cars. Your final position and a strategy rating are
calculated from the simulated race.

> This is an educational simulation inspired by F1 strategy. It is **not** an official F1 product, uses no
> F1 logos or real telemetry, and does not reproduce real F1 physics. All driver and team names are fictional.

## Features
- Interactive console menu and lap-by-lap strategy decisions (PUSH / NORMAL / SAVE / PIT STOP).
- Five tyre types with different grip, wear and weather behaviour (polymorphism).
- Weather that changes occasionally (dry, light rain, heavy rain) and affects lap times and tyres.
- Safety car events (random but limited, or forced for testing) that change lap times and pit-stop cost.
- Three AI opponents; positions come from completed laps and total race time.
- Fuel, energy and engine temperature management; running out of fuel retires the car (DNF).
- Telemetry view, strategy recommendations with explanations, race event history.
- End-of-race classification and a strategy rating out of 100 with a documented formula.
- Reproducible races through an optional random seed.

## Technologies
Java 17 or newer (developed and tested on JDK 21), standard library only. No external libraries,
databases, API keys or internet needed. Works in Visual Studio Code or any terminal.

## Java OOP concepts demonstrated
Encapsulation, inheritance, abstraction, polymorphism, composition, association, collections (`ArrayList`),
enums, custom checked exceptions, input validation. See `docs/OOP_CONCEPTS.md` for the exact classes.

## Folder structure
```
f1-race-strategy-simulator/
  src/
    Main.java
    models/      Driver, Team, Car, Tyre (abstract), SoftTyre, MediumTyre, HardTyre,
                 IntermediateTyre, WetTyre, Weather, Telemetry, RaceResult, RaceEvent
    strategy/    Strategy, DrivingMode (enum), StrategyDecision (enum)
    race/        Race, RaceManager, PitStop, SafetyCar
    exceptions/  InvalidStrategyException, InvalidRaceConfigurationException
    services/    RaceEngine, StrategyAnalyzer, InputValidator
  docs/          PROJECT_OVERVIEW, OOP_CONCEPTS, TEAM_RESPONSIBILITIES ,TESTING
  README.md
  .gitignore
```

## Requirements
JDK 17 or newer. Check with `java -version` and `javac -version`.

## Compile and run (Windows, from the project root folder)
```
mkdir out
javac -d out src\Main.java src\models\*.java src\strategy\*.java src\race\*.java src\exceptions\*.java src\services\*.java
java -cp out Main
```
(`mkdir out` is only needed the first time; `javac -d out` also creates it if it is missing.)

Optional arguments for testing: `java -cp out Main <seed> <safetyCarLap>`
- `java -cp out Main 12345` runs the same race every time (same seed and same choices).
- `java -cp out Main 12345 6` also forces a safety car to come out on lap 6.

On macOS/Linux use `/` instead of `\` in the source paths.

## How to play
1. Choose **Start New Race**, then enter a driver name, team name, number of laps (3-70), starting tyre and weather variability.
2. Each lap choose an action:
   - `1 PUSH`, `2 NORMAL`, `3 SAVE` run the lap in that mode.
   - `4 PIT STOP` lets you pick a new tyre (or cancel). It costs time, does **not** run a lap, and you then choose a driving mode.
   - `5` telemetry, `6` strategy recommendation, `7` race status. These never advance the lap.
3. Finish all laps. You get the classification, your stats, the event history and a strategy analysis.

## Team members
| Member | Name | Main area |
|--------|------|-----------|
| 1 | Aditi | Race system |
| 2 | Aashvi | Car and tyres |
| 3 | Vatsala | Strategy and events |
| 4 | Janki | Main system and integration |

See `docs/TEAM_RESPONSIBILITIES.md`. Adjust it to match the work each member actually did.

## Known limitations
- Console only; no graphics. Single player against 3 simple AI drivers (they use basic rules and small random variation).
- Lap times use a simplified, documented formula, not real F1 physics. There is no refuelling, no overtaking model, no tyre-temperature model.
- Only one pit stop per lap; a pit stop happens before the lap is driven.
- No saving or loading of races. No automated unit-test suite (testing was done with scripted runs; see `docs/TESTING.md`).

## Development status
Version 1.0: complete working console version. Possible future work: GUI, more drivers, saved results, unit tests.
