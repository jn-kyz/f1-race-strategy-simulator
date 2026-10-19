# Project Overview

## What it is
The F1 Race Strategy Simulator is a console program. The player acts as the strategist for one driver and
competes against three AI drivers over 3-70 laps. The player's decisions change how fast the car goes and how
quickly tyres, fuel, energy and engine temperature are used.

## Architecture (packages)
| Package | Responsibility |
|---------|----------------|
| `models` | Data objects: Driver, Team, Car, Telemetry, the Tyre family, Weather, RaceResult, RaceEvent |
| `strategy` | Player decisions and advice: Strategy, DrivingMode, StrategyDecision |
| `race` | Race state and flow: Race, RaceManager, PitStop, SafetyCar |
| `services` | Calculations and utilities: RaceEngine (lap times), StrategyAnalyzer (rating), InputValidator (console input) |
| `exceptions` | InvalidStrategyException, InvalidRaceConfigurationException |
| (default) | Main: menus and the race screen |

## Class relationships
- `Team` has many `Driver`s (association). A `Driver` knows its `Team` and owns its `Car`.
- `Car` owns a `Telemetry` (composition) and holds one `Tyre` at a time (association; the tyre is replaced in a pit stop).
- `Tyre` is abstract; `SoftTyre`, `MediumTyre`, `HardTyre`, `IntermediateTyre`, `WetTyre` extend it.
- `Race` holds the drivers, `Weather`, `SafetyCar` and the list of `RaceEvent`s.
- `RaceManager` uses `Race`, `RaceEngine`, `Strategy` and `PitStop` to run each lap.
- `RaceEngine` calculates one lap for one driver and updates the car.
- `StrategyAnalyzer` reads the finished race and builds the report.
- `Main` creates the objects and talks to the user through `InputValidator`.

## One lap, step by step
1. `Main` shows the lap header and the action menu.
2. The player picks an action. View actions and pit stops return to the menu; PUSH/NORMAL/SAVE continue.
3. `RaceManager.playLap(mode)`: AI drivers may pit and choose a mode; `RaceEngine.simulateLap` runs every driver.
4. Positions are re-sorted by retired flag, completed laps, then total time.
5. `Race.advanceToNextLap()` moves on (or finishes the race after the last lap).
6. Weather and safety car are rolled for the next lap and events are recorded.

## Lap-time formula (RaceEngine)
All values are seconds.
```
lap = 90.0
    + (1 - car performance) x 20
    + (1 - driver skill)    x 15
    + (1 - tyre performance) x 12     (grip x weather suitability x condition factor)
    + weather penalty                  (0 / 5 / 11)
    + driving mode change              (PUSH -0.9, NORMAL 0, SAVE +1.3)
    + fuel% x 0.03
    + 0.15 per degree above 105 C
    + 0.15 per % of energy below 20%
    + 10 if the tyre is completely worn
Safety car: lap = 125 + 0.25 x (all the extra seconds above)
```
Only AI drivers get a random +/-0.4 s per lap, from a seeded `Random`.

## Resource use
- Fuel per lap = 92% / total laps x mode factor (PUSH 1.12, NORMAL 1.00, SAVE 0.88). PUSH all race runs out of fuel just before the end.
- Energy per lap = 75% / total laps x mode factor (PUSH 1.40, NORMAL 1.00, SAVE 0.60); low energy slows the car.
- Engine temperature moves half-way to the mode's target each lap (PUSH 112 C, NORMAL 98 C, SAVE 90 C), clamped to 60-130 C.
- Tyre wear per lap = compound wear rate x mode factor (PUSH 1.35, NORMAL 1.00, SAVE 0.70) x weather multiplier.
- Under a safety car: fuel/energy use x0.6, tyre wear x0.5, pit-stop time x0.5.

## Safety car rules
Checked once per lap; never before lap 3 or on the final lap; 4% chance per lap by default; at least 5 laps after the
previous one; lasts 2-4 laps. It can be forced for testing with the second command-line argument.

## Strategy rating (StrategyAnalyzer)
Finishing position 40 + race completion 20 + pit-stop count 15 + laps on suitable tyres 15 + resources 10 = 100.
A retirement (DNF) caps the rating at 40. The rating only describes the race that was simulated.

## Defaults chosen
Player car performance 0.97 and skill 1.00; three fictional AI drivers; starting grid P3 for the player; pit-stop
penalty 20 s; weather change chance 6% (standard) or 15% (advanced) per lap; starting weather dry.
