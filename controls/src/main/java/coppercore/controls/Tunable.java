package coppercore.controls;

public interface Tunable {
    public double getPosition(int slot);

    public double getVelocity(int slot);

    public double getConversionFactor(int slot);

    public void setVolts(double volts, int slot);

    public void setPID(double p, double i, double d, int slot);

    public void setFF(double kS, double kV, double kA, double kG, int slot);

    public void setMaxProfileProperties(double maxVelocity, double maxAcceleration, int slot);

    public void runToPosition(double position, int slot);
}
