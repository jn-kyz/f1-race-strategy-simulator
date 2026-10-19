# Java OOP Concepts in this Project

## Encapsulation
Fields are `private`; changes go through validated methods.
- `Car`: `consumeFuel`, `consumeEnergy`, `updateEngineTemperature` clamp values (fuel/energy 0-100, temperature 60-130).
- `Tyre`: `applyWear` keeps condition between 0 and 100 and rejects negative wear.
- `Driver`: `setPosition`, `addLapTime`, `addTimePenalty` reject invalid values.
- `Weather.setChangeProbability`, `SafetyCar.setDeployProbability` accept only 0-1.

## Inheritance
`Tyre` (abstract parent) is extended by `SoftTyre`, `MediumTyre`, `HardTyre`, `IntermediateTyre`, `WetTyre`.
They reuse the fields and methods of `Tyre` (`applyWear`, `getPerformanceContribution`, `isHeavilyWorn`, `isUnusable`).

## Abstraction
`Tyre` declares abstract methods `getWeatherSuitability(Weather.Condition)` and `getDescription()`.
It defines *what every tyre can do*; each subclass defines *how*. `Tyre` itself cannot be instantiated.

## Polymorphism
- `Car` holds a `Tyre` reference. `RaceEngine.calculateLapTime` calls `tyre.getPerformanceContribution(weather)` once,
  with no `if (tyre is Soft)` blocks. The overridden `getWeatherSuitability` of the actual object decides the result.
- `Main.showTyreInformation` loops over a `List<Tyre>` and calls `getInfo()` on each.
- `getWeatherWearMultiplier` is overridden only by `IntermediateTyre` and `WetTyre` (they overheat on a dry track).

## Composition
`Car` creates and owns its `Telemetry` (`new Telemetry(this)` in the constructor). Telemetry is part of one car and has no
meaning without it, so it is composition. `Telemetry` stores no copies; it reads the car's current values.

## Association
- `Driver` -> `Team` and `Car`; `Team` -> list of `Driver`s.
- `Race` -> `Driver`s, `Weather`, `SafetyCar`.
- `RaceManager` -> `Race`, `RaceEngine`, `Strategy`, the player `Driver`.
These objects exist independently and collaborate through references.

## Collections
`ArrayList` is used for: drivers in `Team` and `Race`, race events in `Race`, lap times in `Driver`, race results from
`RaceManager.getFinalClassification()`. Sorting uses `List.sort` with a comparator in `RaceManager.getOrderedDrivers()`.
A `HashSet<String>` in `RaceManager` avoids recording the same strategy warning every lap.

## Enums
`DrivingMode` (PUSH, NORMAL, SAVE) carries numeric factors; `StrategyDecision` maps menu numbers to actions;
`Weather.Condition` (DRY, LIGHT_RAIN, HEAVY_RAIN); `RaceEvent.EventType`; `Race.Status`; `SafetyCar.Change`.

## Exception handling
- `InvalidStrategyException` (checked): thrown by `StrategyDecision.fromMenuNumber`, `PitStop.createTyre`, `PitStop.execute`
  (retired driver, same-lap second pit stop, race not running) and `Strategy.chooseDrivingMode(null)`. `Main` catches it and
  keeps the race going.
- `InvalidRaceConfigurationException` (checked): thrown by the `Race` constructor for a bad lap count, no drivers, null parts.
  `Main.startNewRace` catches it and returns to the menu.
- `IllegalArgumentException`: used for programming errors such as a negative lap time or invalid constructor values.
- `InputValidator` catches `NumberFormatException` so letters never crash the program. End of input
  (`NoSuchElementException`) is caught in `Main.main` for a clean exit.

## Other ideas worth knowing
- Constructor chaining: `PitStop(driver, tyre)` calls `PitStop(driver, tyre, 20.0)`; `Weather()` calls `Weather(DRY, 0.06)`.
- `static` utility methods: `RaceResult.formatTime`, `PitStop.createTyre`.
- Packages and imports: each folder under `src` is a package; `Main` (default package) imports the others.
