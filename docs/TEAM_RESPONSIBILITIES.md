# Team Responsibilities (proposal)

| Member | Area | Files |
|--------|------|-------|
| 1 | Race system | `race/Race.java`, `race/RaceManager.java`, `models/RaceResult.java`, `models/Driver.java` |
| 2 | Car and tyres | `models/Car.java`, `Tyre.java`, `SoftTyre.java`, `MediumTyre.java`, `HardTyre.java`, `IntermediateTyre.java`, `WetTyre.java`, `Telemetry.java` |
| 3 | Strategy and events | `strategy/Strategy.java`, `DrivingMode.java`, `StrategyDecision.java`, `race/PitStop.java`, `models/Weather.java`, `race/SafetyCar.java`, `models/RaceEvent.java` |
| 4 | Main and integration | `Main.java`, `models/Team.java`, `services/RaceEngine.java`, `StrategyAnalyzer.java`, `InputValidator.java`, `exceptions/*`, README, integration testing |

## Dependencies (so people can work in parallel)
Start from the bottom of this list; each class depends only on the ones above it.
1. `exceptions/*`, `DrivingMode`, `Weather`, `RaceEvent` (no dependencies on other project classes).
2. `Tyre` and the five tyre classes (need `Weather`).
3. `Car`, `Telemetry` (need `Tyre`, `Weather`).
4. `Team`, `Driver`, `RaceResult` (Driver needs Team, Car).
5. `StrategyDecision` (needs the exceptions and `DrivingMode`), `Strategy` (needs Car, Tyre, Weather, DrivingMode).
6. `SafetyCar`, `Race` (needs Driver, Weather, SafetyCar, RaceEvent, exceptions), `PitStop` (needs Race, Driver, tyres).
7. `RaceEngine` (needs Car, Driver, Race, DrivingMode).
8. `RaceManager` (needs Race, RaceEngine, Strategy, PitStop).
9. `StrategyAnalyzer`, `InputValidator`, `Main`.

## Stable method signatures to agree on first
- `Tyre`: `getWeatherSuitability(Weather.Condition)`, `getPerformanceContribution(Weather.Condition)`, `applyWear(double, Weather.Condition)`, `getCondition()`.
- `Car`: `consumeFuel(double)`, `consumeEnergy(double)`, `updateEngineTemperature(double)`, `wearTyre(double, Weather.Condition)`, `installTyre(Tyre)`.
- `Driver`: `addLapTime(double)`, `addTimePenalty(double)`, `incrementPitStops(int)`, `setPosition(int)`, `retire(String)`.
- `Race`: `start()`, `advanceToNextLap()`, `recordEvent(RaceEvent)`, `getCurrentLap()`, `getWeather()`, `isSafetyCarActive()`.
- `RaceEngine`: `calculateLapTime(...)`, `simulateLap(Driver, DrivingMode, Race, boolean)`.

## Integration checklist for Member 4
- Compile with the documented `javac` command after every merge.
- Run the scenarios in `docs/TESTING.md`.
