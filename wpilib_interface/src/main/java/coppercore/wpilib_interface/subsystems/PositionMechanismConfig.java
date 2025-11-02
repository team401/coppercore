package coppercore.wpilib_interface.subsystems;

import com.ctre.phoenix6.configs.TalonFXConfiguration;
import edu.wpi.first.units.measure.Angle;

/** A base config for a position-control based mechanism */
public class PositionMechanismConfig {
    public String name;
    public int motorId;

    public int[] followerIds;

    public TalonFXConfiguration motorConfig = new TalonFXConfiguration();

    /**
     * Ratio of mechanism position to rotor position
     *
     * <p>Rotor position * this ratio = Mechanism position
     */
    public double positionToRotorRatio = 1.0;

    public Angle minPosition;
    public Angle maxPosition;

    public PositionMechanismConfig(
            String name, int motorId, int[] followerIds, Angle minPosition, Angle maxPosition) {
        this.name = name;
        this.motorId = motorId;
        this.followerIds = followerIds;

        this.minPosition = minPosition;
        this.maxPosition = maxPosition;
    }
}
