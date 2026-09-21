package coppercore.parameter_tools;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.wpilib.units.AngleUnit;
import org.wpilib.units.AngularAccelerationUnit;
import org.wpilib.units.AngularMomentumUnit;
import org.wpilib.units.AngularVelocityUnit;
import org.wpilib.units.CurrentUnit;
import org.wpilib.units.DimensionlessUnit;
import org.wpilib.units.DistanceUnit;
import org.wpilib.units.EnergyUnit;
import org.wpilib.units.ForceUnit;
import org.wpilib.units.FrequencyUnit;
import org.wpilib.units.LinearAccelerationUnit;
import org.wpilib.units.LinearMomentumUnit;
import org.wpilib.units.LinearVelocityUnit;
import org.wpilib.units.MassUnit;
import org.wpilib.units.Measure;
import org.wpilib.units.MomentOfInertiaUnit;
import org.wpilib.units.PerUnit;
import org.wpilib.units.PowerUnit;
import org.wpilib.units.ResistanceUnit;
import org.wpilib.units.TemperatureUnit;
import org.wpilib.units.TimeUnit;
import org.wpilib.units.TorqueUnit;
import org.wpilib.units.Unit;
import org.wpilib.units.VelocityUnit;
import org.wpilib.units.VoltageUnit;
import org.wpilib.units.measure.Angle;
import org.wpilib.units.measure.AngularAcceleration;
import org.wpilib.units.measure.AngularMomentum;
import org.wpilib.units.measure.AngularVelocity;
import org.wpilib.units.measure.Current;
import org.wpilib.units.measure.Dimensionless;
import org.wpilib.units.measure.Distance;
import org.wpilib.units.measure.Energy;
import org.wpilib.units.measure.Force;
import org.wpilib.units.measure.Frequency;
import org.wpilib.units.measure.LinearAcceleration;
import org.wpilib.units.measure.LinearMomentum;
import org.wpilib.units.measure.LinearVelocity;
import org.wpilib.units.measure.Mass;
import org.wpilib.units.measure.MomentOfInertia;
import org.wpilib.units.measure.Per;
import org.wpilib.units.measure.Power;
import org.wpilib.units.measure.Resistance;
import org.wpilib.units.measure.Temperature;
import org.wpilib.units.measure.Time;
import org.wpilib.units.measure.Torque;
import org.wpilib.units.measure.Velocity;
import org.wpilib.units.measure.Voltage;

public class LoggedTunableMeasure<
        MeasureType extends Measure<BaseUnitType>, BaseUnitType extends Unit> {

    private MeasureType value;
    final LoggedTunableNumber tunableNumber;
    final BaseUnitType displayedUnit;

    /**
     * Creates a logged tunable value for a WPILib measure.
     *
     * @param name logged tunable path
     * @param defaultValue default measure value
     * @param displayedUnit unit used for logging and tuning
     * @param addUnitSuffix whether to append the unit name to the path
     */
    public LoggedTunableMeasure(
            String name,
            MeasureType defaultValue,
            BaseUnitType displayedUnit,
            boolean addUnitSuffix) {
        this.value = defaultValue;
        String suffix = (addUnitSuffix) ? "_" + displayedUnit.name() : "";
        this.tunableNumber = new LoggedTunableNumber(name + suffix, defaultValue.in(displayedUnit));
        this.displayedUnit = displayedUnit;
    }

    @SuppressWarnings("unchecked")
    private void updateValue(double newValue) {
        try {
            Class<MeasureType> clazz = (Class<MeasureType>) value.getClass();
            Constructor<MeasureType> con = clazz.getConstructor(double.class);
            value = con.newInstance(newValue);
        } catch (NoSuchMethodException
                | InstantiationException
                | IllegalAccessException
                | IllegalArgumentException
                | InvocationTargetException e) {
            System.err.println(e);
        }
    }

    /** Forces the cached measure to match the current logged tunable number. */
    public void forceUpdate() {
        updateValue(tunableNumber.getAsDouble());
    }

    /**
     * Runs a callback when this value changes for a caller id.
     *
     * @param id caller id used to track changes independently
     * @param callback callback receiving the updated able measure
     */
    public void ifChanged(int id, MeasureConsumer<MeasureType, BaseUnitType> callback) {
        LoggedTunableNumber.ifChanged(
                id,
                newValue -> {
                    updateValue(newValue[0]);
                    callback.accept(value);
                },
                tunableNumber);
    }

    /**
     * Runs a callback when this value changes for this object.
     *
     * @param callback callback receiving the updated able measure
     */
    public void ifChanged(MeasureConsumer<MeasureType, BaseUnitType> callback) {
        ifChanged(hashCode(), callback);
    }

    protected void checkForUpdate() {
        LoggedTunableNumber.ifChanged(
                hashCode(), newValue -> updateValue(newValue[0]), tunableNumber);
    }

    /**
     * Checks whether this value changed for a caller id.
     *
     * @param id caller id used to track changes independently
     * @return true if the logged value changed since this id last checked
     */
    public boolean hasChanged(int id) {
        return tunableNumber.hasChanged(id);
    }

    /**
     * Checks whether this value changed for this object.
     *
     * @return true if the logged value changed since this object last checked
     */
    public boolean hasChanged() {
        return hasChanged(hashCode());
    }

    /**
     * Gets the latest tuned measure value.
     *
     * @return immutable copy of the current value
     */
    public MeasureType get() {
        checkForUpdate();
        return value;
    }

    /**
     * Sets the logged tunable value from a measure.
     *
     * @param newValue new measure value
     */
    public void set(MeasureType newValue) {
        value = newValue;
        tunableNumber.setValue((value.in(displayedUnit)));
    }

    @FunctionalInterface
    public interface MeasureConsumer<M extends Measure<U>, U extends Unit> {
        /**
         * Accepts an updated measure value.
         *
         * @param newValue updated measure
         */
        void accept(M newValue);

        /**
         * Chains another measure consumer after this one.
         *
         * @param after consumer to run after this one
         * @return combined consumer
         */
        default MeasureConsumer<M, U> chain(MeasureConsumer<M, U> after) {
            return (M newValue) -> {
                this.accept(newValue);
                after.accept(newValue);
            };
        }
    }

    public static class LoggedTunableMeasureFactory<
            M extends Measure<U>, U extends Unit, S extends LoggedTunableMeasure<M, U>> {

        LoggedTunableMeasureFactoryFunction<M, U, S> factoryFunction;

        /**
         * Creates a factory for a specific logged measure type.
         *
         * @param factoryFunction constructor-like function for the logged measure type
         */
        public LoggedTunableMeasureFactory(
                LoggedTunableMeasureFactoryFunction<M, U, S> factoryFunction) {
            this.factoryFunction = factoryFunction;
        }

        /**
         * Creates a logged measure with explicit display-unit path behavior.
         *
         * @param name logged tunable path
         * @param defaultValue default able measure
         * @param displayedUnit unit used for logging and tuning
         * @param addUnitSuffix whether to append the unit name to the path
         * @return logged tunable measure
         */
        public S of(String name, M defaultValue, U displayedUnit, boolean addUnitSuffix) {
            return factoryFunction.apply(name, defaultValue, displayedUnit, addUnitSuffix);
        }

        /**
         * Creates a logged measure with an explicit display unit.
         *
         * @param name logged tunable path
         * @param defaultValue default able measure
         * @param displayedUnit unit used for logging and tuning
         * @return logged tunable measure
         */
        public S of(String name, M defaultValue, U displayedUnit) {
            return of(name, defaultValue, displayedUnit, false);
        }

        /**
         * Creates a logged measure using the default value's unit.
         *
         * @param name logged tunable path
         * @param defaultValue default able measure
         * @return logged tunable measure
         */
        public S of(String name, M defaultValue) {
            return of(name, defaultValue, defaultValue.unit());
        }

        /**
         * Creates a logged measure from a raw value in the displayed unit.
         *
         * @param name logged tunable path
         * @param defaultValue default raw value
         * @param displayedUnit unit used for the raw value
         * @return logged tunable measure
         */
        @SuppressWarnings("unchecked")
        public S of(String name, double defaultValue, U displayedUnit) {
            return of(name, (M) displayedUnit.ofBaseUnits(defaultValue), displayedUnit);
        }

        @FunctionalInterface
        public interface LoggedTunableMeasureFactoryFunction<
                M extends Measure<U>, U extends Unit, S extends LoggedTunableMeasure<M, U>> {
            /**
             * Creates the typed logged measure.
             *
             * @param name logged tunable path
             * @param defaultValue default able measure
             * @param displayedUnit unit used for logging and tuning
             * @param addUnitSuffix whether to append the unit name to the path
             * @return logged tunable measure
             */
            S apply(String name, M defaultValue, U displayedUnit, boolean addUnitSuffix);
        }
    }

    public static class LoggedAngle extends LoggedTunableMeasure<Angle, AngleUnit> {
        public LoggedAngle(
                String name, Angle defaultValue, AngleUnit displayedUnit, boolean addUnitSuffix) {
            super(name, defaultValue, displayedUnit, addUnitSuffix);
        }
    }

    public static class LoggedAngularAcceleration
            extends LoggedTunableMeasure<AngularAcceleration, AngularAccelerationUnit> {
        public LoggedAngularAcceleration(
                String name,
                AngularAcceleration defaultValue,
                AngularAccelerationUnit displayedUnit,
                boolean addUnitSuffix) {
            super(name, defaultValue, displayedUnit, addUnitSuffix);
        }
    }

    public static class LoggedAngularMomentum
            extends LoggedTunableMeasure<AngularMomentum, AngularMomentumUnit> {
        public LoggedAngularMomentum(
                String name,
                AngularMomentum defaultValue,
                AngularMomentumUnit displayedUnit,
                boolean addUnitSuffix) {
            super(name, defaultValue, displayedUnit, addUnitSuffix);
        }
    }

    public static class LoggedAngularVelocity
            extends LoggedTunableMeasure<AngularVelocity, AngularVelocityUnit> {
        public LoggedAngularVelocity(
                String name,
                AngularVelocity defaultValue,
                AngularVelocityUnit displayedUnit,
                boolean addUnitSuffix) {
            super(name, defaultValue, displayedUnit, addUnitSuffix);
        }
    }

    public static class LoggedCurrent extends LoggedTunableMeasure<Current, CurrentUnit> {
        public LoggedCurrent(
                String name,
                Current defaultValue,
                CurrentUnit displayedUnit,
                boolean addUnitSuffix) {
            super(name, defaultValue, displayedUnit, addUnitSuffix);
        }
    }

    public static class LoggedDimensionless
            extends LoggedTunableMeasure<Dimensionless, DimensionlessUnit> {
        public LoggedDimensionless(
                String name,
                Dimensionless defaultValue,
                DimensionlessUnit displayedUnit,
                boolean addUnitSuffix) {
            super(name, defaultValue, displayedUnit, addUnitSuffix);
        }
    }

    public static class LoggedDistance extends LoggedTunableMeasure<Distance, DistanceUnit> {
        public LoggedDistance(
                String name,
                Distance defaultValue,
                DistanceUnit displayedUnit,
                boolean addUnitSuffix) {
            super(name, defaultValue, displayedUnit, addUnitSuffix);
        }
    }

    public static class LoggedEnergy extends LoggedTunableMeasure<Energy, EnergyUnit> {
        public LoggedEnergy(
                String name, Energy defaultValue, EnergyUnit displayedUnit, boolean addUnitSuffix) {
            super(name, defaultValue, displayedUnit, addUnitSuffix);
        }
    }

    public static class LoggedForce extends LoggedTunableMeasure<Force, ForceUnit> {
        public LoggedForce(
                String name, Force defaultValue, ForceUnit displayedUnit, boolean addUnitSuffix) {
            super(name, defaultValue, displayedUnit, addUnitSuffix);
        }
    }

    public static class LoggedFrequency extends LoggedTunableMeasure<Frequency, FrequencyUnit> {
        public LoggedFrequency(
                String name,
                Frequency defaultValue,
                FrequencyUnit displayedUnit,
                boolean addUnitSuffix) {
            super(name, defaultValue, displayedUnit, addUnitSuffix);
        }
    }

    public static class LoggedLinearAcceleration
            extends LoggedTunableMeasure<LinearAcceleration, LinearAccelerationUnit> {
        public LoggedLinearAcceleration(
                String name,
                LinearAcceleration defaultValue,
                LinearAccelerationUnit displayedUnit,
                boolean addUnitSuffix) {
            super(name, defaultValue, displayedUnit, addUnitSuffix);
        }
    }

    public static class LoggedLinearMomentum
            extends LoggedTunableMeasure<LinearMomentum, LinearMomentumUnit> {
        public LoggedLinearMomentum(
                String name,
                LinearMomentum defaultValue,
                LinearMomentumUnit displayedUnit,
                boolean addUnitSuffix) {
            super(name, defaultValue, displayedUnit, addUnitSuffix);
        }
    }

    public static class LoggedLinearVelocity
            extends LoggedTunableMeasure<LinearVelocity, LinearVelocityUnit> {
        public LoggedLinearVelocity(
                String name,
                LinearVelocity defaultValue,
                LinearVelocityUnit displayedUnit,
                boolean addUnitSuffix) {
            super(name, defaultValue, displayedUnit, addUnitSuffix);
        }
    }

    public static class LoggedMass extends LoggedTunableMeasure<Mass, MassUnit> {
        public LoggedMass(
                String name, Mass defaultValue, MassUnit displayedUnit, boolean addUnitSuffix) {
            super(name, defaultValue, displayedUnit, addUnitSuffix);
        }
    }

    public static class LoggedMomentOfInertia
            extends LoggedTunableMeasure<MomentOfInertia, MomentOfInertiaUnit> {
        public LoggedMomentOfInertia(
                String name,
                MomentOfInertia defaultValue,
                MomentOfInertiaUnit displayedUnit,
                boolean addUnitSuffix) {
            super(name, defaultValue, displayedUnit, addUnitSuffix);
        }
    }

    public static class LoggedPower extends LoggedTunableMeasure<Power, PowerUnit> {
        public LoggedPower(
                String name, Power defaultValue, PowerUnit displayedUnit, boolean addUnitSuffix) {
            super(name, defaultValue, displayedUnit, addUnitSuffix);
        }
    }

    public static class LoggedResistance extends LoggedTunableMeasure<Resistance, ResistanceUnit> {
        public LoggedResistance(
                String name,
                Resistance defaultValue,
                ResistanceUnit displayedUnit,
                boolean addUnitSuffix) {
            super(name, defaultValue, displayedUnit, addUnitSuffix);
        }
    }

    public static class LoggedTemperature
            extends LoggedTunableMeasure<Temperature, TemperatureUnit> {
        public LoggedTemperature(
                String name,
                Temperature defaultValue,
                TemperatureUnit displayedUnit,
                boolean addUnitSuffix) {
            super(name, defaultValue, displayedUnit, addUnitSuffix);
        }
    }

    public static class LoggedTime extends LoggedTunableMeasure<Time, TimeUnit> {
        public LoggedTime(
                String name, Time defaultValue, TimeUnit displayedUnit, boolean addUnitSuffix) {
            super(name, defaultValue, displayedUnit, addUnitSuffix);
        }
    }

    public static class LoggedTorque extends LoggedTunableMeasure<Torque, TorqueUnit> {
        public LoggedTorque(
                String name, Torque defaultValue, TorqueUnit displayedUnit, boolean addUnitSuffix) {
            super(name, defaultValue, displayedUnit, addUnitSuffix);
        }
    }

    public static class LoggedVoltage extends LoggedTunableMeasure<Voltage, VoltageUnit> {
        public LoggedVoltage(
                String name,
                Voltage defaultValue,
                VoltageUnit displayedUnit,
                boolean addUnitSuffix) {
            super(name, defaultValue, displayedUnit, addUnitSuffix);
        }
    }

    public static LoggedTunableMeasureFactory<Angle, AngleUnit, LoggedAngle> ANGLE =
            new LoggedTunableMeasureFactory<>(LoggedAngle::new);
    public static LoggedTunableMeasureFactory<
                    AngularAcceleration, AngularAccelerationUnit, LoggedAngularAcceleration>
            ANGULAR_ACCELERATION =
                    new LoggedTunableMeasureFactory<>(LoggedAngularAcceleration::new);
    public static LoggedTunableMeasureFactory<
                    AngularMomentum, AngularMomentumUnit, LoggedAngularMomentum>
            ANGULAR_MOMENTUM = new LoggedTunableMeasureFactory<>(LoggedAngularMomentum::new);
    public static LoggedTunableMeasureFactory<
                    AngularVelocity, AngularVelocityUnit, LoggedAngularVelocity>
            ANGULAR_VELOCITY = new LoggedTunableMeasureFactory<>(LoggedAngularVelocity::new);
    public static LoggedTunableMeasureFactory<Current, CurrentUnit, LoggedCurrent> CURRENT =
            new LoggedTunableMeasureFactory<>(LoggedCurrent::new);
    public static LoggedTunableMeasureFactory<Dimensionless, DimensionlessUnit, LoggedDimensionless>
            DIMENSIONLESS = new LoggedTunableMeasureFactory<>(LoggedDimensionless::new);
    public static LoggedTunableMeasureFactory<Distance, DistanceUnit, LoggedDistance> DISTANCE =
            new LoggedTunableMeasureFactory<>(LoggedDistance::new);
    public static LoggedTunableMeasureFactory<Energy, EnergyUnit, LoggedEnergy> ENERGY =
            new LoggedTunableMeasureFactory<>(LoggedEnergy::new);
    public static LoggedTunableMeasureFactory<Force, ForceUnit, LoggedForce> FORCE =
            new LoggedTunableMeasureFactory<>(LoggedForce::new);
    public static LoggedTunableMeasureFactory<Frequency, FrequencyUnit, LoggedFrequency> FREQUENCY =
            new LoggedTunableMeasureFactory<>(LoggedFrequency::new);
    public static LoggedTunableMeasureFactory<
                    LinearAcceleration, LinearAccelerationUnit, LoggedLinearAcceleration>
            LINEAR_ACCELERATION = new LoggedTunableMeasureFactory<>(LoggedLinearAcceleration::new);
    public static LoggedTunableMeasureFactory<
                    LinearMomentum, LinearMomentumUnit, LoggedLinearMomentum>
            LINEAR_MOMENTUM = new LoggedTunableMeasureFactory<>(LoggedLinearMomentum::new);
    public static LoggedTunableMeasureFactory<
                    LinearVelocity, LinearVelocityUnit, LoggedLinearVelocity>
            LINEAR_VELOCITY = new LoggedTunableMeasureFactory<>(LoggedLinearVelocity::new);
    public static LoggedTunableMeasureFactory<Mass, MassUnit, LoggedMass> MASS =
            new LoggedTunableMeasureFactory<>(LoggedMass::new);
    public static LoggedTunableMeasureFactory<
                    MomentOfInertia, MomentOfInertiaUnit, LoggedMomentOfInertia>
            MOMENT_OF_INERTIA = new LoggedTunableMeasureFactory<>(LoggedMomentOfInertia::new);
    public static LoggedTunableMeasureFactory<Power, PowerUnit, LoggedPower> POWER =
            new LoggedTunableMeasureFactory<>(LoggedPower::new);
    public static LoggedTunableMeasureFactory<Resistance, ResistanceUnit, LoggedResistance>
            RESISTANCE = new LoggedTunableMeasureFactory<>(LoggedResistance::new);
    public static LoggedTunableMeasureFactory<Temperature, TemperatureUnit, LoggedTemperature>
            TEMPERATURE = new LoggedTunableMeasureFactory<>(LoggedTemperature::new);
    public static LoggedTunableMeasureFactory<Time, TimeUnit, LoggedTime> TIME =
            new LoggedTunableMeasureFactory<>(LoggedTime::new);
    public static LoggedTunableMeasureFactory<Torque, TorqueUnit, LoggedTorque> TORQUE =
            new LoggedTunableMeasureFactory<>(LoggedTorque::new);
    public static LoggedTunableMeasureFactory<Voltage, VoltageUnit, LoggedVoltage> VOLTAGE =
            new LoggedTunableMeasureFactory<>(LoggedVoltage::new);

    public static class LoggedAngularJerk
            extends LoggedTunableMeasure<
                    Velocity<AngularAccelerationUnit>, VelocityUnit<AngularAccelerationUnit>> {
        public LoggedAngularJerk(
                String name,
                Velocity<AngularAccelerationUnit> defaultValue,
                VelocityUnit<AngularAccelerationUnit> displayedUnit,
                boolean addUnitSuffix) {
            super(name, defaultValue, displayedUnit, addUnitSuffix);
        }
    }

    public static class LoggedVoltagePerAngularVelocity
            extends LoggedTunableMeasure<
                    Per<VoltageUnit, AngularVelocityUnit>,
                    PerUnit<VoltageUnit, AngularVelocityUnit>> {
        public LoggedVoltagePerAngularVelocity(
                String name,
                Per<VoltageUnit, AngularVelocityUnit> defaultValue,
                PerUnit<VoltageUnit, AngularVelocityUnit> displayedUnit,
                boolean addUnitSuffix) {
            super(name, defaultValue, displayedUnit, addUnitSuffix);
        }
    }

    public static class LoggedVoltagePerAngularAcceleration
            extends LoggedTunableMeasure<
                    Per<VoltageUnit, AngularAccelerationUnit>,
                    PerUnit<VoltageUnit, AngularAccelerationUnit>> {
        public LoggedVoltagePerAngularAcceleration(
                String name,
                Per<VoltageUnit, AngularAccelerationUnit> defaultValue,
                PerUnit<VoltageUnit, AngularAccelerationUnit> displayedUnit,
                boolean addUnitSuffix) {
            super(name, defaultValue, displayedUnit, addUnitSuffix);
        }
    }

    public static LoggedTunableMeasureFactory<
                    Velocity<AngularAccelerationUnit>,
                    VelocityUnit<AngularAccelerationUnit>,
                    LoggedAngularJerk>
            ANGULAR_JERK = new LoggedTunableMeasureFactory<>(LoggedAngularJerk::new);
    public static LoggedTunableMeasureFactory<
                    Per<VoltageUnit, AngularVelocityUnit>,
                    PerUnit<VoltageUnit, AngularVelocityUnit>,
                    LoggedVoltagePerAngularVelocity>
            VOLTAGE_PER_ANGULAR_VELOCITY =
                    new LoggedTunableMeasureFactory<>(LoggedVoltagePerAngularVelocity::new);
    public static LoggedTunableMeasureFactory<
                    Per<VoltageUnit, AngularAccelerationUnit>,
                    PerUnit<VoltageUnit, AngularAccelerationUnit>,
                    LoggedVoltagePerAngularAcceleration>
            VOLTAGE_PER_ANGULAR_ACCELERATION =
                    new LoggedTunableMeasureFactory<>(LoggedVoltagePerAngularAcceleration::new);
}
