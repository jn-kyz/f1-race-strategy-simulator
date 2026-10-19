package strategy;

import exceptions.InvalidStrategyException;

public enum StrategyDecision {
    PUSH(1, "PUSH", DrivingMode.PUSH, true),
    NORMAL(2, "NORMAL", DrivingMode.NORMAL, true),
    SAVE(3, "SAVE", DrivingMode.SAVE, true),
    PIT_STOP(4, "PIT STOP", null, false),
    VIEW_TELEMETRY(5, "VIEW TELEMETRY", null, false),
    VIEW_RECOMMENDATION(6, "VIEW STRATEGY RECOMMENDATION", null, false),
    VIEW_RACE_STATUS(7, "VIEW RACE STATUS", null, false);

    private final int menuNumber;
    private final String label;
    private final DrivingMode drivingMode;
    private final boolean advancesLap;

    StrategyDecision(int menuNumber, String label, DrivingMode drivingMode, boolean advancesLap) {
        this.menuNumber = menuNumber;
        this.label = label;
        this.drivingMode = drivingMode;
        this.advancesLap = advancesLap;
    }

    public int getMenuNumber() { return menuNumber; }
    public String getLabel() { return label; }
    public DrivingMode getDrivingMode() { return drivingMode; }

    public boolean advancesLap() { return advancesLap; }

    public static StrategyDecision fromMenuNumber(int number) throws InvalidStrategyException {
        for (StrategyDecision decision : values()) {
            if (decision.menuNumber == number) {
                return decision;
            }
        }
        throw new InvalidStrategyException("Unknown action number: " + number + " (choose 1-7).");
    }
}
