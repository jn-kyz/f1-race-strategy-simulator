# Testing

## Automated-style checks that were actually run (JDK 21, Linux)
1. **Compile** with the documented command: succeeded with no errors.
2. **Scripted console runs** (input piped into the program):
   - Invalid main-menu input (`abc`, `9`) -> message and re-prompt. Passed.
   - Invalid lap count (`xyz`, `0`, `1000`) and invalid tyre (`9`, `q`) -> re-prompt, no crash. Passed.
   - Viewing telemetry / recommendation / race status did not advance the lap (lap number unchanged). Passed.
   - 5-lap race completed with correct classification, rating and return to the main menu. Passed.
   - Pit stop: cancel (0), invalid tyre (9), valid tyre (3), second pit on the same lap rejected with no time added. Passed.
   - Forced safety car (`Main 7 4`): deployed on lap 4, ended after 3 laps, pit stops cost 10 s instead of 20 s. Passed.
   - Weather changed DRY -> LIGHT_RAIN -> HEAVY_RAIN during a race; strategy warning recorded; AI drivers switched to Wet tyres. Passed.
   - All-PUSH 10-lap run: fuel warnings, then retirement "Out of fuel on lap 10", AI drivers finished, DNF classification, rating capped at 40. Passed.
   - Input ending in the middle of a prompt -> "Input ended. Goodbye!". Passed.
   - Same seed and same choices gave identical output twice; a different seed gave different output. Passed.
3. **Randomised stress test** (a temporary test program, not part of the project): 600 races with random lap counts (3-70),
   random starting tyres, random modes, random pit stops, high rain and safety-car probability. For every lap it checked:
   fuel and energy within 0-100, tyre condition within 0-100, engine temperature within 60-130, unique positions, lap counter never
   above total laps, every lap time positive, total time = sum of lap times + penalties, finishers completed exactly the configured
   laps, classification positions 1..n in order, rating within 0-100, second pit stop on the same lap rejected without changing the
   penalty total, no infinite loops. All 600 races passed.
4. **Lap-time sanity table** (NORMAL mode, new tyres): dry Soft 93.6 s, Medium 94.1 s, Hard 94.6 s, Intermediate 98.5 s, Wet 100.3 s;
   heavy rain Wet 107.0 s versus Soft 111.2 s; a completely worn Soft tyre in the dry takes 108.4 s.

Not tested: Windows itself and VS Code (the commands are the standard `javac`/`java` ones), and there is no JUnit suite.

## Manual checklist (run on your own computer)
- [ ] `javac -d out src\Main.java src\models\*.java src\strategy\*.java src\race\*.java src\exceptions\*.java src\services\*.java` shows no errors.
- [ ] `java -cp out Main` shows the menu.
- [ ] Menu 2 prints five tyres with different numbers; menu 3 prints the instructions.
- [ ] Start a 5-lap race; choose action 5, 6, 7: the lap number must stay the same.
- [ ] Choose 1/2/3: a lap time appears and the lap number increases by exactly 1.
- [ ] Choose 4, then 0: nothing changes. Choose 4 then a tyre: +20 s, tyre condition back to 100%.
- [ ] Choose 4 twice in one lap: second attempt rejected.
- [ ] `java -cp out Main 1 3`: a safety car appears on lap 3.
- [ ] Use PUSH on every lap of a 10-lap race: you eventually retire for lack of fuel.
- [ ] At the end: classification, your stats, event history and strategy analysis are all shown; you can return to the menu or exit.

## Example scenarios and expected behaviour
| Scenario | Expected |
|----------|----------|
| Type `abc` in the main menu | "not a whole number", asked again |
| Enter 2 laps | "Please enter a number between 3 and 70", asked again |
| Soft tyre, 30 laps, PUSH every lap | Tyre wears fast (about 4.3% per lap), warnings, probably a pit stop needed |
| Hard tyre when heavy rain starts | Warning "unsuitable for heavy rain", lap about 4-5 s slower than on Wet tyres |
| Pit stop under safety car | Penalty 10 s instead of 20 s |
| SAVE every lap | Slower laps, less fuel/energy/wear, ends with more fuel left |
