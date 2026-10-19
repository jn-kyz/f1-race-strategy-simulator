package strategy;

public enum DrivingMode {
    // label, lap time change, fuel, energy, tyre wear, target engine temp
    PUSH  ("PUSH",   -0.9,             1.12, 1.40,   1.35,     112.0),
    NORMAL("NORMAL",  0.0,             1.00, 1.00,   1.00,      98.0),
    SAVE  ("SAVE",   +1.3,             0.88, 0.60,   0.70,      90.0);

    private final String label;
    private final double lapTimeChange;
    private final double fuelFactor;
    private final double energyFactor;
    private final double tyreWearFactor;
    private final double targetEngineTemperature;

    DrivingMode(String label, double lapTimeChange, double fuelFactor, double energyFactor,
                double tyreWearFactor, double targetEngineTemperature) {
        this.label = label;
        this.lapTimeChange = lapTimeChange;
        this.fuelFactor = fuelFactor;
        this.energyFactor = energyFactor;
        this.tyreWearFactor = tyreWearFactor;
        this.targetEngineTemperature = targetEngineTemperature;
    }

    public String getLabel() { return label; }
    public double getLapTimeChange() { return lapTimeChange; }
    public double getFuelFactor() { return fuelFactor; }
    public double getEnergyFactor() { return energyFactor; }
    public double getTyreWearFactor() { return tyreWearFactor; }
    public double getTargetEngineTemperature() { return targetEngineTemperature; }
}
